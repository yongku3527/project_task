package com.quanhai.dingdingdemo.satoken.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

/**
 * 直接获取用户权限list
 */
@Mapper
public interface AuthMapper {
    public ArrayList<String> getPermissionList(Long userId);

    ArrayList<String> getRoleList(Long userId);

}
