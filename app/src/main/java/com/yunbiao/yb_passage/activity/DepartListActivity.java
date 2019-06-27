package com.yunbiao.yb_passage.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;


import com.yunbiao.yb_passage.R;
import com.yunbiao.yb_passage.adapter.DepartListAdapter;
import com.yunbiao.yb_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_passage.utils.SpUtils;
import com.yunbiao.yb_passage.utils.xutil.MyXutils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2018/10/8.
 */

public class DepartListActivity extends BaseActivity implements  DepartListAdapter.InnerItemOnclickListener{

    private static final String TAG = "DepartListActivity";

    private ListView lv_depart_List;
    private ImageView iv_back;
    private Button btn_addDepart;
    private DepartListAdapter mDepartAdapter;
    private List<String> mDepartList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_departlist);
        } else {
            setContentView(R.layout.activity_departlist_h);
        }

        initViews();
        initData();

    }

    private void initViews() {

        lv_depart_List= (ListView) findViewById(R.id.lv_depart_List);
        iv_back= (ImageView) findViewById(R.id.iv_back);
        btn_addDepart= (Button) findViewById(R.id.btn_addDepart);

        mDepartList=new ArrayList<>();

//        departDao=new DepartDao(DepartListActivity.this);
//        mDepartlist   =	 departDao.selectAll();
//        if (mDepartlist!=null){
//            for (int i = 0; i <mDepartlist.size() ; i++) {
//                mDepartList.add(mDepartlist.get(i).getName());
//            }
//        }

        mDepartAdapter=new DepartListAdapter(this,mDepartList);
        lv_depart_List.setAdapter(mDepartAdapter);
        mDepartAdapter.setOnInnerItemOnClickListener(this);


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_addDepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DepartListActivity.this);

                builder.setTitle("请输入部门名称");
                //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
                View view = LayoutInflater.from(DepartListActivity.this).inflate(R.layout.dialog_depart, null);
                //    设置我们自己定义的布局文件作为弹出框的Content
                builder.setView(view);

                final EditText et_departName = (EditText)view.findViewById(R.id.et_departName);


                builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        final  String name = et_departName.getText().toString().trim();
                        mDepartList.add(name);
                        mDepartAdapter.notifyDataSetChanged();

//                        int companyid= SpUtils.getInt(DepartListActivity.this,SpUtils.COMPANYID,0);
                        int companyid= SpUtils.getInt(SpUtils.COMPANYID);
                        final Map<String, String> map = new HashMap<String, String>();
                        map.put("comId", companyid+"");
                        map.put("name", name+"");
                        MyXutils.getInstance().post(ResourceUpdate.ADDDEPART, map, new MyXutils.XCallBack() {
                            @Override
                            public void onSuccess(String result) {
                                Log.e(TAG, "增加部门--------------->"+result );
                                try {
                                    JSONObject jsonObject=new JSONObject(result);
                                    int status=jsonObject.getInt("status");
                                    if (status==1){
                                        int    departId=jsonObject.getInt("depId");
                                    }else {
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(Throwable ex) {
                                Log.e(TAG, "onError-------> "+ex.getMessage().toString() );
                            }

                            @Override
                            public void onFinish() {

                            }
                        });

                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                builder.show();
            }
        });
    }

    private void initData() {

    }


    @Override
    public void itemClick(View v, final int postion) {

        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(DepartListActivity.this);

        //    设置Title的内容
        builder.setTitle("提示！");
        //    设置Content来显示一个信息
        builder.setMessage("确定删除吗？");
        //    设置一个PositiveButton
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                final Map<String, String> map = new HashMap<String, String>();
//                map.put("depId", mDepartlist.get(postion).getDepartId()+"");
//                MyXutils.getInstance().post(ResourceUpdate.DELETEDEPART, map, new MyXutils.XCallBack() {
//                    @Override
//                    public void onSuccess(String result) {
//                        Log.e(TAG, "删除部门---------> "+result );
//                        try {
//                            JSONObject jsonObject=new JSONObject(result);
//                            int status=jsonObject.getInt("status");
//                            if (status==1){
//                                departDao.remove(mDepartlist.get(postion));
//                                mDepartList.remove(postion);
//                                mDepartAdapter.notifyDataSetChanged();
//                                mDepartlist   =	 departDao.selectAll();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//
//                    @Override
//                    public void onError(Throwable ex) {
//                        Log.e(TAG, "onError-------> "+ex.getMessage() );
//                    }
//
//                    @Override
//                    public void onFinish() {
//
//                    }
//                });



            }


        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        //    显示出该对话框
        builder.show();

    }
}
