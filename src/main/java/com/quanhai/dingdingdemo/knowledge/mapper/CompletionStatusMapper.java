package com.quanhai.dingdingdemo.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.knowledge.model.pojo.CompletionStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 完成状态Mapper接口
 */
@Mapper
public interface CompletionStatusMapper extends BaseMapper<CompletionStatus> {

    /**
     * 根据用户ID查询完成状态列表
     * @param userId 用户ID
     * @return 完成状态列表
     */
    List<CompletionStatus> selectListByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和经验信息ID查询完成状态
     * @param userId 用户ID
     * @param knowledgeInfoId 经验信息ID
     * @return 完成状态
     */
    CompletionStatus selectByUserIdAndKnowledgeInfoId(@Param("userId") String userId, @Param("knowledgeInfoId") Long knowledgeInfoId);

    /**
     * 根据用户ID和完成状态查询完成状态列表
     * @param userId 用户ID
     * @param completionStatus 完成状态
     * @return 完成状态列表
     */
    List<CompletionStatus> selectListByUserIdAndStatus(@Param("userId") String userId, @Param("completionStatus") Integer completionStatus);
}