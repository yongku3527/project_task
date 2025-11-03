package com.quanhai.dingdingdemo.file.minio.config;

import com.quanhai.dingdingdemo.file.minio.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * MinIO存储桶初始化器
 * 在应用启动时自动创建必要的存储桶
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketInitializer implements ApplicationRunner {

    private final MinioService minioService;
    
    // 定义需要自动创建的存储桶
    private static final String[] BUCKETS = {
        "circuit-boards",      // 线路板文件存储桶
        // "semi-products",       // 半成品文件存储桶
        "schematic-files",     // 原理图文件专用存储桶
        "smt-files",          // SMT文件专用存储桶
        "led-board-plugins",   // 灯板插件文件存储桶
        "dwg-files",          // DWG图纸文件存储桶
        "pdf-files"           // PDF图纸文件存储桶
    };

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化MinIO存储桶...");
        
        for (String bucketName : BUCKETS) {
            try {
                // 尝试创建存储桶，如果已存在则忽略
                var result = minioService.createBucket(bucketName);
                if (result.getCode() == 200) {
                    log.info("存储桶创建成功: {}", bucketName);
                } else {
                    // 如果存储桶已存在，记录信息但不视为错误
                    if (result.getMsg().contains("已存在")) {
                        log.info("存储桶已存在: {}", bucketName);
                    } else {
                        log.error("创建存储桶失败: {}, 错误: {}", bucketName, result.getMsg());
                    }
                }
            } catch (Exception e) {
                log.error("初始化存储桶时发生异常: {}", bucketName, e);
            }
        }
        
        log.info("MinIO存储桶初始化完成");
    }
}