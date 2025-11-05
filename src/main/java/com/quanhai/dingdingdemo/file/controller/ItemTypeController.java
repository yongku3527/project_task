package com.quanhai.dingdingdemo.file.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quanhai.dingdingdemo.file.model.ItemType;
import com.quanhai.dingdingdemo.file.service.ItemTypeService;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 物料类型Controller
 */
@RestController
@RequestMapping("/item-type")
public class ItemTypeController {

    @Autowired
    private ItemTypeService itemTypeService;

    /**
     * 分页查询物料类型列表
     * @param page 当前页码
     * @param size 每页大小
     * @param itemPrefix 物料前缀（可选）
     * @param typeName 类型名称（可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String itemPrefix,
            @RequestParam(required = false) String typeName
    ) {
        try {
            IPage<ItemType> result = itemTypeService.getItemTypePage(page, size, itemPrefix, typeName);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", result.getRecords());
            data.put("total", result.getTotal());
            data.put("size", result.getSize());
            data.put("current", result.getCurrent());
            data.put("pages", result.getPages());
            
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.fail("查询物料类型列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取物料类型详情
     * @param id 物料类型ID
     * @return 物料类型详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            ItemType itemType = itemTypeService.getById(id);
            if (itemType == null) {
                return ResultUtil.fail("未找到ID为 " + id + " 的物料类型");
            }
            return ResultUtil.success(itemType);
        } catch (Exception e) {
            return ResultUtil.fail("查询物料类型详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建物料类型
     * @param itemType 物料类型数据
     * @return 创建结果
     */
    @PostMapping
    public Result create(@RequestBody ItemType itemType) {
        try {
            // 检查物料前缀是否已存在
            ItemType existingType = itemTypeService.getByItemPrefix(itemType.getItemPrefix());
            if (existingType != null) {
                return ResultUtil.fail("物料前缀 " + itemType.getItemPrefix() + " 已存在");
            }
            
            boolean success = itemTypeService.save(itemType);
            if (success) {
                return ResultUtil.success(itemType);
            } else {
                return ResultUtil.fail("创建物料类型失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("创建物料类型失败: " + e.getMessage());
        }
    }

    /**
     * 更新物料类型
     * @param itemType 物料类型数据
     * @return 更新结果
     */
    @PutMapping
    public Result update(@RequestBody ItemType itemType) {
        try {
            // 检查物料类型是否存在
            ItemType existingType = itemTypeService.getById(itemType.getId());
            if (existingType == null) {
                return ResultUtil.fail("未找到ID为 " + itemType.getId() + " 的物料类型");
            }
            
            // 如果物料前缀有变化，检查新前缀是否已存在
            if (!existingType.getItemPrefix().equals(itemType.getItemPrefix())) {
                ItemType duplicateType = itemTypeService.getByItemPrefix(itemType.getItemPrefix());
                if (duplicateType != null && !duplicateType.getId().equals(itemType.getId())) {
                    return ResultUtil.fail("物料前缀 " + itemType.getItemPrefix() + " 已存在");
                }
            }
            
            boolean success = itemTypeService.updateById(itemType);
            if (success) {
                return ResultUtil.success(itemType);
            } else {
                return ResultUtil.fail("更新物料类型失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("更新物料类型失败: " + e.getMessage());
        }
    }

    /**
     * 删除物料类型
     * @param id 物料类型ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            // 检查物料类型是否存在
            ItemType itemType = itemTypeService.getById(id);
            if (itemType == null) {
                return ResultUtil.fail("未找到ID为 " + id + " 的物料类型");
            }
            
            boolean success = itemTypeService.removeById(id);
            if (success) {
                return ResultUtil.success("删除成功");
            } else {
                return ResultUtil.fail("删除物料类型失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("删除物料类型失败: " + e.getMessage());
        }
    }

    /**
     * 根据成品编号前三位查询物料类型
     * @param pid 成品编号
     * @return 物料类型
     */
    @GetMapping("/get-type-by-pid")
    public Result getTypeByPid(@RequestParam String pid) {
        try {
            if (pid == null || pid.length() < 3) {
                return ResultUtil.fail("成品编号长度不能少于3位");
            }
            
            // 取前三位作为物料前缀
            String itemPrefix = pid.substring(0, 3);
            ItemType itemType = itemTypeService.getByItemPrefix(itemPrefix);
            
            Map<String, Object> data = new HashMap<>();
            if (itemType != null) {
                data.put("drawingType", itemType.getTypeName());
                return ResultUtil.success(data);
            } else {
                return ResultUtil.fail("未找到前缀为 " + itemPrefix + " 的物料类型");
            }
        } catch (Exception e) {
            return ResultUtil.fail("查询物料类型失败: " + e.getMessage());
        }
    }
}