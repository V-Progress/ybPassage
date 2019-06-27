package com.yunbiao.yb_passage.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.yunbiao.yb_passage.R;
import com.yunbiao.yb_passage.adapter.DepartAdapter;
import com.yunbiao.yb_passage.adapter.EmployAdapter;
import com.yunbiao.yb_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_passage.business.SyncManager;
import com.yunbiao.yb_passage.db.UserBean;
import com.yunbiao.yb_passage.db.UserDao;
import com.yunbiao.yb_passage.faceview.FaceSDK;
import com.yunbiao.yb_passage.utils.SpUtils;
import com.yunbiao.yb_passage.utils.ThreadUitls;
import com.yunbiao.yb_passage.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/8/7.
 */

public class EmployListActivity extends BaseActivity implements EmployAdapter.EmpOnDeleteListener, EmployAdapter.EmpOnEditListener, View.OnClickListener {

    private static final String TAG = "EmployListActivity";

    private ListView lv_employ_List;
    private EmployAdapter employAdapter;

    private Spinner sp_depart;
    private Button btn_addEmploy;
    private Button btn_addDepart;
    private Button btn_sync;
    private ImageView iv_back;

    private UserDao userDao;
    private TextView tv_deviceNo;
    private View avlLoading;

    private List<UserBean> employList = new ArrayList<>();
    private List<String> mDepartList = new ArrayList<>();
    private List<UserBean> showList = new ArrayList<>();
    private DepartAdapter departAdapter;

    private int mDepartIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_employlist);
        } else {
            setContentView(R.layout.activity_employlist_h);
        }

        EventBus.getDefault().register(this);

        userDao = new UserDao(EmployListActivity.this);

        initViews();
        initData();
        initList();
    }

    private void initViews() {
        lv_employ_List = (ListView) findViewById(R.id.lv_employ_List);
        sp_depart = (Spinner) findViewById(R.id.sp_depart);
        btn_addEmploy = (Button) findViewById(R.id.btn_addEmploy);
        btn_addDepart = (Button) findViewById(R.id.btn_addDepart);
        btn_sync = (Button) findViewById(R.id.btn_sync);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_deviceNo = (TextView) findViewById(R.id.tv_deviceNo);
        avlLoading = findViewById(R.id.avl_loading);

        btn_addEmploy.setOnClickListener(this);
        btn_addDepart.setOnClickListener(this);
        btn_sync.setOnClickListener(this);
        iv_back.setOnClickListener(this);

        String deviceSernum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        if (!TextUtils.isEmpty(deviceSernum)) {
            tv_deviceNo.setText(deviceSernum);
        }
    }

    private void initData() {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                employList.clear();
                employList.addAll(userDao.selectAll());

                for (UserBean userBean : employList) {
                    String depart = userBean.getDepart();
                    if (!mDepartList.contains(depart)) {
                        mDepartList.add(depart);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (departAdapter != null) {
                            departAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }

    private void initList() {
        mDepartList.add("全部部门");
        employAdapter = new EmployAdapter(this, showList);
        employAdapter.setOnEmpDeleteListener(this);
        employAdapter.setOnEmpEditListener(this);
        lv_employ_List.setAdapter(employAdapter);

        departAdapter = new DepartAdapter(this, mDepartList);
        sp_depart.setAdapter(departAdapter);
        sp_depart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDepartIndex = position;
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sp_depart.setSelection(0);
    }

    private void loadData() {
        lv_employ_List.setVisibility(View.GONE);
        avlLoading.setVisibility(View.VISIBLE);

        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                showList.clear();
                if (mDepartIndex == 0) {
                    showList.addAll(employList);
                } else {
                    String departName = mDepartList.get(mDepartIndex);
                    for (UserBean userBean : employList) {
                        String depart = userBean.getDepart();
                        if (TextUtils.equals(depart, departName)) {
                            showList.add(userBean);
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (employList != null) {
                            employAdapter.notifyDataSetChanged();
                        }
                        lv_employ_List.setVisibility(View.VISIBLE);
                        avlLoading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void itemDeleteClick(View v, final int postion) {
        final UserBean userBean = showList.get(postion);
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployListActivity.this);
        builder.setTitle("提示！");
        builder.setMessage("确定删除吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final Map<String, String> map = new HashMap<>();
                map.put("entryId", userBean.getEmpId() + "");
                OkHttpUtils.post().url(ResourceUpdate.DELETESTAFF).params(map).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        UIUtils.showTitleTip("删除失败 " + e != null ? e.getMessage() : "NULL");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        boolean b = FaceSDK.instance().removeUser(String.valueOf(userBean.getFaceId()));
                        if (b) {
                            userDao.remove(userBean);
                            employList.remove(userBean);
                            showList.remove(userBean);
//                            initData();
//                            loadData();

                            Log.e(TAG, "onResponse:" + employList.toString());
                            Log.e(TAG, "onResponse:" + showList.toString());

                            employAdapter.notifyDataSetChanged();
                            UIUtils.showTitleTip("删除成功");
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void itemEditClick(View v, final int postion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployListActivity.this);
        builder.setTitle("提示！");
        builder.setMessage("确定去修改吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(EmployListActivity.this, EditEmployActivity.class);
                intent.putExtra("name", employList.get(postion).getName());
                intent.putExtra("depart", employList.get(postion).getDepart());
                startActivity(intent);

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_addEmploy:
                startActivity(new Intent(EmployListActivity.this, AddEmployActivity.class));
                break;
            case R.id.btn_addDepart:
                startActivity(new Intent(EmployListActivity.this, DepartListActivity.class));
                break;
            case R.id.btn_sync:
                SyncManager.instance().initInfo();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    //摄像头错误监听
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(EmployUpdate employUpdate) {

    }

    public static class EmployUpdate {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
