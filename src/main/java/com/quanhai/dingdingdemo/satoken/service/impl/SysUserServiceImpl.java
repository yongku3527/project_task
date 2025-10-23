package com.quanhai.dingdingdemo.satoken.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.satoken.mapper.SysUserMapper;
import com.quanhai.dingdingdemo.satoken.model.SysUser;
import com.quanhai.dingdingdemo.satoken.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * 用户Service实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}