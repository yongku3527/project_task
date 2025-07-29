package com.quanhai.dingdingdemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.TimeLineTask;
import com.quanhai.dingdingdemo.service.TaskService;
import com.quanhai.dingdingdemo.service.TimeLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@RestController
@RequestMapping("/TimeLine")
public class TimeLineController {

    @Autowired
    private TimeLineService timeLineService;

    @PostMapping("/addTimeLineTask")
    private Result addTimeLineTask(@RequestBody TimeLineTask timeLineTask) {
        try {
            timeLineService.save(timeLineTask);
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate")) {
                return ResultUtil.fail("请勿重复添加");
            }

            return ResultUtil.fail(e.getMessage());
        }
        return ResultUtil.success("ok");
    }

    @GetMapping("/getTimeLineTask")
    private Result getTimeLineTask() {
        List<TimeLineTask> list = null;
        try {
            list = timeLineService.list();
        } catch (Exception e) {

            return ResultUtil.fail(e.getMessage());
        }
        return ResultUtil.success(list);
    }

    @DeleteMapping("/removeTimeLineTask/{taskId}")
    private Result removeTimeLineTask(@PathVariable String taskId) {
        LambdaQueryWrapper<TimeLineTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TimeLineTask::getTaskId, taskId);

        try {
            timeLineService.remove(queryWrapper);
        } catch (Exception e) {
            return ResultUtil.success("removeTimeLineTask报错");
        }
        return ResultUtil.success("ok");
    }

}
