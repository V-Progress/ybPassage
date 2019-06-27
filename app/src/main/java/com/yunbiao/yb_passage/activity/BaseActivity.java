package com.yunbiao.yb_passage.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BaseActivity extends Activity {
    protected boolean isLog = true;
    private static final String TAG = "BaseActivity";
    private static List<Activity> activities = new ArrayList<Activity>();
    /**
     * 获得Activity
     */
    public static Activity getActivity() {
        return activities.get(activities.size() - 1);
    }

    /**
     * finish所有Activity
     */
    public static void finishAll() {
        finish(null);
    }

    /**
     * finish所有其它Activity
     */
    public static void finishOthers(Class<? extends Activity> activity) {
        finish(activity);
    }

    protected void d(String log){
        if(isLog){
            Log.d(this.getClass().getSimpleName(),log);
        }
    }

    public static void finish(Class<? extends Activity> currentActivity) {
        for (Iterator<Activity> iterator = activities.iterator(); iterator.hasNext(); ) {
            Activity activity = iterator.next();
            if (activity.getClass() == currentActivity) {
                continue;
            }
            iterator.remove();
            activity.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏navigation
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activities.add(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        activities.remove(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_MENU) {
//            MenuToShow.getInstance().showMenu(this);
//        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 清除图片内存缓存
     */
    public void clearImageMemoryCache(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
