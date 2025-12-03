package com.quanhai.dingdingdemo.file.controller;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.file.dto.CountersignDrawingDTO;
import com.quanhai.dingdingdemo.file.model.CountersignDrawing;
import com.quanhai.dingdingdemo.file.service.CountersignDrawingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会签图纸Controller
 */
@RestController
@RequestMapping("/countersign-drawing")
public class CountersignDrawingController {

    @Autowired
    private CountersignDrawingService countersignDrawingService;

    /**
     * 获取会签图纸列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String drawingSource,
                      @RequestParam(required = false) String productCategory,
                      @RequestParam(required = false) String prodName,
                      @RequestParam(required = false) String partNo) {
        try {
            List<CountersignDrawingDTO> list = countersignDrawingService.getAllCountersignDrawingsWithFiles();
            
            // 如果有筛选条件，进行过滤
            if (drawingSource != null && !drawingSource.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getDrawingSource() != null && item.getDrawingSource().contains(drawingSource)).collect(java.util.stream.Collectors.toList());
            }
            if (productCategory != null && !productCategory.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getProductCategory() != null && item.getProductCategory().contains(productCategory)).collect(java.util.stream.Collectors.toList());
            }
            if (prodName != null && !prodName.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getProdName() != null && item.getProdName().contains(prodName)).collect(java.util.stream.Collectors.toList());
            }
            if (partNo != null && !partNo.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getPartNo() != null && item.getPartNo().contains(partNo)).collect(java.util.stream.Collectors.toList());
            }
            
            // 分页处理
            int total = list.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            List<CountersignDrawingDTO> pageList = list.subList(start, end);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", pageList);
            data.put("total", total);
            data.put("size", size);
            data.put("current", page);
            data.put("pages", (int) Math.ceil((double) total / size));
            
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取会签图纸列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询会签图纸及其文件信息
     */
    @GetMapping("/info/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            CountersignDrawingDTO data = countersignDrawingService.getCountersignDrawingWithFiles(id);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取会签图纸详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据零部件号查询会签图纸列表
     */
    @GetMapping("/list-by-partNo/{partNo}")
    public Result getByPartNo(@PathVariable String partNo) {
        try {
            List<CountersignDrawingDTO> data = countersignDrawingService.getCountersignDrawingsByPartNo(partNo);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("根据零部件号查询会签图纸列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有会签图纸及其文件信息
     */
    @GetMapping("/list-with-files")
    public Result listWithFiles() {
        try {
            List<CountersignDrawingDTO> data = countersignDrawingService.getAllCountersignDrawingsWithFiles();
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取所有会签图纸及其文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 新增会签图纸
     */
    @PostMapping("/save")
    public Result save(@RequestBody CountersignDrawing countersignDrawing) {
        try {
            boolean success = countersignDrawingService.save(countersignDrawing);
            if (success) {
                return ResultUtil.success("新增会签图纸成功");
            } else {
                return ResultUtil.fail("新增会签图纸失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("新增会签图纸失败: " + e.getMessage());
        }
    }

    /**
     * 更新会签图纸
     */
    @PutMapping("/update")
    public Result update(@RequestBody CountersignDrawing countersignDrawing) {
        try {
            boolean success = countersignDrawingService.updateById(countersignDrawing);
            if (success) {
                return ResultUtil.success("更新会签图纸成功");
            } else {
                return ResultUtil.fail("更新会签图纸失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("更新会签图纸失败: " + e.getMessage());
        }
    }

    /**
     * 删除会签图纸
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            boolean success = countersignDrawingService.removeById(id);
            if (success) {
                return ResultUtil.success("删除会签图纸成功");
            } else {
                return ResultUtil.fail("删除会签图纸失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("删除会签图纸失败: " + e.getMessage());
        }
    }
}