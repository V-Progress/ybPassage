package com.yunbiao.yb_passage.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Administrator on 2019/5/23.
 */

public class BaseDao<T> {
    protected Context mContext;
    protected Dao<T,Integer> dao;

    public BaseDao(Context context,Class Class){
        mContext = context;
        try {
            dao = DatabaseHelper.getInstance(context).getDao(Class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int insert(T t){
        try{
            return dao.create(t);
        }catch (SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

    public void delete(T t){
        try {
            dao.delete(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改user表中的一条数据
    public int update(T t) {
        try {
            return dao.update(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<T> selectAll(){
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<T> queryByLong(String key,long l){
        try {
            return dao.queryBuilder().where().eq(key,l).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<T> queryByInt(String key,int id){
        try {
            return dao.queryBuilder().where().eq(key,id).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<T> queryByString(String key,String str){
        try {
            return dao.queryBuilder().where().eq(key,str).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
