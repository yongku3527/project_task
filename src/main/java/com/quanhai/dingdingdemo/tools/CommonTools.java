package com.quanhai.dingdingdemo.tools;

import cn.hutool.Hutool;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.config.msg.MsgConfig;
import com.quanhai.dingdingdemo.model.excption.MyExcption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class CommonTools {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MailConfig mailConfig;

    @Autowired
    private MsgConfig msgConfig;

    public String getAccessToken() {
        return (String) redisTemplate.opsForValue().get("accessToken");
    }

    /**
     * 带附件发送 原版
     */
//    public void send(String downloadUrl,
//                     String attachFileName,
//                     String subject,
//                     String to,
//                     String msg) throws Exception {
//
//        // 1. 下载到内存（解决 403）
//        byte[] bytes = downloadOSS(downloadUrl);
//
//        // 2. 构造数据源
//        ByteArrayDataSource dataSource = new ByteArrayDataSource(bytes, "application/octet-stream");
//
//        // 3. 发送
//        HtmlEmail email = new HtmlEmail();
//        email.setCharset("UTF-8");
//        email.setHostName(mailConfig.getSmtp());
//        email.setSmtpPort(mailConfig.getPort());
//        email.setAuthentication(mailConfig.getUser(), mailConfig.getPwd());
//        email.setSSLOnConnect(true);
//        email.setFrom(mailConfig.getUser());
//
//        String[] mails = new String[2];
//        mails[0] = to;
//        mails[1] = "3123544976@qq.com";
//        email.addTo(mails);
//
//
//        email.setSubject(subject);
//        email.setMsg(msg);
//        email.attach(dataSource, attachFileName, attachFileName);
//        email.send();
//
//        log.info("附件发送成功：  收件人"+to+"  内容：\n"+msg);
//    }


    /**
     * 发送带附件的邮件（修复 .dat 问题）
     */
    public void send(String downloadUrl,
                     String attachFileName,
                     String subject,
                     String to,
                     String msg) throws Exception {

        /* 1. 下载文件到内存 */
        byte[] bytes = downloadOSS(downloadUrl);

        /* 2. 构造 HtmlEmail */
        HtmlEmail email = new HtmlEmail();
        email.setCharset("UTF-8");
        email.setHostName(mailConfig.getSmtp());
        email.setSmtpPort(mailConfig.getPort());
        email.setAuthentication(mailConfig.getUser(), mailConfig.getPwd());
        email.setSSLOnConnect(true);
        email.setFrom(mailConfig.getUser());

        email.addTo(to);
        //抄送
        email.addBcc("3123544976@qq.com");

        email.setSubject(subject);
        email.setMsg(msg);

        /* 3. 手工构造附件（关键修复） */
        // 3-1 识别 MIME
        String mime = URLConnection.guessContentTypeFromName(attachFileName);
        if (mime == null) mime = "application/octet-stream";

        log.info("发送邮件附件的类型："+mime);

        // 3-2 数据源
        ByteArrayDataSource dataSource = new ByteArrayDataSource(bytes, mime);


        // 3-3 挂到邮件——用 HtmlEmail 提供的重载，第三个参数 description 可空
        email.attach(dataSource,
                MimeUtility.encodeText(attachFileName, "UTF-8", null), // 中文文件名
                "钢网附件",                                                  // description
                EmailAttachment.ATTACHMENT);                          // 强制 attachment

        /* 4. 发送 */
        email.send();
        log.info("附件发送成功：  收件人"+to+"  内容：\n"+msg);
    }



    /**
     * 纯文本发送
     */
    public void send(String subject, String to, String msg) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setCharset("UTF-8");
        email.setHostName(mailConfig.getSmtp());
        email.setSmtpPort(mailConfig.getPort());
        email.setAuthentication(mailConfig.getUser(), mailConfig.getPwd());
        email.setSSLOnConnect(true);
        email.setFrom(mailConfig.getUser());
        email.addTo(to);
        email.setSubject(subject);
        email.setMsg(msg);
        email.send();
    }

//    private byte[] downloadOSS(String rawUrl) throws Exception {
//        HttpURLConnection conn = (HttpURLConnection) new URL(rawUrl).openConnection();
//        conn.setRequestProperty("User-Agent",
//                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36");
//        conn.setRequestProperty("Referer", "https://quanhaikeji.aliwork.com/");
//        // 设置超时时间（5分钟）
//        conn.setConnectTimeout(300000);
//        conn.setReadTimeout(300000);
//
//        try (InputStream in = conn.getInputStream();
//             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
//            byte[] buf = new byte[8192];
//            int len;
//            while ((len = in.read(buf)) != -1) {
//                bos.write(buf, 0, len);
//            }
//            return bos.toByteArray();
//        }
//    }

    private byte[] downloadOSS(String rawUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(rawUrl).openConnection();
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36");
        conn.setRequestProperty("Referer", "https://quanhaikeji.aliwork.com/");

        // 设置超时时间（5分钟）
        conn.setConnectTimeout(300000);
        conn.setReadTimeout(300000);

        int expectedLength = conn.getContentLength();
        if (expectedLength <= 0) {
            log.error("无法获取文件长度，可能是网络或链接问题：" + rawUrl);
        }

        try (InputStream in = conn.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }

            byte[] data = bos.toByteArray();
            if (data.length != expectedLength) {
                log.error("文件下载不完整，期望长度：" + expectedLength + "，实际：" + data.length);
            }


            return data;
        } finally {
            conn.disconnect();
        }
    }



    public String getMsgToken(String timestamp) {

        // 获取当前时间戳（精确到毫秒）
//        long timestamp = System.currentTimeMillis();
        // 将时间戳转换为字符串
        String timestampStr = String.valueOf(timestamp);
        // 使用Hutool的DigestUtil进行MD5加密，获取32位小写密文
        String token = DigestUtil.md5Hex(msgConfig.getUserId() + timestampStr + msgConfig.getApikey());

        return token;
    }

    //根据userid获取用户名
    public String getUserName(String userId) {
        String token = getAccessToken();
        String userName = "未知";
        if (Boolean.TRUE.equals(redisTemplate.hasKey("userName_" + userId))) {
            userName = (String) redisTemplate.opsForValue().get("userName_" + userId);
        }else {

            // 2. 构建请求URL（包含Query参数access_token）
            String url = "https://oapi.dingtalk.com/topapi/v2/user/get" +
                    "?access_token=" + token;
            // 3. 构建Body参数（JSON格式）
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("userid", userId);
            // 4. 发送POST请求并获取响应
            String result = HttpUtil.post(url, bodyJson.toString());
            try {
                JSONObject jsonObj = JSONUtil.parseObj(result);
                // 检查接口调用状态
                if (jsonObj.getInt("errcode") != 0) {
                    log.error("接口调用失败: " + jsonObj.getStr("errmsg"));

                }

                // 获取用户信息对象
                JSONObject userInfo = jsonObj.getJSONObject("result");
                if (userInfo == null) {
                    log.error("用户信息为空");
                }

                // 提取用户名称
                userName = userInfo.getStr("name", "未知");

                //当这里是王纯或于长悦或谁的时候 改成未知
                if("王纯".equals(userName)||"于长悦".equals(userName)||"李正鑫".equals(userName)||"赵家洋".equals(userName)) {
                    userName = "未知";
                }else {
                    redisTemplate.opsForValue().set("userName_" + userId, userName);
                }

            } catch (RuntimeException e) {
                try {
                    send("获取根据id用户名失败",mailConfig.getDeveloper(),e.getMessage());
                } catch (EmailException ex) {
                    log.error("!!!!!!!!他妈的,获取用户名失败而且发送邮件也失败了！！！ "+ex.getMessage());
                }
            }
        }
        return userName;
    }







}