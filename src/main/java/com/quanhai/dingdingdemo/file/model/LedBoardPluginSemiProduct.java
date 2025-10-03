package com.quanhai.dingdingdemo.file.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 灯板插件半成品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("led_board_plugin_semi_product")
public class LedBoardPluginSemiProduct extends BaseEntity {
    
    /**
     * 半成品主键（逻辑外键）
     */
    @TableField("semi_product_id")
    private Long semiProductId;
    
    /**
     * 灯板插件半成品编号
     */
    @TableField("led_board_plugin_code")
    private String ledBoardPluginCode;
    
    /**
     * 灯板插件半成品名称
     */
    @TableField("led_board_plugin_name")
    private String ledBoardPluginName;
    
    /**
     * 文件ID
     */
    @TableField("file_id")
    private Long fileId;
}