package com.quanhai.dingdingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.model.TimeLineTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimeLineMapper extends BaseMapper<TimeLineTask> {

}
