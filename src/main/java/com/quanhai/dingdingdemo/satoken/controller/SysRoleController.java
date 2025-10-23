package com.quanhai.dingdingdemo.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.quanhai.dingdingdemo.satoken.model.SysRole;
import com.quanhai.dingdingdemo.satoken.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/sys/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 获取角色列表
     */
    @GetMapping("/list")
    @SaCheckLogin
    public IPage<SysRole> list(@RequestParam(defaultValue = "1") Integer current,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) String roleName) {
        Page<SysRole> page = new Page<>(current, size);
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        if (roleName != null && !roleName.trim().isEmpty()) {
            queryWrapper.like("role_name", roleName);
        }
        return sysRoleService.page(page, queryWrapper);
    }

    /**
     * 根据ID获取角色信息
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    public SysRole getById(@PathVariable Long id) {
        return sysRoleService.getById(id);
    }

    /**
     * 新增角色
     */
    @PostMapping
    @SaCheckPermission("role:add")
    public boolean add(@RequestBody SysRole sysRole) {
        return sysRoleService.save(sysRole);
    }

    /**
     * 修改角色
     */
    @PutMapping
    @SaCheckPermission("role:update")
    public boolean update(@RequestBody SysRole sysRole) {
        return sysRoleService.updateById(sysRole);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("role:delete")
    public boolean delete(@PathVariable Long id) {
        return sysRoleService.removeById(id);
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("role:delete")
    public boolean deleteBatch(@RequestBody List<Long> ids) {
        return sysRoleService.removeByIds(ids);
    }
}