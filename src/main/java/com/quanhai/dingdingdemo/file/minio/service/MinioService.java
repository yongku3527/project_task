package com.quanhai.dingdingdemo.file.minio.service;

import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.file.utils.FileNameFormatter;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultEnum;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    /**
     * 创建存储桶
     * 
     * @param bucketName 存储桶名称
     * @return Result结果
     */
    public Result<Map<String, Object>> createBucket(String bucketName) {
        try {
            // 检查存储桶是否已存在
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (exists) {
                return ResultUtil.fail("存储桶已存在: " + bucketName);
            }

            // 创建存储桶
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());

            log.info("存储桶创建成功: {}", bucketName);
            Map<String, Object> data = new HashMap<>();
            data.put("bucketName", bucketName);
            data.put("created", true);
            
            return ResultUtil.success(data);
            
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", bucketName, e);
            return ResultUtil.fail("创建存储桶失败: " + e.getMessage());
        }
    }

    /**
     * 删除存储桶
     * 
     * @param bucketName 存储桶名称
     * @return Result结果
     */
    public Result<Map<String, Object>> deleteBucket(String bucketName) {
        try {
            // 检查存储桶是否存在
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!exists) {
                return ResultUtil.fail("存储桶不存在: " + bucketName);
            }

            // 删除存储桶
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());

            log.info("存储桶删除成功: {}", bucketName);
            Map<String, Object> data = new HashMap<>();
            data.put("bucketName", bucketName);
            data.put("deleted", true);
            
            return ResultUtil.success(data);
            
        } catch (Exception e) {
            log.error("删除存储桶失败: {}", bucketName, e);
            return ResultUtil.fail("删除存储桶失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有存储桶列表
     * 
     * @return Result结果
     */
    public Result<List<Bucket>> listBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            log.info("获取存储桶列表成功，数量: {}", buckets.size());
            return ResultUtil.success(buckets);
            
        } catch (Exception e) {
            log.error("获取存储桶列表失败", e);
            return ResultUtil.fail("获取存储桶列表失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到指定存储桶
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param file 文件
     * @return Result结果
     */
    public Result<Map<String, Object>> uploadFile(String bucketName, String objectName, MultipartFile file) {
        try {
            // 检查存储桶是否存在
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!exists) {
                return ResultUtil.fail("存储桶不存在: " + bucketName);
            }

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("文件上传成功: {}/{}，原始文件名: {}", bucketName, objectName, file.getOriginalFilename());
            Map<String, Object> data = new HashMap<>();
            data.put("bucketName", bucketName);
            data.put("objectName", objectName);
            data.put("size", file.getSize());
            data.put("contentType", file.getContentType());
            data.put("originalName", file.getOriginalFilename());
            
            return ResultUtil.success(data);
            
        } catch (Exception e) {
            log.error("文件上传失败: {}/{}，原始文件名: {}", bucketName, objectName, file.getOriginalFilename(), e);
            return ResultUtil.fail("文件上传失败: " + e.getMessage());
        }
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
    public Result<Map<String, Object>> uploadFileWithFormattedName(String bucketName, String number, String name, MultipartFile file) {
        try {
            // 生成格式化文件名
            String formattedFileName = FileNameFormatter.generateFormattedFileName(number, name, file.getOriginalFilename());
            log.info("生成格式化文件名: {}，原始文件名: {}", formattedFileName, file.getOriginalFilename());
            
            // 使用格式化文件名上传
            return uploadFile(bucketName, formattedFileName, file);
            
        } catch (Exception e) {
            log.error("格式化文件名上传失败: {}，编号: {}，名称: {}，原始文件名: {}", bucketName, number, name, file.getOriginalFilename(), e);
            return ResultUtil.fail( "格式化文件名上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到指定存储桶，使用时间戳格式化文件名
     * 
     * @param bucketName 存储桶名称
     * @param name 名称
     * @param file 文件
     * @return Result结果
     */
    public Result<Map<String, Object>> uploadFileWithTimeBasedName(String bucketName, String name, MultipartFile file) {
        try {
            // 生成时间戳格式化文件名
            String formattedFileName = FileNameFormatter.generateTimeBasedFileName(name, file.getOriginalFilename());
            log.info("生成时间戳文件名: {}，原始文件名: {}", formattedFileName, file.getOriginalFilename());
            
            // 使用格式化文件名上传
            return uploadFile(bucketName, formattedFileName, file);
            
        } catch (Exception e) {
            log.error("时间戳文件名上传失败: {}，名称: {}，原始文件名: {}", bucketName, name, file.getOriginalFilename(), e);
            return ResultUtil.fail( "时间戳文件名上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return Result结果，包含文件流
     */
    public Result<InputStream> downloadFile(String bucketName, String objectName) {
        try {
            // 检查文件是否存在
            boolean exists = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()) != null;
            
            if (!exists) {
                return ResultUtil.fail("文件不存在: " + bucketName + "/" + objectName);
            }

            // 下载文件
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            log.info("文件下载成功: {}/{}", bucketName, objectName);
            return ResultUtil.success(stream);
            
        } catch (Exception e) {
            log.error("文件下载失败: {}/{}", bucketName, objectName, e);
            return ResultUtil.fail("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return Result结果，包含文件信息
     */
    public Result<Map<String, Object>> getFileInfo(String bucketName, String objectName) {
        try {
            // 获取文件信息
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            log.info("获取文件信息成功: {}/{}", bucketName, objectName);
            Map<String, Object> data = new HashMap<>();
            data.put("bucketName", bucketName);
            data.put("objectName", objectName);
            data.put("size", stat.size());
            data.put("contentType", stat.contentType());
            data.put("etag", stat.etag());
            data.put("lastModified", stat.lastModified());
            
            return ResultUtil.success(data);
            
        } catch (Exception e) {
            log.error("获取文件信息失败: {}/{}", bucketName, objectName, e);
            return ResultUtil.fail( "获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return Result结果
     */
    public Result<Map<String, Object>> deleteFile(String bucketName, String objectName) {
        try {
            // 删除文件
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            log.info("文件删除成功: {}/{}", bucketName, objectName);
            Map<String, Object> data = new HashMap<>();
            data.put("bucketName", bucketName);
            data.put("objectName", objectName);
            data.put("deleted", true);
            
            return ResultUtil.success(data);
            
        } catch (Exception e) {
            log.error("文件删除失败: {}/{}", bucketName, objectName, e);
            return ResultUtil.fail("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取存储桶中的文件列表
     * 
     * @param bucketName 存储桶名称
     * @param prefix 前缀过滤
     * @param recursive 是否递归子目录
     * @return Result结果，包含文件列表
     */
    public Result<List<FileInfo>> listFiles(String bucketName, String prefix, boolean recursive) {
        try {
            // 检查存储桶是否存在
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!exists) {
                return ResultUtil.fail("存储桶不存在: " + bucketName);
            }

            // 获取文件列表
            Iterable<io.minio.Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .recursive(recursive)
                    .build());

            List<FileInfo> fileInfos = new ArrayList<>();
            for (io.minio.Result<Item> result : results) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(result.get().objectName());
                fileInfo.setOriginalName(result.get().objectName());
                fileInfo.setFileSuffix(result.get().objectName().substring(result.get().objectName().lastIndexOf(".")));
                fileInfos.add(fileInfo);
            }

            log.info("获取文件列表成功: {}，数量: {}", bucketName, fileInfos.size());
            return ResultUtil.success(fileInfos);
            
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", bucketName, e);
            return ResultUtil.fail("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 生成预签名URL
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 过期时间（分钟）
     * @param method HTTP方法
     * @return Result结果，包含预签名URL
     */
    public Result<String> generatePresignedUrl(String bucketName, String objectName, int expiry, Method method) {
        try {
            // 生成预签名URL
            String presignedUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(method)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry, TimeUnit.MINUTES)
                    .build());

            log.info("生成预签名URL成功: {}/{}，方法: {}，过期时间: {}分钟", 
                    bucketName, objectName, method, expiry);
            return ResultUtil.success(presignedUrl);
            
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}/{}，方法: {}", bucketName, objectName, method, e);
            return ResultUtil.fail("生成预签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 创建预上传任务，使用普通上传
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param fileSize 文件大小
     * @return Result结果，包含上传任务信息
     */
    public Result<Map<String, Object>> createPresignedUploadTask(String bucketName, String objectName, 
            long fileSize) {
        try {
            // 检查存储桶是否存在，不存在则创建
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("自动创建存储桶: {}", bucketName);
            }

            // 生成普通上传的预签名URL
            String presignedUrl = generatePresignedUrl(bucketName, objectName, 60, Method.PUT).getData();
            
            Map<String, Object> data = new HashMap<>();
            data.put("uploadType", "simple");
            data.put("presignedUrl", presignedUrl);
            data.put("bucketName", bucketName);
            data.put("objectName", objectName);
            data.put("fileSize", fileSize);
            
            log.info("创建普通上传任务成功: {}/{}，文件大小: {}字节", bucketName, objectName, fileSize);
            return ResultUtil.success(data);
            
        } catch (Exception e) {
            log.error("创建预上传任务失败: {}/{}，文件大小: {}字节", bucketName, objectName, fileSize, e);
            return ResultUtil.fail("创建预上传任务失败: " + e.getMessage());
        }
    }

    /**
     * 创建格式化文件名的预上传任务
     * 
     * @param bucketName 存储桶名称
     * @param number 编号
     * @param name 名称
     * @param originalFilename 原始文件名
     * @param fileSize 文件大小
     * @return Result结果，包含上传任务信息
     */
    public Result<Map<String, Object>> createFormattedPresignedUploadTask(String bucketName, String number, 
            String name, String originalFilename, long fileSize) {
        try {
            // 生成格式化文件名
            String formattedObjectName = FileNameFormatter.generateFormattedFileName(number, name, originalFilename);
            
            // 创建预上传任务
            return createPresignedUploadTask(bucketName, formattedObjectName, fileSize);
            
        } catch (Exception e) {
            log.error("创建格式化文件名预上传任务失败: {}/{}/{}，文件大小: {}字节", 
                    bucketName, number, name, fileSize, e);
            return ResultUtil.fail("创建格式化文件名预上传任务失败: " + e.getMessage());
        }
    }


    public Result<Map<String, Object>> createFormattedPresignedUploadTaskForDocProdDrawing(String bucketName, String number, String name, String originalFilename, long fileSize) {

        try {
            // 生成格式化文件名
            String formattedObjectName = FileNameFormatter.generateFormattedFileNameForDocProdDrawing(number, name, originalFilename);

            // 创建预上传任务
            return createPresignedUploadTask(bucketName, formattedObjectName, fileSize);

        } catch (Exception e) {
            log.error("创建格式化文件名预上传任务失败: {}/{}/{}，文件大小: {}字节",
                    bucketName, number, name, fileSize, e);
            return ResultUtil.fail("创建格式化文件名预上传任务失败: " + e.getMessage());
        }

    }
}