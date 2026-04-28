package com.quanhai.dingdingdemo.file.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规格书DTO
 */
@Data
public class SpecificationDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 原材料id
     */
    private String materialId;

    /**
     * 文件类别
     */
    private String drawingType;

    /**
     * 物料名称
     */
    private String itemName;

    /**
     * 规格型号
     */
    private String model;

    /**
     * 文件ID（多个文件用逗号分隔）
     */
    private String fileId;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件列表（用于前端展示多个文件）
     */
    private List<FileInfoDTO> fileList;

    /**
     * 状态（0：停用，1：启用）
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