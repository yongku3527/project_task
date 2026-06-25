package com.quanhai.dingdingdemo.bom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.bom.mapper.BomInfoMapper;
import com.quanhai.dingdingdemo.bom.model.BomInfo;
import com.quanhai.dingdingdemo.bom.service.BomInfoService;
import org.springframework.stereotype.Service;

/**
 * BOM信息主表Service实现类
 */
@Service
public class BomInfoServiceImpl extends ServiceImpl<BomInfoMapper, BomInfo> implements BomInfoService {
}
