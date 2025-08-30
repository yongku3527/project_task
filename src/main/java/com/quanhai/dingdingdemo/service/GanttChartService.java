package com.quanhai.dingdingdemo.service;

import com.quanhai.dingdingdemo.model.TaskVo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GanttChartService {


    Map<String, Map<LocalDate, List<TaskVo>>> getGanttData(List<TaskVo> taskVos,LocalDate startDate, LocalDate endDate);
}
