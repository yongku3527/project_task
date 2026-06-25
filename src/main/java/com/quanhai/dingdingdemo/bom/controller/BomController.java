package com.quanhai.dingdingdemo.bom.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanhai.dingdingdemo.bom.model.BomDetail;
import com.quanhai.dingdingdemo.bom.model.BomInfo;
import com.quanhai.dingdingdemo.bom.model.BomOperationLog;
import com.quanhai.dingdingdemo.bom.service.BomDetailService;
import com.quanhai.dingdingdemo.bom.service.BomInfoService;
import com.quanhai.dingdingdemo.bom.service.BomMesService;
import com.quanhai.dingdingdemo.bom.service.BomOperationLogService;
import com.quanhai.dingdingdemo.satoken.model.SysUser;
import com.quanhai.dingdingdemo.satoken.service.SysUserService;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BOM信息管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/bom")
public class BomController {

    @Autowired
    private BomInfoService bomInfoService;

    @Autowired
    private BomDetailService bomDetailService;

    @Autowired
    private BomOperationLogService bomOperationLogService;

    @Autowired
    private BomMesService bomMesService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取BOM列表
     */
    @GetMapping("/list")
    @SaCheckPermission("bom:view")
    public Result<IPage<BomInfo>> list(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String itemCode) {
        Page<BomInfo> page = new Page<>(current, size);
        QueryWrapper<BomInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        if (itemCode != null && !itemCode.trim().isEmpty()) {
            queryWrapper.like("item_code", itemCode);
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(bomInfoService.page(page, queryWrapper));
    }

    /**
     * 根据ID获取BOM详情（含明细）
     */
    @GetMapping("/{id}")
    @SaCheckPermission("bom:view")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        BomInfo bomInfo = bomInfoService.getById(id);
        if (bomInfo == null || bomInfo.getDeleted() == 1) {
            return ResultUtil.fail("BOM信息不存在");
        }
        List<BomDetail> details = bomDetailService.listByBomId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("bomInfo", bomInfo);
        result.put("details", details);
        return ResultUtil.success(result);
    }

    /**
     * 新增BOM（主表+明细）
     */
    @PostMapping
    @SaCheckPermission("bom:add")
    @Transactional
    public Result<Boolean> add(@RequestBody Map<String, Object> request) {
        BomInfo bomInfo = parseBomInfo(request);
        if (bomInfo.getItemCode() == null || bomInfo.getItemCode().trim().isEmpty()) {
            return ResultUtil.fail("物料编码不能为空");
        }

        // 查询MES物料信息
        fillItemInfoFromMes(bomInfo);

        // 保存主表
        bomInfo.setCreateBy(getCurrentUserName());
        bomInfo.setStatus(1);
        bomInfo.setDeleted(0);
        bomInfoService.save(bomInfo);

        // 保存明细
        List<BomDetail> details = parseDetails(request, bomInfo.getId());
        if (CollUtil.isNotEmpty(details)) {
            bomDetailService.saveBatch(details);
        }

        // 记录创建日志
        saveOperationLog(bomInfo.getId(), "CREATE",
                "创建BOM，物料编码：" + bomInfo.getItemCode());

        return ResultUtil.success(true);
    }

    /**
     * 修改BOM（主要修改明细位号）
     */
    @PutMapping
    @SaCheckPermission("bom:update")
    @Transactional
    public Result<Boolean> update(@RequestBody Map<String, Object> request) {
        BomInfo bomInfo = parseBomInfo(request);
        if (bomInfo.getId() == null) {
            return ResultUtil.fail("BOM主键不能为空");
        }

        BomInfo exist = bomInfoService.getById(bomInfo.getId());
        if (exist == null || exist.getDeleted() == 1) {
            return ResultUtil.fail("BOM信息不存在");
        }

        // 更新主表基础信息
        if (bomInfo.getItemCode() != null && !bomInfo.getItemCode().trim().isEmpty()) {
            exist.setItemCode(bomInfo.getItemCode());
            fillItemInfoFromMes(exist);
        }
        bomInfoService.updateById(exist);

        // 处理明细：对比旧值，记录变更日志
        List<BomDetail> newDetails = parseDetails(request, bomInfo.getId());
        if (newDetails != null) {
            List<BomDetail> oldDetails = bomDetailService.listByBomId(bomInfo.getId());
            Map<Long, BomDetail> oldMap = new HashMap<>();
            for (BomDetail old : oldDetails) {
                oldMap.put(old.getId(), old);
            }

            // 保存原始ID（用于日志对比），然后清除ID重新插入
            List<Long> originalIds = new ArrayList<>();
            for (BomDetail detail : newDetails) {
                originalIds.add(detail.getId());
                detail.setId(null);
                detail.setBomId(bomInfo.getId());
            }

            // 删除旧明细
            bomDetailService.removeByIds(oldDetails.stream().map(BomDetail::getId).toList());

            // 保存新明细
            if (CollUtil.isNotEmpty(newDetails)) {
                bomDetailService.saveBatch(newDetails);
            }

            // 对比并记录变更日志（使用原始ID匹配旧数据）
            for (int i = 0; i < newDetails.size(); i++) {
                Long originalId = originalIds.get(i);
                BomDetail newDetail = newDetails.get(i);
                if (originalId == null) {
                    // 新增的明细
                    saveOperationLog(bomInfo.getId(), newDetail.getId(), "ADD", null,
                            null, null,
                            "新增明细，物料编码：" + newDetail.getItemCode() + "，位号：" + newDetail.getDesignators(), null);
                    continue;
                }
                BomDetail oldDetail = oldMap.get(originalId);
                if (oldDetail == null) {
                    continue;
                }
                // 对比位号
                if (!Objects.equals(oldDetail.getDesignators(), newDetail.getDesignators())) {
                    saveOperationLog(bomInfo.getId(), newDetail.getId(), "UPDATE", "位号",
                            oldDetail.getDesignators(), newDetail.getDesignators(),
                            "物料编码：" + newDetail.getItemCode() + " 位号变更", null);
                }
                // 对比数量
                if (!Objects.equals(oldDetail.getQuantity(), newDetail.getQuantity())) {
                    saveOperationLog(bomInfo.getId(), newDetail.getId(), "UPDATE", "数量",
                            oldDetail.getQuantity() != null ? oldDetail.getQuantity().toString() : null,
                            newDetail.getQuantity() != null ? newDetail.getQuantity().toString() : null,
                            "物料编码：" + newDetail.getItemCode() + " 数量变更", null);
                }
                // 对比物料编码
                if (!Objects.equals(oldDetail.getItemCode(), newDetail.getItemCode())) {
                    saveOperationLog(bomInfo.getId(), newDetail.getId(), "UPDATE", "物料编码",
                            oldDetail.getItemCode(), newDetail.getItemCode(),
                            "物料编码变更", null);
                }
            }

            // 检查被删除的旧明细（在旧数据中存在但新数据中不存在）
            Set<Long> newOriginalIds = new HashSet<>(originalIds);
            for (BomDetail oldDetail : oldDetails) {
                if (!newOriginalIds.contains(oldDetail.getId())) {
                    saveOperationLog(bomInfo.getId(), oldDetail.getId(), "DELETE", null,
                            null, null,
                            "删除明细，物料编码：" + oldDetail.getItemCode() + "，位号：" + oldDetail.getDesignators(), null);
                }
            }
        }

        return ResultUtil.success(true);
    }

    /**
     * 删除BOM（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("bom:delete")
    @Transactional
    public Result<Boolean> delete(@PathVariable Long id) {
        BomInfo exist = bomInfoService.getById(id);
        if (exist == null || exist.getDeleted() == 1) {
            return ResultUtil.fail("BOM信息不存在");
        }
        exist.setDeleted(1);
        bomInfoService.updateById(exist);

        saveOperationLog(id, "DELETE",
                "删除BOM，物料编码：" + exist.getItemCode());
        return ResultUtil.success(true);
    }

    /**
     * 批量导入BOM明细（Excel模板）
     * 由于 multipart/form-data 请求与 Sa-Token 上下文在 Spring Boot 3 + webflux 混合环境下存在兼容性问题，
     * 此处采用手动从 HttpServletRequest 读取 token 并校验的方式。
     */
    @PostMapping("/import/{bomId}")
    @Transactional
    public Result<Map<String, Object>> importDetail(@PathVariable Long bomId,
                                                    @RequestParam("file") MultipartFile file,
                                                    HttpServletRequest request) {
        // 手动读取并校验 token（兼容 multipart/form-data 请求）
        String tokenValue = getTokenValue(request);
        String loginId;
        try {
            loginId = StpUtil.getLoginIdByToken(tokenValue).toString();
        } catch (Exception e) {
            return ResultUtil.defineFail(401, "未登录或登录已过期");
        }

        BomInfo bomInfo = bomInfoService.getById(bomId);
        if (bomInfo == null || bomInfo.getDeleted() == 1) {
            return ResultUtil.fail("BOM信息不存在");
        }

        if (file == null || file.isEmpty()) {
            return ResultUtil.fail("上传文件不能为空");
        }

        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<List<Object>> rows = reader.read();
            if (CollUtil.isEmpty(rows) || rows.size() < 2) {
                return ResultUtil.fail("Excel文件为空或缺少数据行");
            }

            // 校验表头
            List<Object> header = rows.get(0);
            if (header.size() < 3
                    || !"物料编码".equals(String.valueOf(header.get(0)).trim())
                    || !"位号".equals(String.valueOf(header.get(1)).trim())
                    || !"数量".equals(String.valueOf(header.get(2)).trim())) {
                return ResultUtil.fail("Excel模板格式不正确，请使用标准模板");
            }

            List<Map<String, Object>> errorList = new ArrayList<>();
            List<BomDetail> detailList = new ArrayList<>();

            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                if (CollUtil.isEmpty(row) || row.stream().allMatch(Objects::isNull)) {
                    continue;
                }

                String itemCode = row.size() > 0 && row.get(0) != null ? String.valueOf(row.get(0)).trim() : "";
                String designators = row.size() > 1 && row.get(1) != null ? String.valueOf(row.get(1)).trim() : "";
                Integer quantity = null;

                if (row.size() > 2 && row.get(2) != null) {
                    try {
                        quantity = Integer.parseInt(String.valueOf(row.get(2)).trim());
                    } catch (NumberFormatException e) {
                        errorList.add(buildError(i, itemCode, designators, "数量格式不正确"));
                        continue;
                    }
                }

                if (itemCode.isEmpty()) {
                    errorList.add(buildError(i, itemCode, designators, "物料编码不能为空"));
                    continue;
                }

                // 位号按逗号拆分，计算实际位号数量
                int designatorCount = 0;
                if (!designators.isEmpty()) {
                    designatorCount = designators.split(",").length;
                }

                // 校验位号个数和数量是否一致
                if (quantity == null) {
                    quantity = designatorCount;
                } else if (!quantity.equals(designatorCount)) {
                    errorList.add(buildError(i, itemCode, designators,
                            "位号个数（" + designatorCount + "）与数量（" + quantity + "）不一致"));
                    continue;
                }

                BomDetail detail = new BomDetail();
                detail.setBomId(bomId);
                detail.setItemCode(itemCode);
                detail.setDesignators(designators);
                detail.setQuantity(quantity);
                detail.setSortOrder(i);
                detail.setStatus(1);

                // 查询MES获取物料名称和规格型号
                Map<String, Object> mesInfo = bomMesService.getItemInfo(itemCode);
                detail.setItemName((String) mesInfo.get("itemName"));
                detail.setItemSpec((String) mesInfo.get("itemSpec"));

                detailList.add(detail);
            }

            if (CollUtil.isNotEmpty(errorList)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("errors", errorList);
                return ResultUtil.success(result);
            }

            // 清除旧明细并保存新明细
            List<BomDetail> oldDetails = bomDetailService.listByBomId(bomId);
            if (CollUtil.isNotEmpty(oldDetails)) {
                bomDetailService.removeByIds(oldDetails.stream().map(BomDetail::getId).toList());
            }

            if (CollUtil.isNotEmpty(detailList)) {
                bomDetailService.saveBatch(detailList);
            }

            // 记录上传日志
            saveOperationLog(bomId, "UPLOAD",
                    "批量导入BOM明细，共 " + detailList.size() + " 条记录", resolveUserName(loginId));

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", detailList.size());
            return ResultUtil.success(result);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            return ResultUtil.fail("读取Excel文件失败：" + e.getMessage());
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    @SaCheckPermission("bom:view")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("BOM导入模板.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("物料编码", "位号", "数量"));
        rows.add(Arrays.asList("0010050170", "C84,C167,C171,C310", "4"));

        try (ExcelWriter writer = ExcelUtil.getWriter(true);
             ServletOutputStream out = response.getOutputStream()) {
            writer.write(rows, true);
            writer.flush(out, true);
        }
    }

    /**
     * 字段名英文转中文映射
     */
    private static final Map<String, String> FIELD_NAME_MAP = Map.of(
            "designators", "位号",
            "quantity", "数量",
            "itemCode", "物料编码"
    );

    /**
     * 查询BOM操作日志（支持搜索）
     */
    @GetMapping("/log/{bomId}")
    @SaCheckPermission("bom:view")
    public Result<List<BomOperationLog>> getLogs(
            @PathVariable Long bomId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operateBy,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        QueryWrapper<BomOperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bom_id", bomId);
        if (operationType != null && !operationType.trim().isEmpty()) {
            queryWrapper.eq("operation_type", operationType);
        }
        if (operateBy != null && !operateBy.trim().isEmpty()) {
            queryWrapper.like("operate_by", operateBy);
        }
        if (startTime != null) {
            queryWrapper.ge("operate_time", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("operate_time", endTime);
        }
        queryWrapper.orderByDesc("operate_time");
        List<BomOperationLog> logs = bomOperationLogService.list(queryWrapper);
        // 对旧数据做兼容处理：operateBy转用户名、fieldName转中文
        for (BomOperationLog logItem : logs) {
            if (logItem.getOperateBy() != null && logItem.getOperateBy().matches("\\d+")) {
                logItem.setOperateBy(resolveUserName(logItem.getOperateBy()));
            }
            if (logItem.getFieldName() != null && FIELD_NAME_MAP.containsKey(logItem.getFieldName())) {
                logItem.setFieldName(FIELD_NAME_MAP.get(logItem.getFieldName()));
            }
        }
        return ResultUtil.success(logs);
    }

    /**
     * 根据物料编码查询MES物料信息
     */
    @GetMapping("/item-info/{itemCode}")
    @SaCheckPermission("bom:view")
    public Result<Map<String, Object>> getItemInfo(@PathVariable String itemCode) {
        return ResultUtil.success(bomMesService.getItemInfo(itemCode));
    }

    // ==================== 私有方法 ====================

    private BomInfo parseBomInfo(Map<String, Object> request) {
        BomInfo bomInfo = new BomInfo();
        if (request.get("id") != null) {
            bomInfo.setId(Long.valueOf(request.get("id").toString()));
        }
        if (request.get("itemCode") != null) {
            bomInfo.setItemCode(String.valueOf(request.get("itemCode")).trim());
        }
        if (request.get("itemName") != null) {
            bomInfo.setItemName(String.valueOf(request.get("itemName")));
        }
        if (request.get("itemSpec") != null) {
            bomInfo.setItemSpec(String.valueOf(request.get("itemSpec")));
        }
        return bomInfo;
    }

    @SuppressWarnings("unchecked")
    private List<BomDetail> parseDetails(Map<String, Object> request, Long bomId) {
        Object detailObj = request.get("details");
        if (detailObj == null) {
            return null;
        }
        List<Map<String, Object>> detailMaps = (List<Map<String, Object>>) detailObj;
        List<BomDetail> details = new ArrayList<>();
        int sortOrder = 1;
        for (Map<String, Object> map : detailMaps) {
            BomDetail detail = new BomDetail();
            if (map.get("id") != null) {
                detail.setId(Long.valueOf(map.get("id").toString()));
            }
            detail.setBomId(bomId);
            detail.setItemCode(map.get("itemCode") != null ? String.valueOf(map.get("itemCode")).trim() : null);
            detail.setItemName(map.get("itemName") != null ? String.valueOf(map.get("itemName")) : null);
            detail.setItemSpec(map.get("itemSpec") != null ? String.valueOf(map.get("itemSpec")) : null);
            detail.setDesignators(map.get("designators") != null ? String.valueOf(map.get("designators")) : null);
            if (map.get("quantity") != null) {
                detail.setQuantity(Integer.parseInt(map.get("quantity").toString()));
            }
            detail.setSortOrder(sortOrder++);
            detail.setStatus(1);
            details.add(detail);
        }
        return details;
    }

    private void fillItemInfoFromMes(BomInfo bomInfo) {
        if (bomInfo.getItemCode() == null || bomInfo.getItemCode().trim().isEmpty()) {
            return;
        }
        Map<String, Object> mesInfo = bomMesService.getItemInfo(bomInfo.getItemCode());
        if (mesInfo.get("itemName") != null) {
            bomInfo.setItemName((String) mesInfo.get("itemName"));
        }
        if (mesInfo.get("itemSpec") != null) {
            bomInfo.setItemSpec((String) mesInfo.get("itemSpec"));
        }
    }

    private String getCurrentUserName() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId == null) return "系统";
            return resolveUserName(loginId.toString());
        } catch (Exception e) {
            return "系统";
        }
    }

    private String resolveUserName(String userId) {
        try {
            SysUser user = sysUserService.getById(userId);
            if (user != null) {
                return user.getNickname() != null && !user.getNickname().isEmpty()
                        ? user.getNickname() : user.getUsername();
            }
        } catch (Exception e) {
            log.warn("查询用户信息失败，userId={}", userId);
        }
        return userId;
    }

    private void saveOperationLog(Long bomId, String operationType, String remark) {
        saveOperationLog(bomId, null, operationType, null, null, null, remark, null);
    }

    private void saveOperationLog(Long bomId, String operationType, String remark, String operateBy) {
        saveOperationLog(bomId, null, operationType, null, null, null, remark, operateBy);
    }

    private void saveOperationLog(Long bomId, Long detailId, String operationType,
                                  String fieldName, String oldValue, String newValue, String remark, String operateBy) {
        try {
            BomOperationLog operationLog = new BomOperationLog();
            operationLog.setBomId(bomId);
            operationLog.setDetailId(detailId);
            operationLog.setOperationType(operationType);
            operationLog.setFieldName(fieldName);
            operationLog.setOldValue(oldValue);
            operationLog.setNewValue(newValue);
            operationLog.setOperateBy(operateBy != null ? operateBy : getCurrentUserName());
            operationLog.setOperateTime(LocalDateTime.now());
            operationLog.setRemark(remark);
            bomOperationLogService.save(operationLog);
        } catch (Exception e) {
            log.error("保存BOM操作日志失败", e);
        }
    }

    private String getTokenValue(HttpServletRequest request) {
        // 1. 优先从 header 读取
        String tokenValue = request.getHeader("satoken");
        if (tokenValue != null && !tokenValue.trim().isEmpty()) {
            return tokenValue.trim();
        }
        // 2. 从 cookie 读取
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("satoken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private Map<String, Object> buildError(int rowIndex, String itemCode, String designators, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("row", rowIndex + 1);
        error.put("itemCode", itemCode);
        error.put("designators", designators);
        error.put("message", message);
        return error;
    }
}
