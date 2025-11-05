package com.quanhai.dingdingdemo.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.file.service.FileInfoService;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件信息管理控制器
 * 提供从数据库获取文件信息的接口
 */
@Slf4j
@RestController
@RequestMapping("/file-info")

public class FileInfoController {

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * 获取文件列表（从数据库）
     */
    @GetMapping("/list")

    public Result<Map<String, Object>> getFileList(
  @RequestParam(value = "page", defaultValue = "1") Integer page,
 @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
 @RequestParam(value = "keyword", required = false) String keyword,
 @RequestParam(value = "suffix", required = false) String suffix) {
        

        
        try {
            // 构建查询条件
            QueryWrapper<FileInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1); // 只查询启用的文件
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.like("file_name", keyword.trim());
            }
            
            if (suffix != null && !suffix.trim().isEmpty()) {
                queryWrapper.eq("file_suffix", suffix.trim());
            }
            
            // 按创建时间降序排列
            queryWrapper.orderByDesc("create_time");
            
            // 执行分页查询
            Page<FileInfo> pageResult = fileInfoService.page(new Page<>(page, pageSize), queryWrapper);
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("list", pageResult.getRecords());
            data.put("total", pageResult.getTotal());
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("pages", pageResult.getPages());
            

            return ResultUtil.success(data);
            
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return ResultUtil.fail("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取文件信息
     */
    @GetMapping("/{id}")
    public Result<FileInfo> getFileInfoById(@PathVariable Long id) {

        
        try {
            FileInfo fileInfo = fileInfoService.getById(id);
            if (fileInfo == null) {
                return ResultUtil.fail("文件不存在");
            }
            
            return ResultUtil.success(fileInfo);
            
        } catch (Exception e) {
            log.error("获取文件信息失败 - ID: {}", id, e);
            return ResultUtil.fail("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件信息（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteFileInfo(@PathVariable Long id) {
        log.info("删除文件信息 - ID: {}", id);
        
        try {
            FileInfo fileInfo = fileInfoService.getById(id);
            if (fileInfo == null) {
                return ResultUtil.fail("文件不存在");
            }
            
            // 物理删除，直接从数据库中删除记录
            boolean result = fileInfoService.removeById(id);
            
            if (result) {
                log.info("删除文件信息成功 - ID: {}", id);
                return ResultUtil.success("删除成功");
            } else {
                return ResultUtil.fail("删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除文件信息失败 - ID: {}", id, e);
            return ResultUtil.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件统计信息
     */
    @GetMapping("/statistics")

    public Result<Map<String, Object>> getFileStatistics() {

        
        try {
            // 总文件数
            long totalFiles = fileInfoService.count(new QueryWrapper<FileInfo>().eq("status", 1));
            
            // 今日新增文件数
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            long todayFiles = fileInfoService.count(new QueryWrapper<FileInfo>()
                    .eq("status", 1)
                    .ge("create_time", todayStart));
            
            // 按文件类型统计
            List<Map<String, Object>> typeStatistics = fileInfoService.listMaps(
                    new QueryWrapper<FileInfo>()
                            .select("file_suffix as type", "count(*) as count")
                            .eq("status", 1)
                            .groupBy("file_suffix")
                            .orderByDesc("count")
                            .last("limit 10"));
            
            Map<String, Object> data = new HashMap<>();
            data.put("totalFiles", totalFiles);
            data.put("todayFiles", todayFiles);
            data.put("typeStatistics", typeStatistics);
            

            return ResultUtil.success(data);
            
        } catch (Exception e) {
            log.error("获取文件统计信息失败", e);
            return ResultUtil.fail("获取统计信息失败: " + e.getMessage());
        }
    }
}