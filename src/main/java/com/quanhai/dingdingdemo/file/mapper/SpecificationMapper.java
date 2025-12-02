package com.quanhai.dingdingdemo.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.file.dto.SpecificationDTO;
import com.quanhai.dingdingdemo.file.model.Specification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 规格书Mapper接口
 */
@Mapper
public interface SpecificationMapper extends BaseMapper<Specification> {

    /**
     * 根据ID查询规格书及其文件信息
     */
    SpecificationDTO selectSpecificationById(@Param("id") Long id);

    /**
     * 根据原材料ID查询规格书列表
     */
    List<SpecificationDTO> selectSpecificationsByMaterialId(@Param("materialId") String materialId);

    /**
     * 查询所有规格书及其文件信息
     */
    List<SpecificationDTO> selectAllSpecificationsWithFiles();
}