package com.quanhai.dingdingdemo.satoken.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.satoken.model.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联Mapper接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}