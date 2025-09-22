package com.quanhai.dingdingdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mymail")
public class MailConfig {

    private String user;
    private String pwd;
    private String to;
    private String smtp;
    private int port;
    private String badTo;
    private String developer;

}
