package com.quanhai.dingdingdemo.satoken.service;

import java.util.ArrayList;

public interface AuthService {
    public ArrayList<String> getPermissionByUserId(Long userId);

    /*
        根据用户id获取角色列表
     */
    public ArrayList<String> getRoleListByUserId(Long userId);
}
