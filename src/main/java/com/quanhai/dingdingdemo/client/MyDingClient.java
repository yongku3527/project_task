package com.quanhai.dingdingdemo.client;

import com.aliyun.teaopenapi.models.Config;

import com.quanhai.dingdingdemo.config.DingAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MyDingClient {
    @Autowired
    private DingAppConfig dingAppConfig;



    public com.aliyun.dingtalkoauth2_1_0.Client getOauthClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkoauth2_1_0.Client(config);
    }

    public  com.aliyun.dingtalkproject_1_0.Client getProjectClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkproject_1_0.Client(config);
    }








}
