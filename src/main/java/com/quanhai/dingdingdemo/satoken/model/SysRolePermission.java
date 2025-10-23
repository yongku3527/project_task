package com.quanhai.dingdingdemo.satoken.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 角色-权限关联实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_role_permission")
public class SysRolePermission {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("role_id")
    private Long roleId;

    @TableField("permission_id")
    private Long permissionId;
}