package com.quanhai.dingdingdemo.controller.mes;


import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MES系统接口控制器
 * 提供与MES系统对接的接口服务
 */
@Slf4j
@RestController
@RequestMapping("/mes")

public class MesController {

    @Value("${mes.api.enabled:true}")
    private boolean mesApiEnabled;
    
    @Value("${mes.api.url:}")
    private String mesApiUrl;

    /**
     * 根据物料编号查询物料信息
     * 
     * @param itemCode 物料编号（可以是线路板编号、半成品编号或灯板插件半成品编号）
     * @return 包含物料信息的响应结果
     */
    @GetMapping("/item-info/{itemCode}")

    public Result<Map<String, Object>> getItemInfo(
            @PathVariable("itemCode") String itemCode) {
        
        log.info("收到MES物料信息查询请求，物料编号: {}", itemCode);
        
        // 检查接口是否启用
        if (!mesApiEnabled) {
            log.warn("MES接口已禁用");
            return ResultUtil.fail("MES接口服务暂时不可用");
        }
        
        // 参数验证
        if (itemCode == null || itemCode.trim().isEmpty()) {
            log.warn("物料编码为空");
            return ResultUtil.fail("物料编码不能为空");
        }
        
        // 标准化物料编码
        itemCode = itemCode.trim().toUpperCase();
        
        try {
            // 如果配置了真实的MES接口地址，则调用真实接口
            if (mesApiUrl != null && !mesApiUrl.trim().isEmpty()) {
                return callRealMesApi(itemCode);
            } else {
                // 否则返回模拟数据
                return getMockItemInfo(itemCode);
            }
            
        } catch (IllegalArgumentException e) {
            log.error("MES物料信息查询参数错误，物料编号: {}, 错误: {}", itemCode, e.getMessage());
            return ResultUtil.fail("参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("MES物料信息查询失败，物料编号: {}, 错误: {}", itemCode, e.getMessage(), e);
            return ResultUtil.fail("MES物料信息查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 调用真实的MES接口获取物料信息
     */
    private Result<Map<String, Object>> callRealMesApi(String itemCode) {
        try {
            // 创建WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl(mesApiUrl)
                    .build();
            
            // 构建form-data请求体
            String formData = "itemCode=" + itemCode;
            
            // 调用MES接口 - 使用form-data格式
            Mono<Map> response = webClient.post()
                    .uri("/dingdingReport/getSingleItemStock")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            // 获取响应数据
            Map<String, Object> responseData = response.block();
            
            if (responseData == null) {
                log.error("MES接口返回空数据");
                return ResultUtil.fail("MES接口返回空数据");
            }
            
            // 检查MES接口返回状态
            Boolean success = (Boolean) responseData.get("success");
            if (success == null || !success) {
                String message = (String) responseData.get("message");
                log.error("MES接口调用失败: {}", message);
                return ResultUtil.fail("MES接口调用失败: " + (message != null ? message : "未知错误"));
            }
            
            // 获取结果数据
            Map<String, Object> mesResult = (Map<String, Object>) responseData.get("result");
            if (mesResult == null) {
                log.error("MES接口返回结果为空");
                return ResultUtil.fail("MES接口返回结果为空");
            }
            
            // 转换数据格式以适配前端需求
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("itemCode", mesResult.get("itemCode"));
            itemInfo.put("itemName", mesResult.get("itemName"));
            itemInfo.put("itemSpec", mesResult.get("itemSpec"));
            
            // 根据itemType设置物料类型
            String itemType = String.valueOf(mesResult.get("itemType"));
            switch (itemType) {
                case "0":
                    itemInfo.put("itemType", "RAW_MATERIAL");
                    itemInfo.put("description", "原材料");
                    break;
                case "1":
                    itemInfo.put("itemType", "SEMI_PRODUCT");
                    itemInfo.put("description", "半成品");
                    break;
                case "2":
                    itemInfo.put("itemType", "FINISHED_PRODUCT");
                    itemInfo.put("description", "成品");
                    break;
                case "3":
                    itemInfo.put("itemType", "ACCESSORY");
                    itemInfo.put("description", "辅料");
                    break;
                default:
                    itemInfo.put("itemType", "UNKNOWN");
                    itemInfo.put("description", "未知物料类型");
            }
            
            // 添加库存相关数据
            itemInfo.put("stockQualityNum", mesResult.get("stockQualityNum"));
            itemInfo.put("stockDefectiveNum", mesResult.get("stockDefectiveNum"));
            itemInfo.put("projectNum", mesResult.get("projectNum"));
            itemInfo.put("receiveNum", mesResult.get("receiveNum"));
            itemInfo.put("fakeNum", mesResult.get("fakeNum"));
            
            // 添加状态信息
            itemInfo.put("unit", "PCS");
            itemInfo.put("status", "ACTIVE");
            
            log.info("调用真实MES接口成功，物料编号: {}, 物料名称: {}", 
                    itemCode, mesResult.get("itemName"));
            return ResultUtil.success(itemInfo);
            
        } catch (Exception e) {
            log.error("调用真实MES接口失败，物料编号: {}, 错误: {}", itemCode, e.getMessage(), e);
            return ResultUtil.fail("调用MES接口失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模拟的物料信息数据
     */
    private Result<Map<String, Object>> getMockItemInfo(String itemCode) {
        // 模拟MES系统返回的数据结构
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
            // 添加模拟库存数据
            itemInfo.put("stockQualityNum", 1000);
            itemInfo.put("stockDefectiveNum", 5);
            itemInfo.put("projectNum", 200);
            itemInfo.put("receiveNum", 50);
            itemInfo.put("fakeNum", 800);
        } else if (itemCode.startsWith("SP")) {
            // 半成品
            itemInfo.put("itemCode", itemCode);
            itemInfo.put("itemName", "半成品组件-" + itemCode);
            itemInfo.put("itemType", "SEMI_PRODUCT");
            itemInfo.put("description", "半成品物料");
            itemInfo.put("unit", "PCS");
            itemInfo.put("status", "ACTIVE");
            // 添加模拟库存数据
            itemInfo.put("stockQualityNum", 500);
            itemInfo.put("stockDefectiveNum", 3);
            itemInfo.put("projectNum", 150);
            itemInfo.put("receiveNum", 30);
            itemInfo.put("fakeNum", 400);
        } else if (itemCode.startsWith("LED")) {
            // 灯板插件
            itemInfo.put("itemCode", itemCode);
            itemInfo.put("itemName", "灯板插件-" + itemCode);
            itemInfo.put("itemType", "LED_BOARD_PLUGIN");
            itemInfo.put("description", "灯板插件物料");
            itemInfo.put("unit", "PCS");
            itemInfo.put("status", "ACTIVE");
            // 添加模拟库存数据
            itemInfo.put("stockQualityNum", 800);
            itemInfo.put("stockDefectiveNum", 2);
            itemInfo.put("projectNum", 100);
            itemInfo.put("receiveNum", 20);
            itemInfo.put("fakeNum", 600);
        } else {
            // 默认返回
            itemInfo.put("itemCode", itemCode);
            itemInfo.put("itemName", "未知物料-" + itemCode);
            itemInfo.put("itemType", "UNKNOWN");
            itemInfo.put("description", "未知物料类型");
            itemInfo.put("unit", "PCS");
            itemInfo.put("status", "ACTIVE");
            // 添加模拟库存数据
            itemInfo.put("stockQualityNum", 0);
            itemInfo.put("stockDefectiveNum", 0);
            itemInfo.put("projectNum", 0);
            itemInfo.put("receiveNum", 0);
            itemInfo.put("fakeNum", 0);
        }
        
        log.info("返回模拟MES数据，物料编号: {}, 物料名称: {}", itemCode, itemInfo.get("itemName"));
        return ResultUtil.success(itemInfo);
    }

    /**
     * 批量查询物料信息
     * 
     * @param request 包含物料编码列表的请求体
     * @return 包含物料信息列表的响应结果
     */
    @PostMapping("/item-info/batch")

    public Result<Map<String, Object>> batchGetItemInfo(
            @RequestBody Map<String, Object> request) {
        
        // 检查接口是否启用
        if (!mesApiEnabled) {
            log.warn("MES接口已禁用");
            return ResultUtil.fail("MES接口服务暂时不可用");
        }
        // 参数验证
        if (request == null) {
            log.warn("批量查询请求体为空");
            return ResultUtil.fail("请求体不能为空");
        }
        
        @SuppressWarnings("unchecked")
        List<String> itemCodes = (List<String>) request.get("itemCodes");
        
        if (itemCodes == null || itemCodes.isEmpty()) {
            log.warn("物料编码列表为空");
            return ResultUtil.fail("物料编码列表不能为空");
        }
        
        // 限制批量查询数量，防止性能问题
        if (itemCodes.size() > 100) {
            log.warn("批量查询数量超过限制: {}", itemCodes.size());
            return ResultUtil.fail("批量查询数量不能超过100个");
        }
        
        log.info("收到MES批量物料信息查询请求，物料编号数量: {}", itemCodes.size());
        
        try {
            java.util.List<Map<String, Object>> itemInfoList = new java.util.ArrayList<>();
            
            for (String itemCode : itemCodes) {
                // 标准化物料编码
                if (itemCode != null) {
                    itemCode = itemCode.trim().toUpperCase();
                    
                    Map<String, Object> itemInfo = new HashMap<>();
                    
                    // 模拟返回数据
                    if (itemCode.startsWith("PCB")) {
                        itemInfo.put("itemCode", itemCode);
                        itemInfo.put("itemName", "主控制板-" + itemCode);
                        itemInfo.put("itemType", "CIRCUIT_BOARD");
                        itemInfo.put("unit", "PCS");
                        itemInfo.put("status", "ACTIVE");
                    } else if (itemCode.startsWith("SP")) {
                        itemInfo.put("itemCode", itemCode);
                        itemInfo.put("itemName", "半成品组件-" + itemCode);
                        itemInfo.put("itemType", "SEMI_PRODUCT");
                        itemInfo.put("unit", "PCS");
                        itemInfo.put("status", "ACTIVE");
                    } else if (itemCode.startsWith("LED")) {
                        itemInfo.put("itemCode", itemCode);
                        itemInfo.put("itemName", "灯板插件-" + itemCode);
                        itemInfo.put("itemType", "LED_BOARD_PLUGIN");
                        itemInfo.put("unit", "PCS");
                        itemInfo.put("status", "ACTIVE");
                    } else {
                        itemInfo.put("itemCode", itemCode);
                        itemInfo.put("itemName", "未知物料-" + itemCode);
                        itemInfo.put("itemType", "UNKNOWN");
                        itemInfo.put("unit", "PCS");
                        itemInfo.put("status", "ACTIVE");
                    }
                    
                    itemInfoList.add(itemInfo);
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("itemInfoList", itemInfoList);
            result.put("total", itemInfoList.size());
            
            log.info("MES批量物料信息查询成功，查询数量: {}", itemInfoList.size());
            return ResultUtil.success(result);
            
        } catch (IllegalArgumentException e) {
            log.error("MES批量物料信息查询参数错误，错误: {}", e.getMessage());
            return ResultUtil.fail("参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("MES批量物料信息查询失败，错误: {}", e.getMessage(), e);
            return ResultUtil.fail("MES批量物料信息查询失败: " + e.getMessage());
        }
    }

    /**
     * MES接口健康检查
     * 
     * @return 接口状态信息
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", mesApiEnabled ? "UP" : "DOWN");
        healthInfo.put("enabled", mesApiEnabled);
        healthInfo.put("apiUrl", mesApiUrl != null && !mesApiUrl.isEmpty() ? mesApiUrl : "未配置");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "MES Interface Service");
        
        log.info("MES接口健康检查，状态: {}", healthInfo.get("status"));
        return ResultUtil.success(healthInfo);
    }

    /**
     * 获取支持的物料类型
     * 
     * @return 支持的物料类型列表
     */
    @GetMapping("/item-types")
    public Result<List<Map<String, Object>>> getSupportedItemTypes() {
        List<Map<String, Object>> itemTypes = new ArrayList<>();
        
        Map<String, Object> circuitBoard = new HashMap<>();
        circuitBoard.put("type", "CIRCUIT_BOARD");
        circuitBoard.put("prefix", "PCB");
        circuitBoard.put("description", "线路板");
        circuitBoard.put("example", "PCB001");
        itemTypes.add(circuitBoard);
        
        Map<String, Object> semiProduct = new HashMap<>();
        semiProduct.put("type", "SEMI_PRODUCT");
        semiProduct.put("prefix", "SP");
        semiProduct.put("description", "半成品");
        semiProduct.put("example", "SP001");
        itemTypes.add(semiProduct);
        
        Map<String, Object> ledPlugin = new HashMap<>();
        ledPlugin.put("type", "LED_BOARD_PLUGIN");
        ledPlugin.put("prefix", "LED");
        ledPlugin.put("description", "灯板插件");
        ledPlugin.put("example", "LED001");
        itemTypes.add(ledPlugin);
        
        log.info("获取MES支持的物料类型，数量: {}", itemTypes.size());
        return ResultUtil.success(itemTypes);
    }
}