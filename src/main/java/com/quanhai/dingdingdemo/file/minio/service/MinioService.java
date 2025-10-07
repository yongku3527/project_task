package com.example.mongodbpractice.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
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
     * @return 是否创建成功
     */
    public boolean createBucket(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("存储桶 '{}' 创建成功", bucketName);
                return true;
            } else {
                log.info("存储桶 '{}' 已存在", bucketName);
                return true;
            }
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 删除存储桶
     * 
     * @param bucketName 存储桶名称
     * @return 是否删除成功
     */
    public boolean deleteBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            log.info("存储桶 '{}' 删除成功", bucketName);
            return true;
        } catch (Exception e) {
            log.error("删除存储桶失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取所有存储桶列表
     * 
     * @return 存储桶列表
     */
    public List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("获取存储桶列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 上传文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件路径）
     * @param file 文件
     * @return 是否上传成功
     */
    public boolean uploadFile(String bucketName, String objectName, MultipartFile file) {
        try {
            createBucket(bucketName);
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            log.info("文件上传成功: {}/{}", bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 上传文件流
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param inputStream 输入流
     * @param contentType 内容类型
     * @param size 文件大小
     * @return 是否上传成功
     */
    public boolean uploadFile(String bucketName, String objectName, InputStream inputStream, 
                               String contentType, long size) {
        try {
            createBucket(bucketName);
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            );
            
            log.info("文件流上传成功: {}/{}", bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("文件流上传失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 下载文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取文件信息
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件状态信息
     */
    public StatObjectResponse getFileInfo(String bucketName, String objectName) {
        try {
            return minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 删除文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            log.info("文件删除成功: {}/{}", bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取存储桶中的文件列表
     * 
     * @param bucketName 存储桶名称
     * @param prefix 前缀过滤
     * @param recursive 是否递归
     * @return 文件列表
     */
    public List<Item> listFiles(String bucketName, String prefix, boolean recursive) {
        List<Item> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .recursive(recursive)
                    .build()
            );

            for (Result<Item> result : results) {
                files.add(result.get());
            }
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage());
        }
        return files;
    }

    /**
     * 生成预签名URL
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 过期时间（分钟）
     * @return 预签名URL
     */
    public String generatePresignedUrl(String bucketName, String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry, TimeUnit.MINUTES)
                    .build()
            );
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 复制文件
     * 
     * @param sourceBucket 源存储桶
     * @param sourceObject 源对象
     * @param targetBucket 目标存储桶
     * @param targetObject 目标对象
     * @return 是否复制成功
     */
    public boolean copyFile(String sourceBucket, String sourceObject, 
                             String targetBucket, String targetObject) {
        try {
            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .source(CopySource.builder()
                        .bucket(sourceBucket)
                        .object(sourceObject)
                        .build())
                    .bucket(targetBucket)
                    .object(targetObject)
                    .build()
            );
            log.info("文件复制成功: {}/{} -> {}/{}", 
                    sourceBucket, sourceObject, targetBucket, targetObject);
            return true;
        } catch (Exception e) {
            log.error("文件复制失败: {}", e.getMessage());
            return false;
        }
    }


    /**
     * 生成预签名上传URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 过期时间（分钟）
     * @return 预签名上传URL
     */
    public String generatePresignedUploadUrl(String bucketName, String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名上传URL失败: {}", e.getMessage());
            return null;
        }
    }


}