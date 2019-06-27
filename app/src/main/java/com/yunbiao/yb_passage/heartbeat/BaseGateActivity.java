package com.yunbiao.yb_passage.heartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.yunbiao.yb_passage.APP;
import com.yunbiao.yb_passage.activity.BaseActivity;
import com.yunbiao.yb_passage.serialport.ComResponse;
import com.yunbiao.yb_passage.serialport.SerialPortHelper;
import com.yunbiao.yb_passage.serialport.plcgate.GateCommands;
import com.yunbiao.yb_passage.serialport.plcgate.GateConnection;
import com.yunbiao.yb_passage.serialport.utils.ThreadManager;
import com.yunbiao.yb_passage.xmpp.LogUtil;

/**
 * Created by chen on 2019/4/3.
 */

public class BaseGateActivity extends BaseActivity {

    private static final String LOGTAG = LogUtil.makeLogTag(BaseGateActivity.class);

    public GateConnection mGateConnection;

    private BroadcastReceiver mReceiver;

    public boolean mGateIsAlive = false;
    public boolean mGateHeartbeat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPort();
        registerLocalReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unRegisterReceiver();
    }

    private void initPort() {
        ThreadManager.getInstance().executeAsyncTask(new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mGateConnection = GateConnection.getIns();
                if (mGateConnection.isOpen()) {
                    return null;
                }
                mGateConnection.initSerialPort(APP.getContext());

                if (mGateConnection.isOpen()) {
                    //串口初始化成功，开始检测门禁控制板
                    //默认门禁控制板正常
                    mGateIsAlive = true;
                    checkHeartbeat();
                } else {
                    showToast("门控模块加载失败!");
                }
                return null;
            }
        });
    }

    private Handler checkHeartbeatHandler = new Handler();
    private Runnable checkHeartbeatRunnable = null;
    private long checkHeartbeatTime = 60 * 1000;
    //定时检测控制板心跳
    private void checkHeartbeat () {
        if (checkHeartbeatRunnable == null) {
            checkHeartbeatRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!mGateHeartbeat) {
                        //控制板心跳消失
                        Log.i(LOGTAG, "----- 心跳消失 -----");
                        mGateIsAlive = false;
                    } else {
                        Log.i(LOGTAG, "----- 心跳正常 -----");
                        mGateIsAlive = true;
                    }
                    mGateHeartbeat = false;
                    checkHeartbeatHandler.postDelayed(this, checkHeartbeatTime);
                }
            };
        }
        checkHeartbeatHandler.postDelayed(checkHeartbeatRunnable, checkHeartbeatTime);
    }

    //重启串口
    private void restartPort() {
        mGateConnection.closeSerialPort();
        initPort();
    }

    private void registerLocalReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (SerialPortHelper.ACTION_GATE_RECEIVE.equals(action)) {
                    ComResponse response = (ComResponse) intent.getSerializableExtra("comResponse");
                    onGateReceive(response);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(SerialPortHelper.ACTION_GATE_RECEIVE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    private void unRegisterReceiver() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
    }

    private String lastReceive = "";

    private void onGateReceive(ComResponse response) {
        String re = response.getResponse();
        if (!TextUtils.isEmpty(re)) {
            if (lastReceive == "" && !re.equals("6F")) {
                return;
            }
            String allReceive = lastReceive + re;
            lastReceive += re;
            if (allReceive.length() == 4 && allReceive.equals(GateCommands.GATE_CONNECT_OK)) {
                lastReceive = "";
                mGateHeartbeat = true;
                mGateIsAlive = true;
                Log.i("===---===", "---allReceive = " + allReceive);
            } else if (allReceive.length() == 6 && allReceive.equals(GateCommands.GATE_DOOR_ON)) {
                lastReceive = "";
                Log.i("===---===", "---allReceive = " + allReceive);
            } else if (allReceive.length() == 8 && allReceive.equals(GateCommands.GATE_DOOR_OFF)) {
                lastReceive = "";
                Log.i("===---===", "---allReceive = " + allReceive);
            }
        }
    }

    private Toast mToast;

    public void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.setText(message);
                } else {
                    mToast = Toast.makeText(APP.getContext(), message, Toast.LENGTH_SHORT);
                }
                mToast.show();
            }
        });
    }
}