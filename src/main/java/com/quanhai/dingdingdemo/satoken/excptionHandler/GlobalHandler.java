package com.quanhai.dingdingdemo.satoken.excptionHandler;

import cn.dev33.satoken.exception.NotPermissionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(NotPermissionException.class)
    private Map<String, Object> NotAuthExceptionHandler(NotPermissionException e) {

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("msg", "您没有访问此接口的权限");
        return result;
    }
}
