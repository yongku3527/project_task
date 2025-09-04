package com.quanhai.dingdingdemo.client;

import com.aliyun.teaopenapi.models.Config;

import com.quanhai.dingdingdemo.config.DingAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MyDingClient {
    @Autowired
    private DingAppConfig dingAppConfig;



    public com.aliyun.dingtalkoauth2_1_0.Client getOauthClient() {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        try {
            return new com.aliyun.dingtalkoauth2_1_0.Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public  com.aliyun.dingtalkproject_1_0.Client getProjectClient(){
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        try {
            return new com.aliyun.dingtalkproject_1_0.Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public  com.aliyun.dingtalkyida_2_0.Client getyidaClient()  {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        try {
            return new com.aliyun.dingtalkyida_2_0.Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }









}
