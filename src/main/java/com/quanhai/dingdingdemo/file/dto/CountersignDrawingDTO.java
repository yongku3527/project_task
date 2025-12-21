package com.quanhai.dingdingdemo.file.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会签图纸DTO
 */
@Data
public class CountersignDrawingDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 图纸来源
     */
    private String drawingSource;

    /**
     * 产品类别
     */
    private String productCategory;

    /**
     * 产品名称
     */
    private String prodName;

    /**
     * 零部件号
     */
    private String partNo;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 图纸文件ID
     */
    private Long dwgFileId;

    /**
     * 图纸文件URL
     */
    private String dwgFileUrl;

    /**
     * 图纸文件名称
     */
    private String dwgFileName;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}