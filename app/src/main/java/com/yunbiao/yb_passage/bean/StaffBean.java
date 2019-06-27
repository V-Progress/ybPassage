package com.yunbiao.yb_passage.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/10/18.
 */

public class StaffBean {

    /**
     * lateNum : 2
     * dep : [{"gotime":"00:00","entry":[{"head":"http://192.168.1.54/imgserver/resource/head/2018/2018-10-09/4a81efa5-eeb6-48c6-8c26-341e99ccdb8a.jpg","number":"001","sex":0,"name":"刘诗诗","autograph":"大家好,我是气质女神刘诗诗","faceId":0,"id":10,"position":"女神","age":22},{"head":"http://192.168.1.54/imgserver/resource/head/2018/2018-10-10/b1daa6e8-63d1-43c3-9ba6-ae1fbbedf4af.jpg","number":"002","sex":1,"name":"蒿俊闵","autograph":"足球运动员","faceId":1,"id":11,"position":"队长","age":31}],"parentName":"技术部-","downtips":"成功","gotips":"成功","depId":4,"depName":"技术部-财务部","down":"00:00","parentId":1}]
     * status : 1
     */
    private List<DepBean> dep;
    private int status;

    public List<DepBean> getDep() {
        return dep;
    }

    public int getStatus() {
        return status;
    }

    public class EntryInfo{
        String autograph;
        String birthday;
        String cardId;
        String faceId;
        String head;
        int id;
        String name;
        String number;
        String position;
        Integer sex;

        public String getAutograph() {
            return autograph;
        }

        public String getBirthday() {
            return birthday;
        }

        public String getCardId() {
            return cardId;
        }

        public String getFaceId() {
            return faceId;
        }

        public String getHead() {
            return head;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        public String getPosition() {
            return position;
        }

        public Integer getSex() {
            return sex;
        }

        @Override
        public String toString() {
            return "EntryInfo{" +
                    "autograph='" + autograph + '\'' +
                    ", birthday='" + birthday + '\'' +
                    ", cardId='" + cardId + '\'' +
                    ", faceId='" + faceId + '\'' +
                    ", head='" + head + '\'' +
                    ", id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", number='" + number + '\'' +
                    ", position='" + position + '\'' +
                    ", sex=" + sex +
                    '}';
        }
    }

    public class DepBean{
        int depId;
        String depName;
        String down;
        String downtips;
        String gotime;
        String gotips;
        List<EntryInfo> entry;

        public int getDepId() {
            return depId;
        }

        public String getDepName() {
            return depName;
        }

        public String getDown() {
            return down;
        }

        public String getDowntips() {
            return downtips;
        }

        public String getGotime() {
            return gotime;
        }

        public String getGotips() {
            return gotips;
        }

        public List<EntryInfo> getEntry() {
            return entry;
        }

        @Override
        public String toString() {
            return "DepBean{" +
                    "depId='" + depId + '\'' +
                    ", depName='" + depName + '\'' +
                    ", down='" + down + '\'' +
                    ", downtips='" + downtips + '\'' +
                    ", gotime='" + gotime + '\'' +
                    ", gotips='" + gotips + '\'' +
                    ", entry=" + entry +
                    '}';
        }
    }

}
