package com.quanhai.dingdingdemo.bom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.bom.mapper.BomOperationLogMapper;
import com.quanhai.dingdingdemo.bom.model.BomOperationLog;
import com.quanhai.dingdingdemo.bom.service.BomOperationLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BOM操作日志表Service实现类
 */
@Service
public class BomOperationLogServiceImpl extends ServiceImpl<BomOperationLogMapper, BomOperationLog> implements BomOperationLogService {

    @Override
    public List<BomOperationLog> listByBomId(Long bomId) {
        QueryWrapper<BomOperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bom_id", bomId);
        queryWrapper.orderByDesc("operate_time");
        return this.list(queryWrapper);
    }
}
