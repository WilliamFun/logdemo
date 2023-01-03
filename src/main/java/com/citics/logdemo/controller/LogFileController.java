package com.citics.logdemo.controller;

import java.io.FileOutputStream;
import java.io.IOException;

import com.citics.logdemo.util.LoginUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Zheng.Fan
 * @date 2022/12/26
 **/
@RestController
@RequestMapping("/LogFileCtl")
public class LogFileController {
    @Autowired
    private LoginUtil loginUtil;

    @RequestMapping(value="/getFile",method=RequestMethod.GET)
    public void getFile(){
        String path = "D:/Games/WarCraft/BeeWind软件站说明.txt";
        //loginUtil.getListFileList(path, response);
    }
    @RequestMapping(value="/getLinuxFile",method=RequestMethod.GET)
    public void getLinuxFile() throws IOException{
        String fileName = "linux.txt";
        String path = "/home/hadoop/text/";
        loginUtil.login();
        loginUtil.copyFile(LoginUtil.conn, path+fileName,new FileOutputStream("test"));
    }

}
