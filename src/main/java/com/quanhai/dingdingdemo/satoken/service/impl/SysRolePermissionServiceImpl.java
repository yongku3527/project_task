package com.quanhai.dingdingdemo.satoken.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.satoken.mapper.SysRolePermissionMapper;
import com.quanhai.dingdingdemo.satoken.model.SysRolePermission;
import com.quanhai.dingdingdemo.satoken.service.SysRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 角色-权限关联Service实现类
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {
}