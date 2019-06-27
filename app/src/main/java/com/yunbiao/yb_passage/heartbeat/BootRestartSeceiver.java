package com.yunbiao.yb_passage.heartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.yunbiao.yb_passage.APP;
import com.yunbiao.yb_passage.activity.WelComeActivity;
import com.yunbiao.yb_passage.common.power.PowerOffTool;
import com.yunbiao.yb_passage.utils.CommonUtils;
import com.yunbiao.yb_passage.utils.SpUtils;
import com.yunbiao.yb_passage.utils.ThreadUitls;
import com.yunbiao.yb_passage.utils.logutils.LogUtils;


public class BootRestartSeceiver extends BroadcastReceiver {
    private static final String TAG = "BootRestartSeceiver";
    private String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: " + action);
        if (action.equals(ACTION)) {
            //开启看门狗,只会在开机是启动一次
            context.startService(new Intent(context, MyProtectService.class));
            //自动开关机
            ThreadUitls.runInThread(machineRestartRun);
            //开机重置开关机设置标志，A20定时关机会重走程序，定时开关机失效，然后加上这个标志
            LogUtils.i(TAG, "重启当前时间：" + CommonUtils.getStringDate());
            try {
                //开机恢复之前保存的声音的大小，中恒板子关机实际上是屏幕休眠，但是开机是休眠时间到先关机后开机，rom是这样的，
                int sound = SpUtils.getInt(SpUtils.CURR_VOLUME);
                if (sound > 0) {
                    AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sound, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
                Intent i = new Intent(context, WelComeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Runnable machineRestartRun = new Runnable() {
        public void run() {
            PowerOffTool.getPowerOffTool().machineStart();
        }
    };
}
