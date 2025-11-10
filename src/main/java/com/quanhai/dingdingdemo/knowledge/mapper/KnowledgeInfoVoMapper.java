package com.quanhai.dingdingdemo.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.knowledge.model.vo.KnowledgeInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 经验库信息VO Mapper接口
 */
@Mapper
public interface KnowledgeInfoVoMapper extends BaseMapper<KnowledgeInfoVo> {

    /**
     * 根据用户角色查询完整经验库信息列表（包含文件信息和完成状态）
     * @param roleList 角色列表
     * @param userId 用户ID
     * @return 完整经验库信息列表
     */
    List<KnowledgeInfoVo> selectVoListByRole(@Param("roleList") List<String> roleList, @Param("userId") String userId);

    /**
     * 根据ID查询完整经验库信息（包含文件信息和完成状态）
     * @param id 经验库ID
     * @param userId 用户ID
     * @return 完整经验库信息
     */
    KnowledgeInfoVo selectVoById(@Param("id") Long id, @Param("userId") String userId);

    /**
     * 根据用户角色和完成状态查询完整经验库信息列表
     * @param roleList 角色列表
     * @param userId 用户ID
     * @param completionStatus 完成状态
     * @return 完整经验库信息列表
     */
    List<KnowledgeInfoVo> selectVoListByRoleAndStatus(@Param("roleList") List<String> roleList, 
                                                    @Param("userId") String userId, 
                                                    @Param("completionStatus") Integer completionStatus);


}