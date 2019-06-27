package com.yunbiao.yb_passage.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.yunbiao.yb_passage.R;
import com.yunbiao.yb_passage.db.PassageBean;

import org.xutils.x;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Created by Administrator on 2018/9/17.
 */

public class PassageAdapter extends BaseAdapter {
    private static final String TAG = "PassageAdapter";
    private Context context;
    private List<PassageBean> mlist;
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public PassageAdapter(Context context, List<PassageBean> mlist) {
        this.context = context;
        this.mlist = mlist;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public PassageBean getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
       ViewHolder viewHolder=null;
        if (convertView == null){
            convertView = View.inflate(context, R.layout.item_sign,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder= (ViewHolder) convertView.getTag();
        }

        PassageBean passageBean = mlist.get(position);
        viewHolder.tv_name.setText(passageBean.getName());
        viewHolder.tv_No.setText(passageBean.getEntryId());
        viewHolder.tv_date.setText(dateFormat.format(passageBean.getPassTime()));
        viewHolder.tv_departName.setText(passageBean.getDepartName());
        viewHolder.tv_similar.setText(passageBean.getSimilar());
        x.image().bind(viewHolder.iv_photo,passageBean.getHeadPath());

        if (passageBean.isUpload()) {
            viewHolder.layout.setBackgroundColor(Color.parseColor("#00ffffff"));
        } else {
            viewHolder.layout.setBackgroundColor(Color.parseColor("#7CDF4C4C"));
        }

        return convertView;
    }

    class ViewHolder{
        TextView tv_No;
        TextView tv_date;
        TextView tv_name;
        TextView tv_departName;
        TextView tv_similar;
        ImageView iv_photo;
        View layout;

        public ViewHolder(View convertView) {
            layout = convertView.findViewById(R.id.layout);
            tv_No= (TextView) convertView.findViewById(R.id.tv_No);
            tv_date= (TextView) convertView.findViewById(R.id.tv_date);
            tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            tv_departName = (TextView) convertView.findViewById(R.id.tv_departName);
            tv_similar = (TextView) convertView.findViewById(R.id.tv_similar);
            iv_photo= (ImageView) convertView.findViewById(R.id.iv_photo);
        }
    }
}
