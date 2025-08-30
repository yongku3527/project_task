package com.quanhai.dingdingdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.mapper.MeetingInfoMapper;
import com.quanhai.dingdingdemo.mapper.MeetingItemMapper;
import com.quanhai.dingdingdemo.model.metting.MeetingInfo;
import com.quanhai.dingdingdemo.model.metting.MeetingItem;
import com.quanhai.dingdingdemo.service.MeetingInfoService;
import com.quanhai.dingdingdemo.service.MeetingItemService;
import com.quanhai.dingdingdemo.service.MeetingService;
import org.springframework.stereotype.Service;

@Service
public class MeetingItemServiceImpl extends ServiceImpl<MeetingItemMapper, MeetingItem> implements MeetingItemService {
}
