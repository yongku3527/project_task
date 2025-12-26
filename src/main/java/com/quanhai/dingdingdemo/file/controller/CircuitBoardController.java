package com.quanhai.dingdingdemo.file.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanhai.dingdingdemo.file.dto.CircuitBoardDTO;
import com.quanhai.dingdingdemo.file.model.CircuitBoard;
import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.file.model.SemiProduct;
import com.quanhai.dingdingdemo.file.service.CircuitBoardService;
import com.quanhai.dingdingdemo.file.service.FileInfoService;
import com.quanhai.dingdingdemo.file.service.SemiProductService;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 线路板Controller
 */
@RestController
@RequestMapping("/circuit-board")
public class CircuitBoardController {

    @Autowired
    private CircuitBoardService circuitBoardService;

    @Autowired
    private FileInfoService fileInfoService;

    @Autowired
    private SemiProductService semiProductService;

    /**
     * 新增线路板
     */
    @PostMapping("/add")
    public Result addCircuitBoard(@RequestBody CircuitBoardDTO circuitBoardDTO) {
        try {
            CircuitBoard circuitBoard = new CircuitBoard();
            circuitBoard.setBoardCode(circuitBoardDTO.getBoardCode());
            circuitBoard.setBoardName(circuitBoardDTO.getBoardName());
            circuitBoard.setStatus(circuitBoardDTO.getStatus() != null ? circuitBoardDTO.getStatus() : 1);
            circuitBoard.setCreateTime(LocalDateTime.now());
            circuitBoard.setUpdateTime(LocalDateTime.now());
            circuitBoard.setFileId(circuitBoardDTO.getFileId());
            
//            if (circuitBoardDTO.getFileName() != null && circuitBoardDTO.getFileUrl() != null) {
//                // 保存文件信息
//                FileInfo fileInfo = new FileInfo();
//                fileInfo.setFileName(circuitBoardDTO.getFileName());
//                fileInfo.setOriginalName(circuitBoardDTO.getFileName());  // 设置原始文件名
//                fileInfo.setFileUrl(circuitBoardDTO.getFileUrl());
//
//                // 提取文件后缀
//                String fileName = circuitBoardDTO.getFileName();
//                if (fileName != null && fileName.contains(".")) {
//                    fileInfo.setFileSuffix(fileName.substring(fileName.lastIndexOf(".")));
//                } else {
//                    fileInfo.setFileSuffix(""); // 设置空后缀避免NOT NULL约束
//                }
//
//                fileInfo.setStatus(1);
//                fileInfo.setCreateTime(LocalDateTime.now());
//                fileInfoService.save(fileInfo);
//                circuitBoard.setFileId(fileInfo.getId());
//            }
            
            boolean result = circuitBoardService.save(circuitBoard);
            return result ? ResultUtil.success("新增成功") : ResultUtil.fail("新增失败");
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry")) {
                return ResultUtil.fail("线路板编码已存在");
            }
            return ResultUtil.fail("新增失败：" + e.getMessage());
        }
    }

    /**
     * 删除线路板（逻辑删除）
     * 线路板之类的需要逻辑删除吗？
     * 还是只逻辑删除文件？
     * 文件即使物理删除，也还是保存到了minio中
     *
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteCircuitBoard(@PathVariable Long id) {
        try {
            LambdaUpdateWrapper<CircuitBoard> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CircuitBoard::getId, id);

            boolean result = circuitBoardService.removeById(updateWrapper);
            return result ? ResultUtil.success("删除成功") : ResultUtil.fail("删除失败");
        } catch (Exception e) {
            return ResultUtil.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新线路板
     */
    @PutMapping("/update")
    public Result updateCircuitBoard(@RequestBody CircuitBoardDTO circuitBoardDTO) {
        try {
            if (circuitBoardDTO.getId() == null) {
                return ResultUtil.fail("ID不能为空");
            }
            
            CircuitBoard circuitBoard = circuitBoardService.getById(circuitBoardDTO.getId());
            if (circuitBoard == null) {
                return ResultUtil.fail("线路板不存在");
            }
            
            circuitBoard.setBoardCode(circuitBoardDTO.getBoardCode());
            circuitBoard.setBoardName(circuitBoardDTO.getBoardName());
            circuitBoard.setStatus(circuitBoardDTO.getStatus());
            circuitBoard.setUpdateTime(LocalDateTime.now());
            if (circuitBoardDTO.getFileId() != null) {
                circuitBoard.setFileId(circuitBoardDTO.getFileId());
            }else {
                circuitBoard.setFileId(999999999999L);
            }

            boolean result = circuitBoardService.updateById(circuitBoard);
            return result ? ResultUtil.success("更新成功") : ResultUtil.fail("更新失败");
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry")) {
                return ResultUtil.fail("线路板编码已存在");
            }
            return ResultUtil.fail("更新失败：" + e.getMessage());
        }
    }

    /**
     * 查询单个线路板
     */
    @GetMapping("/get/{id}")
    public Result getCircuitBoard(@PathVariable Long id) {
        try {
            CircuitBoard circuitBoard = circuitBoardService.getById(id);
            if (circuitBoard == null) {
                return ResultUtil.fail("线路板不存在");
            }
            
            CircuitBoardDTO dto = convertToDTO(circuitBoard);
            return ResultUtil.success(dto);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询线路板列表（包含完整的嵌套数据）
     */
    @GetMapping("/list")
    public Result getCircuitBoardList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String boardCode,
            @RequestParam(required = false) String boardName,
            @RequestParam(required = false) String semiProductCode,
            @RequestParam(required = false) String semiProductName,
            @RequestParam(required = false) Integer status) {
        try {
            // 如果搜索半成品，需要先找到对应的线路板ID列表
            Set<Long> circuitBoardIds = null;
            if ((semiProductCode != null && !semiProductCode.trim().isEmpty()) || 
                (semiProductName != null && !semiProductName.trim().isEmpty())) {
                
                // 查询符合条件的半成品
                LambdaQueryWrapper<SemiProduct> semiQueryWrapper = new LambdaQueryWrapper<>();
                if (semiProductCode != null && !semiProductCode.trim().isEmpty()) {
                    semiQueryWrapper.like(SemiProduct::getSemiProductCode, semiProductCode);
                }
                if (semiProductName != null && !semiProductName.trim().isEmpty()) {
                    semiQueryWrapper.like(SemiProduct::getSemiProductName, semiProductName);
                }
                
                List<SemiProduct> semiProducts = semiProductService.list(semiQueryWrapper);
                
                if (semiProducts.isEmpty()) {
                    // 如果没有符合条件的半成品，直接返回空结果
                    Map<String, Object> result = new HashMap<>();
                    result.put("list", new ArrayList<>());
                    result.put("total", 0);
                    result.put("page", page);
                    result.put("size", size);
                    return ResultUtil.success(result);
                }
                
                // 获取这些半成品对应的线路板ID
                circuitBoardIds = new HashSet<>();
                for (SemiProduct semiProduct : semiProducts) {
                    circuitBoardIds.add(semiProduct.getCircuitBoardId());
                }
            }
            
            Page<CircuitBoard> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<CircuitBoard> queryWrapper = new LambdaQueryWrapper<>();
//             暂时移除删除条件进行测试
//             queryWrapper.eq(CircuitBoard::getDeleted, 0);
            
            if (boardCode != null && !boardCode.trim().isEmpty()) {
                queryWrapper.like(CircuitBoard::getBoardCode, boardCode);
            }
            if (boardName != null && !boardName.trim().isEmpty()) {
                queryWrapper.like(CircuitBoard::getBoardName, boardName);
            }
            if (status != null) {
                queryWrapper.eq(CircuitBoard::getStatus, status);
            }
            
            // 如果搜索了半成品，只查询这些线路板
            if (circuitBoardIds != null && !circuitBoardIds.isEmpty()) {
                queryWrapper.in(CircuitBoard::getId, circuitBoardIds);
            }
            
            queryWrapper.orderByDesc(CircuitBoard::getCreateTime);
            Page<CircuitBoard> pageResult = circuitBoardService.page(pageParam, queryWrapper);
            
            // 获取完整的嵌套数据，而不是只包含基本信息
            List<CircuitBoardDTO> dtoList = new ArrayList<>();
            for (CircuitBoard circuitBoard : pageResult.getRecords()) {
                // 使用getCircuitBoardWithDetails获取包含半成品和灯板插件的完整数据
                CircuitBoardDTO fullDto = circuitBoardService.getCircuitBoardWithDetails(circuitBoard.getId());
                if (fullDto != null) {
                    dtoList.add(fullDto);
                }
            }
            
            // 返回分页信息，包括列表数据和总记录数
            Map<String, Object> result = new HashMap<>();
            result.put("list", dtoList);
            result.put("total", pageResult.getTotal());
            result.put("page", page);
            result.put("size", size);
            
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有线路板及其嵌套数据（用于前端表格展示）
     */
    @GetMapping("/all-with-details")
    public Result getAllCircuitBoardsWithDetails() {
        try {
            List<CircuitBoardDTO> circuitBoardList = circuitBoardService.getAllCircuitBoardsWithDetails();
            return ResultUtil.success(circuitBoardList);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取线路板及其嵌套数据
     */
    @GetMapping("/with-details/{id}")
    public Result getCircuitBoardWithDetails(@PathVariable Long id) {
        try {
            CircuitBoardDTO circuitBoardDTO = circuitBoardService.getCircuitBoardWithDetails(id);
            if (circuitBoardDTO == null) {
                return ResultUtil.fail("线路板不存在");
            }
            return ResultUtil.success(circuitBoardDTO);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有线路板（用于下拉选项）
     */
    @GetMapping("/all")
    public Result getAllCircuitBoards() {
        try {
            LambdaQueryWrapper<CircuitBoard> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CircuitBoard::getDeleted, 0)
                       .orderByDesc(CircuitBoard::getCreateTime);
            
            List<CircuitBoard> circuitBoardList = circuitBoardService.list(queryWrapper);
            List<CircuitBoardDTO> dtoList = new ArrayList<>();
            for (CircuitBoard circuitBoard : circuitBoardList) {
                dtoList.add(convertToDTO(circuitBoard));
            }
            
            return ResultUtil.success(dtoList);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 实体转DTO
     */
    private CircuitBoardDTO convertToDTO(CircuitBoard circuitBoard) {
        CircuitBoardDTO dto = new CircuitBoardDTO();
        dto.setId(circuitBoard.getId());
        dto.setBoardCode(circuitBoard.getBoardCode());
        dto.setBoardName(circuitBoard.getBoardName());
        dto.setStatus(circuitBoard.getStatus());
        dto.setCreateTime(circuitBoard.getCreateTime());
        dto.setUpdateTime(circuitBoard.getUpdateTime());

            // 设置文件信息
            if (circuitBoard.getFileId() != null) {
                FileInfo fileInfo = fileInfoService.getById(circuitBoard.getFileId());
                if (fileInfo != null) {
                dto.setFileName(fileInfo.getFileName());
                dto.setFileUrl(fileInfo.getFileUrl());
            }
        }
        
        return dto;
    }
}