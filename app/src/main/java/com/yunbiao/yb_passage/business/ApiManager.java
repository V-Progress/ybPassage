package com.yunbiao.yb_passage.business;

import android.os.Handler;
import android.os.Message;

import com.android.xhapimanager.XHApiManager;

public class ApiManager {

    private XHApiManager xhApiManager;
    int ligntTime = 5;
    int gateTime = 5;
    private static ApiManager apiManager = new ApiManager();
    private boolean lignHandlerRunning = false;
    private boolean gateHandlerRunning = false;

    Handler lightHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            lignHandlerRunning = true;
            ligntTime--;
            if(ligntTime <= 0){
                ligntTime = 5;
                offLight();
                lignHandlerRunning = false;
                return;
            }
            sendEmptyMessageDelayed(0,1000);
        }
    };

    Handler gateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            gateHandlerRunning = true;
            gateTime--;
            if(gateTime <= 0){
                gateTime = 5;
                offGate();
                gateHandlerRunning = false;
                return;
            }
            sendEmptyMessageDelayed(0,1000);
        }
    };

    public static ApiManager instance(){
        return apiManager;
    }
    private ApiManager(){
        xhApiManager = new XHApiManager();
    }

    /***
     * 必须在Application中初始化
     */
    public void init(){
        if(isLighting()){
            offLight();
        }
    }

    public void onLignt(){
        if(!isLighting()){
            xhApiManager.XHSetGpioValue(4,1);
        } else {
            ligntTime = 5;
        }

        if(!lignHandlerRunning){
            lightHandler.sendEmptyMessage(0);
        }
    }

    public void offLight(){
        xhApiManager.XHSetGpioValue(4,0);
    }

    public boolean isLighting(){
        return xhApiManager.XHReadGpioValue(4) == 1;
    }

    public void onGate(){
        if(!isGateOpened()){
            xhApiManager.XHSetGpioValue(5,0);
        } else {
            gateTime = 5;
        }

        if(!gateHandlerRunning){
            gateHandler.sendEmptyMessage(0);
        }
    }

    public void offGate(){
        xhApiManager.XHSetGpioValue(5,1);
    }
    public boolean isGateOpened(){
        return xhApiManager.XHReadGpioValue(5) == 0;
    }

}
