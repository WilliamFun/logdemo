package com.citics.logdemo.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

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
    public void getFile(HttpServletResponse response){
        String path = "D:/Games/WarCraft/BeeWind软件站说明.txt";
        //loginUtil.getListFileList(path, response);
    }
    @RequestMapping(value="/getLinuxFile",method=RequestMethod.GET)
    public void getLinuxFile(HttpServletResponse response) throws IOException{
        String fileName = "linux.txt";
        String path = "/home/hadoop/text/";
        loginUtil.login();
        response.reset();
        response.setContentType("octets/stream");
        response.addHeader("Content-Type", "text/html; charset=utf-8");
        response.addHeader("Content-Disposition", "attachment;filename="+new String(fileName.getBytes("UTF-8"),"ISO8859-1"));
        loginUtil.copyFile(LoginUtil.conn, path+fileName,response.getOutputStream());
    }

}
