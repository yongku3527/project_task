package com.quanhai.dingdingdemo.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.satoken.model.SysPermission;
import com.quanhai.dingdingdemo.satoken.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/sys/permission")
public class SysPermissionController {

    @Autowired
    private SysPermissionService sysPermissionService;

    /**
     * 获取权限列表
     */
    @GetMapping("/list")
    @SaCheckPermission("permission:view")
    public Result<IPage<SysPermission>> list(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String permName) {
        Page<SysPermission> page = new Page<>(current, size);
        QueryWrapper<SysPermission> queryWrapper = new QueryWrapper<>();
        if (permName != null && !permName.trim().isEmpty()) {
            queryWrapper.like("perm_name", permName);
        }
        return ResultUtil.success(sysPermissionService.page(page, queryWrapper));
    }

    /**
     * 获取权限树形结构
     */
    @GetMapping("/tree")
    @SaCheckPermission("permission:view")
    public Result<List<SysPermission>> tree() {
        QueryWrapper<SysPermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("parent_id", "id");
        return ResultUtil.success(sysPermissionService.list(queryWrapper));
    }

    /**
     * 根据ID获取权限信息
     */
    @GetMapping("/{id}")
    @SaCheckPermission("permission:view")
    public Result<SysPermission> getById(@PathVariable Long id) {
        return ResultUtil.success(sysPermissionService.getById(id));
    }

    /**
     * 新增权限
     */
    @PostMapping
    @SaCheckPermission("permission:add")
    public Result<Boolean> add(@RequestBody SysPermission sysPermission) {
        return ResultUtil.success(sysPermissionService.save(sysPermission));
    }

    /**
     * 修改权限
     */
    @PutMapping
    @SaCheckPermission("permission:update")
    public Result<Boolean> update(@RequestBody SysPermission sysPermission) {
        return ResultUtil.success(sysPermissionService.updateById(sysPermission));
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("permission:delete")
    public Result<Boolean> delete(@PathVariable Long id) {
        return ResultUtil.success(sysPermissionService.removeById(id));
    }

    /**
     * 批量删除权限
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("permission:delete")
    public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        return ResultUtil.success(sysPermissionService.removeByIds(ids));
    }
}