package com.yunbiao.yb_passage.business;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.yb_passage.APP;
import com.yunbiao.yb_passage.afinel.Constants;
import com.yunbiao.yb_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_passage.db.PassageDao;
import com.yunbiao.yb_passage.db.PassageBean;
import com.yunbiao.yb_passage.db.UserBean;
import com.yunbiao.yb_passage.db.UserDao;
import com.yunbiao.yb_passage.heartbeat.HeartBeatClient;
import com.yunbiao.yb_passage.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

public class PassageManager {
    private static final String TAG = "PassageManager";
    private static PassageManager instance = new PassageManager();
    private Object passLock = new Object();

    public static int TYPE_ONLY_FACE = 0;
    public static int TYPE_ONLY_CARD = 1;
    public static int TYPE_DOUBLE = 2;
    private static int VERIFY_TYPE = 0;//验证类型

    private long verifyOffsetTime = 6000;//验证间隔时间
    private long AUTO_UPLOAD_TIME = 30;

    private final PassageDao passageDao;
    private final ExecutorService threadPool;

    private Map<String, PassageBean> passageMap = new HashMap<>();
    private final UserDao userDao;

    private String today = "";

    private DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private DecimalFormat decimalFormat = new DecimalFormat(".0");
    private final ScheduledExecutorService autoUploadThread;

    public static PassageManager instance() {
        return instance;
    }

    public static void updateVerifyType(int type) {
        if (type >= 0 && type <= 2) {
            VERIFY_TYPE = type;
        } else {
            VERIFY_TYPE = TYPE_ONLY_FACE;
        }
    }

    private PassageManager() {
        today = dateFormat.format(new Date());
        passageDao = APP.getPassageDao();
        userDao = APP.getUserDao();

        threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(initRunnable);

        autoUploadThread = Executors.newScheduledThreadPool(1);
        autoUploadThread.scheduleAtFixedRate(autoUploadRunnable, 10, AUTO_UPLOAD_TIME, TimeUnit.MINUTES);
    }

    private Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            List<PassageBean> passageBeans = passageDao.queryByDate(today);
            if (passageBeans == null) {
                return;
            }
            for (PassageBean passageBean : passageBeans) {
                String faceId = passageBean.getFaceId();
                long passTime = passageBean.getPassTime();
                if (passageMap.containsKey(faceId)) {
                    long passTime1 = passageMap.get(faceId).getPassTime();
                    if (passTime > passTime1) {
                        passageMap.put(faceId, passageBean);
                    } else {
                        continue;
                    }
                } else {
                    passageMap.put(faceId, passageBean);
                }
            }
        }
    };

    //自动上传记录的线程
    private Runnable autoUploadRunnable = new Runnable() {
        @Override
        public void run() {
            //检查日期是否改变，如果改变则重新初始化缓存map
            String currDate = dateFormat.format(new Date());
            if (!TextUtils.equals(currDate, today)) {
                today = currDate;
                passageMap.clear();
                initRunnable.run();
            }

            //自动上传
            final List<PassageBean> passageBeans = passageDao.selectAll();
            if (passageBeans == null) {
                return;
            }

            Log.e(TAG, "run: ------ 共：" + passageBeans.size());
            Iterator<PassageBean> iterator = passageBeans.iterator();
            while (iterator.hasNext()) {
                PassageBean next = iterator.next();
                if (next.isUpload()) {
                    iterator.remove();
                }
            }
            Log.e(TAG, "run: ------ 未上传：" + passageBeans.size());

            if (passageBeans.size() <= 0) {
                return;
            }

            Map<String, File> fileMap = new HashMap<>();
            final List<UploadBean> upBeans = new ArrayList<>();
            for (PassageBean passageBean : passageBeans) {
                UploadBean uploadBean = new UploadBean();
                uploadBean.similar = passageBean.getSimilar();
                uploadBean.entryId = passageBean.getEntryId();
                uploadBean.card = "";
                uploadBean.isPass = "" + passageBean.getIsPass();
                uploadBean.createTime = passageBean.getPassTime();
                upBeans.add(uploadBean);

                File file = new File(passageBean.getHeadPath());
                fileMap.put(file.getName(), file);
            }

            Log.e(TAG, "fileMap.size：" + fileMap.size());

            String jsonArrayStr = new Gson().toJson(upBeans);
            String deviceNo = HeartBeatClient.getDeviceNo();
            int comId = SpUtils.getInt(SpUtils.COMPANYID);

            Map<String, String> params = new HashMap<>();
            params.put("deviceNo", deviceNo);
            params.put("comId", "" + comId);
            params.put("witJson", jsonArrayStr);

            Log.e(TAG, ResourceUpdate.UPLOAD_PASS_RECORD + " --- " + params.toString() + " --- " + fileMap.toString());
            OkHttpUtils.post()
                    .url(ResourceUpdate.UPLOAD_PASS_RECORD)
                    .params(params)
                    .files("heads", fileMap)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Log.e(TAG, "onError: 上传失败：" + e != null ? e.getMessage() : "NULL");
                        }

                        @Override
                        public void onResponse(final String response, int id) {
                            Log.e(TAG, "onResponse: 上传结果：" + response);
                            threadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject jsonObject = JSONObject.parseObject(response);
                                    String status = jsonObject.getString("status");
                                    if(!TextUtils.equals("1",status)){
                                        return;
                                    }

                                    for (PassageBean passageBean : passageBeans) {
                                        passageBean.setUpload(true);
                                        int update = passageDao.update(passageBean);
                                        Log.e(TAG, "更新结果： " + update);
                                    }
                                }
                            });
                        }
                    });
        }
    };

    public void checkPermissions(final Activity context, final VerifyResult verifyResult, final PassCallback passCallback) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                check(context, verifyResult, passCallback);
            }
        });
    }

    private void check(Activity context, VerifyResult verifyResult, final PassCallback passCallback) {
        synchronized (passLock) {
            float verifyScore = verifyResult.getVerifyScore();
            byte[] faceImageBytes = verifyResult.getFaceImageBytes();
            FaceUser user = verifyResult.getUser();

            if (user == null) {
                return;
            }
            String userId = user.getUserId();
            if (TextUtils.isEmpty(userId)) {
                return;
            }
            List<UserBean> userBeans = userDao.queryByFaceId(userId);
            if (userBeans == null || userBeans.size() <= 0) {
                return;
            }

            long currTime = System.currentTimeMillis();
            int isPass = 0;
            File imgFile = saveBitmap(currTime, faceImageBytes);

            UserBean userBean = userBeans.get(0);
            final PassageBean passageBean = new PassageBean();
            passageBean.setEntryId("" + userBean.getEmpId());
            passageBean.setFaceId(userBean.getFaceId());
            passageBean.setHeadPath(imgFile.getPath());
            passageBean.setIsPass(isPass);
            passageBean.setName(userBean.getName());
            passageBean.setPassTime(currTime);
            passageBean.setSimilar(decimalFormat.format(verifyScore * 100));
            passageBean.setUpload(false);
            passageBean.setDate(dateFormat.format(currTime));
            passageBean.setDepartName(userBean.getDepart());

            if (!canPass(passageBean)) {
                return;
            }

            if (passCallback != null) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        passCallback.pass(passageBean);
                    }
                });
            }

            String deviceNo = HeartBeatClient.getDeviceNo();
            int comId = SpUtils.getInt(SpUtils.COMPANYID);
            final Map<String, String> params = new HashMap<>();
            params.put("entryId", passageBean.getEntryId());
            params.put("similar", passageBean.getSimilar());
            params.put("card", VERIFY_TYPE == TYPE_ONLY_CARD || VERIFY_TYPE == TYPE_DOUBLE ? userBean.getCardId() : "");
            params.put("isPass", "" + passageBean.getIsPass());
            params.put("deviceNo", deviceNo);
            params.put("comId", "" + comId);
            Log.e(TAG, ResourceUpdate.CHECK_PASS + " --- " + params.toString());
            PostFormBuilder builder = OkHttpUtils.post().url(ResourceUpdate.CHECK_PASS).params(params);
            if (imgFile != null && imgFile.exists()) {
                builder.addFile("heads", imgFile.getName(), imgFile);
            }
            builder.build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    Log.e(TAG, "onError: " + e != null ? e.getMessage() : "NULL");
                    passageBean.setUpload(false);
                    passageDao.insert(passageBean);
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.e(TAG, "onResponse: " + response);
                    JSONObject jsonObject = JSONObject.parseObject(response);
                    String status = jsonObject.getString("status");
                    passageBean.setUpload(TextUtils.equals("1", status));
                    passageDao.insert(passageBean);
                }

                @Override
                public void onAfter(int id) {

                }
            });
        }
    }

    private boolean canPass(PassageBean passageBean) {
        String faceId = passageBean.getFaceId();
        if (!passageMap.containsKey(faceId)) {
            passageMap.put(faceId, passageBean);
            return true;
        }

        PassageBean cacheBean = passageMap.get(faceId);
        long passTime = cacheBean.getPassTime();
        long currTime = passageBean.getPassTime();
        boolean isCanPass = (currTime - passTime) > verifyOffsetTime;
        if (isCanPass) {
            passageMap.put(faceId, passageBean);
        }
        return isCanPass;
    }

    /**
     * 保存bitmap到本地
     *
     * @return
     */
    public File saveBitmap(long time, byte[] mBitmapByteArry) {
        File filePic;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap image = BitmapFactory.decodeByteArray(mBitmapByteArry, 0, mBitmapByteArry.length, options);

            //格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(time);
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(Constants.CURRENT_FACE_CACHE_PATH + "/" + today + "/" + sdfTime + ".png");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return filePic;
    }

    public interface PassCallback {
        void pass(PassageBean passageBean);
    }

    public class UploadBean {
        private long createTime;
        private String entryId;
        private String similar;
        private String card;
        private String isPass;
        private String heads;
    }
}
