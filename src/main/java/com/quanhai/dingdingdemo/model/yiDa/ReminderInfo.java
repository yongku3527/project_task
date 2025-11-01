package com.quanhai.dingdingdemo.model.yiDa;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@TableName("reminder_info")
@AllArgsConstructor
@NoArgsConstructor
public class ReminderInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("pcb_name")
    private String pcbName;

    @TableField("executor_id")
    private String executorId;


    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
