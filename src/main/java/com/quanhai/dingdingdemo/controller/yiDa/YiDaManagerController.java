package com.quanhai.dingdingdemo.controller.yiDa;

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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
