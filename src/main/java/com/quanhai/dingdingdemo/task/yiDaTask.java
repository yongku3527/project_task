package com.quanhai.dingdingdemo.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.mapper.yiDa.PrcsMapper;
import com.quanhai.dingdingdemo.mapper.yiDa.ReminderInfoMapper;
import com.quanhai.dingdingdemo.mapper.yiDa.YiDaPrcsCreationLogMapper;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.yiDa.CodeAndPrcsIns;
import com.quanhai.dingdingdemo.model.yiDa.ReminderInfo;
import com.quanhai.dingdingdemo.model.yiDa.YiDaPrcsCreationLog;
import com.quanhai.dingdingdemo.service.TaskService;
import com.quanhai.dingdingdemo.service.msg.MsgService;
import com.quanhai.dingdingdemo.service.yiDa.PrcsServiceImpl;
import com.quanhai.dingdingdemo.tools.CommonTools;
import com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdResponseBody;
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class yiDaTask {
    @Autowired
    private PrcsServiceImpl prcsService;

    @Autowired
    private PrcsMapper prcsMapper;

    @Autowired
    private CommonTools commonTools;

    @Autowired
    private MailConfig mailConfig;

    @Autowired
    private YiDaPrcsCreationLogMapper prcsCreationLogMapper;

    private Logger logger = LoggerFactory.getLogger(yiDaTask.class);

    @Scheduled(cron = "0 5 8-18 * * *")
    public void checkItemNumber() {

        logger.info("=====================checkItemNumber定时任务执行======================");
        for (CodeAndPrcsIns codeAndPrcsIns : prcsMapper.getAll()) {
            logger.info("当前执行参数：ItemCode: "+codeAndPrcsIns.getItemCode()+",InstanceCode: "+codeAndPrcsIns.getPrcsInstanceCode());
            //虽说这里写的是ItemNum旦其实是获取的receiveNum ！！！
            BigDecimal itemNum = new BigDecimal(0);
            try {
                itemNum = prcsService.getItemNum(codeAndPrcsIns.getItemCode());
            }catch (Exception e) {
                logger.error("Mes获取项目数量失败："+e.getMessage()+"\n");
                continue;
            }


            if (itemNum.compareTo(new BigDecimal(0)) > 0) {
                String interfaceId = null;
                try {
                    interfaceId = prcsService.creatNewInterface(codeAndPrcsIns.getPrcsInstanceCode());
                } catch (Exception e) {
                    logger.error(e.getMessage()+"\n");
                        try {
                            commonTools.send("创建新流程失败(定时任务)",mailConfig.getBadTo(),codeAndPrcsIns.toString()+"\n报错信息： "+e.getMessage());
                        } catch (EmailException ex) {

                            logger.error("报错邮件,发送失败 ："+ex.getMessage()+"\n");
                        }
                    // 更新日志表为失败
                    updatePrcsCreationLogStatus(codeAndPrcsIns.getItemCode(), codeAndPrcsIns.getPrcsInstanceCode(), 2, e.getMessage());
                    continue;
                }

                prcsMapper.deleteById(codeAndPrcsIns.getId());
                // 更新日志表为成功
                updatePrcsCreationLogStatus(codeAndPrcsIns.getItemCode(), codeAndPrcsIns.getPrcsInstanceCode(), 1, null, interfaceId);
                logger.info("创建新流程 projectNum："+itemNum+"   流程实例号："+interfaceId+"\n");

            }else {
                logger.info("不做任何处理 projectNum："+itemNum+"\n");
            }
        }
    }

    /**
     * 更新流程创建日志表状态（供定时任务使用）
     */
    private void updatePrcsCreationLogStatus(String itemCode, String prcsInstanceCode, Integer status, String errorMsg) {
        updatePrcsCreationLogStatus(itemCode, prcsInstanceCode, status, errorMsg, null);
    }

    private void updatePrcsCreationLogStatus(String itemCode, String prcsInstanceCode, Integer status, String errorMsg, String newInstanceId) {
        LambdaQueryWrapper<YiDaPrcsCreationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YiDaPrcsCreationLog::getItemCode, itemCode)
               .eq(YiDaPrcsCreationLog::getPrcsInstanceCode, prcsInstanceCode)
               .eq(YiDaPrcsCreationLog::getStatus, 0)
               .orderByDesc(YiDaPrcsCreationLog::getCreateTime)
               .last("limit 1");
        YiDaPrcsCreationLog log = prcsCreationLogMapper.selectOne(wrapper);
        if (log != null) {
            log.setStatus(status);
            if (errorMsg != null) log.setErrorMsg(errorMsg);
            if (newInstanceId != null) log.setNewInstanceId(newInstanceId);
            log.setUpdateTime(LocalDateTime.now());
            // 尝试填充发起人信息（失败不影响主流程）
            enrichOriginatorInfo(log);
            prcsCreationLogMapper.updateById(log);
        }
    }

    /**
     * 填充发起人信息（调用钉钉宜搭API，失败不抛异常）
     */
    private void enrichOriginatorInfo(YiDaPrcsCreationLog log) {
        try {
            GetInstanceByIdResponseBody body = prcsService.getInterfaceInfo(log.getPrcsInstanceCode());
            if (body != null && body.getOriginator() != null) {
                log.setOriginatorName(body.getOriginator().getName() != null
                        ? body.getOriginator().getName().toString() : null);
                log.setInstanceCreateTime(body.getCreateTimeGMT());
            }
        } catch (Exception e) {
            logger.warn("填充发起人信息失败（不影响主流程）: itemCode={}, error={}", log.getItemCode(), e.getMessage());
        }
    }


    @Autowired
    private ReminderInfoMapper reminderInfoMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private MsgService msgService;

//    @Scheduled(cron = "0 0/10 8-18 * * *")
    public void checkReminderInfo() {

        logger.info("=====================checkReminderInfo定时任务执行======================");

        // 1. 算边界
        LocalDateTime fourHoursAgo = LocalDateTime.now().minusHours(4);
//        LocalDateTime fourHoursAgo = LocalDateTime.now().minusSeconds(10);

        // 2. 查 createTime < 边界 的数据
        List<ReminderInfo> list = reminderInfoMapper.selectList(
                Wrappers.<ReminderInfo>lambdaQuery()
                        .lt(ReminderInfo::getCreateTime, fourHoursAgo)   // 早于 4 小时前
        );

        for (ReminderInfo reminderInfo : list) {
            //1.获取id信息
            String executorId = reminderInfo.getExecutorId();
            String pcbName = reminderInfo.getPcbName();

            String usernameAndPhone  = redisTemplate.opsForValue().get("Reminder:" + executorId);
            //没在redis中找到就跳过
            if (usernameAndPhone == null) {
                logger.info("在redis中未找到用户信息 executorId："+executorId);
                continue;
            }
            String[] usernameAndPhoneArr = usernameAndPhone.split(",");
            //3. 发送短信

            msgService.sendCustomMsg(usernameAndPhoneArr[1],"【山东泉海汽车科技有限公司】"+pcbName+"的原理图、PCB设计审批已超4小时未审批，请尽快处理。");
            logger.info("短信发送成功 手机号："+usernameAndPhoneArr[1]+"  用户名："+usernameAndPhoneArr[0]+"  pcbName："+pcbName);


            //将数据库中的数据删除
            reminderInfoMapper.deleteById(reminderInfo.getId());

        }


        logger.info("=====================checkReminderInfo定时任务执行===完毕===================");
    }
}
