package com.citics.logdemo.service.impl;

import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import com.citics.logdemo.bean.LogFile;
import com.citics.logdemo.bean.LogServer;
import com.citics.logdemo.config.LogServerConfig;
import com.citics.logdemo.service.LogFileService;
import com.citics.logdemo.util.LoginUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LogFileServiceImpl implements LogFileService {

    @Autowired
    private LogServerConfig logServerConfig;


    private static LogServer logServer = null;

    @Override
    public void setServer(String serverName) {
        logServer = selectServer(serverName);
    }

    @Override
    public LogServer getServer() {
        return logServer;
    }

    /**
     * 关键字过滤日志行
     * @param service
     * @param date
     * @param toFinds
     */
    @Override
    public void logMatchByWord(String service, Date date, List<String> toFinds) {
        List<LogFile> list = prefix(service, date);
        if (list.size() == 0) {
            System.out.println("无符合要求文件日志");
            return;
        }
        StringBuilder toFind = new StringBuilder();
        for (String str : toFinds) {
            toFind.append(str).append("-");
        }
        toFind.deleteCharAt(toFind.length()-1);
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
        try {
            OutputStream outputStream = Files.newOutputStream(Paths.get("info." + service + '.' +
                    formatter.format(date) + '.' + toFind + ".log"));
            for (LogFile log : list) {
                LoginUtil.checkFile(logServer.getConnection(),log,toFinds,outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LoginUtil.logout(logServer);
        }
    }

    /**
     * 时间范围过滤日志行
     * @param service
     * @param date
     * @param start
     * @param end
     */
    @Override
    public void logMatchByTime(String service, Date date, Date start, Date end) {
        List<LogFile> list = prefix(service, date);
        if (list.size() == 0) {
            System.out.println("无符合要求文件日志");
            return;
        }
        LogFile logFile = list.get(0);
        SimpleDateFormat formatter=new SimpleDateFormat("HH:mm:ss");
        try {
            OutputStream outputStream = Files.newOutputStream(Paths.get("info." + logFile.getService() + '.' +
                    logFile.getDateStr() + '.' + formatter.format(start) + '~' + formatter.format(end) + ".log"));
            for (LogFile log : list) {
                LoginUtil.checkTime(logServer.getConnection(),log,start,end,outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LoginUtil.logout(logServer);
        }
    }

    /**
     * 文件数量拆分日志文件
     * @param service
     * @param date
     * @param num
     */
    @Override
    public void splitLogFileByNum(String service, Date date, int num) {
        LogFile logFile = prefix(service, date).get(0);
        if (LoginUtil.SplitLogFile(logServer.getConnection(),logFile,num) == -1) {
            System.out.println("拆分失败");
            return;
        }
        copyToLocal(logServer,logFile);
        LoginUtil.logout(logServer);
    }

    /**
     * 文件大小拆分日志文件
     * @param service
     * @param date
     * @param size
     */
    @Override
    public void splitLogFileBySize(String service, Date date, String size){
        LogFile logFile = prefix(service, date).get(0);
        if (LoginUtil.SplitLogFile(logServer.getConnection(),logFile,size) == -1) {
            System.out.println("拆分失败");
            return;
        }
        copyToLocal(logServer,logFile);
        LoginUtil.logout(logServer);
    }

    /**
     * 前置操作
     * @param service
     * @param date
     * @return
     */
    private List<LogFile> prefix(String service, Date date) {
        LoginUtil.login(logServer);
        List<LogFile> list = selectLogFiles(logServer);
        return logMatchByName(list,date,service);
    }

    /**
     * 选择服务器
     * @param ServerName
     * @return
     */
    private LogServer selectServer(String ServerName) {
        Map<String, LogServer> map = logServerConfig.getServerList();
        return map.get(ServerName);
    }

    /**
     * 读取全部日志
     * @param logServer
     * @return
     */
    private List<LogFile> selectLogFiles(LogServer logServer) {
        List<SFTPv3DirectoryEntry> SFTPlist = LoginUtil.getFiles(logServer);
        List<LogFile> list = new ArrayList<>();
        for (SFTPv3DirectoryEntry s : SFTPlist) {
            if (LoginUtil.fileType(s.filename)) {
                list.add(convertSFTPToLogfile(s));
            }
        }
        return list;
    }

    /**
     * 服务名和日期过滤日志文件
     * @param logFiles
     * @param date
     * @param service
     * @return
     */
    private List<LogFile> logMatchByName(List<LogFile> logFiles, Date date, String service) {
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = formatter.format(date);
        String pattern = "info." + service + '.' + dateStr;
        List<LogFile> res = new ArrayList<>();
        for (LogFile log : logFiles) {
            if (log.getFilename().startsWith(pattern)) {
                res.add(log);
            }
        }
        return res;
    }

    /**
     * 存回本地
     * @param server
     * @param logFile
     */
    private void copyToLocal(LogServer server, LogFile logFile) {
        List<LogFile> list = logMatchByName(selectLogFiles(server),logFile.getDate(),logFile.getService());
        try {
            for (LogFile log : list) {
                if (log.getFilename().equals(logFile.getFilename())) {
                    continue;
                }
                OutputStream outputStream = Files.newOutputStream(Paths.get(log.getFilename()));
                LoginUtil.copyFile(server.getConnection(),log.getFilename(),outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 类型转换
     * @param sftPv3DirectoryEntry
     * @return
     */
    private LogFile convertSFTPToLogfile(SFTPv3DirectoryEntry sftPv3DirectoryEntry) {
        LogFile res = new LogFile();
        String[] info = sftPv3DirectoryEntry.filename.split("\\.");
        res.setFilename(sftPv3DirectoryEntry.filename);
        res.setSize(sftPv3DirectoryEntry.attributes.size);
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
        try {
            res.setDate(formatter.parse(info[2]));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        res.setService(info[1]);
        return res;
    }
}
