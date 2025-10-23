package com.quanhai.dingdingdemo.satoken.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.satoken.model.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}