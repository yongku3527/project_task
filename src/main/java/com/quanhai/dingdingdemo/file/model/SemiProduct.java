package com.quanhai.dingdingdemo.file.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 半成品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("semi_product")
public class SemiProduct extends BaseEntity {
    
    /**
     * 线路板主键（逻辑外键）
     */
    @TableField("circuit_board_id")
    private Long circuitBoardId;
    
    /**
     * 半成品编号
     */
    @TableField("semi_product_code")
    private String semiProductCode;
    
    /**
     * 半成品名称
     */
    @TableField("semi_product_name")
    private String semiProductName;
    
    /**
     * 原理图文件ID
     */
    @TableField("schematic_file_id")
    private Long schematicFileId;
    
    /**
     * 贴片图文件ID
     */
    @TableField("smt_file_id")
    private Long smtFileId;
    
    /**
     * 备注信息
     */
    @TableField("remarks")
    private String remarks;
}