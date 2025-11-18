package com.quanhai.dingdingdemo.controller.test;

import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.service.yiDa.PrcsServiceImpl;
import com.quanhai.dingdingdemo.tools.CommonTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequestMapping("/RecsTest")
public class TestController {

    @Autowired
    private MailConfig mailConfig;

    @Autowired
    private PrcsServiceImpl prcsService;

    @GetMapping("/prce")
    public Result test1() {

        try {
            prcsService.creatNewInterface("eba3b2bd-fb03-4e45-80a7-cd8b28485fa3");
        } catch (Exception e) {

           return ResultUtil.fail("创建失败");
//           我需要联系erp要选好手指落下的位置如 差点摸不到就
            //


        }
        return ResultUtil.success("success");
    }
}
