package com.quanhai.dingdingdemo.file.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanhai.dingdingdemo.file.dto.CircuitBoardDTO;
import com.quanhai.dingdingdemo.file.model.CircuitBoard;
import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.file.service.CircuitBoardService;
import com.quanhai.dingdingdemo.file.service.FileInfoService;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
            
            if (circuitBoardDTO.getFileName() != null && circuitBoardDTO.getFileUrl() != null) {
                // 保存文件信息
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(circuitBoardDTO.getFileName());
                fileInfo.setFileUrl(circuitBoardDTO.getFileUrl());
                fileInfo.setStatus(1);
                fileInfo.setCreateTime(LocalDateTime.now());
                fileInfoService.save(fileInfo);
                circuitBoard.setFileId(fileInfo.getId());
            }
            
            boolean result = circuitBoardService.save(circuitBoard);
            return result ? ResultUtil.success("新增成功") : ResultUtil.fail("新增失败");
        } catch (Exception e) {
            return ResultUtil.fail("新增失败：" + e.getMessage());
        }
    }

    /**
     * 删除线路板（逻辑删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteCircuitBoard(@PathVariable Long id) {
        try {
            LambdaUpdateWrapper<CircuitBoard> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CircuitBoard::getId, id)
                        .set(CircuitBoard::getDeleted, 1)
                        .set(CircuitBoard::getUpdateTime, LocalDateTime.now());
            boolean result = circuitBoardService.update(updateWrapper);
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
            
            // 更新文件信息
            if (circuitBoardDTO.getFileName() != null && circuitBoardDTO.getFileUrl() != null) {
                if (circuitBoard.getFileId() != null) {
                    // 更新现有文件
                    FileInfo fileInfo = fileInfoService.getById(circuitBoard.getFileId());
                    if (fileInfo != null) {
                        fileInfo.setFileName(circuitBoardDTO.getFileName());
                        fileInfo.setFileUrl(circuitBoardDTO.getFileUrl());
                        fileInfoService.updateById(fileInfo);
                    }
                } else {
                    // 新增文件
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(circuitBoardDTO.getFileName());
                    fileInfo.setFileUrl(circuitBoardDTO.getFileUrl());
                    fileInfo.setStatus(1);
                    fileInfo.setCreateTime(LocalDateTime.now());
                    fileInfoService.save(fileInfo);
                    circuitBoard.setFileId(fileInfo.getId());
                }
            }
            
            boolean result = circuitBoardService.updateById(circuitBoard);
            return result ? ResultUtil.success("更新成功") : ResultUtil.fail("更新失败");
        } catch (Exception e) {
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
     * 分页查询线路板列表
     */
    @GetMapping("/list")
    public Result getCircuitBoardList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String boardCode,
            @RequestParam(required = false) String boardName,
            @RequestParam(required = false) Integer status) {
        try {
            Page<CircuitBoard> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<CircuitBoard> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CircuitBoard::getDeleted, 0);
            
            if (boardCode != null && !boardCode.trim().isEmpty()) {
                queryWrapper.like(CircuitBoard::getBoardCode, boardCode);
            }
            if (boardName != null && !boardName.trim().isEmpty()) {
                queryWrapper.like(CircuitBoard::getBoardName, boardName);
            }
            if (status != null) {
                queryWrapper.eq(CircuitBoard::getStatus, status);
            }
            
            queryWrapper.orderByDesc(CircuitBoard::getCreateTime);
            Page<CircuitBoard> pageResult = circuitBoardService.page(pageParam, queryWrapper);
            
            List<CircuitBoardDTO> dtoList = new ArrayList<>();
            for (CircuitBoard circuitBoard : pageResult.getRecords()) {
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