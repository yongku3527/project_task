package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.dto.SpecificationDTO;
import com.quanhai.dingdingdemo.file.mapper.SpecificationMapper;
import com.quanhai.dingdingdemo.file.model.Specification;
import com.quanhai.dingdingdemo.file.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 规格书Service实现类
 */
@Service
public class SpecificationServiceImpl extends ServiceImpl<SpecificationMapper, Specification> implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    /**
     * 根据ID查询规格书及其文件信息
     */
    @Override
    public SpecificationDTO getSpecificationWithFiles(Long id) {
        return specificationMapper.selectSpecificationById(id);
    }

    /**
     * 根据原材料ID查询规格书列表
     */
    @Override
    public List<SpecificationDTO> getSpecificationsByMaterialId(String materialId) {
        return specificationMapper.selectSpecificationsByMaterialId(materialId);
    }

    /**
     * 获取所有规格书及其文件信息
     */
    @Override
    public List<SpecificationDTO> getAllSpecificationsWithFiles() {
        return specificationMapper.selectAllSpecificationsWithFiles();
    }
}