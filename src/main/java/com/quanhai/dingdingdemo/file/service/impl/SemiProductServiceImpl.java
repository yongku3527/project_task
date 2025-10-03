package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.mapper.SemiProductMapper;
import com.quanhai.dingdingdemo.file.model.SemiProduct;
import com.quanhai.dingdingdemo.file.service.SemiProductService;
import org.springframework.stereotype.Service;

/**
 * 半成品Service实现类
 */
@Service
public class SemiProductServiceImpl extends ServiceImpl<SemiProductMapper, SemiProduct> implements SemiProductService {
}