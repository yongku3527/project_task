package com.quanhai.dingdingdemo.satoken.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.satoken.mapper.SysPermissionMapper;
import com.quanhai.dingdingdemo.satoken.model.SysPermission;
import com.quanhai.dingdingdemo.satoken.service.SysPermissionService;
import org.springframework.stereotype.Service;

/**
 * 权限Service实现类
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {
}