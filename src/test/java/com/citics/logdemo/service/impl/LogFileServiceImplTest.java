package com.citics.logdemo.service.impl;

import com.citics.logdemo.LogdemoApplication;
import com.citics.logdemo.bean.LogFile;
import com.citics.logdemo.bean.LogServer;
import com.citics.logdemo.service.LogFileService;
import com.citics.logdemo.util.LoginUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


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
    }

    @Test
    public void logMatchByTime() {
    }

    @Test
    public void splitLogFileByNum() {
    }

    @Test
    public void splitLogFileBySize() {

    }
}