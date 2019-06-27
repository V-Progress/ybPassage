package com.yunbiao.yb_passage.afinel;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.yunbiao.yb_passage.APP;
import com.yunbiao.yb_passage.heartbeat.HeartBeatClient;
import com.yunbiao.yb_passage.utils.SdCardUtils;
import com.yunbiao.yb_passage.utils.UIUtils;
import com.yunbiao.yb_passage.utils.xutil.MyXutils;

import java.util.HashMap;


public class ResourceUpdate {

    public static String WEB_BASE_URL = Constants.RESOURCE_URL;

    public static String CHECK_PASS = WEB_BASE_URL + "api/facewitness/check.html";
    public static String UPLOAD_PASS_RECORD = WEB_BASE_URL + "api/facewitness/checkByarray.html";
    public static String UPDATE_TYPE = WEB_BASE_URL + "api/device/updateDeviceType.html";
    public static String HOLIREM = WEB_BASE_URL + "api/holidayrem/getHolirem.html";//节日提醒背景
    public static String JOINREM = WEB_BASE_URL + "api/holidayrem/getJoinrem.html";//生日提醒背景
    public static String QRCODE_ADD = WEB_BASE_URL + "nhb/entryCode.html";
    public static String COMPANYINFO = WEB_BASE_URL + "api/company/getcompany.html";//获取公司信息以及下的部门信息接口
    public static String UPDATSTAFF = WEB_BASE_URL + "api/entry/entryupdate.html";//修改员工信息接口
    public static String ADDSTAFF = WEB_BASE_URL + "api/entry/entryadd.html";//添加员工信息接口
    public static String DELETESTAFF = WEB_BASE_URL + "api/entry/entrydelete.html";//删除员工接口
    public static String GETSTAFF = WEB_BASE_URL + "api/entry/getentry.html";//获取员工信息接口
    public static String SIGNLOG = WEB_BASE_URL + "api/sign/signlog.html";//创建签到信息接口
    public static String BULUSIGN = WEB_BASE_URL + "api/entry/entryecord.html";//补录接口
    public static String SIGNARRAY = WEB_BASE_URL + "api/sign/signlogByarray.html";//定时发送签到列表
    public static String ADDDEPART = WEB_BASE_URL + "api/department/departmentadd.html";//创建部门
    public static String DELETEDEPART = WEB_BASE_URL + "api/department/departmentdelete.html";//删除部门
    public static String GETAD = WEB_BASE_URL + "api/company/getadvert.html";//获取广告
    public static String getWeatherInfo = "http://www.yunbiaowulian.com/weather/city.html";
    public static String DEVICE_ONLINE_STATUS = WEB_BASE_URL + "device/status/getrunstatus.html";
    public static String VERSION_URL = WEB_BASE_URL + "device/service/getversion.html";
    public static String POWER_OFF_URL = WEB_BASE_URL + "device/service/poweroff.html";
    public static String SCREEN_UPLOAD_URL = WEB_BASE_URL + "device/service/uploadScreenImg.html";

    /**
     * 音量调节值获取
     * http://tyiyun.com/device/service/getVolume.html?deviceId=ffffffff-
     * be09-eca9-756a-0d8000000000
     */
    private static String VOLUME_URL = WEB_BASE_URL + "device/service/getVolume.html";
    private static String UPLOAD_APP_VERSION_URL = WEB_BASE_URL + "device/service/uploadAppVersionNew.html";
    private static String UPLOAD_DISK_URL = WEB_BASE_URL + "device/service/uploadDisk.html";


    private static String CACHE_BASE_PATH = Constants.LOCAL_ROOT_PATH;
    public static String PROPERTY_CACHE_PATH = CACHE_BASE_PATH + "property/";// 参数缓存存储目录
    public static String SCREEN_CACHE_PATH = CACHE_BASE_PATH + "screen/";//参数缓存存储目录

    public static void uploadAppVersion() {
        UIUtils.showTitleTip("版本:" + getVersionName());
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("deviceNo", HeartBeatClient.getDeviceNo());
        paramMap.put("version", getVersionName());
        paramMap.put("type", 1 + "");
        MyXutils.getInstance().post(UPLOAD_APP_VERSION_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    /**
     * 获取当前版本号
     *
     * @return
     */
    public static String getVersionName() {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = APP.getContext().getApplicationContext().getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(APP.getContext().getApplicationContext().getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception e) {
        }
        return version;
    }

    /**
     * 上传磁盘数据
     */
    public static void uploadDiskInfo() {
        String diskInfo = SdCardUtils.getSDDiskCon();
        String ss = "磁盘:" + diskInfo;
        UIUtils.showTitleTip(ss);
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("deviceNo", HeartBeatClient.getDeviceNo());
        paramMap.put("diskInfo", diskInfo);
        MyXutils.getInstance().post(UPLOAD_DISK_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }


}
