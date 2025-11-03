package com.quanhai.dingdingdemo.file.controller;

import com.quanhai.dingdingdemo.file.dto.DocProdDrawingDTO;
import com.quanhai.dingdingdemo.file.model.docProdDrawing;
import com.quanhai.dingdingdemo.file.service.DocProdDrawingService;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成品图纸Controller
 */
@RestController
@RequestMapping("/doc-prod-drawing")
public class DocProdDrawingController {

    @Autowired
    private DocProdDrawingService docProdDrawingService;

    /**
     * 获取成品图纸列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String pid,
                      @RequestParam(required = false) String drawingType,
                      @RequestParam(required = false) String itemName,
                      @RequestParam(required = false) String model) {
        try {
            List<DocProdDrawingDTO> list = docProdDrawingService.getAllDocProdDrawingsWithFiles();
            
            // 如果有筛选条件，进行过滤
            if (pid != null && !pid.trim().isEmpty()) {
                list = list.stream().filter(item -> item.getPid() != null && item.getPid().contains(pid)).collect(java.util.stream.Collectors.toList());
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
            List<DocProdDrawingDTO> pageList = list.subList(start, end);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", pageList);
            data.put("total", total);
            data.put("size", size);
            data.put("current", page);
            data.put("pages", (int) Math.ceil((double) total / size));
            
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取成品图纸列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询成品图纸及其文件信息
     */
    @GetMapping("/info/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            DocProdDrawingDTO data = docProdDrawingService.getDocProdDrawingWithFiles(id);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取成品图纸详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据成品编号查询图纸列表
     */
    @GetMapping("/list-by-pid/{pid}")
    public Result getByPid(@PathVariable String pid) {
        try {
            List<DocProdDrawingDTO> data = docProdDrawingService.getDocProdDrawingsByPid(pid);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("根据成品编号查询图纸列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有成品图纸及其文件信息
     */
    @GetMapping("/list-with-files")
    public Result listWithFiles() {
        try {
            List<DocProdDrawingDTO> data = docProdDrawingService.getAllDocProdDrawingsWithFiles();
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("获取所有成品图纸及其文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 新增成品图纸
     */
    @PostMapping("/save")
    public Result save(@RequestBody docProdDrawing docProdDrawing) {
        try {
            boolean success = docProdDrawingService.save(docProdDrawing);
            if (success) {
                return ResultUtil.success("新增成功");
            } else {
                return ResultUtil.fail("新增失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("新增成品图纸失败: " + e.getMessage());
        }
    }

    /**
     * 更新成品图纸
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody docProdDrawing docProdDrawing) {
        try {
            boolean success = docProdDrawingService.updateById(docProdDrawing);
            if (success) {
                return ResultUtil.success("更新成功");
            } else {
                return ResultUtil.fail("更新失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("更新成品图纸失败: " + e.getMessage());
        }
    }

    /**
     * 删除成品图纸
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        try {
            boolean success = docProdDrawingService.removeById(id);
            if (success) {
                return ResultUtil.success("删除成功");
            } else {
                return ResultUtil.fail("删除失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("删除成品图纸失败: " + e.getMessage());
        }
    }

    
}