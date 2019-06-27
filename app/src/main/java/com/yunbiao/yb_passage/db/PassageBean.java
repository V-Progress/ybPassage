package com.yunbiao.yb_passage.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Passage")
public class PassageBean {
    public PassageBean() {
    }

    @DatabaseField(generatedId = true)
    private int id;

    public int getId() {
        return id;
    }

    @DatabaseField(columnName = "departName")
    private String departName;

    @DatabaseField(columnName = "date")
    private String date;

    @DatabaseField(columnName = "faceId")
    private String faceId;

    @DatabaseField(columnName = "similar")
    private String similar;

    @DatabaseField(columnName = "headPath")
    private String headPath;

    @DatabaseField(columnName = "isPass")
    private int isPass;

    @DatabaseField(columnName = "entryId")
    private String entryId;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "passTime")
    private long passTime;

    @DatabaseField(columnName = "isUpload")
    private boolean isUpload = false;

    public PassageBean(String similar, String headPath, int isPass, String entryId, String name, long passTime, boolean isUpload) {
        this.similar = similar;
        this.headPath = headPath;
        this.isPass = isPass;
        this.entryId = entryId;
        this.name = name;
        this.passTime = passTime;
        this.isUpload = isUpload;
    }

    public String getDepartName() {
        return departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public long getPassTime() {
        return passTime;
    }

    public void setPassTime(long passTime) {
        this.passTime = passTime;
    }

    public String getSimilar() {
        return similar;
    }

    public void setSimilar(String similar) {
        this.similar = similar;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public int getIsPass() {
        return isPass;
    }

    public void setIsPass(int isPass) {
        this.isPass = isPass;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PassageBean{" +
                "departName='" + departName + '\'' +
                ", date='" + date + '\'' +
                ", faceId='" + faceId + '\'' +
                ", similar='" + similar + '\'' +
                ", headPath='" + headPath + '\'' +
                ", isPass=" + isPass +
                ", entryId='" + entryId + '\'' +
                ", name='" + name + '\'' +
                ", passTime=" + passTime +
                ", isUpload=" + isUpload +
                '}';
    }
}
