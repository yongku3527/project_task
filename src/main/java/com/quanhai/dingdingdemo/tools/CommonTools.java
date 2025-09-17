package com.quanhai.dingdingdemo.tools;

import cn.hutool.Hutool;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.config.msg.MsgConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Component;

import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
     * 带附件发送
     */
    public void send(String downloadUrl,
                     String attachFileName,
                     String subject,
                     String to,
                     String msg) throws Exception {

        // 1. 下载到内存（解决 403）
        byte[] bytes = downloadOSS(downloadUrl);

        // 2. 构造数据源
        ByteArrayDataSource dataSource = new ByteArrayDataSource(bytes, "application/octet-stream");

        // 3. 发送
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
        email.attach(dataSource, attachFileName, "附件描述");
        email.send();

        log.info("\n附件发送成功：  收件人"+to+"  内容：\n"+msg);
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

    private byte[] downloadOSS(String rawUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(rawUrl).openConnection();
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36");
        conn.setRequestProperty("Referer", "https://quanhaikeji.aliwork.com/");

        try (InputStream in = conn.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
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






}