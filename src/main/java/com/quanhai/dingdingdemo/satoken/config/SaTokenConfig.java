package com.quanhai.dingdingdemo.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限认证配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册Sa-Token的路由拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(handle -> {
//            // 指定一条 match 规则
//            SaRouter
//                    .match("/**")    // 拦截的 path 列表，可以写多个
//                    .notMatch("/auth/login")    // 排除掉的 path 列表，可以写多个
//                    .notMatch("/auth/register")
//                    //临时排除权限接口
//                    .notMatch("/auth/permission")
//                    .notMatch("/auth/role")
//                    .notMatch("/test/cookie")    // 排除cookie测试接口
//
//                    // 放行游客访问
//                    .notMatch("/dingTask/**")
//                    .notMatch("/TimeLine/**")
//                    .notMatch("/meeting/**")
//                    .notMatch("/basicData/**")
//                    .notMatch("/gantt/**")
//                    .notMatch("/file-manager/**")
//                    .notMatch("/file-info/**")
//                    .notMatch("/minio/**")
//
//                    .notMatch("/error")
//                    .notMatch("/swagger-ui/**")
//                    .notMatch("/swagger-resources/**")
//                    .notMatch("/webjars/**")
//                    .notMatch("/v2/api-docs")
//                    .notMatch("/v3/api-docs")
//                    .notMatch("/")    // 排除首页
//                    .notMatch("/index.html")    // 排除首页
//                    .notMatch("/crud-test.html")    // 排除CRUD测试页面
//                    .notMatch("/static/**")    // 排除静态资源
//                    .check(r -> StpUtil.checkLogin());        // 要执行的校验动作，可以写完整的 lambda 表达式


            SaRouter.match("/sys/**"
                            ,"/circuit-board/**"
                            ,"/semi-product/**"
                            ,"/doc-prod-drawing/**"
                            ,"/knowledge-info/**"

                            )
                    .check(r -> StpUtil.checkLogin());


        })).addPathPatterns("/**");
    }


}