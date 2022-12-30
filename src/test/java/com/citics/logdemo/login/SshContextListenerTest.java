package com.citics.logdemo.login;

import com.citics.logdemo.LogdemoApplication;
import com.citics.logdemo.util.LoginUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = LogdemoApplication.class)
class SshContextListenerTest {

    @Autowired
    private SshContextListener listener;

    @Test
    void test() throws Throwable {
        LoginUtil login = new LoginUtil();
    }

}