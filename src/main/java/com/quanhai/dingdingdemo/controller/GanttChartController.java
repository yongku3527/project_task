package com.quanhai.dingdingdemo.controller;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.TaskVo;
import com.quanhai.dingdingdemo.service.GanttChartService;
import com.quanhai.dingdingdemo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gantt")
public class GanttChartController {

    @Autowired
    private GanttChartService ganttChartService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/getGanttData")
    public Result getGanttData(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        System.out.println("startDate = " + startDate + ", endDate = " + endDate);
        List<TaskVo> taskVos = null;
        try {
             taskVos = (List<TaskVo>) redisTemplate.opsForValue().get("taskVos");
        } catch (Exception e) {
            return ResultUtil.fail("查找taskVos报错");
        }

        Map<String, Map<LocalDate, List<TaskVo>>> dailyTasksDetail = null;
        try {
            dailyTasksDetail = ganttChartService.getGanttData(taskVos,startDate,endDate);
        } catch (Exception e) {
            return ResultUtil.fail("getGanttData 方法报错");
        }

        return ResultUtil.success(dailyTasksDetail);
    }

}
