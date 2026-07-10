package com.quanhai.dingdingdemo.controller.yiDa;

import cn.hutool.json.JSONUtil;
import com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdResponseBody;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.mapper.yiDa.YiDaMailLogMapper;
import com.quanhai.dingdingdemo.mapper.yiDa.YiDaPrcsCreationLogMapper;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.yiDa.YiDaMailLog;
import com.quanhai.dingdingdemo.model.yiDa.YiDaPrcsCreationLog;
import com.quanhai.dingdingdemo.service.yiDa.PrcsServiceImpl;
import com.quanhai.dingdingdemo.tools.CommonTools;
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 宜搭管理控制器 - 流程创建记录 & 邮件发送记录
 */
@RestController
@RequestMapping("/yi-da")
public class YiDaManagerController {

    private Logger logger = LoggerFactory.getLogger(YiDaManagerController.class);

    @Autowired
    private YiDaPrcsCreationLogMapper prcsCreationLogMapper;

    @Autowired
    private YiDaMailLogMapper mailLogMapper;

    @Autowired
    private PrcsServiceImpl prcsService;

    @Autowired
    private CommonTools commonTools;

    @Autowired
    private MailConfig mailConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 流程创建记录 ====================

    /**
     * 分页查询流程创建记录
     */
    @GetMapping("/prcs/list")
    public Result<Page<YiDaPrcsCreationLog>> listPrcsCreationLog(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<YiDaPrcsCreationLog> wrapper = new LambdaQueryWrapper<>();
        if (itemCode != null && !itemCode.isBlank()) {
            wrapper.like(YiDaPrcsCreationLog::getItemCode, itemCode);
        }
        if (status != null) {
            wrapper.eq(YiDaPrcsCreationLog::getStatus, status);
        }
        wrapper.orderByDesc(YiDaPrcsCreationLog::getCreateTime);

        Page<YiDaPrcsCreationLog> result = prcsCreationLogMapper.selectPage(
                new Page<>(page, pageSize), wrapper);
        return ResultUtil.success(result);
    }

    /**
     * 手动触发流程创建
     */
    @PostMapping("/prcs/trigger/{id}")
    public Result<String> triggerPrcsCreation(@PathVariable Long id) {
        YiDaPrcsCreationLog log = prcsCreationLogMapper.selectById(id);
        if (log == null) {
            return ResultUtil.fail("记录不存在");
        }
        if (log.getStatus() != null && log.getStatus() == 1) {
            return ResultUtil.fail("该流程已创建成功，无需重复触发");
        }

        try {
            logger.info("手动触发流程创建: id={}, itemCode={}, prcsInstance={}", id, log.getItemCode(), log.getPrcsInstanceCode());
            String newInstanceId = prcsService.creatNewInterface(log.getPrcsInstanceCode());

            // 更新记录为成功
            log.setNewInstanceId(newInstanceId);
            log.setStatus(1);
            log.setErrorMsg(null);
            log.setUpdateTime(LocalDateTime.now());
            prcsCreationLogMapper.updateById(log);

            return ResultUtil.success("流程创建成功，新流程实例ID: " + newInstanceId);
        } catch (Exception e) {
            logger.error("手动触发流程创建失败: id={}, error={}", id, e.getMessage());

            // 更新记录为失败
            log.setStatus(2);
            log.setErrorMsg(e.getMessage());
            log.setUpdateTime(LocalDateTime.now());
            prcsCreationLogMapper.updateById(log);

            try {
                commonTools.send("手动触发流程创建失败", mailConfig.getBadTo(),
                        "记录ID: " + id + "\n物料编码: " + log.getItemCode() + "\n报错: " + e.getMessage());
            } catch (EmailException ex) {
                logger.error("报错邮件发送失败: {}", ex.getMessage());
            }

            return ResultUtil.fail("流程创建失败: " + e.getMessage());
        }
    }

    /**
     * 删除流程创建记录
     */
    @DeleteMapping("/prcs/{id}")
    public Result<String> deletePrcsCreationLog(@PathVariable Long id) {
        YiDaPrcsCreationLog log = prcsCreationLogMapper.selectById(id);
        if (log == null) {
            return ResultUtil.fail("记录不存在");
        }
        prcsCreationLogMapper.deleteById(id);
        // 同时清除 Redis 缓存
        redisTemplate.delete("yiDa:instance-detail:" + id);
        logger.info("删除流程创建记录: id={}, itemCode={}", id, log.getItemCode());
        return ResultUtil.success("删除成功");
    }

    /**
     * 获取原始宜搭实例详情（Redis缓存，7天过期）
     */
    @GetMapping("/prcs/instance-detail/{logId}")
    public Result<Map<String, Object>> getPrcsInstanceDetail(@PathVariable Long logId) {
        YiDaPrcsCreationLog log = prcsCreationLogMapper.selectById(logId);
        if (log == null) {
            return ResultUtil.fail("记录不存在");
        }

        // 先查 Redis 缓存
        String cacheKey = "yiDa:instance-detail:" + logId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedDetail = (Map<String, Object>) cached;
            return ResultUtil.success(cachedDetail);
        }

        try {
            GetInstanceByIdResponseBody body = prcsService.getInterfaceInfo(log.getPrcsInstanceCode());
            if (body == null) {
                return ResultUtil.fail("获取宜搭实例详情失败");
            }

            Map<String, Object> detail = new LinkedHashMap<>();
            // 发起人信息
            if (body.getOriginator() != null) {
                Map<String, Object> originator = new LinkedHashMap<>();
                originator.put("userId", body.getOriginator().getUserId());
                originator.put("name", body.getOriginator().getName());
                detail.put("originator", originator);
            }
            // 创建时间
            detail.put("createTimeGMT", body.getCreateTimeGMT());
            // 修改时间
            detail.put("modifiedTimeGMT", body.getModifiedTimeGMT());
            // 表单数据
            if (body.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> formData = prcsService.replaceEmployeeFieldIds(
                        (Map<String, Object>) body.getData());
                detail.put("formData", PrcsServiceImpl.removePrefix(formData));
            }

            // 写入 Redis 缓存，7天过期
            redisTemplate.opsForValue().set(cacheKey, detail, 1, TimeUnit.DAYS);

            return ResultUtil.success(detail);
        } catch (Exception e) {
            logger.error("获取宜搭实例详情失败: logId={}, error={}", logId, e.getMessage());
            return ResultUtil.fail("获取宜搭实例详情失败: " + e.getMessage());
        }
    }

    // ==================== 邮件发送记录 ====================

    /**
     * 分页查询邮件发送记录
     */
    @GetMapping("/mail/list")
    public Result<Page<YiDaMailLog>> listMailLog(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String pcbName,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<YiDaMailLog> wrapper = new LambdaQueryWrapper<>();
        if (pcbName != null && !pcbName.isBlank()) {
            wrapper.like(YiDaMailLog::getPcbName, pcbName);
        }
        if (status != null) {
            wrapper.eq(YiDaMailLog::getStatus, status);
        }
        wrapper.orderByDesc(YiDaMailLog::getSendTime);

        Page<YiDaMailLog> result = mailLogMapper.selectPage(
                new Page<>(page, pageSize), wrapper);
        return ResultUtil.success(result);
    }
}
