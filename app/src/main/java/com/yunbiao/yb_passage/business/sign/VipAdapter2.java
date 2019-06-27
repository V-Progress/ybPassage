package com.yunbiao.yb_passage.business.sign;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.yb_passage.R;
import com.yunbiao.yb_passage.db.PassageBean;

import java.util.LinkedList;
import java.util.List;

public class VipAdapter2 extends RecyclerView.Adapter<VipAdapter2.VH>{
    private List<PassageBean> mList ;
    private Context mContext;
    public VipAdapter2(Context context, LinkedList<PassageBean> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_vip_item_test, viewGroup, false);
        return new VH(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull VH vh, int i) {
//        SignBean item = mList.get(i);
//        Bitmap currentImg = item.getCurrentImg();
//        vh.ivHead.setImageBitmap(currentImg);
//        vh.tvName.setText(item.getName());
//        vh.tvSign.setText(item.getSignature());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class VH extends RecyclerView.ViewHolder{
        public ImageView ivHead;
        public TextView tvName;
        public TextView tvSign;
        public VH(@NonNull View itemView) {
            super(itemView);
            ivHead = itemView.findViewById(R.id.civ_userPhoto);
            tvName = itemView.findViewById(R.id.tv_nameAndJob);
            tvSign = itemView.findViewById(R.id.tv_sign);
        }
    }
}