package com.yunbiao.yb_passage.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.yunbiao.yb_passage.R;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final View ivYuan = findViewById(R.id.iv_yuan);
        final View ivLine = findViewById(R.id.iv_line);

        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(3000);//设置动画持续周期
        rotate.setRepeatCount(-1);//无限重复
        rotate.setStartOffset(10);//执行前的等待时间
        ivYuan.startAnimation(rotate);

        TranslateAnimation ta = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, -0.2f,
                Animation.RELATIVE_TO_PARENT, 0.35f);
        ta.setRepeatCount(-1);
        ta.setFillAfter(true);
        ta.setDuration(2000);
        ta.setRepeatMode(Animation.REVERSE);

        ScaleAnimation bigToSmallAnim = new ScaleAnimation(1, 0.6f, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);//x轴0倍，x轴1倍，y轴0倍，y轴1倍
        bigToSmallAnim.setRepeatCount(-1);
        bigToSmallAnim.setDuration(2000);
        bigToSmallAnim.setRepeatMode(Animation.REVERSE);
        bigToSmallAnim.setFillAfter(true);

        AnimationSet animationSet = new AnimationSet(true);//共用动画补间
        animationSet.addAnimation(ta);
        animationSet.addAnimation(bigToSmallAnim);
        // 动画是作用到某一个控件上
        ivLine.startAnimation(animationSet);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ivYuan.clearAnimation();
                ivLine.clearAnimation();
                startActivity(new Intent(SplashActivity.this,WelComeActivity.class));
                finish();
            }
        },5 * 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
