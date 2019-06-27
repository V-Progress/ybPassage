package com.yunbiao.yb_passage.db;

import android.content.Context;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by Administrator on 2018/10/8.
 */

public class UserDao extends BaseDao{
    public UserDao(Context context) {
        super(context,UserBean.class);
    }

    // 向user表中添加一条数据
    public int add(UserBean data) {
        return insert(data);
    }

    // 删除user表中的一条数据
    public void remove(UserBean data) {
        delete(data);
    }

    // 删除user表中的一条数据
    public void deleteByFaceId(String faceId) {
        List<UserBean> userList = queryByFaceId(faceId);
        if(userList != null && userList.size()>0){
            for (UserBean userBean : userList) {
                remove(userBean);
            }
        }
    }

    // 根据ID取出用户信息
    public List<UserBean> queryByFaceId(int faceId) {
        return queryByInt("faceId", faceId);
    }

    public List<UserBean> queryByFaceId(String faceId){
        return queryByString("faceId",faceId);
    }

    //根据depart取出人脸信息
    public List<UserBean> queryByDepart(String depart) {
        return queryByString("depart",depart);
    }

    //根据depart取出人脸信息
    public List<UserBean> queryByDepartAndName(String depart, String name) {
        List<UserBean> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("depart", depart).and().eq("name", name).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }
}
