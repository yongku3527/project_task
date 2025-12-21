package com.quanhai.dingdingdemo.excption;

import cn.dev33.satoken.exception.NotLoginException;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.excption.MyExcption;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MyExceptionHandler {

//    @ExceptionHandler(value = Exception.class)
//    private Result exceptionHandler(Exception e){
//        return ResultUtil.fail("未知系统异常"+e.getMessage());
//    }

    @ExceptionHandler(value = MyExcption.class)
    private Result customHandler(MyExcption e){

        return ResultUtil.fail("常规系统异常"+e.getMsg());
    }

    @ExceptionHandler(value = NotLoginException.class)
    private Result notLoginHandler(NotLoginException e, HttpServletRequest request){
        String message = e.getMessage();
        //获取请求的IP地址
        
        System.out.println("satoken报错，报错原因："+message);
        return ResultUtil.fail(message);
    }
}
