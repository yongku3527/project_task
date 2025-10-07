package com.quanhai.dingdingdemo.file.controller.minio;

import com.example.mongodbpractice.service.MinioService;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/minio")
@RequiredArgsConstructor
public class MinioController {

    private final MinioService minioService;

    /**
     * 创建存储桶
     * 
     * @param bucketName 存储桶名称
     * @return 创建结果
     */
    @PostMapping("/buckets/{bucketName}")
    public ResponseEntity<Map<String, Object>> createBucket(@PathVariable String bucketName) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = minioService.createBucket(bucketName);
            response.put("success", success);
            response.put("message", success ? "存储桶创建成功" : "存储桶创建失败");
            response.put("bucketName", bucketName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建存储桶失败", e);
            response.put("success", false);
            response.put("message", "存储桶创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除存储桶
     * 
     * @param bucketName 存储桶名称
     * @return 删除结果
     */
    @DeleteMapping("/buckets/{bucketName}")
    public ResponseEntity<Map<String, Object>> deleteBucket(@PathVariable String bucketName) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = minioService.deleteBucket(bucketName);
            response.put("success", success);
            response.put("message", success ? "存储桶删除成功" : "存储桶删除失败");
            response.put("bucketName", bucketName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除存储桶失败", e);
            response.put("success", false);
            response.put("message", "存储桶删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取所有存储桶列表
     * 
     * @return 存储桶列表
     */
    @GetMapping("/buckets")
    public ResponseEntity<Map<String, Object>> listBuckets() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Bucket> buckets = minioService.listBuckets();
            response.put("success", true);
            response.put("buckets", buckets);
            response.put("count", buckets.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取存储桶列表失败", e);
            response.put("success", false);
            response.put("message", "获取存储桶列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 上传文件到指定存储桶
     * 
     * @param bucketName 存储桶名称
     * @param file 文件
     * @return 上传结果
     */
    @PostMapping("/buckets/{bucketName}/files")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @PathVariable String bucketName,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            String objectName = file.getOriginalFilename();
            boolean success = minioService.uploadFile(bucketName, objectName, file);
            
            response.put("success", success);
            response.put("message", success ? "文件上传成功" : "文件上传失败");
            response.put("bucketName", bucketName);
            response.put("objectName", objectName);
            response.put("fileSize", file.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 下载文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件内容
     */
    @GetMapping("/buckets/{bucketName}/files/{objectName:.+}")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable String bucketName,
            @PathVariable String objectName) {
        
        try {
            InputStream inputStream = minioService.downloadFile(bucketName, objectName);
            if (inputStream == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + objectName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(inputStream));
                    
        } catch (Exception e) {
            log.error("文件下载失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取文件信息
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件信息
     */
    @GetMapping("/buckets/{bucketName}/files/{objectName:.+}/info")
    public ResponseEntity<Map<String, Object>> getFileInfo(
            @PathVariable String bucketName,
            @PathVariable String objectName) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            var fileInfo = minioService.getFileInfo(bucketName, objectName);
            if (fileInfo == null) {
                response.put("success", false);
                response.put("message", "文件不存在");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("bucketName", bucketName);
            response.put("objectName", objectName);
            response.put("size", fileInfo.size());
            response.put("contentType", fileInfo.contentType());
            response.put("lastModified", fileInfo.lastModified());
            response.put("etag", fileInfo.etag());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取文件信息失败", e);
            response.put("success", false);
            response.put("message", "获取文件信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 删除结果
     */
    @DeleteMapping("/buckets/{bucketName}/files/{objectName:.+}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @PathVariable String bucketName,
            @PathVariable String objectName) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = minioService.deleteFile(bucketName, objectName);
            response.put("success", success);
            response.put("message", success ? "文件删除成功" : "文件删除失败");
            response.put("bucketName", bucketName);
            response.put("objectName", objectName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            response.put("success", false);
            response.put("message", "文件删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取存储桶中的文件列表
     * 
     * @param bucketName 存储桶名称
     * @param prefix 前缀过滤（可选）
     * @param recursive 是否递归（默认true）
     * @return 文件列表
     */
    @GetMapping("/buckets/{bucketName}/files")
    public ResponseEntity<Map<String, Object>> listFiles(
            @PathVariable String bucketName,
            @RequestParam(required = false) String prefix,
            @RequestParam(defaultValue = "true") boolean recursive) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            List<Item> files = minioService.listFiles(bucketName, prefix, recursive);
            
            response.put("success", true);
            response.put("bucketName", bucketName);
            response.put("files", files);
            response.put("count", files.size());
            response.put("prefix", prefix);
            response.put("recursive", recursive);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            response.put("success", false);
            response.put("message", "获取文件列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 生成预签名URL
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 过期时间（分钟，默认60）
     * @return 预签名URL
     */
    @GetMapping("/buckets/{bucketName}/files/{objectName:.+}/presigned-url")
    public ResponseEntity<Map<String, Object>> generatePresignedUrl(
            @PathVariable String bucketName,
            @PathVariable String objectName,
            @RequestParam(defaultValue = "60") int expiry) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            String presignedUrl = minioService.generatePresignedUrl(bucketName, objectName, expiry);
            
            if (presignedUrl == null) {
                response.put("success", false);
                response.put("message", "生成预签名URL失败");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("bucketName", bucketName);
            response.put("objectName", objectName);
            response.put("presignedUrl", presignedUrl);
            response.put("expiryMinutes", expiry);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            response.put("success", false);
            response.put("message", "生成预签名URL失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 复制文件
     * 
     * @param sourceBucket 源存储桶
     * @param sourceObject 源对象
     * @param targetBucket 目标存储桶
     * @param targetObject 目标对象
     * @return 复制结果
     */
    @PostMapping("/buckets/{sourceBucket}/files/{sourceObject:.+}/copy")
    public ResponseEntity<Map<String, Object>> copyFile(
            @PathVariable String sourceBucket,
            @PathVariable String sourceObject,
            @RequestParam String targetBucket,
            @RequestParam String targetObject) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = minioService.copyFile(sourceBucket, sourceObject, targetBucket, targetObject);
            
            response.put("success", success);
            response.put("message", success ? "文件复制成功" : "文件复制失败");
            response.put("sourceBucket", sourceBucket);
            response.put("sourceObject", sourceObject);
            response.put("targetBucket", targetBucket);
            response.put("targetObject", targetObject);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文件复制失败", e);
            response.put("success", false);
            response.put("message", "文件复制失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}