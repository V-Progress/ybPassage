package com.yunbiao.yb_passage.db;

/**
 * Created by Administrator on 2017/7/28.
 */

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "UserBean")
public class UserBean {


    public UserBean() {
    }

    public UserBean(int empId, int departId, String faceId, String name, String sex, String job, String imgUrl, long time, String depart, String employNum, String birthday, String signature, boolean downloadTag) {
        this.empId = empId;
        this.departId = departId;
        this.faceId = faceId;
        this.name = name;
        this.sex = sex;
        this.job = job;
        this.imgUrl = imgUrl;
        this.time = time;
        this.depart = depart;
        this.employNum = employNum;
        this.birthday = birthday;
        Signature = signature;
        this.downloadTag = downloadTag;
    }

    public void setDownloadTag(boolean downloadTag) {
        this.downloadTag = downloadTag;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "cardId")
    private String cardId;

    @DatabaseField(columnName = "empId")
    private int empId;//员工id

    @DatabaseField(columnName = "departId")
    private int departId;//部门id

    @DatabaseField(columnName = "faceId")
    private String faceId;//图片id


    @DatabaseField(columnName = "name")
    private String name;//名字


    @DatabaseField(columnName = "sex")
    private String sex;//性别

    @DatabaseField(columnName = "job")
    private String job;//职位

     @DatabaseField(columnName = "imgUrl")
    private String imgUrl;//头像文件

    @DatabaseField(columnName = "time")
    private long time;//时间

    @DatabaseField(columnName = "depart")
    private String depart;//员工部门

    @DatabaseField(columnName = "employNum")
    private String employNum;//员工编号

     @DatabaseField(columnName = "birthday")
    private String birthday;//员工生日

     @DatabaseField(columnName = "Signature")
    private String Signature;//个性签名

    @DatabaseField(columnName = "downloadTag")
    private boolean downloadTag = true;//失败标签

    @DatabaseField(columnName = "errMark")
    private String errMark;//个性签名

    public UserBean(int departId, int empId, int faceId, String xingbie, String s, String name, String departName, String job, String employNum, String birthday, String signature, String filepath) {

    }

    public int getId() {
        return id;
    }

    public String getCardId() {
        return cardId;
    }

    public int getEmpId() {
        return empId;
    }

    public int getDepartId() {
        return departId;
    }

    public String getFaceId() {
        return faceId;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public String getJob() {
        return job;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public long getTime() {
        return time;
    }

    public String getDepart() {
        return depart;
    }

    public String getEmployNum() {
        return employNum;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getSignature() {
        return Signature;
    }

    public boolean isDownloadTag() {
        return downloadTag;
    }

    public String getErrMark() {
        return errMark;
    }

    public void setErrMark(String errMark) {
        this.errMark = errMark;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "id=" + id +
                ", empId=" + empId +
                ", departId=" + departId +
                ", faceId=" + faceId +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", job='" + job + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", time=" + time +
                ", depart='" + depart + '\'' +
                ", employNum='" + employNum + '\'' +
                ", birthday='" + birthday + '\'' +
                ", Signature='" + Signature + '\'' +
                ", downloadTag=" + downloadTag +
                '}';
    }
}
