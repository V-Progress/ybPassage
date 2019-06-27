package com.yunbiao.yb_passage.db;

import android.content.Context;

import java.util.List;

public class PassageDao extends BaseDao {

    public PassageDao(Context context) {
        super(context, PassageBean.class);
    }

    public List<PassageBean> queryByEntryId(String entryId){
        return queryByString("entryId",entryId);
    }

    public List<PassageBean> queryByTime(long time){
        return queryByLong("passTime",time);
    }

    public List<PassageBean> selectAll(){
        return super.selectAll();
    }

    public List<PassageBean> queryByDate(String date){
        return super.queryByString("date",date);
    }
}
