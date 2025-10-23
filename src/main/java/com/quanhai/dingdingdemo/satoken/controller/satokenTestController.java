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
        return ResultUtil.success("登录成功");
    }
    /**
     * 登录
     */
    @RequestMapping("/test")
//    @SaCheckRole("admin")
//    @SaCheckPermission("user:role")
    public Result test() {
//        StpUtil.checkLogin();
//        boolean b = StpUtil.hasRole("admin");
        boolean b = StpUtil.hasPermission("user:role");
        if (!b) {
            return ResultUtil.fail("没有user:role权限");
        }
//        if (!b) {
//            return ResultUtil.fail("没有admin角色");
//        }
        return ResultUtil.success("通过");
    }


    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    @GetMapping("/userInfo")
    @SaCheckLogin
    public Result getUserInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "获取用户信息成功");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", StpUtil.getLoginId());
        userInfo.put("tokenName", StpUtil.getTokenName());
        userInfo.put("tokenValue", StpUtil.getTokenValue());
        userInfo.put("loginDevice", StpUtil.getLoginDevice());
        userInfo.put("isLogin", StpUtil.isLogin());

        userInfo.put("roleList", StpUtil.getRoleList());
        userInfo.put("permissionList", StpUtil.getPermissionList());

        result.put("data", userInfo);
        return ResultUtil.success(result);
    }

}
