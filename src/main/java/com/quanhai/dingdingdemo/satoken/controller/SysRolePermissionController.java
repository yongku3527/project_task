package com.quanhai.dingdingdemo.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.quanhai.dingdingdemo.satoken.model.SysRolePermission;
import com.quanhai.dingdingdemo.satoken.service.SysRolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色-权限关联控制器
 */
@RestController
@RequestMapping("/sys/rolePermission")
public class SysRolePermissionController {

    @Autowired
    private SysRolePermissionService sysRolePermissionService;

    /**
     * 获取角色的权限列表
     */
    @GetMapping("/role/{roleId}")
    @SaCheckLogin
    public List<SysRolePermission> getByRoleId(@PathVariable Long roleId) {
        QueryWrapper<SysRolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return sysRolePermissionService.list(queryWrapper);
    }

    /**
     * 获取权限的角色列表
     */
    @GetMapping("/permission/{permissionId}")
    @SaCheckLogin
    public List<SysRolePermission> getByPermissionId(@PathVariable Long permissionId) {
        QueryWrapper<SysRolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("permission_id", permissionId);
        return sysRolePermissionService.list(queryWrapper);
    }

    /**
     * 分配角色权限
     */
    @PostMapping
    @SaCheckPermission("role:permission")
    public boolean assign(@RequestBody SysRolePermission sysRolePermission) {
        // 先删除角色原有权限
        QueryWrapper<SysRolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", sysRolePermission.getRoleId());
        sysRolePermissionService.remove(queryWrapper);
        
        // 添加新权限
        return sysRolePermissionService.save(sysRolePermission);
    }

    /**
     * 批量分配角色权限
     */
    @PostMapping("/batch")
    @SaCheckPermission("role:permission")
    public boolean assignBatch(@RequestBody List<SysRolePermission> rolePermissions) {
        if (rolePermissions.isEmpty()) {
            return true;
        }
        
        Long roleId = rolePermissions.get(0).getRoleId();
        
        // 先删除角色原有权限
        QueryWrapper<SysRolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        sysRolePermissionService.remove(queryWrapper);
        
        // 批量添加新权限
        return sysRolePermissionService.saveBatch(rolePermissions);
    }

    /**
     * 取消角色权限
     */
    @DeleteMapping("/role/{roleId}/permission/{permissionId}")
    @SaCheckPermission("role:permission")
    public boolean cancel(@PathVariable Long roleId, @PathVariable Long permissionId) {
        QueryWrapper<SysRolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId).eq("permission_id", permissionId);
        return sysRolePermissionService.remove(queryWrapper);
    }
}