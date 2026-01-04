package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.dto.CircuitBoardDTO;
import com.quanhai.dingdingdemo.file.dto.LedBoardPluginSemiProductDTO;
import com.quanhai.dingdingdemo.file.dto.SemiProductDTO;
import com.quanhai.dingdingdemo.file.mapper.CircuitBoardMapper;
import com.quanhai.dingdingdemo.file.model.CircuitBoard;
import com.quanhai.dingdingdemo.file.service.CircuitBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线路板Service实现类
 */
@Service
public class CircuitBoardServiceImpl extends ServiceImpl<CircuitBoardMapper, CircuitBoard> implements CircuitBoardService {

    @Autowired
    private CircuitBoardMapper circuitBoardMapper;

    @Override
    public CircuitBoardDTO getCircuitBoardWithDetails(Long id) {
        CircuitBoardDTO circuitBoardDTO = circuitBoardMapper.selectCircuitBoardById(id);
        if (circuitBoardDTO != null) {
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
    public Map<String, Object> getAllCircuitBoardsWithDetails(Integer page, Integer size) {
        Page<CircuitBoard> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CircuitBoard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CircuitBoard::getCreateTime);

        Page<CircuitBoard> pageResult = this.page(pageParam, queryWrapper);

        List<CircuitBoardDTO> dtoList = new ArrayList<>();
        for (CircuitBoard circuitBoard : pageResult.getRecords()) {
            CircuitBoardDTO dto = getCircuitBoardWithDetails(circuitBoard.getId());
            if (dto != null) {
                dtoList.add(dto);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", dtoList);
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);

        return result;
    }
}
