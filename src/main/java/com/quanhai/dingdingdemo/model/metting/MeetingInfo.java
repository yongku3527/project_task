package com.quanhai.dingdingdemo.model.metting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

@Data
public class MeetingInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer meetingId;

    private String Date;

    private String content;

    private Boolean isMarked;
}
