package com.quanhai.dingdingdemo.satoken.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 */
@Controller
public class IndexController {

    /**
     * 首页
     * @return 重定向到测试页面
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }
}