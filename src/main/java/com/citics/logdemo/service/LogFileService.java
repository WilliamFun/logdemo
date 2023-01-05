package com.citics.logdemo.service;

import com.citics.logdemo.bean.LogServer;

import java.util.Date;
import java.util.List;

public interface LogFileService {

    void setServer(String serverName);

    LogServer getServer();

    void logMatchByWord(String service, Date date, List<String> toFind);

    void logMatchByTime(String service, Date date, Date start, Date end);

    void splitLogFileByNum(String service, Date date, int num);

    void splitLogFileBySize(String service, Date date, String size);

}
