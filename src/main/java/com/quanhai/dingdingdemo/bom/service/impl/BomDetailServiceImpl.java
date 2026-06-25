package com.quanhai.dingdingdemo.bom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.bom.mapper.BomDetailMapper;
import com.quanhai.dingdingdemo.bom.model.BomDetail;
import com.quanhai.dingdingdemo.bom.service.BomDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BOM明细表Service实现类
 */
@Service
public class BomDetailServiceImpl extends ServiceImpl<BomDetailMapper, BomDetail> implements BomDetailService {

    @Override
    public List<BomDetail> listByBomId(Long bomId) {
        QueryWrapper<BomDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bom_id", bomId);
        queryWrapper.orderByAsc("sort_order", "id");
        return this.list(queryWrapper);
    }
}
