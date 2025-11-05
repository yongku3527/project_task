package com.quanhai.dingdingdemo.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
    
    /**
     * 分页查询物料类型
     * @param page 当前页码
     * @param size 每页大小
     * @param itemPrefix 物料前缀（可选）
     * @param typeName 类型名称（可选）
     * @return 分页结果
     */
    IPage<ItemType> getItemTypePage(Integer page, Integer size, String itemPrefix, String typeName);
}