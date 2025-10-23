package com.quanhai.dingdingdemo.satoken.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.satoken.mapper.SysRoleMapper;
import com.quanhai.dingdingdemo.satoken.model.SysRole;
import com.quanhai.dingdingdemo.satoken.service.SysRoleService;
import org.springframework.stereotype.Service;

/**
 * 角色Service实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
}