package com.quanhai.dingdingdemo.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.quanhai.dingdingdemo.satoken.model.SysUser;
import com.quanhai.dingdingdemo.satoken.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    @SaCheckLogin
    public IPage<SysUser> list(@RequestParam(defaultValue = "1") Integer current,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) String username) {
        Page<SysUser> page = new Page<>(current, size);
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like("username", username);
        }
        return sysUserService.page(page, queryWrapper);
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    public SysUser getById(@PathVariable Long id) {
        return sysUserService.getById(id);
    }

    /**
     * 新增用户
     */
    @PostMapping
    @SaCheckPermission("user:add")
    public boolean add(@RequestBody SysUser sysUser) {
        return sysUserService.save(sysUser);
    }

    /**
     * 修改用户
     */
    @PutMapping
    @SaCheckPermission("user:update")
    public boolean update(@RequestBody SysUser sysUser) {
        return sysUserService.updateById(sysUser);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("user:delete")
    public boolean delete(@PathVariable Long id) {
        return sysUserService.removeById(id);
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("user:delete")
    public boolean deleteBatch(@RequestBody List<Long> ids) {
        return sysUserService.removeByIds(ids);
    }
}