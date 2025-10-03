package com.quanhai.dingdingdemo.file.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 线路板实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("circuit_board")
public class CircuitBoard extends BaseEntity {
    
    /**
     * 线路板编号
     */
    @TableField("board_code")
    private String boardCode;
    
    /**
     * 线路板名称
     */
    @TableField("board_name")
    private String boardName;
    
    /**
     * 线路板文件ID
     */
    @TableField("file_id")
    private Long fileId;
}