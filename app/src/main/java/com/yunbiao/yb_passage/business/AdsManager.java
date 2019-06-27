package com.yunbiao.yb_passage.business;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.google.gson.Gson;
import com.yunbiao.yb_passage.R;
import com.yunbiao.yb_passage.afinel.Constants;
import com.yunbiao.yb_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_passage.bean.AdvertBean;
import com.yunbiao.yb_passage.utils.FileUtils;
import com.yunbiao.yb_passage.utils.SpUtils;
import com.yunbiao.yb_passage.utils.ThreadUitls;
import com.yunbiao.yb_passage.utils.xutil.MyXutils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/16.
 */

public class AdsManager {
    private String TAG = getClass().getSimpleName();
    private static AdsManager instance;
    private Activity mAct;

    private boolean isJumping;//跳转标识
    private boolean isInited = false;//初始化标识
    private boolean isDebug = true;

    private FrameLayout insideImageBg;

    private final long DELAY_OPEN_ADS = 30 * 1000;//广告开启延时
    private final int ADS_START = 1;//广告开启标识
    private int adsPlayTime = 10;//广告播放时间，默认10秒
    private int adsPlayTime_h = 10;//横屏广告播放时间

    private int index_in = 0;//内部广告指针
    private int index_out = 0;//外部广告指针

    private List<String> playList = new ArrayList<>();
    private List<String> playList_h = new ArrayList<>();

    public static String SCREEN_BASE_PATH_IMGS = Constants.ADS_PATH;//广告视频图片存储路径
    private VideoView insideVideo;
    private ImageView insideImage;

    public static AdsManager instance() {
        if (instance == null) {
            synchronized (AdsManager.class) {
                if (instance == null) {
                    instance = new AdsManager();
                }
            }
        }
        return instance;
    }

    private AdsManager() {
        File file = new File(SCREEN_BASE_PATH_IMGS);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    public interface OnAdsVisibleListener {
        void onAdsVisibleStateChanged(boolean isShown);
    }

    private OnAdsVisibleListener mListener;

    public void init(@NonNull Activity act, OnAdsVisibleListener listener) {
        mAct = act;
        mListener = listener;
        //内部广告
        insideImageBg = (FrameLayout) act.findViewById(R.id.fl_inside_ads);
        insideImage = (ImageView) act.findViewById(R.id.iv_inside_ads);
        insideVideo = (VideoView) act.findViewById(R.id.vv_inside_ads);

        startAdsHandler();
        loadAdsData();
        isInited = true;
    }

    /***
     * 开始广告循环
     */
    private void startAdsHandler() {
        adsUpdateHandler.removeMessages(ADS_START);
        adsUpdateHandler.sendEmptyMessage(ADS_START);
    }

    /***
     * 加载本地缓存广告
     */
    private void loadAdsData() {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                d("开始加载本地缓存广告...");
                String ad_heng = SpUtils.getStr(SpUtils.AD_HENG);
                if (!TextUtils.isEmpty(ad_heng)) {
                    AdvertBean advertBean = new Gson().fromJson(ad_heng, AdvertBean.class);
                    loadImageList(advertBean.getAdvertObject().getImgArray(), playList_h, new Runnable() {
                        @Override
                        public void run() {
                            d("横屏广告加载完成...");
                        }
                    });
                }

                requestAdsData();
            }
        });
    }

    /***
     * 请求网络广告
     */
    public void requestAdsData() {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                d("开始请求广告数据...");

                int companyid = SpUtils.getInt(SpUtils.COMPANYID);
                final Map<String, String> map = new HashMap<String, String>();
                map.put("comId", companyid + "");
                map.put("type", 1 + "");
                MyXutils.getInstance().post(ResourceUpdate.GETAD, map, new MyXutils.XCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        d("横屏广告请求成功：" + result);
                        AdvertBean advertBean = new Gson().fromJson(result, AdvertBean.class);
                        if (advertBean == null || advertBean.getStatus() != 1 || advertBean.getAdvertObject() == null) {
                            return;
                        }
                        List<AdvertBean.AdvertObjectEntity.ImgArrayEntity> imgArray = advertBean.getAdvertObject().getImgArray();
                        if (imgArray == null) {
                            return;
                        }

                        SpUtils.saveStr(SpUtils.AD_HENG, result);
                        adsPlayTime_h = advertBean.getAdvertObject().getAdvertTime();
                        final List<String> tempList = new ArrayList<>();
                        loadImageList(imgArray, tempList, new Runnable() {
                            @Override
                            public void run() {
                                d("横屏广告处理完毕...");
                                playList_h.clear();
                                playList_h.addAll(tempList);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable ex) {
                        d("横屏广告请求失败：" + ex.getMessage());
                    }

                    @Override
                    public void onFinish() {
                    }
                });
            }
        });
    }

    /***
     * 加载资源列表
     * @param imgArray
     * @param list
     * @param runnable
     */
    private void loadImageList(List<AdvertBean.AdvertObjectEntity.ImgArrayEntity> imgArray, final List<String> list, Runnable runnable) {
        for (AdvertBean.AdvertObjectEntity.ImgArrayEntity imgArrayEntity : imgArray) {
            final String adUrl = imgArrayEntity.getAdvertimg();
            int index = adUrl.lastIndexOf("/");
            String adName = adUrl.substring(index + 1, adUrl.length());
            d("检查广告文件..." + adName);
            String adPath = SCREEN_BASE_PATH_IMGS + adName;
            File file = new File(adPath);
            if (file.exists() && file.isFile()) {
                d("广告文件存在..." + adPath);
                list.add(adPath);
                continue;
            }

            d("广告文件不存在，准备下载..." + adUrl);
            MyXutils.getInstance().downLoadFile(adUrl, adPath, true,new MyXutils.XDownLoadCallBack() {
                @Override
                public void onStart() {

                }

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {
                    d("百分比---> " + ((float) current / total * 100));
                }

                @Override
                public void onSuccess(File result) {
                    d("下载成功: " + result.getName());
                    list.add(result.getPath());
                }

                @Override
                public void onError(Throwable ex) {
                    d("下载失败：" + adUrl);
                }

                @Override
                public void onFinished() {

                }
            });
        }

        if (runnable != null) {
            runnable.run();
        }
    }

    /***
     * 广告更新handler
     */
    private Handler adsUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int time = 10;
            if (adsPlayTime_h != 0) {
                time = adsPlayTime_h;
            }
            if(!insideVideo.isPlaying()){
                updateInsideAds(playList_h);
            }
            adsUpdateHandler.sendEmptyMessageDelayed(ADS_START, time * 1000);
        }
    };

    /***
     * 更新内部广告
     * @param adsList 需要用到的广告列表
     */
    private void updateInsideAds(List<String> adsList) {
        if (adsList == null || adsList.size() <= 0) {
            return;
        }
        if (index_in >= adsList.size()) {
            index_in = 0;
        }

        String filePath = adsList.get(index_in);
        index_in++;

        File file = new File(filePath);
        if (FileUtils.isVideo(file.toString())) {
            insideImageBg.setVisibility(View.GONE);
            insideVideo.setVisibility(View.VISIBLE);
            playVideo(file.getPath(), insideVideo, new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    updateInsideAds(playList_h);
                }
            }, new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    updateInsideAds(playList_h);
                    return true;
                }
            });
        } else {
            if (insideVideo != null) {
                insideVideo.setVisibility(View.GONE);
                insideVideo.stopPlayback();
            }
            insideImageBg.setVisibility(View.VISIBLE);
            if (Util.isOnMainThread()) {
                Glide.with(mAct).load(file.toString()).crossFade(1600)/*.override(insideImageBg.getWidth(),insideImageBg.getHeight())*/.skipMemoryCache(true).into(insideImage);
//                Glide.with(mAct).load(file.toString()).asBitmap().skipMemoryCache(true).override(insideImageBg.getWidth(), insideImageBg.getHeight()).into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                        toggleImage(resource,insideImageBg,insideImage);
//                    }
//                });
            }
        }
    }

    /***====界面====================================================================================*/

    public void destory() {
        if (insideVideo != null) {
            insideVideo.stopPlayback();
        }

        adsUpdateHandler.removeMessages(ADS_START);
    }

    private void d(@NonNull String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    /*===UI相关==================================================================================*/

    /***
     * 播放视频
     * @param videoPath
     * @param videoView
     * @param compListener
     * @param errorListener
     */
    private void playVideo(String videoPath, VideoView videoView, MediaPlayer.OnCompletionListener compListener, MediaPlayer.OnErrorListener errorListener) {
        videoView.stopPlayback();
        videoView.setVideoPath(videoPath);
        videoView.start();
        videoView.setOnCompletionListener(compListener);
        videoView.setOnErrorListener(errorListener);
    }

    interface OnBGCreatedListener{
        void onCreated(Drawable drawable);
    }

    private void toggleImage(final Bitmap resource, final View view, final ImageView image){
        setImageAds(resource, view,new OnBGCreatedListener() {
            @Override
            public void onCreated(final Drawable drawable) {
                Log.e(TAG, "onCreated: 55555555555555555");
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        final AlphaAnimation showAnim = new AlphaAnimation(0.0f,1.0f);
                        final AlphaAnimation showAnim = new AlphaAnimation(1.0f,0.0f);
                        showAnim.setDuration(1000);
                        showAnim.setFillAfter(true);

                        if(drawable != null){
                            view.setBackground(drawable);
                        }
                        image.setImageBitmap(resource);
                    }
                });
            }
        });
    }

    /***
     * 加载图片广告背景
     * @param resource
     * @param view
     */
    private void setImageAds(final Bitmap resource, final View view, final OnBGCreatedListener onBGCreatedListener) {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {

                if(mBackgroundBitmap != null){
                    mBackgroundBitmap.recycle();
                    mBackgroundBitmap = null;
                    Log.e(TAG, "run: **********************");
                }

                int iw = resource.getWidth();
                int ih = resource.getHeight();
                int vw = view.getWidth();
                final int vh = view.getHeight();
                if(iw == vw && ih == vh){
                    onBGCreatedListener.onCreated(null);
                    return;
                }

                Log.e(TAG, "run: 11111111111111111111");
                Palette palette = Palette.generate(resource);
                if(palette != null){
                    Log.e(TAG, "run: 22222222222222222222222222222");

                    if (palette.getDarkVibrantColor(Color.TRANSPARENT) != Color.TRANSPARENT) {
                        mBackgroundBitmap = createGradBitmap(view, resource, palette.getDarkVibrantColor(Color.TRANSPARENT), palette.getVibrantColor(Color.TRANSPARENT));
                    } else if (palette.getDarkMutedColor(Color.TRANSPARENT) != Color.TRANSPARENT) {
                        mBackgroundBitmap = createGradBitmap(view, resource, palette.getDarkMutedColor(Color.TRANSPARENT), palette.getMutedColor(Color.TRANSPARENT));
                    } else {
                        mBackgroundBitmap = createGradBitmap(view, resource, palette.getLightMutedColor(Color.TRANSPARENT), palette.getLightVibrantColor(Color.TRANSPARENT));
                    }
                    onBGCreatedListener.onCreated(new BitmapDrawable(mBackgroundBitmap));
                }
            }
        });
    }

    private Bitmap mBackgroundBitmap;//背景图
    private Paint mPaint;
    private Canvas mCanvas;

    /***
     * 绘制渐变色背景
     * @param view
     * @param originBitmap
     * @param darkColor
     * @param color
     * @return
     */
    private Bitmap createGradBitmap(View view, Bitmap originBitmap, int darkColor, int color) {
        if (originBitmap == null) {
            return null;
        }

        if(mBackgroundBitmap != null){
            mBackgroundBitmap.recycle();
            mBackgroundBitmap = null;
        }

        int bgColors[] = new int[2];
        bgColors[0] = darkColor;
        bgColors[1] = color;

        mBackgroundBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_4444);

        if (mCanvas == null) {
            mCanvas = new Canvas();
        }
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mCanvas.setBitmap(mBackgroundBitmap);
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        LinearGradient gradient = new LinearGradient(0, 0, 0, mBackgroundBitmap.getHeight(), bgColors[0], bgColors[1], Shader.TileMode.CLAMP);
        mPaint.setShader(gradient);
        mPaint.setAlpha(180);
        RectF rectF = new RectF(0, 0, mBackgroundBitmap.getWidth(), mBackgroundBitmap.getHeight());
        // mCanvas.drawRoundRect(rectF,16,16,mPaint); 这个用来绘制圆角的哈
        mCanvas.drawRect(rectF, mPaint);
        return mBackgroundBitmap;
    }

}