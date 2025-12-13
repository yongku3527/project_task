package com.quanhai.dingdingdemo.file.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 线路板实体类
 */
@Data
@TableName("doc_prod_drawing")
@NoArgsConstructor
public class docProdDrawing  {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 成品编号
     */
    @TableField("pid")
    private String pid;

    /**
     * 文件类别
     */
    @TableField("drawing_type")
    private String drawingType;

    /**
     * 物料名称
     */
    @TableField("item_name")
    private String itemName;

    /**
     * 规格型号
     */
    @TableField("model")
    private String model;

    /**
     * dwg图纸文件ID
     */
    @TableField("dwg_file_id")
    private Long dwgFileId;


    /**
     * dwg文件URL
     */
    @TableField(exist = false)
    private String dwgFileUrl;

    /**
     * dwg文件名称
     */
    @TableField(exist = false)
    private String dwgFileName;

    /**
     * pdf图纸文件ID
     */
    @TableField("pdf_file_id")
    private Long pdfFileId;


    /**
     * pdf文件URL
     */
    @TableField(exist = false)
    private String pdfFileUrl;

    /**
     * pdf文件名称
     */
    @TableField(exist = false)
    private String pdfFileName;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}