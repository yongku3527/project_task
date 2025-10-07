package com.quanhai.dingdingdemo.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.file.dto.CircuitBoardDTO;
import com.quanhai.dingdingdemo.file.model.CircuitBoard;

/**
 * 线路板Service接口
 */
public interface CircuitBoardService extends IService<CircuitBoard> {

    /**
     * 根据ID查询线路板及其嵌套的半成品、灯板插件、文件信息
     */
    CircuitBoardDTO getCircuitBoardWithDetails(Long id);

    /**
     * 获取所有线路板及其嵌套数据
     */
    java.util.List<CircuitBoardDTO> getAllCircuitBoardsWithDetails();
}