package com.quanhai.dingdingdemo.excption;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenContextException;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.excption.MyExcption;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 * <p>
 * 异常匹配优先级：精确子类 > 父类 > 兜底 Exception
 * 所有处理器均记录完整堆栈日志，便于线上问题排查。
 */
@RestControllerAdvice
@Slf4j
public class MyExceptionHandler {

    // ==================== Sa-Token 权限相关异常 ====================

    /**
     * 未登录 / Token 失效
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleNotLogin(NotLoginException e, HttpServletRequest request) {
        logRequestInfo("未登录", e, request);
        return ResultUtil.defineFail(401, "请重新登录");
    }

    /**
     * Sa-Token 上下文异常（如 multipart 请求下上下文不可用）
     */
    @ExceptionHandler(SaTokenContextException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleSaTokenContext(SaTokenContextException e, HttpServletRequest request) {
        logRequestInfo("SaToken上下文异常", e, request);
        return ResultUtil.defineFail(401, "认证上下文异常，请重新登录");
    }

    /**
     * 无权限（缺少指定权限码）
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleNotPermission(NotPermissionException e, HttpServletRequest request) {
        logRequestInfo("无权限", e, request);
        return ResultUtil.defineFail(403, "没有操作权限：" + e.getPermission());
    }

    /**
     * 无角色（缺少指定角色）
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleNotRole(NotRoleException e, HttpServletRequest request) {
        logRequestInfo("无角色", e, request);
        return ResultUtil.defineFail(403, "没有角色权限：" + e.getRole());
    }

    // ==================== 自定义业务异常 ====================

    /**
     * 自定义业务异常
     */
    @ExceptionHandler(MyExcption.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMyException(MyExcption e) {
        log.warn("业务异常: {}", e.getMsg(), e);
        return ResultUtil.defineFail(400, e.getMsg());
    }

    // ==================== 参数校验异常 ====================

    /**
     * @RequestBody 参数校验失败（@Valid / @Validated）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleValidation(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", errorMsg);
        return ResultUtil.defineFail(400, errorMsg);
    }

    /**
     * 表单绑定校验失败
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBind(BindException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        log.warn("参数绑定失败: {}", errorMsg);
        return ResultUtil.defineFail(400, errorMsg);
    }

    /**
     * 必填参数缺失
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少必填参数: {}", e.getParameterName());
        return ResultUtil.defineFail(400, "缺少必填参数：" + e.getParameterName());
    }

    // ==================== 请求格式异常 ====================

    /**
     * 请求体 JSON 解析失败
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ResultUtil.defineFail(400, "请求参数格式错误，请检查 JSON 格式");
    }

    /**
     * 请求方法不支持（如 GET 请求了 POST 接口）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return ResultUtil.defineFail(405, "不支持的请求方法：" + e.getMethod());
    }

    /**
     * 404 资源不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleNoResourceFound(NoResourceFoundException e) {
        log.warn("接口不存在: {}", e.getResourcePath());
        return ResultUtil.defineFail(404, "接口不存在");
    }

    // ==================== 文件上传异常 ====================

    /**
     * 文件上传超过大小限制
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("文件上传超限: {}", e.getMessage());
        return ResultUtil.defineFail(400, "上传文件超过大小限制");
    }

    // ==================== 兜底异常 ====================

    /**
     * 兜底：捕获所有未处理的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e, HttpServletRequest request) {
        logRequestInfo("未知系统异常", e, request);
        return ResultUtil.defineFail(500, "系统内部错误，请联系管理员");
    }

    // ==================== 私有工具方法 ====================

    /**
     * 统一记录请求信息 + 异常堆栈
     */
    private void logRequestInfo(String errorType, Exception e, HttpServletRequest request) {
        log.error("{}: IP={}, URI={}, Method={}, 原因: {}",
                errorType,
                request.getRemoteAddr(),
                request.getRequestURI(),
                request.getMethod(),
                e.getMessage(),
                e);
    }
}
//package com.quanhai.dingdingdemo.excption;
//
//import cn.dev33.satoken.exception.NotLoginException;
//import cn.dev33.satoken.exception.SaTokenContextException;
//import com.quanhai.dingdingdemo.model.Resp.Result;
//import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
//import com.quanhai.dingdingdemo.model.excption.MyExcption;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@RestControllerAdvice
//@Slf4j
//public class MyExceptionHandler {
//
////    @ExceptionHandler(value = RuntimeException.class)
////    private Result exceptionHandler(Exception e){
////        return ResultUtil.fail("未知系统异常"+e.getMessage());
////    }
//
//    @ExceptionHandler(value = MyExcption.class)
//    public Result customHandler(MyExcption e){
//
//        return ResultUtil.fail("常规系统异常"+e.getMsg());
//    }
//
//    @ExceptionHandler(value = NotLoginException.class)
//    public Result notLoginHandler(NotLoginException e, HttpServletRequest request){
//        String message = e.getMessage();
//        //获取请求的IP地址等各种信息
//        String ipAddress = request.getRemoteAddr();
//        String requestURI = request.getRequestURI();
//        String method = request.getMethod();
//        // 记录日志
//
//        log.warn("未登录异常: IP={}, URI={}, Method={}, 报错原因: {}", ipAddress, requestURI, method, message);
//
////        return ResultUtil.fail(message);
//        return ResultUtil.defineFail(401, "请重新登录，在登录页面多刷新几次");
//
//    }
//    @ExceptionHandler(value = SaTokenContextException.class)
//    public Result saTokenContextHandler(SaTokenContextException e, HttpServletRequest request){
//        String message = e.getMessage();
//        //获取请求的IP地址等各种信息
//        String ipAddress = request.getRemoteAddr();
//        String requestURI = request.getRequestURI();
//        StringBuffer requestURL = request.getRequestURL();
//        String method = request.getMethod();
//
//
//
//        // 记录日志
//
//        log.warn("SaToken上下文异常: IP={}, URI={}, URL={},Method={}, 报错原因: {}", ipAddress, requestURI,requestURL, method, message);
//
//        return ResultUtil.fail("SaToken上下文异常"+e.getMessage());
//    }
//}
