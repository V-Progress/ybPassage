package com.yunbiao.yb_passage.common;


import android.os.SystemProperties;
import android.util.Log;

import com.yunbiao.yb_passage.APP;
import com.yunbiao.yb_passage.afinel.Constants;
import com.yunbiao.yb_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_passage.afinel.VersionUpdateConstants;
import com.yunbiao.yb_passage.heartbeat.HeartBeatClient;
import com.yunbiao.yb_passage.utils.*;
import com.yunbiao.yb_passage.utils.xutil.MyXutils;
import com.yunbiao.yb_passage.utils.CommonUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by LiuShao on 2016/3/4.
 */

public class MachineDetail {
    private static final String TAG = "MachineDetail";
    private String upMechineDetialUrl = Constants.RESOURCE_URL + "device/service/updateDeviceHardwareInfo.html";

    private static MachineDetail machineDetial;

    public static MachineDetail getInstance() {
        if (machineDetial == null) {
            machineDetial = new MachineDetail();
        }
        return machineDetial;
    }

    private MachineDetail() {

    }

    public void upLoadDeviceType() {
        String deviceNo = HeartBeatClient.getDeviceNo();
        HashMap<String, String> map = new HashMap<>();
        map.put("deviceNo", deviceNo);
        map.put("type", "3");
        Log.e(TAG, "upLoadDeviceType: " + ResourceUpdate.UPDATE_TYPE + " --- " + map.toString());
        OkHttpUtils.post().url(ResourceUpdate.UPDATE_TYPE).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: ---------------" + e == null ? "NULL" : e.getMessage());
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: " + response);
            }
        });

    }

    /**
     * 上传设备信息
     */
    public void upLoadHardWareMessage() {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> map = new HashMap<>();
                map.put("deviceNo", HeartBeatClient.getDeviceNo());
                map.put("screenWidth", String.valueOf(CommonUtils.getScreenWidth(APP.getContext())));
                map.put("screenHeight", String.valueOf(CommonUtils.getScreenHeight(APP.getContext())));
                map.put("diskSpace", SdCardUtils.getMemoryTotalSize());
                map.put("useSpace", SdCardUtils.getMemoryUsedSize());
                map.put("softwareVersion", CommonUtils.getAppVersion(APP.getContext()) + "_" + VersionUpdateConstants
                        .CURRENT_VERSION);
                map.put("screenRotate", String.valueOf(SystemProperties.get("persist.sys.hwrotation")));
                map.put("deviceCpu", com.yunbiao.yb_passage.utils.CommonUtils.getCpuName() + " " + com.yunbiao.yb_passage.utils.CommonUtils.getNumCores() + "核" + com.yunbiao.yb_passage.utils.CommonUtils
                        .getMaxCpuFreq() + "khz");
                map.put("deviceIp", NetworkUtils.getIpAddress());//当前设备IP地址
                map.put("mac", NetworkUtils.getLocalMacAddress());//设备的本机MAC地址

                map.put("camera", CommonUtils.checkCamera());//设备是否有摄像头 1有  0没有

                MyXutils.getInstance().post(upMechineDetialUrl, map, new MyXutils.XCallBack() {
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
        });
    }
}
