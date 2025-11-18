package com.quanhai.dingdingdemo.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 半成品DTO，用于前端展示
 */
@Data
public class SemiProductDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 电路板ID
     */
    private Long circuitBoardId;

    /**
     * 电路板编码
     */
    private String circuitBoardCode;

    /**
     * 电路板名称
     */
    private String circuitBoardName;

    /**
     * 半成品编码
     */
    private String semiProductCode;

    /**
     * 半成品名称
     */
    private String semiProductName;

    /**
     * 原理图文件ID
     */
    private Long schematicFileId;

    /**
     * 原理图文件URL
     */
    private String schematicFileUrl;

    /**
     * 原理图文件名称
     */
    private String schematicFileName;

    /**
     * SMT文件ID
     */
    private Long smtFileId;

    /**
     * SMT文件URL
     */
    private String smtFileUrl;

    /**
     * SMT文件名称
     */
    private String smtFileName;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 灯板插件半成品DTO列表
     */
    private List<LedBoardPluginSemiProductDTO> ledBoardPluginSemiProductDTOList;

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
