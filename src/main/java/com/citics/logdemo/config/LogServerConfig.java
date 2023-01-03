package com.citics.logdemo.config;

import com.citics.logdemo.bean.LogServer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Zheng.Fan
 * @date 2022/12/29
 **/
@ConfigurationProperties(prefix = "log")
@Configuration
public class LogServerConfig {

    private Map<String, LogServer> serverList;

    public Map<String, LogServer> getServerList() {
        return serverList;
    }

    public void setServerList(Map<String, LogServer> serverList) {
        this.serverList = serverList;
    }
}
