package com.quanhai.dingdingdemo.file.model;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_countersign_drawing")
public class CountersignDrawing {


    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 图纸来源
     */
    @TableField("drawing_source")
    private String DrawingSource;

    /**
     * 产品类别
     */
    @TableField("product_category")
    private String ProductCategory;

    /**
     * 产品名称
     */
    @TableField("prod_name")
    private String prodName;

    /**
     * 零部件号
     */
    @TableField("part_no")
    private String partNo;

    /**
     * 客户名称
     */
    @TableField("customer_name")
    private String customerName;

    /**
     * 图纸文件ID
     */
    @TableField("dwg_file_id")
    private Long dwgFileId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    /**
     * 图纸文件URL
     */
    @TableField(exist = false)
    private String dwgFileUrl;

    /**
     * 图纸文件名称
     */
    @TableField(exist = false)
    private String dwgFileName;

    /**
     * 状态（0：禁用，1：启用）
     */
    @TableField("status")
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
