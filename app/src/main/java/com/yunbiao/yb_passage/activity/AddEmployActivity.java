package com.yunbiao.yb_passage.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.yb_passage.R;
import com.yunbiao.yb_passage.adapter.DepartAdapter;
import com.yunbiao.yb_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_passage.db.UserBean;
import com.yunbiao.yb_passage.db.UserDao;
import com.yunbiao.yb_passage.faceview.FaceSDK;
import com.yunbiao.yb_passage.faceview.FaceView;
import com.yunbiao.yb_passage.utils.SpUtils;
import com.yunbiao.yb_passage.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;


import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Request;


/**
 * Created by Administrator on 2018/8/7.
 */

public class AddEmployActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "AddEmployActivity";
    private static final int REQUEST_CODE_1 = 0x001;

    private Button btn_submit;
    private Button btn_cancle;
    private Button btn_TakePhoto;
    private Button btn_ReTakePhoto;

    private ImageView iv_back;
    private ImageView iv_capture;

    private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private EditText et_name;
    private Spinner sp_depart;
    private EditText et_num;

    private String strFileAdd;
    private String depart;//部门
    private int departId;//部门id
    private List<String> mDepartList;
    private UserDao userDao;
    private TextView tv_takephoto_time;
    private MediaPlayer shootMP;
    private View pbTakePhoto;

    private FaceView faceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addemploy);

        userDao = new UserDao(this);
        initViews();
        initSpinnerData();
        strFileAdd = "";
    }

    private void initViews() {
        faceView = findViewById(R.id.face_view);

        et_name = (EditText) findViewById(R.id.et_name);
        sp_depart = (Spinner) findViewById(R.id.sp_depart);
        et_num = (EditText) findViewById(R.id.et_num);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        iv_capture = (ImageView) findViewById(R.id.iv_capture);
        btn_TakePhoto = (Button) findViewById(R.id.btn_TakePhoto);
        btn_cancle = (Button) findViewById(R.id.btn_cancle);
        btn_ReTakePhoto = (Button) findViewById(R.id.btn_ReTakePhoto);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        pbTakePhoto = findViewById(R.id.alv_take_photo);

        btn_TakePhoto.setOnClickListener(this);
        btn_ReTakePhoto.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    private void initSpinnerData() {
        mDepartList = new ArrayList<>();
//        departDao = new DepartDao(AddEmployActivity.this);
//        mDepartlist = departDao.selectAll();
//        if (mDepartlist != null) {
//            for (int i = 0; i < mDepartlist.size(); i++) {
//                mDepartList.add(mDepartlist.get(i).getName());
//            }
//        }
//
//        if (mDepartList.size() > 0) {
//            depart = mDepartList.get(0);
//            departId = mDepartlist.get(0).getDepartId();
//        } else {
//            UIUtils.showTitleTip("请先返回创建增加部门！");
//        }

        DepartAdapter departAdapter = new DepartAdapter(this, mDepartList);
        sp_depart.setAdapter(departAdapter);

        sp_depart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemSelected------> " + mDepartList.get(position));
                depart = mDepartList.get(position);
//                departId = mDepartlist.get(position).getDepartId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String name = getIntent().getStringExtra("name");
        String depart = getIntent().getStringExtra("depart");
        if (name != null && depart != null) {
            List<UserBean> mlist = userDao.queryByDepartAndName(depart, name);
            if (mlist != null && mlist.size() > 0) {
                UserBean userBean = mlist.get(0);

                et_name.setText(name);

                x.image().bind(iv_capture, userBean.getImgUrl());

                for (int i = 0; i < mDepartList.size(); i++) {
                    if (depart.equals(mDepartList.get(i))) {
                        depart = mDepartList.get(i);
                        sp_depart.setSelection(i);
                    }
                }
            }
        }
    }

    private void chooseBitmap(int requestCode) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, requestCode);
    }

    public static String SCREEN_BASE_PATH = sdPath + "/mnt/sdcard/photo/";

    /**
     * 播放系统拍照声音
     */
    public void shootSound() {
        AudioManager meng = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume(AudioManager.STREAM_ALARM);

        if (volume != 0) {
            if (shootMP == null)
                shootMP = MediaPlayer.create(this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            if (shootMP != null)
                shootMP.start();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            byte[] faceImageBytes = faceView.getFaceImage();
            if(faceImageBytes == null || faceImageBytes.length<=0){
                handler.sendEmptyMessageDelayed(0,200);
                return;
            }
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap faceImage = BitmapFactory.decodeByteArray(faceImageBytes, 0, faceImageBytes.length, options);
            strFileAdd = saveBitmap(faceImage);
            iv_capture.setImageBitmap(faceImage);

            pbTakePhoto.setVisibility(View.GONE);
            btn_TakePhoto.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_TakePhoto://点击拍照
                pbTakePhoto.setVisibility(View.VISIBLE);
                btn_TakePhoto.setVisibility(View.GONE);
                handler.sendEmptyMessage(0);

                break;
            case R.id.btn_ReTakePhoto://重置所有状态
                tv_takephoto_time.setText("");
                pbTakePhoto.setVisibility(View.GONE);
                btn_TakePhoto.setVisibility(View.VISIBLE);
                iv_capture.setImageResource(R.mipmap.avatar);
                break;

            case R.id.btn_submit:
                final String name = et_name.getText().toString();
                final String empNum = et_num.getText().toString();

                if (strFileAdd.equals("")) {
                    UIUtils.showTitleTip("请先拍照！");
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    UIUtils.showTitleTip("名字不能为空！");
                    return;
                }
                if (TextUtils.isEmpty(depart)) {
                    UIUtils.showTitleTip("请返回增加部门！");
                    return;
                }

                // TODO: 2019/6/4
                final int cnt = 0/*mipsFaceService.mipsGetDbFaceCnt()*/;
                Log.e(TAG, "cnt----------> " + cnt);
                int index = strFileAdd.lastIndexOf("/");
                String str = strFileAdd.substring(index + 1, strFileAdd.length());
                Log.e(TAG, "strFileAdd-----------------> " + strFileAdd);
                Log.e(TAG, "str-----------------> " + str);
                Log.e(TAG, "departId-----------------> " + departId);

                File imgFile = new File(strFileAdd);
                Map<String,String> params = new HashMap<>();
                params.put("faceId", cnt + "");
                params.put("name", name + "");
                params.put("sex", 1 + "");
                params.put("headName", str);
                params.put("number", empNum);
                params.put("depId", departId + "");
                int companyid = SpUtils.getInt(SpUtils.COMPANYID);
                Log.e(TAG, "companyid-----------------> " + companyid);
                params.put("comId", companyid + "");

                PostFormBuilder paramsBuilder = OkHttpUtils.post()
                        .url(ResourceUpdate.ADDSTAFF)
                        .params(params);
                if(imgFile != null && imgFile.exists()){
                    paramsBuilder = paramsBuilder.addFile("head",imgFile.getName(), imgFile);
                }
                paramsBuilder.build().execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        btn_submit.setEnabled(false);
                        UIUtils.showNetLoading(AddEmployActivity.this);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError------------------->" + (e != null ?e.getMessage() : e));
                        btn_submit.setEnabled(true);
                        UIUtils.showTitleTip("连接服务器失败,请重新再试！（"+ e!=null?e.getMessage():"NULL" +"）");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "result----------> " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getInt("status") == 1) {
                                final int empId = jsonObject.getInt("entryId");
                                final int faceId = jsonObject.getInt("faceId");

                                FaceSDK.instance().addUser(String.valueOf(faceId), strFileAdd, new FaceUserManager.FaceUserCallback() {
                                    @Override
                                    public void onUserResult(boolean b, int i) {
                                        if(b){
                                            UserBean countDetail = new UserBean(departId, empId, faceId, "1", "", name, depart, "", empNum, "", "", strFileAdd);
                                            List<UserBean> mlistQ = userDao.queryByDepartAndName(depart, name);
                                            if (mlistQ == null | (mlistQ != null && mlistQ.size() == 0)) {
                                                userDao.add(countDetail);
                                            }

                                            UIUtils.showTitleTip("员工保存成功！");
                                            finish();
                                        } else {
                                            UIUtils.showTitleTip("保存失败！");
                                        }
                                    }
                                });

                            } else if(jsonObject.getInt("status") == 7){
                                UIUtils.showTitleTip("部门不存在！");
                            } else {
                                UIUtils.showTitleTip("提交失败，错误代码："+jsonObject.getInt("status"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            btn_submit.setEnabled(true);
                            UIUtils.showTitleTip("员工保存失败,请重新再试！("+  (e != null ? e .getMessage() :"NULL") +")");
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        btn_submit.setEnabled(true);
                        UIUtils.dismissNetLoading();
                    }
                });
                break;
            case R.id.btn_cancle:
                finish();

                break;
            case R.id.iv_back:
                finish();
                break;

        }
    }

    /**
     * 随机生产文件名
     *
     * @return
     */
    private static String generateFileName() {
        return UUID.randomUUID().toString();
    }

    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Bitmap mBitmap) {
        File filePic;
        try {
            //格式化时间
            long time = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(SCREEN_BASE_PATH + sdfTime + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            // TODO: 2019/3/28 闪退问题
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            // TODO: 2019/3/28 闪退问题
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }


    private String getScreenHot(View v) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            v.draw(canvas);

            try {
                long time = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String sdfTime = sdf.format(time);
                String filePath = SCREEN_BASE_PATH + sdfTime + ".png";
                FileOutputStream fos = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                Toast.makeText(AddEmployActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                System.out.println("保存成功");
                return filePath;
            } catch (FileNotFoundException e) {
                throw new InvalidParameterException();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        UIUtils.dismissNetLoading();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        faceView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        faceView.pause();
    }

}
