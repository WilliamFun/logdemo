package com.citics.logdemo.service.impl;

import com.citics.logdemo.LogdemoApplication;
import com.citics.logdemo.service.LogFileService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = LogdemoApplication.class)
class LogFileServiceImplTest {

    @Autowired
    private LogFileService logFileService;

    @Test
    public void setServer() {
        logFileService.setServer("serverA");
        System.out.println(logFileService.getServer().getRemoteServer());
        logFileService.setServer("serverB");
        System.out.println(logFileService.getServer().getRemoteServer());
    }

    @Test
    public void logMatchByWord() {
        logFileService.setServer("serverA");
        String service = "bussiness-interface";
        Date date = new Date(2022-1900, Calendar.DECEMBER,30);
        List<String> toFinds = new ArrayList<>();
        toFinds.add("生成");
        toFinds.add("请求");
        logFileService.logMatchByWord(service,date,toFinds);
    }

    @Test
    public void logMatchByTime() {
        logFileService.setServer("serverA");
        String service = "bussiness-interface";
        Date date = new Date(2022-1900, Calendar.DECEMBER,30);
        SimpleDateFormat formatter=new SimpleDateFormat("HH:mm:ss");
        try {
            Date start = formatter.parse("05:00:00");
            Date end = formatter.parse("08:00:00");
            logFileService.logMatchByTime(service,date,start,end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void splitLogFileByNum() {
        logFileService.setServer("serverA");
        String service = "bussiness-interface";
        Date date = new Date(2021-1900, Calendar.AUGUST,30);
        logFileService.splitLogFileByNum(service,date,5);
    }

    @Test
    public void splitLogFileBySize() {
        logFileService.setServer("serverA");
        String service = "bussiness-interface";
        Date date = new Date(2021-1900, Calendar.AUGUST,30);
        logFileService.splitLogFileBySize(service,date,"500K");
    }
}