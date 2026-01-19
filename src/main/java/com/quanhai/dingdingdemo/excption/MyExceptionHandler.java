package com.quanhai.dingdingdemo.excption;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenContextException;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.excption.MyExcption;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class MyExceptionHandler {

//    @ExceptionHandler(value = RuntimeException.class)
//    private Result exceptionHandler(Exception e){
//        return ResultUtil.fail("未知系统异常"+e.getMessage());
//    }

    @ExceptionHandler(value = MyExcption.class)
    public Result customHandler(MyExcption e){

        return ResultUtil.fail("常规系统异常"+e.getMsg());
    }

    @ExceptionHandler(value = NotLoginException.class)
    public Result notLoginHandler(NotLoginException e, HttpServletRequest request){
        String message = e.getMessage();
        //获取请求的IP地址等各种信息
        String ipAddress = request.getRemoteAddr();
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        // 记录日志

        log.warn("未登录异常: IP={}, URI={}, Method={}, 报错原因: {}", ipAddress, requestURI, method, message);

//        return ResultUtil.fail(message);
        return ResultUtil.defineFail(401, "请重新登录，在登录页面多刷新几次");

    }
    @ExceptionHandler(value = SaTokenContextException.class)
    public Result saTokenContextHandler(SaTokenContextException e, HttpServletRequest request){
        String message = e.getMessage();
        //获取请求的IP地址等各种信息
        String ipAddress = request.getRemoteAddr();
        String requestURI = request.getRequestURI();
        StringBuffer requestURL = request.getRequestURL();
        String method = request.getMethod();


        
        // 记录日志

        log.warn("SaToken上下文异常: IP={}, URI={}, URL={},Method={}, 报错原因: {}", ipAddress, requestURI,requestURL, method, message);

        return ResultUtil.fail("SaToken上下文异常"+e.getMessage());
    }
}
