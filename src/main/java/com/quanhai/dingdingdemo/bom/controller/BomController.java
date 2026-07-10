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
                // 对比位号（显示添加/删除/未变的差异）
                if (!Objects.equals(oldDetail.getDesignators(), newDetail.getDesignators())) {
                    String diffRemark = buildDesignatorDiffRemark(oldDetail.getDesignators(), newDetail.getDesignators(), newDetail.getItemCode());
                    saveOperationLog(bomInfo.getId(), newDetail.getId(), "UPDATE", "位号",
                            oldDetail.getDesignators(), newDetail.getDesignators(),
                            diffRemark, null);
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
     * 删除BOM（逻辑删除，由 @TableLogic 自动处理）
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("bom:delete")
    @Transactional
    public Result<Boolean> delete(@PathVariable Long id) {
        BomInfo exist = bomInfoService.getById(id);
        if (exist == null) {
            return ResultUtil.fail("BOM信息不存在");
        }
        // @TableLogic 会将 removeById 转为 UPDATE SET deleted=1
        bomInfoService.removeById(id);

        saveOperationLog(id, "DELETE",
                "删除BOM，物料编码：" + exist.getItemCode());
        return ResultUtil.success(true);
    }

    /**
     * Excel导入预览（只解析不保存）
     */
    @PostMapping("/import/preview/{bomId}")
    public Result<Map<String, Object>> previewImport(@PathVariable Long bomId,
                                                      @RequestParam("file") MultipartFile file,
                                                      HttpServletRequest request) {
        String tokenValue = getTokenValue(request);
        try {
            StpUtil.getLoginIdByToken(tokenValue);
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

        return parseExcelForPreview(bomId, file);
    }

    /**
     * 批量导入BOM明细（Excel模板）
     * 支持 importMode 参数：REPLACE（覆盖） / APPEND（增量追加），默认 REPLACE
     */
    @PostMapping("/import/{bomId}")
    @Transactional
    public Result<Map<String, Object>> importDetail(@PathVariable Long bomId,
                                                    @RequestParam("file") MultipartFile file,
                                                    @RequestParam(defaultValue = "REPLACE") String importMode,
                                                    @RequestParam(defaultValue = "false") boolean forceImport,
                                                    HttpServletRequest request) {
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

        try {
            List<BomDetail> detailList = parseAndValidateExcel(bomId, file);
            // 有错误行时的处理
            if (detailList == null) {
                if (forceImport) {
                    // 强制导入：全部导入，不跳过任何行
                    detailList = parseAllRows(bomId, file);
                    if (CollUtil.isEmpty(detailList)) {
                        return ResultUtil.fail("没有数据可导入");
                    }
                } else {
                    return parseExcelForPreview(bomId, file);
                }
            }

            int newCount = detailList.size();
            String modeText;
            String forceText = forceImport ? "（跳过错误行）" : "";

            if ("APPEND".equalsIgnoreCase(importMode)) {
                bomDetailService.saveBatch(detailList);
                modeText = "增量导入";
            } else {
                List<BomDetail> oldDetails = bomDetailService.listByBomId(bomId);
                if (CollUtil.isNotEmpty(oldDetails)) {
                    bomDetailService.removeByIds(oldDetails.stream().map(BomDetail::getId).toList());
                }
                bomDetailService.saveBatch(detailList);
                modeText = "覆盖导入";
            }

            saveOperationLog(bomId, "UPLOAD",
                    modeText + forceText + "BOM明细，共 " + newCount + " 条记录", resolveUserName(loginId));

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", newCount);
            result.put("importMode", importMode.toUpperCase());
            return ResultUtil.success(result);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            return ResultUtil.fail("读取Excel文件失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除BOM明细
     */
    @PostMapping("/detail/batch-delete/{bomId}")
    @SaCheckPermission("bom:update")
    @Transactional
    public Result<Boolean> batchDeleteDetails(@PathVariable Long bomId,
                                               @RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        if (CollUtil.isEmpty(ids)) {
            return ResultUtil.fail("请选择要删除的明细");
        }

        // 获取明细信息用于记录日志
        List<BomDetail> toDelete = bomDetailService.listByIds(ids);
        int count = toDelete.size();
        bomDetailService.removeByIds(ids);

        // 构建详细的删除日志（最大 500 字符）
        StringBuilder remark = new StringBuilder("批量删除BOM明细，共 " + count + " 条：");
        int maxShow = Math.min(count, 10);
        for (int i = 0; i < maxShow; i++) {
            BomDetail d = toDelete.get(i);
            remark.append("[").append(d.getItemCode())
                    .append(" 位号:").append(d.getDesignators() != null ? d.getDesignators() : "-")
                    .append("]");
            if (i < maxShow - 1) remark.append(", ");
        }
        if (count > maxShow) {
            remark.append("...等").append(count).append("条");
        }
        // 截断到 480 字符，超出部分追加 ...
        String finalRemark = remark.toString();
        if (finalRemark.length() > 480) {
            finalRemark = finalRemark.substring(0, 480) + "...";
        }
        saveOperationLog(bomId, "DELETE", finalRemark);
        return ResultUtil.success(true);
    }

    /**
     * 导出BOM明细为Excel
     */
    @GetMapping("/export/{bomId}")
    @SaCheckPermission("bom:view")
    public void exportDetails(@PathVariable Long bomId, HttpServletResponse response) throws IOException {
        BomInfo bomInfo = bomInfoService.getById(bomId);
        if (bomInfo == null || bomInfo.getDeleted() == 1) {
            response.setStatus(404);
            response.getWriter().write("BOM信息不存在");
            return;
        }

        List<BomDetail> details = bomDetailService.listByBomId(bomId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("BOM明细_" + bomInfo.getItemCode() + ".xlsx", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        try (ExcelWriter writer = ExcelUtil.getWriter(true);
             ServletOutputStream out = response.getOutputStream()) {
            // 写入标题行
            writer.addHeaderAlias("itemCode", "物料编码");
            writer.addHeaderAlias("itemName", "物料名称");
            writer.addHeaderAlias("itemSpec", "规格型号");
            writer.addHeaderAlias("designators", "位号");
            writer.addHeaderAlias("quantity", "数量");
            // 只导出已设置别名（白名单）的字段
            writer.setOnlyAlias(true);

            writer.write(details, true);
            writer.flush(out, true);
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
        // 合并同一秒内同一明细的多字段UPDATE日志
        return ResultUtil.success(mergeUpdateLogs(logs));
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

    /**
     * 合并同一秒内同一明细的多字段UPDATE日志为单条记录
     */
    private List<BomOperationLog> mergeUpdateLogs(List<BomOperationLog> logs) {
        if (CollUtil.isEmpty(logs)) {
            return logs;
        }

        // 使用 LinkedHashMap 保持顺序
        Map<String, List<BomOperationLog>> grouped = new LinkedHashMap<>();
        List<BomOperationLog> result = new ArrayList<>();

        for (BomOperationLog logItem : logs) {
            if ("UPDATE".equals(logItem.getOperationType()) && logItem.getOperateTime() != null && logItem.getDetailId() != null) {
                // 分组 key：操作时间截断到秒 + detailId
                String key = logItem.getOperateTime().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
                        + "_" + logItem.getDetailId();
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(logItem);
            } else {
                result.add(logItem);
            }
        }

        // 合并每组
        for (List<BomOperationLog> group : grouped.values()) {
            if (group.size() == 1) {
                result.add(group.get(0));
            } else {
                result.add(mergeGroup(group));
            }
        }

        // 按操作时间倒序重排
        result.sort((a, b) -> {
            if (a.getOperateTime() == null) return 1;
            if (b.getOperateTime() == null) return -1;
            return b.getOperateTime().compareTo(a.getOperateTime());
        });

        return result;
    }

    /**
     * 将一组同明细同时间的UPDATE日志合并为一条
     */
    private BomOperationLog mergeGroup(List<BomOperationLog> group) {
        BomOperationLog merged = new BomOperationLog();
        BomOperationLog first = group.get(0);

        merged.setId(first.getId());
        merged.setBomId(first.getBomId());
        merged.setDetailId(first.getDetailId());
        merged.setOperationType(first.getOperationType());
        merged.setOperateBy(first.getOperateBy());
        merged.setOperateTime(first.getOperateTime());

        // 合并字段名、旧值、新值、备注
        List<String> fieldNames = new ArrayList<>();
        List<String> oldValues = new ArrayList<>();
        List<String> newValues = new ArrayList<>();
        List<String> remarks = new ArrayList<>();

        for (BomOperationLog logItem : group) {
            if (logItem.getFieldName() != null && !logItem.getFieldName().isEmpty()) {
                fieldNames.add(logItem.getFieldName());
            }
            if (logItem.getOldValue() != null) {
                oldValues.add(logItem.getOldValue());
            }
            if (logItem.getNewValue() != null) {
                newValues.add(logItem.getNewValue());
            }
            if (logItem.getRemark() != null && !logItem.getRemark().isEmpty()) {
                remarks.add(logItem.getRemark());
            }
        }

        merged.setFieldName(String.join("\n", fieldNames));
        merged.setOldValue(String.join("\n", oldValues));
        merged.setNewValue(String.join("\n", newValues));
        // 备注取差异汇总（位号变更那条）
        if (!remarks.isEmpty()) {
            merged.setRemark(remarks.stream()
                    .filter(r -> r.contains("新增") || r.contains("删除"))
                    .findFirst()
                    .orElse(remarks.get(0)));
        }

        return merged;
    }

    /**
     * 计算位号变更的具体差异（新增/删除/未变）
     */
    private String buildDesignatorDiffRemark(String oldDesignators, String newDesignators, String itemCode) {
        Set<String> oldSet = parseDesignatorSet(oldDesignators);
        Set<String> newSet = parseDesignatorSet(newDesignators);

        Set<String> added = new HashSet<>(newSet);
        added.removeAll(oldSet);

        Set<String> removed = new HashSet<>(oldSet);
        removed.removeAll(newSet);

        Set<String> unchanged = new HashSet<>(oldSet);
        unchanged.retainAll(newSet);

        StringBuilder sb = new StringBuilder("物料编码：").append(itemCode).append(" 位号变更");
        if (!added.isEmpty()) {
            sb.append("；新增: ").append(String.join(",", added));
        }
        if (!removed.isEmpty()) {
            sb.append("；删除: ").append(String.join(",", removed));
        }
        if (!unchanged.isEmpty()) {
            sb.append("；未变: ").append(String.join(",", unchanged));
        }
        return sb.toString();
    }

    private Set<String> parseDesignatorSet(String designators) {
        if (designators == null || designators.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(designators.split(","))
                .map(s -> s.trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * 物料编码以 001009 开头时自动追加\"线路板\"位号
     */
    private String appendLineBoardDesignator(String itemCode, String designators) {
        if (itemCode != null && itemCode.startsWith("001009")) {
            String lb = "线路板";
            if (designators == null || designators.trim().isEmpty()) {
                return lb;
            }
            Set<String> set = parseDesignatorSet(designators);
            if (!set.contains(lb)) {
                return designators + "," + lb;
            }
        }
        return designators;
    }

    /**
     * 解析Excel并校验，返回预览数据（所有行均显示，错误行标记 valid=false 和 errorMessage），不保存
     */
    private Result<Map<String, Object>> parseExcelForPreview(Long bomId, MultipartFile file) {
        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<List<Object>> rows = reader.read();
            if (CollUtil.isEmpty(rows) || rows.size() < 2) {
                return ResultUtil.fail("Excel文件为空或缺少数据行");
            }

            List<Object> header = rows.get(0);
            if (header.size() < 3
                    || !"物料编码".equals(String.valueOf(header.get(0)).trim())
                    || !"位号".equals(String.valueOf(header.get(1)).trim())
                    || !"数量".equals(String.valueOf(header.get(2)).trim())) {
                return ResultUtil.fail("Excel模板格式不正确，请使用标准模板");
            }

            int errorCount = 0;
            List<Map<String, Object>> previewList = new ArrayList<>();

            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                if (CollUtil.isEmpty(row) || row.stream().allMatch(Objects::isNull)) {
                    continue;
                }

                String itemCode = row.size() > 0 && row.get(0) != null ? String.valueOf(row.get(0)).trim() : "";
                String designators = row.size() > 1 && row.get(1) != null ? String.valueOf(row.get(1)).trim() : "";
                String errorMessage = null;
                Integer quantity = null;

                if (row.size() > 2 && row.get(2) != null) {
                    try {
                        quantity = Integer.parseInt(String.valueOf(row.get(2)).trim());
                    } catch (NumberFormatException e) {
                        errorMessage = "数量格式不正确";
                    }
                }

                if (errorMessage == null && itemCode.isEmpty()) {
                    errorMessage = "物料编码不能为空";
                }

                int designatorCount = 0;
                if (errorMessage == null && !designators.isEmpty()) {
                    designatorCount = designators.split(",").length;
                }

                if (errorMessage == null && quantity == null) {
                    quantity = designatorCount;
                } else if (errorMessage == null && !quantity.equals(designatorCount)) {
                    errorMessage = "位号个数（" + designatorCount + "）与数量（" + quantity + "）不一致";
                }

                boolean valid = (errorMessage == null);
                if (!valid) {
                    errorCount++;
                }

                Map<String, Object> previewItem = new HashMap<>();
                previewItem.put("rowNum", i + 1);
                previewItem.put("itemCode", itemCode);
                // 001009 前缀自动添加"线路板"位号，数量同步+1
                String displayDesignators = appendLineBoardDesignator(itemCode, designators);
                previewItem.put("designators", displayDesignators);
                int displayQty = (quantity != null ? quantity : 0);
                if (!displayDesignators.equals(designators)) {
                    displayQty++;
                }
                previewItem.put("quantity", displayQty);
                previewItem.put("valid", valid);
                previewItem.put("errorMessage", errorMessage != null ? errorMessage : "");

                // 查询MES获取物料名称和规格型号（有物料编码就查，不区分有效/无效行）
                if (!itemCode.isEmpty()) {
                    Map<String, Object> mesInfo = bomMesService.getItemInfo(itemCode);
                    previewItem.put("itemName", mesInfo.getOrDefault("itemName", ""));
                    previewItem.put("itemSpec", mesInfo.getOrDefault("itemSpec", ""));
                } else {
                    previewItem.put("itemName", "");
                    previewItem.put("itemSpec", "");
                }

                previewList.add(previewItem);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", errorCount == 0);
            result.put("preview", previewList);
            result.put("errorCount", errorCount);
            result.put("totalRows", previewList.size());
            return ResultUtil.success(result);

        } catch (IOException e) {
            log.error("预览Excel文件失败", e);
            return ResultUtil.fail("读取Excel文件失败：" + e.getMessage());
        }
    }

    /**
     * 解析Excel并校验，返回 BomDetail 列表。
     * 如果有校验错误则返回 null（调用方需自行处理错误展示）。
     */
    @SuppressWarnings("unchecked")
    private List<BomDetail> parseAndValidateExcel(Long bomId, MultipartFile file) throws IOException {
        List<BomDetail> detailList = new ArrayList<>();

        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<List<Object>> rows = reader.read();
            if (CollUtil.isEmpty(rows) || rows.size() < 2) {
                return detailList;
            }

            List<Object> header = rows.get(0);
            if (header.size() < 3
                    || !"物料编码".equals(String.valueOf(header.get(0)).trim())
                    || !"位号".equals(String.valueOf(header.get(1)).trim())
                    || !"数量".equals(String.valueOf(header.get(2)).trim())) {
                return null;
            }

            boolean hasError = false;
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
                        hasError = true;
                        continue;
                    }
                }

                if (itemCode.isEmpty()) {
                    hasError = true;
                    continue;
                }

                int designatorCount = 0;
                if (!designators.isEmpty()) {
                    designatorCount = designators.split(",").length;
                }

                if (quantity == null) {
                    quantity = designatorCount;
                } else if (!quantity.equals(designatorCount)) {
                    hasError = true;
                    continue;
                }

                BomDetail detail = new BomDetail();
                detail.setBomId(bomId);
                detail.setItemCode(itemCode);
                // 001009 前缀自动添加"线路板"位号，数量同步+1
                String finalDesignators = appendLineBoardDesignator(itemCode, designators);
                detail.setDesignators(finalDesignators);
                int finalQty = (quantity != null ? quantity : 0);
                if (!finalDesignators.equals(designators)) {
                    finalQty++;
                }
                detail.setQuantity(finalQty);
                detail.setSortOrder(i);
                detail.setStatus(1);

                Map<String, Object> mesInfo = bomMesService.getItemInfo(itemCode);
                detail.setItemName((String) mesInfo.get("itemName"));
                detail.setItemSpec((String) mesInfo.get("itemSpec"));

                detailList.add(detail);
            }

            if (hasError) {
                return null;
            }
        }
        return detailList;
    }

    /**
     * 解析Excel全部行，不做校验直接导入（仅跳过全空行）
     */
    private List<BomDetail> parseAllRows(Long bomId, MultipartFile file) throws IOException {
        List<BomDetail> detailList = new ArrayList<>();

        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<List<Object>> rows = reader.read();
            if (CollUtil.isEmpty(rows) || rows.size() < 2) {
                return detailList;
            }

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
                        quantity = 0;
                    }
                }
                if (quantity == null) {
                    quantity = (designators.isEmpty() ? 0 : designators.split(",").length);
                }

                BomDetail detail = new BomDetail();
                detail.setBomId(bomId);
                detail.setItemCode(itemCode);
                String finalDesignators = appendLineBoardDesignator(itemCode, designators);
                detail.setDesignators(finalDesignators);
                int finalQty = quantity;
                if (!finalDesignators.equals(designators)) finalQty++;
                detail.setQuantity(finalQty);
                detail.setSortOrder(i);
                detail.setStatus(1);
                if (!itemCode.isEmpty()) {
                    Map<String, Object> mesInfo = bomMesService.getItemInfo(itemCode);
                    detail.setItemName((String) mesInfo.getOrDefault("itemName", ""));
                    detail.setItemSpec((String) mesInfo.getOrDefault("itemSpec", ""));
                }
                detailList.add(detail);
            }
        }
        return detailList;
    }

    /**
     * 解析Excel，跳过所有错误行，只返回通过校验的行（永远不会返回 null）
     */
    private List<BomDetail> parseAllValidRows(Long bomId, MultipartFile file) throws IOException {
        List<BomDetail> detailList = new ArrayList<>();

        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<List<Object>> rows = reader.read();
            if (CollUtil.isEmpty(rows) || rows.size() < 2) {
                return detailList;
            }

            List<Object> header = rows.get(0);
            if (header.size() < 3) {
                return detailList;
            }

            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                if (CollUtil.isEmpty(row) || row.stream().allMatch(Objects::isNull)) {
                    continue;
                }

                String itemCode = row.size() > 0 && row.get(0) != null ? String.valueOf(row.get(0)).trim() : "";
                if (itemCode.isEmpty()) continue;

                String designators = row.size() > 1 && row.get(1) != null ? String.valueOf(row.get(1)).trim() : "";
                Integer quantity = null;
                if (row.size() > 2 && row.get(2) != null) {
                    try {
                        quantity = Integer.parseInt(String.valueOf(row.get(2)).trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }

                int designatorCount = 0;
                if (!designators.isEmpty()) {
                    designatorCount = designators.split(",").length;
                }
                if (quantity == null) {
                    quantity = designatorCount;
                } else if (!quantity.equals(designatorCount)) {
                    continue;
                }

                BomDetail detail = new BomDetail();
                detail.setBomId(bomId);
                detail.setItemCode(itemCode);
                String finalDesignators = appendLineBoardDesignator(itemCode, designators);
                detail.setDesignators(finalDesignators);
                int finalQty = quantity;
                if (!finalDesignators.equals(designators)) finalQty++;
                detail.setQuantity(finalQty);
                detail.setSortOrder(i);
                detail.setStatus(1);
                Map<String, Object> mesInfo = bomMesService.getItemInfo(itemCode);
                detail.setItemName((String) mesInfo.get("itemName"));
                detail.setItemSpec((String) mesInfo.get("itemSpec"));
                detailList.add(detail);
            }
        }
        return detailList;
    }
}
