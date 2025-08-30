package com.quanhai.dingdingdemo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Data
public class TaskVo {

    //任务名
    private String taskName;
    //任务id
    private String taskId;
    //执行者id
    private String executorId;
    //执行者名称
    private String executorName;
    //部门Map
    private List<Integer> deptIdList;
    private List<String> deptNameList;

    //任务分组id
    public String taskListId;
    //任务分组名称
    public String taskListName;

    //父任务id
    public String parentTaskId;

    private String taskStatus;
    //项目名
    private String ProjectName;
    //开始时间
    //任务状态
    @DateTimeFormat(pattern = "yyyy-MM-dd") //此注解用来接收字符串类型的参数封装成LocalDateTime类型
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8", shape = JsonFormat.Shape.STRING) //此注解将date类型数据转成字符串响应出去
    @JsonDeserialize(using = LocalDateDeserializer.class)		// 反序列化
    @JsonSerialize(using = LocalDateSerializer.class)		// 序列化
    private LocalDate startDate;
    //结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd") //此注解用来接收字符串类型的参数封装成LocalDateTime类型
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8", shape = JsonFormat.Shape.STRING) //此注解将date类型数据转成字符串响应出去
    @JsonDeserialize(using = LocalDateDeserializer.class)		// 反序列化
    @JsonSerialize(using = LocalDateSerializer.class)		// 序列化
    private LocalDate dueDate;
    //剩余天数
    private Integer remainTimeDays;
    //是否已完成
    private Boolean isDone;


}
