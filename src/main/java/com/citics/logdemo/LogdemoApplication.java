package com.citics.logdemo;

import com.citics.logdemo.config.LogServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({LogServerConfig.class})
public class LogdemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogdemoApplication.class, args);
    }

}
