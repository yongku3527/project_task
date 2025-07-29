package com.quanhai.dingdingdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.mapper.TimeLineMapper;
import com.quanhai.dingdingdemo.model.TimeLineTask;
import com.quanhai.dingdingdemo.service.TimeLineService;
import org.springframework.stereotype.Service;

@Service
public class TimeLineServiceImpl extends ServiceImpl<TimeLineMapper, TimeLineTask> implements TimeLineService {


}
