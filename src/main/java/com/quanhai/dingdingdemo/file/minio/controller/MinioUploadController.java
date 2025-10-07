package com.quanhai.dingdingdemo.file.controller.minio;

import com.example.mongodbpractice.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/minio/upload")
@RequiredArgsConstructor
public class MinioUploadController {

    private final MinioService minioService;

    @GetMapping("/presigned-url")

    public Map<String, Object> getPresignedUploadUrl(
            @RequestParam String bucketName,
            @RequestParam String objectName,
            @RequestParam(defaultValue = "60") int expiry,
            @RequestParam(required = false) String contentType) {

        Map<String, Object> result = new HashMap<>();

        try {
            String presignedUrl;

            presignedUrl = minioService.generatePresignedUploadUrl(bucketName, objectName, expiry);


            if (presignedUrl != null) {
                result.put("success", true);
                result.put("url", presignedUrl);
                result.put("bucketName", bucketName);
                result.put("objectName", objectName);
                result.put("expiry", expiry);
                log.info("生成预签名上传URL成功: {}/{}", bucketName, objectName);
            } else {
                result.put("success", false);
                result.put("message", "生成预签名上传URL失败");
                log.error("生成预签名上传URL失败: {}/{}", bucketName, objectName);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "生成预签名上传URL异常: " + e.getMessage());
            log.error("生成预签名上传URL异常", e);
        }

        return result;
    }
}
