package com.quanhai.dingdingdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//            .allowedOrigins("http://192.168.70.56:80")
//            .allowedMethods("*")
//            .allowedHeaders("*")
//            .allowCredentials(true);
//    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 允许所有域名进行跨域调用
                .allowedMethods("*") // 允许任何方法（POST、GET等）
                .allowedHeaders("*") // 允许任何请求头
                .allowCredentials(true) // 允许携带凭证（如Cookie）
                .maxAge(3600); // 预检请求的有效期，单位为秒
    }
}