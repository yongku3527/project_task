package com.quanhai.dingdingdemo.model.metting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class MeetingItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer meetingInfoId;

    private String content;

    private Boolean isMarked;
}
