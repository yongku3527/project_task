package com.quanhai.dingdingdemo.satoken.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.satoken.model.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-权限关联Mapper接口
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}