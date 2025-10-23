package com.quanhai.dingdingdemo.satoken.service.impl;

import com.quanhai.dingdingdemo.satoken.mapper.AuthMapper;
import com.quanhai.dingdingdemo.satoken.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthMapper authMapper;


    @Override
    public ArrayList<String> getPermissionByUserId(Long userId) {


        return authMapper.getPermissionList(userId);
    }

    /*
        根据用户id获取角色列表
     */
    @Override
    public ArrayList<String> getRoleListByUserId(Long userId) {
        return authMapper.getRoleList(userId);
    }
}
