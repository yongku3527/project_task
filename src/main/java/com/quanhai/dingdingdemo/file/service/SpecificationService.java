package com.quanhai.dingdingdemo.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.file.dto.SpecificationDTO;
import com.quanhai.dingdingdemo.file.model.Specification;

import java.util.List;

/**
 * 规格书Service接口
 */
public interface SpecificationService extends IService<Specification> {

    /**
     * 根据ID查询规格书及其文件信息
     */
    SpecificationDTO getSpecificationWithFiles(Long id);

    /**
     * 根据原材料ID查询规格书列表
     */
    List<SpecificationDTO> getSpecificationsByMaterialId(String materialId);

    /**
     * 获取所有规格书及其文件信息
     */
    List<SpecificationDTO> getAllSpecificationsWithFiles();
}