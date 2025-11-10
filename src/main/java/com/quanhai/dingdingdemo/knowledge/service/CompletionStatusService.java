package com.quanhai.dingdingdemo.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.knowledge.model.pojo.CompletionStatus;

import java.util.List;

/**
 * 完成状态Service接口
 */
public interface CompletionStatusService extends IService<CompletionStatus> {

    /**
     * 根据用户ID查询完成状态列表
     * @param userId 用户ID
     * @return 完成状态列表
     */
    List<CompletionStatus> getListByUserId(String userId);

    /**
     * 根据用户ID和经验信息ID查询完成状态
     * @param userId 用户ID
     * @param knowledgeInfoId 经验信息ID
     * @return 完成状态
     */
    CompletionStatus getByUserIdAndKnowledgeInfoId(String userId, Long knowledgeInfoId);

    /**
     * 根据用户ID和完成状态查询完成状态列表
     * @param userId 用户ID
     * @param completionStatus 完成状态
     * @return 完成状态列表
     */
    List<CompletionStatus> getListByUserIdAndStatus(String userId, Integer completionStatus);

    /**
     * 更新或创建完成状态
     * @param userId 用户ID
     * @param knowledgeInfoId 经验信息ID
     * @param completionStatus 完成状态
     * @return 是否成功
     */
    boolean updateOrCreateStatus(String userId, Long knowledgeInfoId, Integer completionStatus);
}