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
}
