package com.quanhai.dingdingdemo.file.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件名格式化工具类
 * 用于生成指定格式的文件名
 */
public class FileNameFormatter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /**
     * 生成格式化文件名
     * 格式：编号~名称~日期~时间戳.扩展名
     * 
     * @param number 编号
     * @param name 名称
     * @param originalFilename 原始文件名（用于获取扩展名）
     * @return 格式化后的文件名
     */
    public static String generateFormattedFileName(String number, String name, String originalFilename) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DATE_FORMATTER);
        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 构建新文件名
        return String.format("%s~%s~%s%s",
                sanitizeFileName(number), 
                sanitizeFileName(name), 
                date,
                extension);
    }

    /**
     * 生成格式化文件名 专为成品图纸使用
     * 格式：编号~日期.扩展名
     *
     * @param number 编号
     * @param name 名称
     * @param originalFilename 原始文件名（用于获取扩展名）
     * @return 格式化后的文件名
     */
    public static String generateFormattedFileNameForDocProdDrawing(String number, String name, String originalFilename) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DATE_FORMATTER);
        // 生成4位短随机码，确保同批次上传文件名不重复
        String shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 4);

        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 构建新文件名
        return String.format("%s~%s~%s%s",
                sanitizeFileName(number),
                date,
                shortCode,
                extension);

    }
    
    /**
     * 生成格式化文件名（使用UUID作为编号）
     * 格式：UUID-名称-日期-时间戳.扩展名
     * 
     * @param name 名称
     * @param originalFilename 原始文件名
     * @return 格式化后的文件名
     */
    public static String generateFormattedFileName(String name, String originalFilename) {
        return generateFormattedFileName(UUID.randomUUID().toString().replace("-", "").substring(0, 8), 
                                       name, originalFilename);
    }
    
    /**
     * 生成格式化文件名（使用当前时间作为编号）
     * 格式：时间戳-名称-日期.扩展名
     * 
     * @param name 名称
     * @param originalFilename 原始文件名
     * @return 格式化后的文件名
     */
    public static String generateTimeBasedFileName(String name, String originalFilename) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);
        
        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        return String.format("%s-%s%s", 
                timestamp, 
                sanitizeFileName(name), 
                extension);
    }
    
    /**
     * 清理文件名中的特殊字符
     * 
     * @param fileName 文件名
     * @return 清理后的文件名
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "unknown";
        }
        // 替换路径分隔符、通配符和URL敏感字符
        return fileName.replaceAll("[/\\\\* #%&+]", "_")
                      .trim();
    }
    
    /**
     * 从格式化文件名中提取信息
     * 
     * @param formattedFileName 格式化的文件名
     * @return 文件信息数组 [编号, 名称, 日期, 时间戳]
     */
    public static String[] extractFileInfo(String formattedFileName) {
        if (formattedFileName == null || !formattedFileName.contains("~")) {
            return new String[]{formattedFileName, "", "", ""};
        }
        
        // 移除扩展名
        String nameWithoutExt = formattedFileName;
        if (formattedFileName.contains(".")) {
            nameWithoutExt = formattedFileName.substring(0, formattedFileName.lastIndexOf("."));
        }
        
        String[] parts = nameWithoutExt.split("~");
        if (parts.length >= 4) {
            return new String[]{parts[0], parts[1], parts[2], parts[3]};
        }
        
        return new String[]{formattedFileName, "", "", ""};
    }


}