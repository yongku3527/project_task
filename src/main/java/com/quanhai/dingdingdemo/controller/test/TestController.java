package com.quanhai.dingdingdemo.controller.test;

import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profileTest")
public class TestController {

    @Autowired
    private MailConfig mailConfig;

    @GetMapping("/test1")
    public Result test1() {

        return ResultUtil.success(mailConfig.toString());
    }
}
