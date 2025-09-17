package com.quanhai.dingdingdemo.config.msg;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "msgconfig")
public class MsgConfig {

    private String userId;

    private String apikey;

    private String url;

}
