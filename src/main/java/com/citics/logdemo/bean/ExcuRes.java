package com.citics.logdemo.bean;

import java.io.InputStream;

public class ExcuRes {
    private InputStream stdOut;

    private InputStream stdErr;

    private int ret;

    public InputStream getStdOut() {
        return stdOut;
    }

    public void setStdOut(InputStream stdOut) {
        this.stdOut = stdOut;
    }

    public InputStream getStdErr() {
        return stdErr;
    }

    public void setStdErr(InputStream stdErr) {
        this.stdErr = stdErr;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }
}
