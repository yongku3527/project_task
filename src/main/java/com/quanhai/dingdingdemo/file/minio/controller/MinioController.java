package com.quanhai.dingdingdemo.file.minio.controller;

import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.file.minio.service.MinioService;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.file.service.FileInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/minio")
@RequiredArgsConstructor
public class MinioController {

    private final MinioService minioService;
    private final FileInfoService fileInfoService;

    /**
     * 创建存储桶
     * 
     * @param bucketName 存储桶名称
     * @return Result结果
     */
    @PostMapping("/buckets/{bucketName}")
    public Result<Map<String, Object>> createBucket(@PathVariable String bucketName) {
        log.info("创建存储桶: {}", bucketName);
        return minioService.createBucket(bucketName);
    }

    /**
     * 删除存储桶
     * 
     * @param bucketName 存储桶名称
     * @return Result结果
     */
    @DeleteMapping("/buckets/{bucketName}")
    public Result<Map<String, Object>> deleteBucket(@PathVariable String bucketName) {
        log.info("删除存储桶: {}", bucketName);
        return minioService.deleteBucket(bucketName);
    }

    /**
     * 获取所有存储桶列表
     * 
     * @return Result结果，包含存储桶名称列表
     */
    @GetMapping("/buckets")
    public Result<List<String>> listBuckets() {
        log.info("获取存储桶列表");
        Result<List<Bucket>> bucketsResult = minioService.listBuckets();
        
        if (bucketsResult.getCode() == 200 && bucketsResult.getData() != null) {
            List<String> bucketNames = bucketsResult.getData().stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());
            log.info("获取存储桶列表成功，共 {} 个存储桶", bucketNames.size());
            return ResultUtil.success(bucketNames);
        } else {
            log.error("获取存储桶列表失败: {}", bucketsResult.getMsg());
            return ResultUtil.fail(bucketsResult.getMsg());
        }
    }

    /**
     * 上传文件到指定存储桶
     * 
     * @param bucketName 存储桶名称
     * @param file 文件
     * @return Result结果
     */
    @PostMapping("/buckets/{bucketName}/files")
    public Result<Map<String, Object>> uploadFile(
            @PathVariable String bucketName,
            @RequestParam("file") MultipartFile file) {
        
        log.info("上传文件到存储桶: {}，文件名: {}", bucketName, file.getOriginalFilename());
        String objectName = file.getOriginalFilename();
        return minioService.uploadFile(bucketName, objectName, file);
    }

    /**
     * 上传文件到指定存储桶，使用格式化文件名（编号-名称-日期-时间戳）
     * 
     * @param bucketName 存储桶名称
     * @param number 编号
     * @param name 名称
     * @param file 文件
     * @return Result结果
     */
    @PostMapping("/buckets/{bucketName}/files/upload-formatted")
    public Result<Map<String, Object>> uploadFileWithFormattedName(
            @PathVariable String bucketName,
            @RequestParam("number") String number,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {
        
        log.info("格式化文件名上传文件到存储桶: {}，编号: {}，名称: {}，文件名: {}", 
                bucketName, number, name, file.getOriginalFilename());
        return minioService.uploadFileWithFormattedName(bucketName, number, name, file);
    }

    /**
     * 上传文件到指定存储桶，使用时间戳格式化文件名
     * 
     * @param bucketName 存储桶名称
     * @param name 名称
     * @param file 文件
     * @return Result结果
     */
    @PostMapping("/buckets/{bucketName}/files/upload-timestamp")
    public Result<Map<String, Object>> uploadFileWithTimeBasedName(
            @PathVariable String bucketName,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {
        
        log.info("时间戳文件名上传文件到存储桶: {}，名称: {}，文件名: {}", 
                bucketName, name, file.getOriginalFilename());
        return minioService.uploadFileWithTimeBasedName(bucketName, name, file);
    }

    /**
     * 下载文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return ResponseEntity文件流
     */
    @GetMapping("/buckets/{bucketName}/files/{objectName:.+}")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable String bucketName,
            @PathVariable String objectName) {
        
        log.info("下载文件: {}/{}", bucketName, objectName);
        
        Result<InputStream> result = minioService.downloadFile(bucketName, objectName);
        
        if (result.getCode() != 200 || result.getData() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + objectName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(result.getData()));
    }

    /**
     * 获取文件信息
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return Result结果
     */
    @GetMapping("/buckets/{bucketName}/files/{objectName:.+}/info")
    public Result<Map<String, Object>> getFileInfo(
            @PathVariable String bucketName,
            @PathVariable String objectName) {
        
        log.info("获取文件信息: {}/{}", bucketName, objectName);
        return minioService.getFileInfo(bucketName, objectName);
    }

    /**
     * 删除文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return Result结果
     */
    @DeleteMapping("/buckets/{bucketName}/files/{objectName:.+}")
    public Result<Map<String, Object>> deleteFile(
            @PathVariable String bucketName,
            @PathVariable String objectName) {
        
        log.info("删除文件: {}/{}", bucketName, objectName);
        return minioService.deleteFile(bucketName, objectName);
    }

    /**
     * 获取存储桶中的文件列表
     * 
     * @param bucketName 存储桶名称
     * @param prefix 前缀过滤（可选）
     * @param recursive 是否递归子目录（默认false）
     * @return Result结果
     */
    @GetMapping("/buckets/{bucketName}/files")
    public Result<List<FileInfo>> listFiles(
            @PathVariable String bucketName,
            @RequestParam(value = "prefix", required = false) String prefix,
            @RequestParam(value = "recursive", defaultValue = "false") boolean recursive) {
        
        log.info("获取文件列表: {}，前缀: {}，递归: {}", bucketName, prefix, recursive);
        return minioService.listFiles(bucketName, prefix, recursive);
    }

    /**
     * 生成文件下载预签名URL
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 过期时间（分钟，默认60分钟）
     * @return Result结果
     */
    @GetMapping("/buckets/{bucketName}/files/{objectName:.+}/presigned-url")
    public Result<String> generatePresignedDownloadUrl(
            @PathVariable String bucketName,
            @PathVariable String objectName,
            @RequestParam(value = "expiry", defaultValue = "60") int expiry) {
        
//        log.info("生成预签名下载URL: {}，过期时间: {}分钟", bucketName, expiry);
        return minioService.generatePresignedUrl(bucketName, objectName, expiry, io.minio.http.Method.GET);
    }

    /**
     * 生成文件上传预签名URL
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 过期时间（分钟，默认60分钟）
     * @return Result结果
     */
    @GetMapping("/buckets/{bucketName}/files/{objectName:.+}/presigned-upload")
    public Result<String> generatePresignedUploadUrl(
            @PathVariable String bucketName,
            @PathVariable String objectName,
            @RequestParam(value = "expiry", defaultValue = "60") int expiry) {
        
        log.info("生成预签名上传URL: {}，过期时间: {}分钟", bucketName, expiry);
        return minioService.generatePresignedUrl(bucketName, objectName, expiry, io.minio.http.Method.PUT);
    }

    /**
     * 创建预上传任务，使用普通上传
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param fileSize 文件大小（字节）
     * @return Result结果，包含上传任务信息
     */
    @PostMapping("/buckets/{bucketName}/files/{objectName:.+}/presigned-upload")
    public Result<Map<String, Object>> createPresignedUploadTask(
            @PathVariable String bucketName,
            @PathVariable String objectName,
            @RequestParam("fileSize") long fileSize) {
        
        log.info("创建预上传任务: {}/{}，文件大小: {}字节", 
                bucketName, objectName, fileSize);
        return minioService.createPresignedUploadTask(bucketName, objectName, fileSize);
    }

    /**
     * 创建格式化文件名预上传任务
     * @param bucketName 存储桶名称
     * @param code 编号
     * @param name 名称
     * @param originalFileName 原始文件名
     * @param fileSize 文件大小（字节）
     * @return Result结果，包含上传任务信息
     */
    @PostMapping("/buckets/{bucketName}/files/formatted-presigned-upload")
    public Result<Map<String, Object>> createFormattedPresignedUploadTask(
            @PathVariable String bucketName,
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam("originalFileName") String originalFileName,
            @RequestParam("fileSize") long fileSize) {
        
        log.info("创建格式化文件名预上传任务: {}，编号: {}，名称: {}，原始文件名: {}，文件大小: {}字节", 
                bucketName, code, name, originalFileName, fileSize);
        return minioService.createFormattedPresignedUploadTask(bucketName, code, name, originalFileName, fileSize);
    }




    /**
     * 创建格式化文件名预上传任务
     * 成品图纸独有的创建格式化文件名预上传任务
     *
     *
     * @param bucketName 存储桶名称
     * @param code 编号
     * @param name 名称
     * @param originalFileName 原始文件名
     * @param fileSize 文件大小（字节）
     * @return Result结果，包含上传任务信息
     */
    @PostMapping("/buckets/{bucketName}/files/formatted-presigned-upload-for-doc-prod-drawing")
    public Result<Map<String, Object>> createFormattedPresignedUploadTaskForDocProdDrawing(
            @PathVariable String bucketName,
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam("originalFileName") String originalFileName,
            @RequestParam("fileSize") long fileSize) {

        log.info("创建格式化文件名预上传任务: {}，编号: {}，名称: {}，原始文件名: {}，文件大小: {}字节",
                bucketName, code, name, originalFileName, fileSize);
        return minioService.createFormattedPresignedUploadTaskForDocProdDrawing(bucketName, code, name, originalFileName, fileSize);
    }



    /**
     * 保存文件信息到数据库（客户端直传后调用）
     * 
     * @param bucketName 存储桶名称
     * @param fileInfo 文件信息
     * @return Result结果
     */
    @PostMapping("/buckets/{bucketName}/files/save-info")
    public Result<Map<String, Object>> saveFileInfo(
            @PathVariable String bucketName,
            @RequestBody Map<String, Object> fileInfo) {
        
        log.info("保存文件信息到数据库: {}，文件名: {}", bucketName, fileInfo.get("objectName"));
        
        try {
            FileInfo file = new FileInfo();
            String originalName = (String) fileInfo.get("originalName");
            String objectName = (String) fileInfo.get("objectName");
            
            file.setFileName(objectName);
            file.setOriginalName(originalName);
            long fileSize = Long.parseLong(fileInfo.get("fileSize").toString());
            file.setFileSize(fileSize);

            // 提取文件后缀
            if (originalName != null && originalName.contains(".")) {
                file.setFileSuffix(originalName.substring(originalName.lastIndexOf(".")));
            } else {
                file.setFileSuffix(""); // 设置空后缀避免NOT NULL约束
            }
            
            // 构建文件URL
            String fileUrl = String.format("/minio/buckets/%s/files/%s", bucketName, objectName);
            file.setFileUrl(fileUrl);
            file.setStatus(1); // 启用状态
            file.setCreateTime(LocalDateTime.now());
            
            boolean saved = fileInfoService.save(file);
            
            if (saved) {
                Map<String, Object> data = new HashMap<>();
                data.put("fileId", file.getId());
                data.put("fileUrl", fileUrl);
                data.put("fileName", file.getFileName());
                data.put("originalName", file.getOriginalName());
                
                log.info("文件信息保存成功，文件ID: {}", file.getId());
                return ResultUtil.success(data);
            } else {
                log.error("文件信息保存失败");
                return ResultUtil.fail("文件信息保存失败");
            }
            
        } catch (Exception e) {
            log.error("保存文件信息异常", e);
            return ResultUtil.fail("保存文件信息失败: " + e.getMessage());
        }
    }
}