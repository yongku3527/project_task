package com.quanhai.dingdingdemo.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.satoken.model.SysUserRole;
import com.quanhai.dingdingdemo.satoken.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户-角色关联控制器
 */
@RestController
@RequestMapping("/sys/userRole")
public class SysUserRoleController {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/user/{userId}")
    @SaCheckPermission("user:role")
    public Result<List<SysUserRole>> getByUserId(@PathVariable Long userId) {
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return ResultUtil.success(sysUserRoleService.list(queryWrapper));
    }

    /**
     * 获取角色的用户列表
     */
    @GetMapping("/role/{roleId}")
    @SaCheckPermission("user:role")
    public Result<List<SysUserRole>> getByRoleId(@PathVariable Long roleId) {
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return ResultUtil.success(sysUserRoleService.list(queryWrapper));
    }

    /**
     * 分配用户角色
     */
    @PostMapping
    @SaCheckPermission("user:role")
    public Result<Boolean> assign(@RequestBody SysUserRole sysUserRole) {
        // 先删除用户原有角色
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", sysUserRole.getUserId());
        sysUserRoleService.remove(queryWrapper);
        
        // 添加新角色
        return ResultUtil.success(sysUserRoleService.save(sysUserRole));
    }

    /**
     * 批量分配用户角色
     */
    @PostMapping("/batch")
    @SaCheckPermission("user:role")
    public Result<Boolean> assignBatch(@RequestBody List<SysUserRole> userRoles) {
        if (userRoles.isEmpty()) {
            return ResultUtil.success(true);
        }
        
        Long userId = userRoles.get(0).getUserId();
        
        // 先删除用户原有角色
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        sysUserRoleService.remove(queryWrapper);
        
        // 批量添加新角色
        return ResultUtil.success(sysUserRoleService.saveBatch(userRoles));
    }

    /**
     * 取消用户角色
     */
    @DeleteMapping("/user/{userId}/role/{roleId}")
    @SaCheckPermission("user:role")
    public Result<Boolean> cancel(@PathVariable Long userId, @PathVariable Long roleId) {
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("role_id", roleId);
        return ResultUtil.success(sysUserRoleService.remove(queryWrapper));
    }
}