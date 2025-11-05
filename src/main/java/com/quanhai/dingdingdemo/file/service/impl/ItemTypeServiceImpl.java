package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.mapper.ItemTypeMapper;
import com.quanhai.dingdingdemo.file.model.ItemType;
import com.quanhai.dingdingdemo.file.service.ItemTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 物料类型Service实现类
 */
@Service
public class ItemTypeServiceImpl extends ServiceImpl<ItemTypeMapper, ItemType> implements ItemTypeService {

    @Autowired
    private ItemTypeMapper itemTypeMapper;

    @Override
    public ItemType getByItemPrefix(String itemPrefix) {
        if (itemPrefix == null || itemPrefix.trim().isEmpty()) {
            return null;
        }
        
        QueryWrapper<ItemType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("item_prefix", itemPrefix);
        return itemTypeMapper.selectOne(queryWrapper);
    }
    
    @Override
    public IPage<ItemType> getItemTypePage(Integer page, Integer size, String itemPrefix, String typeName) {
        Page<ItemType> pageParam = new Page<>(page, size);
        QueryWrapper<ItemType> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(itemPrefix)) {
            queryWrapper.like("item_prefix", itemPrefix);
        }
        
        if (StringUtils.hasText(typeName)) {
            queryWrapper.like("type_name", typeName);
        }
        
        queryWrapper.orderByDesc("id");
        
        return itemTypeMapper.selectPage(pageParam, queryWrapper);
    }
}