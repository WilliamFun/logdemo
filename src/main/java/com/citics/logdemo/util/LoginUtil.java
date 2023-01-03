package com.citics.logdemo.util;

import ch.ethz.ssh2.*;
import com.citics.logdemo.bean.LogServer;
import org.springframework.stereotype.Component;
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
    //ssh 会话
    private Session session;

//    //远程mysql连接的IP地址
//    private final static String MYSQL_REMOTE_SERVER = "124.543.789.111";
//    //本地数据库连接时用的端口号，和yml配置的端口一致
//    private final static int LOCAl_PORT = 3309;
//    //远程数据库端口用的端口号
//    private final static int REMOTE_PORT = 31494;

    public boolean login(LogServer logServer){
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

    public void logout (Connection connection)
    {
        connection.close();
    }


    /**
     * 上传本地文件到服务器目录下
     * @param conn Connection对象
     * @param fileName 本地文件
     * @param remotePath 服务器目录
     */
    public void putFile(Connection conn, String fileName, String remotePath){
        SCPClient sc = new SCPClient(conn);
        try {
            //将本地文件放到远程服务器指定目录下，默认的文件模式为 0600，即 rw，
            //如要更改模式，可调用方法 put(fileName, remotePath, mode),模式须是4位数字且以0开头
            sc.put(fileName, remotePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载服务器文件到本地目录（java所在服务器）
     * @param fileName 服务器文件
     * @param localPath 本地目录
     */
    public void copyFile(Connection conn, String fileName,String localPath){
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
    public void copyFile(Connection conn, String fileName, OutputStream outputStream){
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
     * @param remoteDir
     */
    public List<SFTPv3DirectoryEntry> getFiles(LogServer logServer, String remoteDir) {
        Connection connection = logServer.getConnection();
        SFTPv3Client sft = null;
        List<SFTPv3DirectoryEntry> res = new ArrayList<>();
        try {
            sft = new SFTPv3Client(connection);
            //获取远程目录下文件列表
            Vector<?> v = sft.ls(remoteDir);
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
     * 在远程LINUX服务器上，在指定目录下，获取文件各个属性
     * @param[in] conn Conncetion对象
     * @param[in] remotePath 远程主机的指定目录
     */
    public void getFileProperties(Connection conn, String remotePath){
        try {
            SFTPv3Client sft = new SFTPv3Client(conn);
            Vector<?> v = sft.ls(remotePath);
            for(int i=0;i<v.size();i++){
                SFTPv3DirectoryEntry s = new SFTPv3DirectoryEntry();
                s = (SFTPv3DirectoryEntry) v.get(i);
                //文件名
                String filename = s.filename;
                //文件的大小
                Long fileSize = s.attributes.size;

                System.out.println("file:" + filename + " " +"attri:" + fileSize);

            }

            sft.close();

        } catch (Exception e1) {
            e1.printStackTrace();
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
    public int exec(Connection conn, String cmds){
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

            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * 根据文件名判断是否是日志文件
     * @param filename
     * @return
     */
    public boolean fileType (String filename) {
        return filename.endsWith(".log");
    }


    public void checkFile(Connection conn, SFTPv3DirectoryEntry s, String toFind, OutputStream outputStream){
        if(s.filename.contains(toFind)){
            System.out.println("该文件名中包含关键词：" + toFind);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyFile(conn,s.filename,out);

        try(InputStream inputStream = new ByteArrayInputStream(out.toByteArray())){
            Scanner scanner = new Scanner(inputStream);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                line = line + "\n";
                if (line.contains(toFind)) {
                    outputStream.write(line.getBytes(StandardCharsets.UTF_8));
                    System.out.println("该行内容包含关键字: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkTime(Connection conn, SFTPv3DirectoryEntry s, Date start, Date end, OutputStream outputStream) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyFile(conn,s.filename,out);

        try(InputStream inputStream = new ByteArrayInputStream(out.toByteArray())){
            Scanner scanner = new Scanner(inputStream);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                line = line + "\n";
                String timeStr = line.substring(0,8);
                SimpleDateFormat formatter=new SimpleDateFormat("HH:mm:ss");
                Date time = formatter.parse(timeStr);
                if (time.after(start) || time.before(end)) {
                    outputStream.write(line.getBytes(StandardCharsets.UTF_8));
                    System.out.println("该行在时间范围内: " + line);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }




    public static void main(String[] args) throws IOException {
        LoginUtil log = new LoginUtil();
        System.out.println(log.login());
        OutputStream outputStream = new FileOutputStream("test");
        log.getFiles(conn,"/root/",outputStream);
    }

}
