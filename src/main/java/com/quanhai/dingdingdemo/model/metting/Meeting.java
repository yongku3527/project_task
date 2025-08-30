package com.quanhai.dingdingdemo.model.metting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

@Data
public class Meeting {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String machineType;

    private String Factory;

    private String salesPerson;

    private Integer status;

    @TableField(exist = false)
    private List<MeetingInfo> meetingInfoList;

}
