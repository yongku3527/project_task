package com.quanhai.dingdingdemo.knowledge.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.knowledge.mapper.KnowledgeInfoMapper;
import com.quanhai.dingdingdemo.knowledge.mapper.KnowledgeInfoVoMapper;
import com.quanhai.dingdingdemo.knowledge.model.pojo.KnowledgeInfo;
import com.quanhai.dingdingdemo.knowledge.model.vo.KnowledgeInfoVo;
import com.quanhai.dingdingdemo.knowledge.service.KnowledgeInfoService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 经验库Service实现类
 */
@Service
public class KnowledgeInfoServiceImpl extends ServiceImpl<KnowledgeInfoMapper, KnowledgeInfo> implements KnowledgeInfoService {

    @Resource
    private KnowledgeInfoVoMapper knowledgeInfoVoMapper;

    @Override
    public List<KnowledgeInfoVo> getListByRole() {
        // 获取当前用户角色列表
        List<String> roleList = StpUtil.getRoleList();
        
        // 如果用户没有角色，返回空列表
        if (roleList == null || roleList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 调用Mapper层查询与用户角色匹配的数据
        List<KnowledgeInfo> knowledgeInfoList = baseMapper.selectListByRoles(roleList);
        
        // 转换为VO对象
        return knowledgeInfoList.stream().map(knowledgeInfo -> {
            KnowledgeInfoVo vo = new KnowledgeInfoVo();
            BeanUtils.copyProperties(knowledgeInfo, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public KnowledgeInfoVo getById(Long id) {
        // 获取当前用户角色列表
        List<String> roleList = StpUtil.getRoleList();
        
        // 如果用户没有角色，返回null
        if (roleList == null || roleList.isEmpty()) {
            return null;
        }
        
        // 调用Mapper层查询指定ID且与用户角色匹配的数据
        KnowledgeInfo knowledgeInfo = baseMapper.selectByIdAndRoles(id, roleList);
        
        if (knowledgeInfo == null) {
            return null;
        }
        
        // 转换为VO对象
        KnowledgeInfoVo vo = new KnowledgeInfoVo();
        BeanUtils.copyProperties(knowledgeInfo, vo);
        return vo;
    }

    @Override
    public boolean save(KnowledgeInfo entity) {
        // 获取当前用户角色列表
        List<String> roleList = StpUtil.getRoleList();
        
        // 如果用户没有角色，不允许保存
        if (roleList == null || roleList.isEmpty()) {
            throw new RuntimeException("用户没有角色权限，无法保存数据");
        }
        
        // 将角色列表设置到实体类中
        entity.setRole(String.join(",", roleList));
        
        // 调用父类方法保存
        return super.save(entity);
    }

    @Override
    public boolean updateById(KnowledgeInfo entity) {
        // 获取当前用户角色列表
        List<String> roleList = StpUtil.getRoleList();
        
        // 如果用户没有角色，不允许更新
        if (roleList == null || roleList.isEmpty()) {
            throw new RuntimeException("用户没有角色权限，无法更新数据");
        }
        
        // 检查原数据是否存在且用户有权限修改
        KnowledgeInfo originalData = baseMapper.selectByIdAndRoles(entity.getId(), roleList);
        if (originalData == null) {
            throw new RuntimeException("无权限修改该数据或数据不存在");
        }
        
        // 保持原有的角色信息不变
        entity.setRole(originalData.getRole());

        // 调用父类方法更新
        return super.updateById(entity);
    }

    @Override
    public boolean removeById(KnowledgeInfo entity) {
        // 获取当前用户角色列表
        List<String> roleList = StpUtil.getRoleList();
        
        // 如果用户没有角色，不允许删除
        if (roleList == null || roleList.isEmpty()) {
            throw new RuntimeException("用户没有角色权限，无法删除数据");
        }
        
        // 检查数据是否存在且用户有权限删除
        KnowledgeInfo originalData = baseMapper.selectByIdAndRoles(entity.getId(), roleList);
        if (originalData == null) {
            throw new RuntimeException("无权限删除该数据或数据不存在");
        }
        
        // 调用父类方法删除
        return super.removeById(entity);
    }

    @Override
    public List<KnowledgeInfoVo> getCompleteListByRole() {
        // 获取当前用户角色列表和用户ID
        List<String> roleList = StpUtil.getRoleList();
        String userId = StpUtil.getLoginId().toString();
        
        // 如果用户没有角色，返回空列表
        if (roleList == null || roleList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 调用KnowledgeInfoVoMapper查询完整数据
        return knowledgeInfoVoMapper.selectVoListByRole(roleList, userId);
    }

    @Override
    public KnowledgeInfoVo getCompleteById(Long id) {
        // 获取当前用户角色列表和用户ID
        List<String> roleList = StpUtil.getRoleList();
        String userId = StpUtil.getLoginId().toString();
        
        // 如果用户没有角色，返回null
        if (roleList == null || roleList.isEmpty()) {
            return null;
        }
        
        // 调用KnowledgeInfoVoMapper查询完整数据
        return knowledgeInfoVoMapper.selectVoById(id, userId);
    }

    @Override
    public List<KnowledgeInfoVo> getCompleteListByStatus(Integer completionStatus) {
        // 获取当前用户角色列表和用户ID
        List<String> roleList = StpUtil.getRoleList();
        String userId = StpUtil.getLoginId().toString();
        
        // 如果用户没有角色，返回空列表
        if (roleList == null || roleList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 调用KnowledgeInfoVoMapper查询完整数据
        return knowledgeInfoVoMapper.selectVoListByRoleAndStatus(roleList, userId, completionStatus);
    }
    
    @Override
    public List<String> getFailureModes() {
        return baseMapper.selectDistinctFailureModes();
    }
    
    @Override
    public List<String> getIssueSources() {
        return baseMapper.selectDistinctIssueSources();
    }
    
    @Override
    public List<String> getProductModels() {
        return baseMapper.selectDistinctProductModels();
    }
    
    @Override
    public List<String> getProductCategories() {
        return baseMapper.selectDistinctProductCategories();
    }
}