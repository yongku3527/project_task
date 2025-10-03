package com.quanhai.dingdingdemo.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.mapper.yiDa.PrcsMapper;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.yiDa.CodeAndPrcsIns;
import com.quanhai.dingdingdemo.service.TaskService;
import com.quanhai.dingdingdemo.service.yiDa.PrcsServiceImpl;
import com.quanhai.dingdingdemo.tools.CommonTools;
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// @Component
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
            BigDecimal itemNum = prcsService.getItemNum(codeAndPrcsIns.getItemCode());

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
}
