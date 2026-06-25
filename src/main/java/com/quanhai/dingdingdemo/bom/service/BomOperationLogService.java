package com.quanhai.dingdingdemo.bom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.bom.model.BomOperationLog;

import java.util.List;

/**
 * BOM操作日志表Service接口
 */
public interface BomOperationLogService extends IService<BomOperationLog> {

    /**
     * 根据BOM主表ID查询操作日志
     *
     * @param bomId BOM主表ID
     * @return 操作日志列表
     */
    List<BomOperationLog> listByBomId(Long bomId);
}
