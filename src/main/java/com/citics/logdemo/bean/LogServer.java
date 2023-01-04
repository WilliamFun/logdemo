package com.citics.logdemo.bean;

import ch.ethz.ssh2.Connection;

/**
 * @author Zheng.Fan
 * @date 2022/12/29
 **/
public class LogServer {
    //ssh连接的用户名
    private String user;
    //ssh连接的密码
    private String password;
    //ssh远程连接的ip地址
    private String remoteServer;
    //日志文件路径
    private String remotePath;
    //远程连接
    private Connection connection = null;

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

    public String getRemoteServer() {
        return remoteServer;
    }

    public void setRemoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
