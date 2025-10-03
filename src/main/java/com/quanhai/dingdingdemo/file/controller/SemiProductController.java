package com.quanhai.dingdingdemo.file.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanhai.dingdingdemo.file.dto.SemiProductDTO;
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
import java.util.List;

/**
 * 半成品Controller
 */
@RestController
@RequestMapping("/semi-product")
public class SemiProductController {

    @Autowired
    private SemiProductService semiProductService;

    @Autowired
    private CircuitBoardService circuitBoardService;

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * 新增半成品
     */
    @PostMapping("/add")
    public Result addSemiProduct(@RequestBody SemiProductDTO semiProductDTO) {
        try {
            SemiProduct semiProduct = new SemiProduct();
            semiProduct.setCircuitBoardId(semiProductDTO.getCircuitBoardId());
            semiProduct.setSemiProductCode(semiProductDTO.getSemiProductCode());
            semiProduct.setSemiProductName(semiProductDTO.getSemiProductName());
            semiProduct.setStatus(semiProductDTO.getStatus() != null ? semiProductDTO.getStatus() : 1);
            semiProduct.setCreateTime(LocalDateTime.now());
            semiProduct.setUpdateTime(LocalDateTime.now());
            
            // 保存原理图文件
            if (semiProductDTO.getSchematicFileName() != null && semiProductDTO.getSchematicFileUrl() != null) {
                FileInfo schematicFile = new FileInfo();
                schematicFile.setFileName(semiProductDTO.getSchematicFileName());
                schematicFile.setFileUrl(semiProductDTO.getSchematicFileUrl());
                schematicFile.setStatus(1);
                schematicFile.setCreateTime(LocalDateTime.now());
                fileInfoService.save(schematicFile);
                semiProduct.setSchematicFileId(schematicFile.getId());
            }
            
            // 保存贴片图文件
            if (semiProductDTO.getSmtFileName() != null && semiProductDTO.getSmtFileUrl() != null) {
                FileInfo smtFile = new FileInfo();
                smtFile.setFileName(semiProductDTO.getSmtFileName());
                smtFile.setFileUrl(semiProductDTO.getSmtFileUrl());
                smtFile.setStatus(1);
                smtFile.setCreateTime(LocalDateTime.now());
                fileInfoService.save(smtFile);
                semiProduct.setSmtFileId(smtFile.getId());
            }
            
            boolean result = semiProductService.save(semiProduct);
            return result ? ResultUtil.success("新增成功") : ResultUtil.fail("新增失败");
        } catch (Exception e) {
            return ResultUtil.fail("新增失败：" + e.getMessage());
        }
    }

    /**
     * 删除半成品（逻辑删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteSemiProduct(@PathVariable Long id) {
        try {
            LambdaUpdateWrapper<SemiProduct> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SemiProduct::getId, id)
                        .set(SemiProduct::getDeleted, 1)
                        .set(SemiProduct::getUpdateTime, LocalDateTime.now());
            boolean result = semiProductService.update(updateWrapper);
            return result ? ResultUtil.success("删除成功") : ResultUtil.fail("删除失败");
        } catch (Exception e) {
            return ResultUtil.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新半成品
     */
    @PutMapping("/update")
    public Result updateSemiProduct(@RequestBody SemiProductDTO semiProductDTO) {
        try {
            if (semiProductDTO.getId() == null) {
                return ResultUtil.fail("ID不能为空");
            }
            
            SemiProduct semiProduct = semiProductService.getById(semiProductDTO.getId());
            if (semiProduct == null) {
                return ResultUtil.fail("半成品不存在");
            }
            
            semiProduct.setCircuitBoardId(semiProductDTO.getCircuitBoardId());
            semiProduct.setSemiProductCode(semiProductDTO.getSemiProductCode());
            semiProduct.setSemiProductName(semiProductDTO.getSemiProductName());
            semiProduct.setStatus(semiProductDTO.getStatus());
            semiProduct.setUpdateTime(LocalDateTime.now());
            
            // 更新原理图文件
            if (semiProductDTO.getSchematicFileName() != null && semiProductDTO.getSchematicFileUrl() != null) {
                if (semiProduct.getSchematicFileId() != null) {
                    FileInfo fileInfo = fileInfoService.getById(semiProduct.getSchematicFileId());
                    if (fileInfo != null) {
                        fileInfo.setFileName(semiProductDTO.getSchematicFileName());
                        fileInfo.setFileUrl(semiProductDTO.getSchematicFileUrl());
                        fileInfoService.updateById(fileInfo);
                    }
                } else {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(semiProductDTO.getSchematicFileName());
                    fileInfo.setFileUrl(semiProductDTO.getSchematicFileUrl());
                    fileInfo.setStatus(1);
                    fileInfo.setCreateTime(LocalDateTime.now());
                    fileInfoService.save(fileInfo);
                    semiProduct.setSchematicFileId(fileInfo.getId());
                }
            }
            
            // 更新贴片图文件
            if (semiProductDTO.getSmtFileName() != null && semiProductDTO.getSmtFileUrl() != null) {
                if (semiProduct.getSmtFileId() != null) {
                    FileInfo fileInfo = fileInfoService.getById(semiProduct.getSmtFileId());
                    if (fileInfo != null) {
                        fileInfo.setFileName(semiProductDTO.getSmtFileName());
                        fileInfo.setFileUrl(semiProductDTO.getSmtFileUrl());
                        fileInfoService.updateById(fileInfo);
                    }
                } else {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(semiProductDTO.getSmtFileName());
                    fileInfo.setFileUrl(semiProductDTO.getSmtFileUrl());
                    fileInfo.setStatus(1);
                    fileInfo.setCreateTime(LocalDateTime.now());
                    fileInfoService.save(fileInfo);
                    semiProduct.setSmtFileId(fileInfo.getId());
                }
            }
            
            boolean result = semiProductService.updateById(semiProduct);
            return result ? ResultUtil.success("更新成功") : ResultUtil.fail("更新失败");
        } catch (Exception e) {
            return ResultUtil.fail("更新失败：" + e.getMessage());
        }
    }

    /**
     * 查询单个半成品
     */
    @GetMapping("/get/{id}")
    public Result getSemiProduct(@PathVariable Long id) {
        try {
            SemiProduct semiProduct = semiProductService.getById(id);
            if (semiProduct == null) {
                return ResultUtil.fail("半成品不存在");
            }
            
            SemiProductDTO dto = convertToDTO(semiProduct);
            return ResultUtil.success(dto);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询半成品列表
     */
    @GetMapping("/list")
    public Result getSemiProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long circuitBoardId,
            @RequestParam(required = false) String semiProductCode,
            @RequestParam(required = false) String semiProductName,
            @RequestParam(required = false) Integer status) {
        try {
            Page<SemiProduct> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<SemiProduct> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SemiProduct::getDeleted, 0);
            
            if (circuitBoardId != null) {
                queryWrapper.eq(SemiProduct::getCircuitBoardId, circuitBoardId);
            }
            if (semiProductCode != null && !semiProductCode.trim().isEmpty()) {
                queryWrapper.like(SemiProduct::getSemiProductCode, semiProductCode);
            }
            if (semiProductName != null && !semiProductName.trim().isEmpty()) {
                queryWrapper.like(SemiProduct::getSemiProductName, semiProductName);
            }
            if (status != null) {
                queryWrapper.eq(SemiProduct::getStatus, status);
            }
            
            queryWrapper.orderByDesc(SemiProduct::getCreateTime);
            Page<SemiProduct> pageResult = semiProductService.page(pageParam, queryWrapper);
            
            List<SemiProductDTO> dtoList = new ArrayList<>();
            for (SemiProduct semiProduct : pageResult.getRecords()) {
                dtoList.add(convertToDTO(semiProduct));
            }
            
            return ResultUtil.success(dtoList);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据线路板ID查询半成品列表
     */
    @GetMapping("/list-by-circuit-board/{circuitBoardId}")
    public Result getSemiProductByCircuitBoardId(@PathVariable Long circuitBoardId) {
        try {
            LambdaQueryWrapper<SemiProduct> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SemiProduct::getCircuitBoardId, circuitBoardId)
                       .eq(SemiProduct::getDeleted, 0)
                       .orderByDesc(SemiProduct::getCreateTime);
            
            List<SemiProduct> semiProductList = semiProductService.list(queryWrapper);
            List<SemiProductDTO> dtoList = new ArrayList<>();
            for (SemiProduct semiProduct : semiProductList) {
                dtoList.add(convertToDTO(semiProduct));
            }
            
            return ResultUtil.success(dtoList);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 实体转DTO
     */
    private SemiProductDTO convertToDTO(SemiProduct semiProduct) {
        SemiProductDTO dto = new SemiProductDTO();
        dto.setId(semiProduct.getId());
        dto.setCircuitBoardId(semiProduct.getCircuitBoardId());
        dto.setSemiProductCode(semiProduct.getSemiProductCode());
        dto.setSemiProductName(semiProduct.getSemiProductName());
        dto.setStatus(semiProduct.getStatus());
        dto.setCreateTime(semiProduct.getCreateTime());
        dto.setUpdateTime(semiProduct.getUpdateTime());
        
        // 设置线路板信息
        if (semiProduct.getCircuitBoardId() != null) {
            CircuitBoard circuitBoard = circuitBoardService.getById(semiProduct.getCircuitBoardId());
            if (circuitBoard != null) {
                dto.setCircuitBoardCode(circuitBoard.getBoardCode());
                dto.setCircuitBoardName(circuitBoard.getBoardName());
            }
        }
        
        // 设置文件信息
        if (semiProduct.getSchematicFileId() != null) {
            FileInfo schematicFile = fileInfoService.getById(semiProduct.getSchematicFileId());
            if (schematicFile != null) {
                dto.setSchematicFileName(schematicFile.getFileName());
                dto.setSchematicFileUrl(schematicFile.getFileUrl());
            }
        }
        
        if (semiProduct.getSmtFileId() != null) {
            FileInfo smtFile = fileInfoService.getById(semiProduct.getSmtFileId());
            if (smtFile != null) {
                dto.setSmtFileName(smtFile.getFileName());
                dto.setSmtFileUrl(smtFile.getFileUrl());
            }
        }
        
        return dto;
    }
}