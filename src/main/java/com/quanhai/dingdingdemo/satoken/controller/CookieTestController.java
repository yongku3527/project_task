package com.quanhai.dingdingdemo.satoken.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Cookie测试控制器
 */
@RestController
@RequestMapping("/test")
public class CookieTestController {

    /**
     * 测试获取cookie
     * @param request HTTP请求
     * @return 测试结果
     */
    @GetMapping("/cookie")
    public Result testCookie(HttpServletRequest request) {
        StringBuilder result = new StringBuilder();
        
        // 获取所有cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            result.append("Cookies found: ").append(cookies.length).append("\n");
            for (Cookie cookie : cookies) {
                result.append("Name: ").append(cookie.getName())
                      .append(", Value: ").append(cookie.getValue())
                      .append(", Path: ").append(cookie.getPath())
                      .append(", MaxAge: ").append(cookie.getMaxAge())
                      .append("\n");
            }
        } else {
            result.append("No cookies found");
        }
        
        // 检查Sa-Token是否能获取token
        String tokenValue = StpUtil.getTokenValue();
        result.append("\nSa-Token token value: ").append(tokenValue);
        
        // 检查是否登录
        boolean isLogin = StpUtil.isLogin();
        result.append("\nIs logged in: ").append(isLogin);
        
        if (isLogin) {
            result.append("\nLogin ID: ").append(StpUtil.getLoginId());
        }
        
        return ResultUtil.success(result.toString());
    }
}