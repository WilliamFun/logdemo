package com.citics.logdemo.util;

import ch.ethz.ssh2.*;
import com.citics.logdemo.bean.ExcuRes;
import com.citics.logdemo.bean.LogFile;
import com.citics.logdemo.bean.LogServer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Zheng.Fan
 * @date 2022/12/23
 **/
public class LoginUtil {
    //ssh连接的端口号
    private final static int SSH_REMOTE_PORT = 22;
    //等待时间
    private final static long TIME_OUT = 10;

    public static boolean login(LogServer logServer){
        String server = logServer.getRemoteServer();
        String user = logServer.getUser();
        String password = logServer.getPassword();
        //创建远程连接，默认连接端口为22，如果不使用默认，可以使用方法
        //new Connection(ip, port)创建对象
        Connection connection = new Connection(server,SSH_REMOTE_PORT);
        try {
            //连接远程服务器
            connection.connect();
            //使用用户名和密码登录
            return connection.authenticateWithPassword(user, password);
        } catch (IOException e) {
            System.err.printf("用户%s密码%s登录服务器%s失败！", user, password, server);
            e.printStackTrace();
        } finally {
            logServer.setConnection(connection);
        }
        return false;
    }

    public static void logout (LogServer logServer)
    {
        logServer.getConnection().close();
    }

    /**
     * 下载服务器文件到本地目录（java所在服务器）
     * @param fileName 服务器文件
     * @param localPath 本地目录
     */
    public static void copyFile(Connection conn, String fileName,String localPath){
        SCPClient sc = new SCPClient(conn);
        try {
            sc.get(fileName, localPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 流式输出，用于浏览器下载
     * @param conn
     * @param fileName
     * @param outputStream
     */
    public static void copyFile(Connection conn, String fileName, OutputStream outputStream){
        SCPClient sc = new SCPClient(conn);
        try {
            sc.get(fileName, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得远程服务器文件夹下所有文件
     *
     * @param logServer
     */
    public static List<SFTPv3DirectoryEntry> getFiles(LogServer logServer) {
        Connection connection = logServer.getConnection();
        SFTPv3Client sft = null;
        List<SFTPv3DirectoryEntry> res = new ArrayList<>();
        try {
            sft = new SFTPv3Client(connection);
            //获取远程目录下文件列表
            Vector<?> v = sft.ls(logServer.getRemotePath());
            for (Object o : v) {
                SFTPv3DirectoryEntry s = (SFTPv3DirectoryEntry) o;
                res.add(s);
            }
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (sft != null) {
                sft.close();
            }
        }
    }

    /**
     * 在远程LINUX服务器上，在指定目录下，删除指定文件
     * @param[in] fileName 文件名
     * @param[in] remotePath 远程主机的指定目录
     * @return
     */
    public void delFile(Connection conn, String fileName, String remotePath){
        try {
            SFTPv3Client sft = new SFTPv3Client(conn);
            //获取远程目录下文件列表
            Vector<?> v = sft.ls(remotePath);

            for (int i=0;i<v.size();i++) {
                SFTPv3DirectoryEntry s = new SFTPv3DirectoryEntry();
                s = (SFTPv3DirectoryEntry) v.get(i);
                //判断列表中文件是否与指定文件名相同
                if (s.filename.equals(fileName)) {
                    //rm()方法中，须是文件绝对路径+文件名称
                    sft.rm(remotePath + s.filename);
                }
                sft.close();
            }
        } catch (Exception e1) {
                e1.printStackTrace();
        }
    }

    /**
     * 执行脚本
     * @param conn Connection对象
     * @param cmds 要在linux上执行的指令
     */
    private static ExcuRes exec(Connection conn, String cmds){
        InputStream stdOut = null;
        InputStream stdErr = null;
        int ret = -1;
        try {
            //在connection中打开一个新的会话
            Session session = conn.openSession();
            //在远程服务器上执行linux指令
            session.execCommand(cmds);
            //指令执行结束后的输出
            stdOut = new StreamGobbler(session.getStdout());
            //指令执行结束后的错误
            stdErr = new StreamGobbler(session.getStderr());
            //等待指令执行结束
            session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
            //取得指令执行结束后的状态
            ret = session.getExitStatus();

            session.close();

        } catch (Exception e){
            e.printStackTrace();
        }

        ExcuRes res = new ExcuRes();
        res.setRet(ret);
        res.setStdErr(stdErr);
        res.setStdOut(stdOut);
        return res;
    }

    /**
     * 按照文件大小拆分文件
     * @param conn
     * @param logFile
     * @param size
     * @return
     */

    public static int SplitLogFile(Connection conn, LogFile logFile, String size) {
        StringBuilder cmd = new StringBuilder("split " + "-C " + size + " -d ");
        cmd.append(logFile.getFilename());
        cmd.append(" --additional-suffix=.log " + "info.").append(logFile.getService()).append(".")
                .append(logFile.getDateStr()).append('.');
        return exec(conn,cmd.toString()).getRet();
    }

    /**
     * 按照拆分数量拆分文件
     * @param conn
     * @param logFile
     * @param num
     * @return
     */
    public static int SplitLogFile(Connection conn, LogFile logFile, int num) {
        ExcuRes res = exec(conn,"wc -l " + logFile.getFilename());
        if (res.getRet() == -1) {
            return -1;
        }
        long fileRows = new Scanner(res.getStdOut()).nextLong();
        fileRows += (num-1);
        long eveyRow = fileRows / num;
        StringBuilder cmd = new StringBuilder("split " + "-l " + eveyRow + " -d ");
        cmd.append(logFile.getFilename());
        cmd.append(" --additional-suffix=.log " + "info.").append(logFile.getService()).append(".")
                .append(logFile.getDateStr()).append('.');
        return exec(conn,cmd.toString()).getRet();
    }

    /**
     * 根据文件名判断是否是日志文件
     * @param filename
     * @return
     */
    public static boolean fileType (String filename) {
        return filename.startsWith("info.") && filename.endsWith(".log");
    }


    /**
     * 通过关键字过滤文件日志行
     * @param conn
     * @param logFile
     * @param toFinds
     * @param outputStream
     */
    public static void checkFile(Connection conn, LogFile logFile, List<String> toFinds, OutputStream outputStream){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyFile(conn, logFile.getFilename(), out);

        try(InputStream inputStream = new ByteArrayInputStream(out.toByteArray())){
            Scanner scanner = new Scanner(inputStream);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                for (String toFind : toFinds) {
                    if (line.contains(toFind)) {
                        outputStream.write((line+"\n").getBytes(StandardCharsets.UTF_8));
                        System.out.println("该行内容包含关键字: " + line);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过时间范围过滤日志行
     * @param conn
     * @param logFile
     * @param start
     * @param end
     * @param outputStream
     */

    public static void checkTime(Connection conn, LogFile logFile, Date start, Date end, OutputStream outputStream) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyFile(conn, logFile.getFilename(), out);

        try(InputStream inputStream = new ByteArrayInputStream(out.toByteArray())){
            Scanner scanner = new Scanner(inputStream);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String timeStr = line.substring(0,8);
                SimpleDateFormat formatter=new SimpleDateFormat("HH:mm:ss");
                try {
                    Date time = formatter.parse(timeStr);
                    if (time.after(start) && time.before(end)) {
                        outputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                        System.out.println("该行在时间范围内: " + line);
                    }
                } catch (ParseException pe) {}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
