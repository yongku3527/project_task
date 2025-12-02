package com.quanhai.dingdingdemo.file.controller;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.file.dto.SpecificationDTO;
import com.quanhai.dingdingdemo.file.model.Specification;
import com.quanhai.dingdingdemo.file.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 规格书Controller
 */
@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 获取规格书列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String materialId,
                      @RequestParam(required = false) String drawingType,
                      @RequestParam(required = false) String itemName,
                      @RequestParam(required = false) String model) {
        try {
            List<SpecificationDTO> list = specificationService.getAllSpecificationsWithFiles();
            
            // 如果有筛选条件，进行过滤
            if (materialId != null && !materialId.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getMaterialId() != null && item.getMaterialId().contains(materialId)).collect(java.util.stream.Collectors.toList());
            }
            if (drawingType != null && !drawingType.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getDrawingType() != null && item.getDrawingType().contains(drawingType)).collect(java.util.stream.Collectors.toList());
            }
            if (itemName != null && !itemName.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getItemName() != null && item.getItemName().contains(itemName)).collect(java.util.stream.Collectors.toList());
            }
            if (model != null && !model.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getModel() != null && item.getModel().contains(model)).collect(java.util.stream.Collectors.toList());
            }
            
            // 分页处理
            int total = list.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            List<SpecificationDTO> pageList = list.subList(start, end);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", pageList);
            data.put("total", total);
            data.put("size", size);
            data.put("current", page);
            data.put("pages", (int) Math.ceil((double) total / size));
            
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取规格书列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询规格书及其文件信息
     */
    @GetMapping("/info/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            SpecificationDTO data = specificationService.getSpecificationWithFiles(id);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取规格书详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据原材料ID查询规格书列表
     */
    @GetMapping("/list-by-materialId/{materialId}")
    public Result getByMaterialId(@PathVariable String materialId) {
        try {
            List<SpecificationDTO> data = specificationService.getSpecificationsByMaterialId(materialId);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("根据原材料ID查询规格书列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有规格书及其文件信息
     */
    @GetMapping("/list-with-files")
    public Result listWithFiles() {
        try {
            List<SpecificationDTO> data = specificationService.getAllSpecificationsWithFiles();
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取所有规格书及其文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 新增规格书
     */
    @PostMapping("/save")
    public Result save(@RequestBody Specification specification) {
        try {
            boolean success = specificationService.save(specification);
            if (success) {
                return ResultUtil.success("新增规格书成功");
            } else {
                return ResultUtil.fail("新增规格书失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("新增规格书失败: " + e.getMessage());
        }
    }

    /**
     * 更新规格书
     */
    @PutMapping("/update")
    public Result update(@RequestBody Specification specification) {
        try {
            boolean success = specificationService.updateById(specification);
            if (success) {
                return ResultUtil.success("更新规格书成功");
            } else {
                return ResultUtil.fail("更新规格书失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("更新规格书失败: " + e.getMessage());
        }
    }

    /**
     * 删除规格书
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            boolean success = specificationService.removeById(id);
            if (success) {
                return ResultUtil.success("删除规格书成功");
            } else {
                return ResultUtil.fail("删除规格书失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("删除规格书失败: " + e.getMessage());
        }
    }
}