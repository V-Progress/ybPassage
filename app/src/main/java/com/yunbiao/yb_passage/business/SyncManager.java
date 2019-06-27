package com.yunbiao.yb_passage.business;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.yb_passage.APP;
import com.yunbiao.yb_passage.activity.EmployListActivity;
import com.yunbiao.yb_passage.afinel.Constants;
import com.yunbiao.yb_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_passage.bean.CompanyBean;
import com.yunbiao.yb_passage.bean.StaffBean;
import com.yunbiao.yb_passage.db.UserBean;
import com.yunbiao.yb_passage.db.UserDao;
import com.yunbiao.yb_passage.faceview.FaceSDK;
import com.yunbiao.yb_passage.heartbeat.HeartBeatClient;
import com.yunbiao.yb_passage.utils.SpUtils;
import com.yunbiao.yb_passage.utils.xutil.MyXutils;
import com.yunbiao.yb_passage.views.FloatSyncView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2019/5/14.
 */

public class SyncManager extends BroadcastReceiver {

    private static SyncManager instance;
    private Activity mAct;
    private boolean isLocalServ = false;
    private UserDao userDao;

    public static final int TYPE_ADD = 0;
    public static final int TYPE_UPDATE_INFO = 1;
    public static final int TYPE_UPDATE_HEAD = 2;

    private static int COMPANY_ID = 000000;
    public static String SCREEN_BASE_PATH = Constants.HEAD_PATH + COMPANY_ID + "/";//人脸头像存储路径

    private FloatSyncView floatSyncView;
    private ExecutorService executorService;

    private int remoteCount = 0;
    private int localCount = 0;

    public static SyncManager instance() {
        if (instance == null) {
            synchronized (SyncManager.class) {
                if (instance == null) {
                    instance = new SyncManager();
                }
            }
        }
        return instance;
    }

    private SyncManager() {
        File file = new File(SCREEN_BASE_PATH);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION) {
            /*判断当前网络时候可用以及网络类型*/
            boolean networkConnected = isNetworkConnected(context);
            if (networkConnected) {
                initInfo();
            } else {
                setFailed("失败", "网络不可用，请检查网络");
            }
        }
    }

    public interface LoadListener{
        void onLoaded(CompanyBean companyBean);

        void onFinish();
    }
    private LoadListener mListener;
    public void setListener(LoadListener listener){
        mListener = listener;
    }

    /***
     * 初始化数据
     * @param act
     * @return
     */
    public SyncManager init(@NonNull Activity act) {
        mAct = act;
        userDao = APP.getUserDao();
        executorService = Executors.newFixedThreadPool(2);
        String webBaseUrl = ResourceUpdate.WEB_BASE_URL;
        String[] split = webBaseUrl.split(":");
        for (String s : split) {
            if (s.startsWith("192.168")) {
                isLocalServ = true;
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mAct.registerReceiver(this, filter);
        return instance;
    }

    /***
     * 全部流程重新初始化
     */
    public void initInfo() {
        OkHttpUtils.getInstance().cancelTag(this);
        cancelTimer();
        remoteCount = 0;
        localCount = 0;
        show();
        loadCompany();
    }

    public UserDao getUserDao(){
        return userDao;
    }

    private void loadCompany() {
        setStep(1,null);
        d("-------------" + ResourceUpdate.COMPANYINFO);
        final Map<String, String> map = new HashMap<>();
        String deviceNo = HeartBeatClient.getDeviceNo();
        Log.e(TAG, "loadCompany: " + deviceNo);
        map.put("deviceNo", deviceNo);
        d("99999",map.toString());
        OkHttpUtils.post().params(map).tag(this).url(ResourceUpdate.COMPANYINFO).build().execute(new MyStringCallback<CompanyBean>(MyStringCallback.STEP_COMPANY) {
            @Override public void onRetryAfter5s() {
                loadCompany();
            }
            @Override public void onFailed() {}
            @Override public void onSucc(final String response, final CompanyBean companyBean) {
                COMPANY_ID = companyBean.getCompany().getComid();
                String abbname = companyBean.getCompany().getAbbname();
                SCREEN_BASE_PATH = Constants.HEAD_PATH + COMPANY_ID + "/";//人脸头像存储路径

                SpUtils.saveStr(SpUtils.COMPANY_INFO, response);
                //保存公司信息
                SpUtils.saveInt(SpUtils.COMPANYID, companyBean.getCompany().getComid());
                SpUtils.saveStr(SpUtils.GOTIME, companyBean.getCompany().getGotime());
                SpUtils.saveStr(SpUtils.GOTIPS, companyBean.getCompany().getGotips());
                SpUtils.saveStr(SpUtils.DOWNTIME, companyBean.getCompany().getDowntime());
                SpUtils.saveStr(SpUtils.DOWNTIPS, companyBean.getCompany().getDowntips());
                SpUtils.saveStr(SpUtils.COMPANY_NAME, abbname);

                if(mListener != null){
                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onLoaded(companyBean);
                        }
                    });
                }
                loadStaff(companyBean);
            }
        });
    }

    //加载员工信息
    private void loadStaff(final CompanyBean companyBean) {
        setStep(2,null);
        int comId = companyBean.getCompany().getComid();
        if (comId == 0) {
            setFailed("失败", "数据异常");
            return;
        }
        d("请求员工信息");
        final HashMap<String, String> map = new HashMap<>();
        map.put("companyId", comId + "");
        OkHttpUtils.post().params(map).tag(this).url(ResourceUpdate.GETSTAFF).build().execute(new MyStringCallback<StaffBean>(MyStringCallback.STEP_STAFF) {
            @Override
            public void onRetryAfter5s() {
                loadStaff(companyBean);
            }

            @Override
            public void onFailed() {
            }

            @Override
            public void onSucc(String response, final StaffBean staffBean) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        syncUserDao(staffBean);
                    }
                });
            }
        });
    }

    final Map<String, UserBean> staffMap = new HashMap<>();
    private void syncUserDao(final StaffBean staffBean) {
        if (staffBean == null) {
            return;
        }
        setStep(4,null);
        List<UserBean> localDataList = userDao.selectAll();


        List<StaffBean.DepBean> depList = staffBean.getDep();
        for (StaffBean.DepBean depBean : depList) {
            List<StaffBean.EntryInfo> entry = depBean.getEntry();
            for (StaffBean.EntryInfo entryInfo : entry) {
                staffMap.put(entryInfo.getFaceId(),new UserBean(entryInfo.getId(),depBean.getDepId(),entryInfo.getFaceId()
                        ,entryInfo.getName(),entryInfo.getSex() ==1 ?"男":"女",
                        entryInfo.getPosition(),entryInfo.getHead(),0,depBean.getDepName()
                        ,entryInfo.getNumber(),entryInfo.getBirthday(),entryInfo.getAutograph()
                        ,false));
            }
        }

        if(localDataList != null){
            localCount = localDataList.size();
        }
        if(staffMap != null){
            remoteCount = staffMap.size();
        }
        d("本地数据共有：" + localCount);
        d("服务器数据共有：" + remoteCount);

        for (UserBean userBean : localDataList) {
            String faceId = userBean.getFaceId();
            if(!staffMap.containsKey(faceId)){
                boolean isRemoveSucc = FaceSDK.instance().removeUser(faceId);
                userDao.remove(userBean);
                Log.e(TAG, "syncUserDao: ----- 删除结果： " + isRemoveSucc + " ----- " + userBean.toString());
            }
        }

        Queue<UpdateBean> updateList = new LinkedList<>();
        for (Map.Entry<String, UserBean> entryInfo : staffMap.entrySet()) {
            UserBean newUserBean = entryInfo.getValue();
            String faceId = newUserBean.getFaceId();
            List<UserBean> userBeans = userDao.queryByFaceId(faceId);
            UpdateBean updateBean = new UpdateBean();
            updateBean.head = newUserBean.getImgUrl();

            if(userBeans == null || userBeans.size()<= 0){//添加
                updateBean.ctrlType = TYPE_ADD;
            } else {//更新
                UserBean oldUserBean = userBeans.get(0);

                if (hasUpdate(newUserBean,oldUserBean)) {
                    updateBean.ctrlType = TYPE_UPDATE_INFO;
                }

                String urlPath = newUserBean.getImgUrl();
                int index = urlPath.lastIndexOf("/");
                String str = urlPath.substring(index + 1, urlPath.length());
                String newFilePath = SCREEN_BASE_PATH + str;
                newUserBean.setImgUrl(newFilePath);

                if(hasHeadUpdate(newFilePath,oldUserBean.getImgUrl())){
                    updateBean.ctrlType = TYPE_UPDATE_HEAD;
                }
            }
            updateBean.userBean = newUserBean;

            updateList.add(updateBean);
            setStep(0,newUserBean.getName());
        }

        download(updateList);
    }

    private void retryAddSDK(){
        if (FaceSDK.instance().getAllUserSize() <= 0 || FaceSDK.instance().getAllUserSize() != staffMap.size()) {
            for (Map.Entry<String, UserBean> entry : staffMap.entrySet()) {
                FaceSDK.instance().addUser(entry.getKey(), entry.getValue().getImgUrl(), new FaceUserManager.FaceUserCallback() {
                    @Override
                    public void onUserResult(boolean b, int i) {
                        Log.e(TAG, "重添加库结果：" + b + " --- " + i );
                    }
                });
            }
        }
    }

    private void download(final Queue<UpdateBean> queue){
        if(queue.size()<= 0){
            retryAddSDK();
            close();
            return;
        }
        final UpdateBean bean = queue.poll();
        if(bean.ctrlType == -1){
            download(queue);
            return;
        }

        final UserBean userBean = bean.userBean;
        if(bean.ctrlType == TYPE_ADD || bean.ctrlType == TYPE_UPDATE_HEAD){
            setStep(5, userBean.getName());

            staffMap.put(userBean.getFaceId(),userBean);

            MyXutils.getInstance().downLoadFile(bean.head, userBean.getImgUrl(), false, new MyXutils.XDownLoadCallBack() {
                @Override
                public void onStart() {
                    Log.e(TAG, "开始：" + userBean.getImgUrl());
                }

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {
                    Log.e(TAG, "进度：" + current);
                }

                @Override
                public void onSuccess(File result) {
                    userBean.setDownloadTag(true);
                    userBean.setImgUrl(result.getPath());
                    if(bean.ctrlType == TYPE_ADD){
                        int add = userDao.add(userBean);
                        Log.e(TAG, "添加Dao结果：" + add);
                        FaceSDK.instance().addUser(userBean.getFaceId(), userBean.getImgUrl(), new FaceUserManager.FaceUserCallback() {
                            @Override
                            public void onUserResult(boolean b, int i) {
                                Log.e(TAG, "添加结果：" + b + " --- " + i);
                            }
                        });
                    } else {
                        userDao.remove(userBean);

                        int update = userDao.update(userBean);
                        Log.e(TAG, "更新Dao结果：" + update);

                        boolean b = FaceSDK.instance().removeUser(userBean.getFaceId());
                        Log.e(TAG, "删除结果：" + b);
                        FaceSDK.instance().addUser(userBean.getFaceId(), userBean.getImgUrl(), new FaceUserManager.FaceUserCallback() {
                            @Override
                            public void onUserResult(boolean b, int i) {
                                Log.e(TAG, "添加结果：" + b + " --- " + i);
                            }
                        });
                    }
                    Log.e(TAG, "完成：" + result.getPath());
                }

                @Override
                public void onError(Throwable ex) {
                    userBean.setDownloadTag(false);
                    String msg;
                    if (bean.ctrlType == TYPE_ADD) {
                        msg = "添加员工失败，原因：下载头像失败 ";
                    } else {
                        msg = "更新头像失败，原因：下载头像失败 ";
                    }
                    msg +=  ex != null ? ex.getMessage() : "NULL";

                    if(bean.ctrlType == TYPE_ADD){
                        int add = userDao.add(userBean);
                        Log.e(TAG, "添加Dao结果：" + add);
                    } else {
                        int update = userDao.update(userBean);
                        Log.e(TAG, "更新Dao结果：" + update);
                    }

                    userBean.setErrMark(msg);
                    Log.e(TAG, msg);
                }

                @Override
                public void onFinished() {
                    download(queue);
                }
            });
        }
    }

    private boolean hasUpdate(UserBean newUserBean,UserBean oldUserBean){
        if (!TextUtils.equals(newUserBean.getName(),oldUserBean.getName())) { return true; }
        if(!TextUtils.equals(newUserBean.getBirthday(),oldUserBean.getBirthday())){ return true; }
        if(!TextUtils.equals(newUserBean.getEmployNum(),oldUserBean.getEmployNum())){ return true; }
        if( !TextUtils.equals(newUserBean.getSignature(),oldUserBean.getSignature())){return true;}
        if(!TextUtils.equals(newUserBean.getCardId(),oldUserBean.getCardId())){return true;}
        if(!TextUtils.equals(newUserBean.getJob(),oldUserBean.getJob())){return true;}
        if(newUserBean.getDepartId() != oldUserBean.getDepartId()){return true;}
        if(!TextUtils.equals(newUserBean.getDepart(),oldUserBean.getDepart())){return true;}
        return false;
    }

    //判断头像是否有更新
    private boolean hasHeadUpdate(String newImgUrl, String oldImagUrl) {//员工头像和本地存储的头像是否冲突一致
        File fileLoc = new File(oldImagUrl);
        boolean isUpdate  = !TextUtils.equals(newImgUrl,oldImagUrl);
        Log.e(TAG, "isHeadUpdate: -----" + newImgUrl + "-----" + oldImagUrl + "-----" +fileLoc.exists() + "-----" + isUpdate);
        if (!fileLoc.exists()) {
            return true;
        }
        return isUpdate;
    }

    public void destory() {
        OkHttpUtils.getInstance().cancelTag(this);
        if(floatSyncView != null){
            floatSyncView.dismiss();
            floatSyncView = null;
        }
        try{
            mAct.unregisterReceiver(this);
        }catch (Exception e){
            Log.d(TAG,TAG+"广播未注册");
        }
    }

    //更新bean
    class UpdateBean {
        int ctrlType = -1;
        String head;
        UserBean userBean;
    }

    /***
     * 带UI更新的请求回调
     * @param <T>
     */
    abstract class MyStringCallback<T> extends StringCallback {
        private String title;
        private int step;
        public static final int STEP_COMPANY = 1;
        public static final int STEP_STAFF = 3;
        public static final String TITLE_COMPANY = "公司信息";
        public static final String TITLE_STAFF = "员工信息";
        private Handler handler = new Handler(Looper.getMainLooper());

        public MyStringCallback(int s) {
            step = s;
            switch (step) {
                case STEP_COMPANY:
                    title = TITLE_COMPANY;
                    break;
                case STEP_STAFF:
                    title = TITLE_STAFF;
                    break;
            }
        }

        public abstract void onRetryAfter5s();

        public abstract void onFailed();

        public abstract void onSucc(String response, T t);

        @Override
        public void onBefore(Request request, int id) {
            super.onBefore(request, id);
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
            Log.e(TAG, "onError: ----- " + e != null ? e.getMessage() : "NULL");
            String err = "请求失败";
            if(e != null && (!TextUtils.isEmpty(e.getMessage()))){
                if(e.getMessage().contains("404")){
                    err = "服务器异常";
                } else if(e.getMessage().contains("500")){
                    err = "服务器异常";
                }
            }
            if (isLocalServ || isNetworkConnected(APP.getContext())) {
                setErr("失败",err + "，5秒后重试...");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onRetryAfter5s();
                    }
                }, 5 * 1000);
            } else {
                setFailed("失败", "同步失败，请检查网络连接");
                onFailed();
            }
        }

        @Override
        public void onResponse(String response, int id) {
            d(TAG,response);
            Object o = null;
            if (step == STEP_COMPANY) {
                CompanyBean bean = new Gson().fromJson(response, CompanyBean.class);
                if (bean.getStatus() != 1) {
                    String err = "同步失败，请检查网络或重启设备";
                    switch (bean.getStatus()) {
                        case 3://设备不存在（参数错误）
                            err = "设备不存在";
                            break;
                        case 4://设备未绑定
                            err = "请先绑定设备";
                            break;
                        case 5://未设置主题
                            err = "该设备未设置主题";
                            break;
                        default://获取失败
                            err = "同步失败，错误码：" + bean.getStatus();
                            break;
                    }
                    setFailed("失败", err);
                    onFailed();
                    return;
                }
                o = bean;
            } else {//员工信息
                StaffBean staffInfo = new Gson().fromJson(response, StaffBean.class);
                if (staffInfo.getStatus() != 1) {
                    String err = "同步失败，请检查网络或重启设备";
                    switch (staffInfo.getStatus()) {
                        case 3://公司不存在
                            err = "公司不存在";
                            break;
                        case 4://公司未设置部门
                            err = "该公司未设置部门";
                            break;
                        default://参数错误
                            err = "数据异常，错误码：" + staffInfo.getStatus();
                            break;
                    }
                    setFailed("失败", err);
                    onFailed();
                    return;
                }
                o = staffInfo;
            }
            onSucc(response,(T) o);
        }
    }

    /*======UI显示============================================================================================*/
    //显示同步UI
    private void show() {
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView == null) {
                    floatSyncView = new FloatSyncView(APP.getContext());
                }
                floatSyncView.initUIState();
                floatSyncView.show();
            }
        });
    }

    private Timer timer;
    private void startTimer(TimerTask timerTask,long delay){
        if(timer != null){
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(timerTask,delay);
    }

    private void cancelTimer(){
        if(timer != null){
            timer.cancel();
        }
    }

    //关闭同步
    private void close() {
        List<UserBean> userBeans = userDao.selectAll();
        localCount = userBeans.size();
        EventBus.getDefault().postSticky(new EmployListActivity.EmployUpdate());

        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView != null) {
                    setStep(0,"同步完成");
                    if(mListener != null){
                        mListener.onFinish();
                    }

                    floatSyncView.hideLoadingView();
                    floatSyncView.showDownloadView(false);
                    floatSyncView.showCount(localCount, remoteCount);

                    startTimer(new TimerTask() {
                        @Override
                        public void run() {
                            if(floatSyncView != null){
                                floatSyncView.dismiss();
                            }
                        }
                    }, 3 * 1000);
                }
            }

        });
    }

    //错误显示
    private void setErr(final String info, final String err){
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(floatSyncView != null){
                    if(!TextUtils.isEmpty(info)){
                        floatSyncView.setErr(err,true);
                    }
                    if(!TextUtils.isEmpty(err)){
                        floatSyncView.setInfo(info);
                    }
                }
            }
        });
    }

    //失败显示
    private void setFailed(final String info, final String errStr) {
        if (floatSyncView != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.hideLoadingView();
                }
            });
        }
        setErr(info,errStr);
    }

    //下载进度
    private void setDownloadP(final int max, final int p){
        if(floatSyncView != null){
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setP(max,p);
                }
            });
        }
    }

    private void setStep(final int max, final int p, final String info){
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView != null) {
                    floatSyncView.showDownloadView(true);
                    floatSyncView.setStep(p + "/" + max);

                    if (!TextUtils.isEmpty(info)) {
                        floatSyncView.setInfo(info);
                    }
                }
            }
        });
    }

    //步骤
    private void setStep(int i,String info){
        String step = null;
        if(i>=1 && i <=5){
            step = i + "/5";
        }
        boolean showDownload = false;
        switch (i) {
            case 1:
                info = "查询公司信息";
                step += info;
                info = null;
                break;
            case 2:
                info = "查询员工信息";
                step += info;
                info = null;
                break;
            case 3:
                info = "删除无效数据";
                step += info;
                info = null;
                break;
            case 4:
                info = "同步员工信息";
                step += info;
                info = null;
                break;
            case 5:
                step += "下载头像";
                showDownload = true;
                break;
            default:break;
        }

        final String finalInfo = info;
        final String finalStep = step;
        final boolean finalShowDownload = showDownload;
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView != null) {
                    floatSyncView.showDownloadView(finalShowDownload);

                    if(!TextUtils.isEmpty(finalStep)){
                        floatSyncView.setStep(finalStep);
                    }
                    if (!TextUtils.isEmpty(finalInfo)) {
                        floatSyncView.setInfo(finalInfo);
                    }
                }
            }
        });
    }

    /*===========判断方法=====================================================================================*/
    //判断网络连接
    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private static final String TAG = "SyncManager";

    private void d(String log) {
        if (true) {
            Log.d(TAG, log);
        }
    }

    //日志打印不全
    public static void d(String tag, String msg) {  //信息太长,分段打印

        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，

        //  把4*1024的MAX字节打印长度改为2001字符数

        int max_str_length = 2001 - tag.length();

        //大于4000时

        while (msg.length() > max_str_length) {

            Log.i(tag, msg.substring(0, max_str_length));

            msg = msg.substring(max_str_length);

        }

        //剩余部分

        Log.d(tag, msg);

    }
}
