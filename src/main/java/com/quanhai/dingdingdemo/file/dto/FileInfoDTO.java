package com.quanhai.dingdingdemo.file.dto;

import lombok.Data;

/**
 * 文件信息DTO
 */
@Data
public class FileInfoDTO {

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件后缀
     */
    private String fileSuffix;
}
