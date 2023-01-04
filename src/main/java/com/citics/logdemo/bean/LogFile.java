package com.citics.logdemo.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFile {
    private String filename;

    private String service;

    private Date date;

    private long size;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateStr() {
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }
}
