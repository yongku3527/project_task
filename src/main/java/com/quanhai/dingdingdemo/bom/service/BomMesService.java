package com.quanhai.dingdingdemo.bom.service;

import java.util.Map;

/**
 * BOM相关MES接口Service
 */
public interface BomMesService {

    /**
     * 根据物料编码查询MES物料信息
     *
     * @param itemCode 物料编码
     * @return 物料信息（itemName, itemSpec等）
     */
    Map<String, Object> getItemInfo(String itemCode);
}
