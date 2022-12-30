package com.citics.logdemo.bean;

/**
 * @author Zheng.Fan
 * @date 2022/12/29
 **/
public class LogServer {

    private String ip;

    private String user;

    private String password;

    private String path;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
