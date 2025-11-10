package com.quanhai.dingdingdemo.knowledge.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.knowledge.mapper.CompletionStatusMapper;
import com.quanhai.dingdingdemo.knowledge.model.pojo.CompletionStatus;
import com.quanhai.dingdingdemo.knowledge.service.CompletionStatusService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 完成状态Service实现类
 */
@Service
public class CompletionStatusServiceImpl extends ServiceImpl<CompletionStatusMapper, CompletionStatus> implements CompletionStatusService {

    @Override
    public List<CompletionStatus> getListByUserId(String userId) {
        return baseMapper.selectListByUserId(userId);
    }

    @Override
    public CompletionStatus getByUserIdAndKnowledgeInfoId(String userId, Long knowledgeInfoId) {
        return baseMapper.selectByUserIdAndKnowledgeInfoId(userId, knowledgeInfoId);
    }

    @Override
    public List<CompletionStatus> getListByUserIdAndStatus(String userId, Integer completionStatus) {
        return baseMapper.selectListByUserIdAndStatus(userId, completionStatus);
    }

    @Override
    public boolean updateOrCreateStatus(String userId, Long knowledgeInfoId, Integer completionStatus) {
        // 查询是否已存在记录
        CompletionStatus existingStatus = baseMapper.selectByUserIdAndKnowledgeInfoId(userId, knowledgeInfoId);
        
        if (existingStatus != null) {
            // 更新现有记录
            existingStatus.setCompletionStatus(completionStatus);
            return super.updateById(existingStatus);
        } else {
            // 创建新记录
            CompletionStatus newStatus = new CompletionStatus();
            newStatus.setUserId(userId);
            newStatus.setKnowledgeInfoId(knowledgeInfoId);
            newStatus.setCompletionStatus(completionStatus);
            return super.save(newStatus);
        }
    }
}