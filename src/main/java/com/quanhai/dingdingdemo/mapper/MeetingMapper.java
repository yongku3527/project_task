package com.quanhai.dingdingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.model.metting.Meeting;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MeetingMapper extends BaseMapper<Meeting> {

    public List<Meeting> getMeetingList();

}
