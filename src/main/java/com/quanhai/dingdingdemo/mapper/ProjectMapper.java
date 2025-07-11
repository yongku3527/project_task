package com.quanhai.dingdingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.model.Project;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    // 继承BaseMapper即可获得基本CRUD方法
}