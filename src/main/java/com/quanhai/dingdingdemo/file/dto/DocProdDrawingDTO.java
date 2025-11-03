package com.quanhai.dingdingdemo.file.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 成品图纸DTO
 */
@Data
public class DocProdDrawingDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 成品编号
     */
    private String pid;

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
     * dwg文件URL
     */
    private String dwgFileUrl;

    /**
     * dwg文件名称
     */
    private String dwgFileName;

    /**
     * pdf文件URL
     */
    private String pdfFileUrl;

    /**
     * pdf文件名称
     */
    private String pdfFileName;

    /**
     * dwg图纸文件ID
     */
    private Long dwgFileId;

    /**
     * pdf图纸文件ID
     */
    private Long pdfFileId;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}