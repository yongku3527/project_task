package com.quanhai.dingdingdemo.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.mapper.yiDa.PrcsMapper;
import com.quanhai.dingdingdemo.mapper.yiDa.ReminderInfoMapper;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.yiDa.CodeAndPrcsIns;
import com.quanhai.dingdingdemo.model.yiDa.ReminderInfo;
import com.quanhai.dingdingdemo.service.TaskService;
import com.quanhai.dingdingdemo.service.msg.MsgService;
import com.quanhai.dingdingdemo.service.yiDa.PrcsServiceImpl;
import com.quanhai.dingdingdemo.tools.CommonTools;
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

//@Component
public class yiDaTask {
    @Autowired
    private PrcsServiceImpl prcsService;

    @Autowired
    private PrcsMapper prcsMapper;

    @Autowired
    private CommonTools commonTools;

    @Autowired
    private MailConfig mailConfig;

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
                    continue;
                }

                prcsMapper.deleteById(codeAndPrcsIns.getId());
                logger.info("创建新流程 projectNum："+itemNum+"   流程实例号："+interfaceId+"\n");

            }else {
                logger.info("不做任何处理 projectNum："+itemNum+"\n");
            }
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
