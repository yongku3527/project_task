package com.quanhai.dingdingdemo.satoken.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_permission")
public class SysPermission {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("perm_name")
    private String permName;

    @TableField("perm_code")
    private String permCode;

    @TableField("parent_id")
    private Long parentId;

    @TableField("perm_type")
    private Integer permType;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}