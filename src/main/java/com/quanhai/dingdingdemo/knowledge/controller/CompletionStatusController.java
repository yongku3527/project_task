package com.quanhai.dingdingdemo.knowledge.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.quanhai.dingdingdemo.knowledge.model.pojo.CompletionStatus;
import com.quanhai.dingdingdemo.knowledge.service.CompletionStatusService;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 完成状态Controller
 */
@RestController
@RequestMapping("/completion-status")
public class CompletionStatusController {

    @Autowired
    private CompletionStatusService completionStatusService;

    /**
     * 获取当前用户的完成状态列表
     */
    @GetMapping("/list")
    public Result list() {
        try {
            // 获取当前登录用户ID
            String userId = StpUtil.getLoginId().toString();
            List<CompletionStatus> list = completionStatusService.getListByUserId(userId);
            return ResultUtil.success(list);
        } catch (Exception e) {
            return ResultUtil.fail("获取完成状态列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据经验信息ID获取当前用户的完成状态
     */
    @GetMapping("/info/{knowledgeInfoId}")
    public Result getByKnowledgeInfoId(@PathVariable Long knowledgeInfoId) {
        try {
            // 获取当前登录用户ID
            String userId = StpUtil.getLoginId().toString();
            CompletionStatus data = completionStatusService.getByUserIdAndKnowledgeInfoId(userId, knowledgeInfoId);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取完成状态失败: " + e.getMessage());
        }
    }

    /**
     * 根据完成状态获取当前用户的完成状态列表
     */
    @GetMapping("/list-by-status/{status}")
    public Result listByStatus(@PathVariable Integer status) {
        try {
            // 获取当前登录用户ID
            String userId = StpUtil.getLoginId().toString();
            List<CompletionStatus> list = completionStatusService.getListByUserIdAndStatus(userId, status);
            return ResultUtil.success(list);
        } catch (Exception e) {
            return ResultUtil.fail("获取完成状态列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新或创建完成状态
     */
    @PostMapping("/update-status")
    public Result updateStatus(@RequestParam Long knowledgeInfoId, @RequestParam Integer completionStatus) {
        try {
            // 获取当前登录用户ID
            String userId = StpUtil.getLoginId().toString();
            boolean success = completionStatusService.updateOrCreateStatus(userId, knowledgeInfoId, completionStatus);
            if (success) {
                return ResultUtil.success("更新完成状态成功");
            } else {
                return ResultUtil.fail("更新完成状态失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("更新完成状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除完成状态
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        try {
            boolean success = completionStatusService.removeById(id);
            if (success) {
                return ResultUtil.success("删除成功");
            } else {
                return ResultUtil.fail("删除失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("删除完成状态失败: " + e.getMessage());
        }
    }
}