package com.quanhai.dingdingdemo.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.file.model.ItemType;

/**
 * 物料类型Service接口
 */
public interface ItemTypeService extends IService<ItemType> {
    
    /**
     * 根据物料前缀查询物料类型
     * @param itemPrefix 物料前缀
     * @return 物料类型
     */
    ItemType getByItemPrefix(String itemPrefix);
}