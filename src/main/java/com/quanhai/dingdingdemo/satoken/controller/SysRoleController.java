package com.quanhai.dingdingdemo.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
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
    @SaCheckPermission("role:view")
    public Result<IPage<SysRole>> list(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String roleName) {

        Page<SysRole> page = new Page<>(current, size);
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        if (roleName != null && !roleName.trim().isEmpty()) {
            queryWrapper.like("role_name", roleName);
        }
        return ResultUtil.success(sysRoleService.page(page, queryWrapper));
    }

    /**
     * 根据ID获取角色信息
     */
    @GetMapping("/{id}")
    @SaCheckPermission("role:view")
    public Result<SysRole> getById(@PathVariable Long id) {
        return ResultUtil.success(sysRoleService.getById(id));
    }

    /**
     * 新增角色
     */
    @PostMapping
    @SaCheckPermission("role:add")
    public Result<Boolean> add(@RequestBody SysRole sysRole) {
        return ResultUtil.success(sysRoleService.save(sysRole));
    }

    /**
     * 修改角色
     */
    @PutMapping
    @SaCheckPermission("role:update")
    public Result<Boolean> update(@RequestBody SysRole sysRole) {
        return ResultUtil.success(sysRoleService.updateById(sysRole));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("role:delete")
    public Result<Boolean> delete(@PathVariable Long id) {
        return ResultUtil.success(sysRoleService.removeById(id));
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("role:delete")
    public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        return ResultUtil.success(sysRoleService.removeByIds(ids));
    }
}