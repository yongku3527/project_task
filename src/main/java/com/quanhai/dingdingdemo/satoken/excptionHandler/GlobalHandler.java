package com.quanhai.dingdingdemo.satoken.excptionHandler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(NotPermissionException.class)
    private Map<String, Object> NotAuthExceptionHandler(NotPermissionException e) {

        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("msg", "您没有访问此接口的权限");
        return result;
    }


    @ExceptionHandler(NotLoginException.class)
    private Map<String, Object> NotLoginExceptionHandler(NotLoginException e) {

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("msg", "您没有登录，请先登录");
        return result;
    }
    @ExceptionHandler(NotRoleException.class)
    private Map<String, Object> NotRoleExceptionHandler(NotRoleException e) {

        Map<String, Object> result = new HashMap<>();
        result.put("code", 402);

        result.put("msg", "您没有" + e.getRole() + "角色权限");
        return result;
    }

}
