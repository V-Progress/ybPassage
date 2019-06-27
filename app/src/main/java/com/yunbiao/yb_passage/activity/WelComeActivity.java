package com.yunbiao.yb_passage.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.yb_passage.APP;
import com.yunbiao.yb_passage.R;
import com.yunbiao.yb_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_passage.bean.AddQRCodeBean;
import com.yunbiao.yb_passage.bean.CompanyBean;
import com.yunbiao.yb_passage.business.AdsManager;
import com.yunbiao.yb_passage.business.ApiManager;
import com.yunbiao.yb_passage.business.LocateManager;
import com.yunbiao.yb_passage.business.PassageManager;
import com.yunbiao.yb_passage.business.SpeechManager;
import com.yunbiao.yb_passage.business.SyncManager;
import com.yunbiao.yb_passage.business.VipDialogManager;
import com.yunbiao.yb_passage.db.PassageBean;
import com.yunbiao.yb_passage.faceview.FaceView;
import com.yunbiao.yb_passage.heartbeat.BaseGateActivity;
import com.yunbiao.yb_passage.serialport.plcgate.GateCommands;
import com.yunbiao.yb_passage.utils.RestartAPPTool;
import com.yunbiao.yb_passage.utils.SpUtils;
import com.yunbiao.yb_passage.utils.UIUtils;
import com.yunbiao.yb_passage.xmpp.ServiceManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/11/26.
 */

public class WelComeActivity extends BaseGateActivity {

    private static final String TAG = "WelComeActivity";

    private ImageView imageView;//公司logo
    private TextView tv_comName;//公司名
    private TextView tv_notice;//公司提醒
    private TextView tv_topTitle;//标题
    private TextView tv_bottomTitle;//底部标题

    // xmpp推送服务
    private ServiceManager serviceManager;

    //摄像头分辨率
    private FaceView faceView;
    private TextView tvVerify;

    private int verifyTime = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        initViews();

        //开启Xmpp
        startXmpp();

        //初始化定位工具
        LocateManager.instance().init(this);

        PassageManager.instance();
    }

    private void initViews() {
        faceView = findViewById(R.id.face_view);
        imageView = findViewById(R.id.imageView_logo);
        tv_comName = findViewById(R.id.tv_comName);
        tv_notice = findViewById(R.id.tv_notice);
        tv_topTitle = findViewById(R.id.tv_topTitle);
        tv_bottomTitle = findViewById(R.id.tv_bottomTitle);
        tvVerify = findViewById(R.id.tv_verify);

        faceView.setCallback(faceCallback);
    }

    private void startXmpp() {//开启xmpp
        serviceManager = new ServiceManager(this);
        serviceManager.startService();
    }

    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            syncData();
        }
        @Override
        public void onFaceDetection() {
            ApiManager.instance().onLignt();
        }

        @Override
        public void onNoFace() {
            verifyTime = 2;
            if(tvVerify.isShown()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvVerify.setVisibility(View.GONE);
                    }
                });
            }
        }

        @Override
        public void onFaceVerify(VerifyResult verifyResult) {
            if(verifyResult == null){
                return;
            }

            final int result = verifyResult.getResult();
            if(result != VerifyResult.UNKNOWN_FACE){
                verifyTime--;
                if(verifyTime <= 0){
                    verifyTime = 2;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvVerify.setVisibility(View.VISIBLE);
                            String msg = "审核未通过";
                            tvVerify.setText(msg);
                        }
                    });
                }
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvVerify.setVisibility(View.GONE);
                }
            });

            PassageManager.instance().checkPermissions(WelComeActivity.this,verifyResult, passCallback);
        }
    };

    private PassageManager.PassCallback passCallback = new PassageManager.PassCallback() {
        @Override
        public void pass(PassageBean passageBean) {
            VipDialogManager.showVipDialog(WelComeActivity.this,passageBean);
            SpeechManager.instance().speak(passageBean.getName());

            if (mGateIsAlive) {
                mGateConnection.writeCom(GateCommands.GATE_OPEN_DOOR);
            }
            ApiManager.instance().onGate();
        }
    };


    //跳转设置界面
    public void goSetting(View view){
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd();
            return;
        }
        startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
    }

    private void loadQrCode(CompanyBean bean) {
        Map<String, String> params = new HashMap();
        params.put("comId", bean.getCompany().getComid() + "");
        OkHttpUtils.post().url(ResourceUpdate.QRCODE_ADD).params(params).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
            }

            @Override
            public void onResponse(String response, int id) {
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                AddQRCodeBean addQRCodeBean = new Gson().fromJson(response, AddQRCodeBean.class);
                if (!TextUtils.equals(addQRCodeBean.status, "1")) {
                    return;
                }
                if (addQRCodeBean == null || TextUtils.isEmpty(addQRCodeBean.codeurl)) {
                    return;
                }
            }
        });
    }

    private void syncData(){
        SyncManager.instance()
                .init(WelComeActivity.this)
                .setListener(new SyncManager.LoadListener() {
                    @Override
                    public void onLoaded(CompanyBean bean) {
                        tv_comName.setText(bean.getCompany().getAbbname());
                        tv_notice.setText(bean.getCompany().getNotice());
                        tv_topTitle.setText(bean.getCompany().getToptitle());
                        tv_bottomTitle.setText(bean.getCompany().getBottomtitle());
                        Glide.with(WelComeActivity.this)
                                .load(bean.getCompany().getComlogo())
                                .skipMemoryCache(true)
                                .crossFade(500)
                                .into(imageView);

                        loadQrCode(bean);

                        EventBus.getDefault().postSticky(new SystemActivity.UpdateEvent());
                    }

                    @Override
                    public void onFinish() {
                        AdsManager.instance().init(WelComeActivity.this, null);
                    }
                });
    }

    private void showTips(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIUtils.showTitleTip(msg);
            }
        });
    }

    //密码弹窗
    private void inputPwd() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(WelComeActivity.this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_input_pwd);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    edtPwd.setError("不要忘记输入密码哦");
                    rootView.startAnimation(animation);
                    return;
                }
                String spPwd = SpUtils.getStr(SpUtils.MENU_PWD);
                if (!TextUtils.equals(pwd, spPwd)) {
                    edtPwd.setError("密码错了，重新输入吧");
                    rootView.startAnimation(animation);
                    return;
                }
                startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
                dialog.dismiss();
            }
        });

        dialog.show();
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setLayout(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

    /*=======摄像头检测=============================================================================*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
            if (!TextUtils.isEmpty(pwd)) {
                inputPwd();
                return true;
            }
            startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        RestartAPPTool.showExitDialog(this,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAll();
                APP.exit();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SpeechManager.instance().resume();
        faceView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SpeechManager.instance().pause();
        faceView.pause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        SpeechManager.instance().stop();
        ApiManager.instance().offLight();
        ApiManager.instance().offGate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SpeechManager.instance().destory();
        faceView.destory();
        if (serviceManager != null) {
            serviceManager.stopService();
            serviceManager = null;
        }
        SyncManager.instance().destory();
        LocateManager.instance().destory();
    }
}
