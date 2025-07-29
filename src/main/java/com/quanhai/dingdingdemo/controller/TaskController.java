package com.quanhai.dingdingdemo.controller;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dingTask")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/getTaskInfo")
    public Result getTaskInfo(){

        return taskService.getTaskInfo();
    }

    @GetMapping("/getProjectInfo")
    public Result getProjectInfo(){

        try {
            return taskService.getProjectInfo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/getStatusInfo")
    public Result getStatusInfo(){

        try {
            return taskService.getStatusInfo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/getProjectTime")
    public Result getProjectTime(){

        return taskService.getProjectTime();
    }




}
