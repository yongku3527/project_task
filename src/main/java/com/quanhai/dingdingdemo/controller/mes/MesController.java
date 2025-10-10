package com.quanhai.dingdingdemo.controller.mes;


import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MES系统接口控制器
 * 提供与MES系统对接的接口服务
 */
@Slf4j
@RestController
@RequestMapping("/mes")

public class MesController {

    /**
     * 根据物料编号查询物料信息
     * 
     * @param itemCode 物料编号（可以是线路板编号、半成品编号或灯板插件半成品编号）
     * @return 包含物料信息的响应结果
     */
    @GetMapping("/item-info")

    public Result<Map<String, Object>> getItemInfo(
            @RequestParam("itemCode") String itemCode) {
        
        log.info("收到MES物料信息查询请求，物料编号: {}", itemCode);
        
        try {
            // 模拟MES系统返回的数据结构
            // 实际项目中这里会调用真实的MES系统接口
            Map<String, Object> itemInfo = new HashMap<>();
            
            // 根据物料编号前缀判断物料类型并返回相应的模拟数据
            if (itemCode.startsWith("PCB")) {
                // 线路板
                itemInfo.put("itemCode", itemCode);
                itemInfo.put("itemName", "主控制板-" + itemCode);
                itemInfo.put("itemType", "CIRCUIT_BOARD");
                itemInfo.put("description", "电路板物料");
                itemInfo.put("unit", "PCS");
                itemInfo.put("status", "ACTIVE");
            } else if (itemCode.startsWith("SP")) {
                // 半成品
                itemInfo.put("itemCode", itemCode);
                itemInfo.put("itemName", "半成品组件-" + itemCode);
                itemInfo.put("itemType", "SEMI_PRODUCT");
                itemInfo.put("description", "半成品物料");
                itemInfo.put("unit", "PCS");
                itemInfo.put("status", "ACTIVE");
            } else if (itemCode.startsWith("LED")) {
                // 灯板插件
                itemInfo.put("itemCode", itemCode);
                itemInfo.put("itemName", "灯板插件-" + itemCode);
                itemInfo.put("itemType", "LED_BOARD_PLUGIN");
                itemInfo.put("description", "灯板插件物料");
                itemInfo.put("unit", "PCS");
                itemInfo.put("status", "ACTIVE");
            } else {
                // 默认返回
                itemInfo.put("itemCode", itemCode);
                itemInfo.put("itemName", "未知物料-" + itemCode);
                itemInfo.put("itemType", "UNKNOWN");
                itemInfo.put("description", "未知物料类型");
                itemInfo.put("unit", "PCS");
                itemInfo.put("status", "ACTIVE");
            }
            
            log.info("MES物料信息查询成功，物料编号: {}, 物料名称: {}", itemCode, itemInfo.get("itemName"));
            return ResultUtil.success(itemInfo);
            
        } catch (Exception e) {
            log.error("MES物料信息查询失败，物料编号: {}, 错误: {}", itemCode, e.getMessage());
            return ResultUtil.fail("MES物料信息查询失败: " + e.getMessage());
        }
    }

    /**
     * 批量查询物料信息
     * 

     * @return 包含物料信息列表的响应结果
     */
    @PostMapping("/item-info/batch")

    public Result<Map<String, Object>> batchGetItemInfo(
             @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        java.util.List<String> itemCodes = (java.util.List<String>) request.get("itemCodes");
        
        log.info("收到MES批量物料信息查询请求，物料编号数量: {}", itemCodes != null ? itemCodes.size() : 0);
        
        try {
            java.util.List<Map<String, Object>> itemInfoList = new java.util.ArrayList<>();
            
            if (itemCodes != null) {
                for (String itemCode : itemCodes) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    
                    // 模拟返回数据
                    if (itemCode.startsWith("PCB")) {
                        itemInfo.put("itemCode", itemCode);
                        itemInfo.put("itemName", "主控制板-" + itemCode);
                        itemInfo.put("itemType", "CIRCUIT_BOARD");
                    } else if (itemCode.startsWith("SP")) {
                        itemInfo.put("itemCode", itemCode);
                        itemInfo.put("itemName", "半成品组件-" + itemCode);
                        itemInfo.put("itemType", "SEMI_PRODUCT");
                    } else if (itemCode.startsWith("LED")) {
                        itemInfo.put("itemCode", itemCode);
                        itemInfo.put("itemName", "灯板插件-" + itemCode);
                        itemInfo.put("itemType", "LED_BOARD_PLUGIN");
                    } else {
                        itemInfo.put("itemCode", itemCode);
                        itemInfo.put("itemName", "未知物料-" + itemCode);
                        itemInfo.put("itemType", "UNKNOWN");
                    }
                    
                    itemInfoList.add(itemInfo);
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("itemInfoList", itemInfoList);
            result.put("total", itemInfoList.size());
            
            log.info("MES批量物料信息查询成功，查询数量: {}", itemInfoList.size());
            return ResultUtil.success(result);
            
        } catch (Exception e) {
            log.error("MES批量物料信息查询失败，错误: {}", e.getMessage());
            return ResultUtil.fail("MES批量物料信息查询失败: " + e.getMessage());
        }
    }
}