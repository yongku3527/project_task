package com.quanhai.dingdingdemo.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Sa-Token 测试类
 */
@RestController
@RequestMapping("/tokenTest")
public class satokenTestController {


    /**
     * 登录
     */
    @RequestMapping("/login")
    public Result login( @RequestParam("id") Long id) {
        StpUtil.login(id);
        return ResultUtil.success("测试成功");
    }

}
