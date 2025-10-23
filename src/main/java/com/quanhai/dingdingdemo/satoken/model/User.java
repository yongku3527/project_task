package com.quanhai.dingdingdemo.satoken.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 用户实体类
 */
@Data
@Accessors(chain = true)
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户角色
     */
    private String role;
}