package com.quanhai.dingdingdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.mapper.MeetingMapper;
import com.quanhai.dingdingdemo.model.metting.Meeting;
import com.quanhai.dingdingdemo.service.MeetingService;
import org.springframework.stereotype.Service;

@Service
public class MeetingServiceImpl extends ServiceImpl<MeetingMapper, Meeting> implements MeetingService {
}
