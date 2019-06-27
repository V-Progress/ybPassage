package com.yunbiao.yb_passage.activity;

import android.os.Bundle;
import android.view.View;

import com.yunbiao.yb_passage.R;
import com.yunbiao.yb_passage.heartbeat.BaseGateActivity;

public class TestActivity extends BaseGateActivity {

    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void addSign(View view){
    }
}


