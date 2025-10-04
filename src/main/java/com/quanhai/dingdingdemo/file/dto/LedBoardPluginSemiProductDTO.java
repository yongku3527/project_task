package com.quanhai.dingdingdemo.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 灯板插件半成品DTO，用于前端展示
 */
@Data
public class LedBoardPluginSemiProductDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 半成品ID
     */
    private Long semiProductId;

    /**
     * 半成品编码
     */
    private String semiProductCode;

    /**
     * 半成品名称
     */
    private String semiProductName;

    /**
     * 灯板插件编码
     */
    private String ledBoardPluginCode;

    /**
     * 灯板插件名称
     */
    private String ledBoardPluginName;

    /**
     * 文件ID
     */

    private Long fileId;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}