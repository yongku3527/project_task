package com.quanhai.dingdingdemo.service.impl;

import com.quanhai.dingdingdemo.model.TaskVo;
import com.quanhai.dingdingdemo.service.GanttChartService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GanttChartServiceImpl implements GanttChartService {


    @Override
    public Map<String, Map<LocalDate, List<TaskVo>>> getGanttData(List<TaskVo> taskVos,LocalDate startDate, LocalDate endDate) {
        return getDailyTasksByExecutor(taskVos,startDate,endDate);
    }


    /**
     * 获取指定时间范围内每个人每天的任务详情
     * @param taskVos 任务列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 包含每个人每天任务统计和详情的Map
     */
    private Map<String, Map<LocalDate, List<TaskVo>>> getDailyTasksByExecutor(
            List<TaskVo> taskVos, LocalDate startDate, LocalDate endDate) {

        if (taskVos == null || taskVos.isEmpty()) {
            return Collections.emptyMap();
        }

        // 创建日期范围列表
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        // 按执行者ID分组任务
        Map<String, List<TaskVo>> tasksByExecutor = taskVos.stream()
                .filter(task -> task.getExecutorId() != null && !task.getExecutorId().isEmpty())
                .collect(Collectors.groupingBy(TaskVo::getExecutorId));

        // 统计每个人每天的具体任务
        Map<String, Map<LocalDate, List<TaskVo>>> dailyTasksDetail = new HashMap<>();

        for (Map.Entry<String, List<TaskVo>> entry : tasksByExecutor.entrySet()) {
            String executorId = entry.getKey();
            List<TaskVo> executorTasks = entry.getValue();

            // 初始化该执行者每天的任务列表
            Map<LocalDate, List<TaskVo>> dailyTasks = new HashMap<>();
            for (LocalDate date : dateRange) {
                dailyTasks.put(date, new ArrayList<>());
            }

            // 计算每个任务在日期范围内的天数，并记录具体任务
            for (TaskVo task : executorTasks) {
                LocalDate taskStartDate = task.getStartDate() != null ? task.getStartDate() : startDate;
                LocalDate taskDueDate = task.getDueDate() != null ? task.getDueDate() : endDate;

                // 确保日期在范围内
                if (taskStartDate.isAfter(endDate) || taskDueDate.isBefore(startDate)) {
                    continue;
                }

                // 限制在日期范围内
                LocalDate actualStart = taskStartDate.isBefore(startDate) ? startDate : taskStartDate;
                LocalDate actualEnd = taskDueDate.isAfter(endDate) ? endDate : taskDueDate;

                // 为每个日期记录具体任务
                LocalDate current = actualStart;
                while (!current.isAfter(actualEnd)) {
                    dailyTasks.get(current).add(task);
                    current = current.plusDays(1);
                }
            }

            dailyTasksDetail.put(executorId, dailyTasks);
        }

        return dailyTasksDetail;
    }
}
