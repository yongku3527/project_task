package com.quanhai.dingdingdemo.satoken.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.satoken.mapper.SysUserRoleMapper;
import com.quanhai.dingdingdemo.satoken.model.SysUserRole;
import com.quanhai.dingdingdemo.satoken.service.SysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户-角色关联Service实现类
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
}