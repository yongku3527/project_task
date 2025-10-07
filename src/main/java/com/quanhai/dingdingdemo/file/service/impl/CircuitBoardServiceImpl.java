package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.dto.CircuitBoardDTO;
import com.quanhai.dingdingdemo.file.dto.LedBoardPluginSemiProductDTO;
import com.quanhai.dingdingdemo.file.dto.SemiProductDTO;
import com.quanhai.dingdingdemo.file.mapper.CircuitBoardMapper;
import com.quanhai.dingdingdemo.file.model.CircuitBoard;
import com.quanhai.dingdingdemo.file.service.CircuitBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 线路板Service实现类
 */
@Service
public class CircuitBoardServiceImpl extends ServiceImpl<CircuitBoardMapper, CircuitBoard> implements CircuitBoardService {

    @Autowired
    private CircuitBoardMapper circuitBoardMapper;

    @Override
    public CircuitBoardDTO getCircuitBoardWithDetails(Long id) {
        // 获取线路板基本信息
        CircuitBoardDTO circuitBoardDTO = circuitBoardMapper.selectCircuitBoardById(id);
        if (circuitBoardDTO != null) {
            // 获取半成品列表及其灯板插件
            List<SemiProductDTO> semiProducts = circuitBoardMapper.selectSemiProductByBoardId(id);
            for (SemiProductDTO semiProduct : semiProducts) {
                List<LedBoardPluginSemiProductDTO> ledPlugins = circuitBoardMapper.selectLedPluginBySemiId(semiProduct.getId());
                semiProduct.setLedBoardPluginSemiProductDTOList(ledPlugins);
            }
            circuitBoardDTO.setSemiProductDTOList(semiProducts);
        }
        return circuitBoardDTO;
    }

    @Override
    public List<CircuitBoardDTO> getAllCircuitBoardsWithDetails() {
        // 获取所有线路板
        List<CircuitBoard> circuitBoards = list();
        List<CircuitBoardDTO> result = new java.util.ArrayList<>();
        
        for (CircuitBoard circuitBoard : circuitBoards) {
            CircuitBoardDTO dto = getCircuitBoardWithDetails(circuitBoard.getId());
            result.add(dto);
        }
        
        return result;
    }
}