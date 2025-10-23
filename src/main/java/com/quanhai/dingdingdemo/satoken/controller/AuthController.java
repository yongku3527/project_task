package com.quanhai.dingdingdemo.satoken.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.quanhai.dingdingdemo.satoken.model.SysUser;
import com.quanhai.dingdingdemo.satoken.model.User;
import com.quanhai.dingdingdemo.satoken.service.AuthService;
import com.quanhai.dingdingdemo.satoken.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 用户登录
     * @param user 用户信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody SysUser user) {

        if (user.getUsername() != null && user.getPassword() != null) {
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getUsername, user.getUsername());
            SysUser sysUser = sysUserService.getOne(queryWrapper);

            if (sysUser != null) {
                if (sysUser.getPassword().equals(user.getPassword())) {
                    StpUtil.login(sysUser.getId());
                    String tokenValue = StpUtil.getTokenValue();

                    // 登录成功
                    Map<String, Object> result = new HashMap<>();
                    result.put("code", 200);
                    result.put("msg", "登录成功");
                    result.put("data", tokenValue);
                    return result;
                }
            }
        }

        
        // 登录失败
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("msg", "用户名或密码错误");
        return result;
    }

    /**
     * 用户注销
     * @return 注销结果
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        StpUtil.logout();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "注销成功");
        return result;
    }

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        // 此处仅作模拟，实际项目应该将用户信息保存到数据库
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "注册成功");
        result.put("data", user);
        return result;
    }

    @GetMapping("/permission")
    public Map<String, Object> getPermissionList(Long userId) {
        List<String> permissionList = authService.getPermissionByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "获取权限成功");
        result.put("data", permissionList);
        return result;
    }

    @GetMapping("/role")
    public Map<String, Object> getRoleList(Long userId) {
        List<String> roleList = authService.getRoleListByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "获取权限成功");
        result.put("data", roleList);
        return result;
    }
}