package com.quanhai.dingdingdemo.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.quanhai.dingdingdemo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// @Component
public class CronTask {
    @Autowired
    private TaskService taskService;
    @Scheduled(cron = "0 0 7-19 * * ?")
    public void setTaskJob() {

        try {
            LocalDateTime now = LocalDateTime.now();
            System.out.println("当前执行时间 = " + now);
            taskService.setTaskVoToRedis();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}