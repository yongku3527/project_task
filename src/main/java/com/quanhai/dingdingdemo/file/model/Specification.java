package com.quanhai.dingdingdemo.file.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_specification")
public class Specification {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 原材料id
     */
    @TableField("material_id")
    private String materialId;

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
     * 文件ID（多个文件用逗号分隔）
     */
    @TableField("file_id")
    private String fileId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 文件URL
     */
    @TableField(exist = false)
    private String fileUrl;

    /**
     * 文件名称
     */
    @TableField(exist = false)
    private String fileName;

    /**
     * 状态（0：停用，1：启用）
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
