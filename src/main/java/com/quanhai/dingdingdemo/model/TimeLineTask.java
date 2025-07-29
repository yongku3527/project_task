package com.quanhai.dingdingdemo.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
public class TimeLineTask {


    private String taskId;
    private String taskName;
    private String projectName;
}
