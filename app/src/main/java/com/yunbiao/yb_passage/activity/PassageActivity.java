package com.yunbiao.yb_passage.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.yunbiao.yb_passage.APP;
import com.yunbiao.yb_passage.R;
import com.yunbiao.yb_passage.adapter.PassageAdapter;
import com.yunbiao.yb_passage.db.PassageBean;
import com.yunbiao.yb_passage.db.PassageDao;
import com.yunbiao.yb_passage.utils.ThreadUitls;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2018/10/10.
 */

public class PassageActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "PassageActivity";

    private ListView lv_sign_List;
    private TextView tv_date;
    private ImageView iv_back;
    private View pb_load_list;
    private TextView tv_load_tips;
    private TextView tv_export_sign_data;

    private final int MODE_ALL = 0;
    private final int MODE_SENDED = 1;
    private final int MODE_UNSENDED = 2;
    private int DATA_MODE = MODE_ALL;

    private String queryDate = "";

    private Spinner spnDataMode;
    private PassageDao passageDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_table);

        initViews();
        initData();

        initSpinner();
    }

    private void initViews() {
        lv_sign_List= (ListView) findViewById(R.id.lv_sign_List);
        tv_date= (TextView) findViewById(R.id.tv_date);
        iv_back= (ImageView) findViewById(R.id.iv_back);
        pb_load_list = findViewById(R.id.pb_load_list);
        tv_load_tips = (TextView)findViewById(R.id.tv_load_tips);
        tv_export_sign_data = (TextView)findViewById(R.id.tv_export_sign_data);
        spnDataMode = (Spinner) findViewById(R.id.spn_data_mode);
        tv_export_sign_data.setOnClickListener(this);
        tv_date.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    private void initData() {
        passageDao = APP.getPassageDao();

        Calendar calendar=Calendar.getInstance();
        String yearStr = calendar.get(Calendar.YEAR)+"";//获取年份
        String dayStr = calendar.get(Calendar.DAY_OF_MONTH)+"";//获取天

        int realMonth = calendar.get(Calendar.MONTH) + 1;
        String monthStr = "";
        if(realMonth< 10){
            monthStr = "0" + realMonth + "月";
        } else {
            monthStr = realMonth + "月";
        }

        String today =yearStr + "年" + monthStr + dayStr + "日";
        Log.e(TAG, "today--------->"+today );
        tv_date.setText(today);
        queryDate = today;
    }

    private void initSpinner(){
        final String[] modeArray = {"全部","已发送","未发送"};
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,modeArray);
        spnDataMode.setAdapter(spnAdapter);
        spnDataMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DATA_MODE = position;
                loadSignList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnDataMode.setSelection(0);
    }

    private List<PassageBean> mShowList = new ArrayList<>();
    private List<PassageBean> mPassList = new ArrayList<>();
    private void loadSignList(){
        pb_load_list.setVisibility(View.VISIBLE);
        lv_sign_List.setVisibility(View.GONE);
        tv_load_tips.setVisibility(View.GONE);

        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "loadSignList: " + queryDate + " ----- " + DATA_MODE );

                mShowList.clear();
                mPassList = passageDao.queryByDate(queryDate);
                if(mPassList == null || mPassList.size()<=0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_load_tips.setVisibility(View.VISIBLE);
                            pb_load_list.setVisibility(View.GONE);
                        }
                    });
                    return;
                }

                for (PassageBean passageBean : mPassList) {
                    if(DATA_MODE == MODE_UNSENDED && !passageBean.isUpload()){
                        mShowList.add(passageBean);
                    } else if(DATA_MODE == MODE_SENDED && passageBean.isUpload()){
                        mShowList.add(passageBean);
                    } else if(DATA_MODE == MODE_ALL){
                        mShowList.add(passageBean);
                    }
                }

                Collections.sort(mShowList, new Comparator<PassageBean>() {
                    @Override
                    public int compare(PassageBean o1, PassageBean o2) {
                        return o1.getPassTime() > o2.getPassTime()? -1 : 1;
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mShowList != null && mShowList.size()>0){
                            PassageAdapter adapter=new PassageAdapter(PassageActivity.this,mShowList);
                            lv_sign_List.setAdapter(adapter);

                            lv_sign_List.setVisibility(View.VISIBLE);
                            pb_load_list.setVisibility(View.GONE);
                            tv_load_tips.setVisibility(View.GONE);
                        } else {
                            if(DATA_MODE == MODE_UNSENDED){
                                tv_load_tips.setText("数据已全部上传");
                            } else {
                                tv_load_tips.setText("暂无数据");
                            }
                            tv_load_tips.setVisibility(View.VISIBLE);
                            lv_sign_List.setVisibility(View.GONE);
                            pb_load_list.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_date:
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(
                        PassageActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("Orignal", "Got clicked");
                                int realMonth = month + 1;
                                String monthStr = "";
                                if(realMonth< 10){
                                    monthStr = "0" + realMonth + "月";
                                } else {
                                    monthStr = realMonth + "月";
                                }
                                String date =year+"年"+ monthStr +dayOfMonth+"日";
                                tv_date.setText(date);

                                queryDate = date;
                                loadSignList();
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                ).show();
                break;
            case R.id.tv_export_sign_data:
                if(mShowList.size()<=0){
                    Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show();
                    return;
                }

                String exportListJson = new Gson().toJson(mShowList);
                Log.e(TAG, "当前可导出："+exportListJson);

                break;
        }
    }
}
