package com.quanhai.dingdingdemo.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.knowledge.model.pojo.KnowledgeInfo;
import com.quanhai.dingdingdemo.knowledge.model.vo.KnowledgeInfoVo;

import java.util.List;

/**
 * 经验库Service接口
 */
public interface KnowledgeInfoService extends IService<KnowledgeInfo> {

    /**
     * 根据用户角色权限获取经验库列表
     */
    List<KnowledgeInfoVo> getListByRole();

    /**
     * 根据ID查询经验库信息（带权限检查）
     */
    KnowledgeInfoVo getById(Long id);

    /**
     * 获取完整经验库信息列表（包含文件信息和完成状态）
     */
    List<KnowledgeInfoVo> getCompleteListByRole();

    /**
     * 根据ID获取完整经验库信息（包含文件信息和完成状态）
     */
    KnowledgeInfoVo getCompleteById(Long id);

    /**
     * 根据完成状态获取完整经验库信息列表
     */
    List<KnowledgeInfoVo> getCompleteListByStatus(Integer completionStatus);
}