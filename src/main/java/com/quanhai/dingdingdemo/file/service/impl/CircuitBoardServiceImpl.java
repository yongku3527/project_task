package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.mapper.CircuitBoardMapper;
import com.quanhai.dingdingdemo.file.model.CircuitBoard;
import com.quanhai.dingdingdemo.file.service.CircuitBoardService;
import org.springframework.stereotype.Service;

/**
 * 线路板Service实现类
 */
@Service
public class CircuitBoardServiceImpl extends ServiceImpl<CircuitBoardMapper, CircuitBoard> implements CircuitBoardService {
}