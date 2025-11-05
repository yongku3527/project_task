package com.quanhai.dingdingdemo.file.controller;

import com.quanhai.dingdingdemo.file.model.ItemType;
import com.quanhai.dingdingdemo.file.service.ItemTypeService;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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