package com.quanhai.dingdingdemo.file.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件实体类（不继承base类）
 */
@Data
@TableName("file_info")
public class FileInfo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;
    
    /**
     * 原名
     */
    @TableField("original_name")
    private String originalName;
    
    /**
     * 后缀名
     */
    @TableField("file_suffix")
    private String fileSuffix;
    
    /**
     * URL地址
     */
    @TableField("file_url")
    private String fileUrl;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;
}