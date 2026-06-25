package com.quanhai.dingdingdemo.bom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.bom.model.BomDetail;

import java.util.List;

/**
 * BOM明细表Service接口
 */
public interface BomDetailService extends IService<BomDetail> {

    /**
     * 根据BOM主表ID查询明细列表
     *
     * @param bomId BOM主表ID
     * @return 明细列表
     */
    List<BomDetail> listByBomId(Long bomId);
}
