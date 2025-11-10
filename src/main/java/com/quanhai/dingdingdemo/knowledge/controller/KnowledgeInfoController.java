package com.quanhai.dingdingdemo.knowledge.controller;

import com.quanhai.dingdingdemo.knowledge.model.pojo.KnowledgeInfo;
import com.quanhai.dingdingdemo.knowledge.model.vo.KnowledgeInfoVo;
import com.quanhai.dingdingdemo.knowledge.service.KnowledgeInfoService;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 经验库Controller
 */
@RestController
@RequestMapping("/knowledge-info")
public class KnowledgeInfoController {

    @Autowired
    private KnowledgeInfoService knowledgeInfoService;

    /**
     * 获取经验库列表（基于用户角色权限）
     */
    @GetMapping("/list")
    public Result list() {
        try {
            List<KnowledgeInfoVo> list = knowledgeInfoService.getListByRole();
            return ResultUtil.success(list);
        } catch (Exception e) {
            return ResultUtil.fail("获取经验库列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询经验库信息
     */
    @GetMapping("/info/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            KnowledgeInfoVo data = knowledgeInfoService.getById(id);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取经验库详情失败: " + e.getMessage());
        }
    }

    /**
     * 新增经验库信息
     */
    @PostMapping("/save")
    public Result save(@RequestBody KnowledgeInfo knowledgeInfo) {
        try {
            boolean success = knowledgeInfoService.save(knowledgeInfo);
            if (success) {
                return ResultUtil.success("新增成功");
            } else {
                return ResultUtil.fail("新增失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("新增经验库失败: " + e.getMessage());
        }
    }

    /**
     * 更新经验库信息
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody KnowledgeInfo knowledgeInfo) {
        try {
            boolean success = knowledgeInfoService.updateById(knowledgeInfo);
            if (success) {
                return ResultUtil.success("更新成功");
            } else {
                return ResultUtil.fail("更新失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("更新经验库失败: " + e.getMessage());
        }
    }

    /**
     * 删除经验库信息
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        try {
            boolean success = knowledgeInfoService.removeById(id);
            if (success) {
                return ResultUtil.success("删除成功");
            } else {
                return ResultUtil.fail("删除失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("删除经验库失败: " + e.getMessage());
        }
    }

    /**
     * 获取完整经验库列表（包含文件信息和完成状态）
     */
    @GetMapping("/complete-list")
    public Result getCompleteList() {
        try {
            List<KnowledgeInfoVo> list = knowledgeInfoService.getCompleteListByRole();
            return ResultUtil.success(list);
        } catch (Exception e) {
            return ResultUtil.fail("获取完整经验库列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取完整经验库信息（包含文件信息和完成状态）
     */
    @GetMapping("/complete-info/{id}")
    public Result getCompleteById(@PathVariable Long id) {
        try {
            KnowledgeInfoVo data = knowledgeInfoService.getCompleteById(id);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取完整经验库详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据完成状态获取完整经验库列表
     */
    @GetMapping("/complete-list-by-status/{completionStatus}")
    public Result getCompleteListByStatus(@PathVariable Integer completionStatus) {
        try {
            List<KnowledgeInfoVo> list = knowledgeInfoService.getCompleteListByStatus(completionStatus);
            return ResultUtil.success(list);
        } catch (Exception e) {
            return ResultUtil.fail("根据完成状态获取完整经验库列表失败: " + e.getMessage());
        }
    }
}