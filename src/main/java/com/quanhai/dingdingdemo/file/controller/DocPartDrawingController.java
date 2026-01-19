package com.quanhai.dingdingdemo.file.controller;

import com.quanhai.dingdingdemo.file.dto.DocPartDrawingDTO;
import com.quanhai.dingdingdemo.file.model.DocPartDrawing;
import com.quanhai.dingdingdemo.file.service.DocPartDrawingService;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 零件图纸Controller
 */
@RestController
@RequestMapping("/doc-part-drawing")
public class DocPartDrawingController {

    @Autowired
    private DocPartDrawingService docPartDrawingService;

    /**
     * 获取零件图纸列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String partId,
                      @RequestParam(required = false) String drawingType,
                      @RequestParam(required = false) String itemName,
                      @RequestParam(required = false) String model) {
        try {
            List<DocPartDrawingDTO> list = docPartDrawingService.getAllDocPartDrawingsWithFiles();
            
            // 如果有筛选条件，进行过滤
            if (partId != null && !partId.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getPartId() != null && item.getPartId().contains(partId)).collect(java.util.stream.Collectors.toList());
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
            List<DocPartDrawingDTO> pageList = list.subList(start, end);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", pageList);
            data.put("total", total);
            data.put("size", size);
            data.put("current", page);
            data.put("pages", (int) Math.ceil((double) total / size));
            
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取零件图纸列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询零件图纸及其文件信息
     */
    @GetMapping("/info/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            DocPartDrawingDTO data = docPartDrawingService.getDocPartDrawingWithFiles(id);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取零件图纸详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据零件编号查询图纸列表
     */
    @GetMapping("/list-by-partId/{partId}")
    public Result getByPartId(@PathVariable String partId) {
        try {
            List<DocPartDrawingDTO> data = docPartDrawingService.getDocPartDrawingsByPartId(partId);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("根据零件编号查询图纸列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有零件图纸及其文件信息
     */
    @GetMapping("/list-with-files")
    public Result listWithFiles() {
        try {
            List<DocPartDrawingDTO> data = docPartDrawingService.getAllDocPartDrawingsWithFiles();
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取所有零件图纸及其文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 新增零件图纸
     */
    @PostMapping("/save")
    public Result save(@RequestBody DocPartDrawing docPartDrawing) {
        try {
            boolean success = docPartDrawingService.save(docPartDrawing);
            if (success) {
                return ResultUtil.success("新增成功");
            } else {
                return ResultUtil.fail("新增失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("新增零件图纸失败: " + e.getMessage());
        }
    }

    /**
     * 更新零件图纸
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody DocPartDrawing docPartDrawing) {
        try {
            boolean success = docPartDrawingService.updateById(docPartDrawing);
            if (success) {
                return ResultUtil.success("更新成功");
            } else {
                return ResultUtil.fail("更新失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("更新零件图纸失败: " + e.getMessage());
        }
    }

    /**
     * 删除零件图纸
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        try {
            boolean success = docPartDrawingService.removeById(id);
            if (success) {
                return ResultUtil.success("删除成功");
            } else {
                return ResultUtil.fail("删除失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("删除零件图纸失败: " + e.getMessage());
        }
    }

    
}

