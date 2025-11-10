package com.quanhai.dingdingdemo.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.knowledge.model.pojo.KnowledgeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 经验库Mapper接口
 */
@Mapper
public interface KnowledgeInfoMapper extends BaseMapper<KnowledgeInfo> {

    /**
     * 根据角色列表查询经验库信息
     * @param roleList 角色列表
     * @return 经验库信息列表
     */
    List<KnowledgeInfo> selectListByRoles(@Param("roleList") List<String> roleList);

    /**
     * 根据ID和角色列表查询经验库信息
     * @param id 主键ID
     * @param roleList 角色列表
     * @return 经验库信息
     */
    KnowledgeInfo selectByIdAndRoles(@Param("id") Long id, @Param("roleList") List<String> roleList);
    
    /**
     * 获取所有不重复的失效模式
     * @return 失效模式列表
     */
    List<String> selectDistinctFailureModes();
    
    /**
     * 获取所有不重复的问题来源
     * @return 问题来源列表
     */
    List<String> selectDistinctIssueSources();
    
    /**
     * 获取所有不重复的产品机型
     * @return 产品机型列表
     */
    List<String> selectDistinctProductModels();
    
    /**
     * 获取所有不重复的产品分类
     * @return 产品分类列表
     */
    List<String> selectDistinctProductCategories();
}