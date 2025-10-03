package com.quanhai.dingdingdemo.file.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanhai.dingdingdemo.file.dto.LedBoardPluginSemiProductDTO;
import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.file.model.LedBoardPluginSemiProduct;
import com.quanhai.dingdingdemo.file.model.SemiProduct;
import com.quanhai.dingdingdemo.file.service.FileInfoService;
import com.quanhai.dingdingdemo.file.service.LedBoardPluginSemiProductService;
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
 * 灯板插件半成品Controller
 */
@RestController
@RequestMapping("/led-board-plugin-semi-product")
public class LedBoardPluginSemiProductController {

    @Autowired
    private LedBoardPluginSemiProductService ledBoardPluginSemiProductService;

    @Autowired
    private SemiProductService semiProductService;

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * 新增灯板插件半成品
     */
    @PostMapping("/add")
    public Result addLedBoardPluginSemiProduct(@RequestBody LedBoardPluginSemiProductDTO dto) {
        try {
            LedBoardPluginSemiProduct entity = new LedBoardPluginSemiProduct();
            entity.setSemiProductId(dto.getSemiProductId());
            entity.setLedBoardPluginCode(dto.getLedBoardPluginCode());
            entity.setLedBoardPluginName(dto.getLedBoardPluginName());
            entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
            entity.setCreateTime(LocalDateTime.now());
            
            // 保存文件信息
            if (dto.getFileName() != null && dto.getFileUrl() != null) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(dto.getFileName());
                fileInfo.setFileUrl(dto.getFileUrl());
                fileInfo.setStatus(1);
                fileInfo.setCreateTime(LocalDateTime.now());
                fileInfoService.save(fileInfo);
                entity.setFileId(fileInfo.getId());
            }
            
            boolean result = ledBoardPluginSemiProductService.save(entity);
            return result ? ResultUtil.success("新增成功") : ResultUtil.fail("新增失败");
        } catch (Exception e) {
            return ResultUtil.fail("新增失败：" + e.getMessage());
        }
    }

    /**
     * 删除灯板插件半成品（逻辑删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteLedBoardPluginSemiProduct(@PathVariable Long id) {
        try {
            LambdaUpdateWrapper<LedBoardPluginSemiProduct> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(LedBoardPluginSemiProduct::getId, id)
                        .set(LedBoardPluginSemiProduct::getDeleted, 1)
                        .set(LedBoardPluginSemiProduct::getUpdateTime, LocalDateTime.now());
            boolean result = ledBoardPluginSemiProductService.update(updateWrapper);
            return result ? ResultUtil.success("删除成功") : ResultUtil.fail("删除失败");
        } catch (Exception e) {
            return ResultUtil.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新灯板插件半成品
     */
    @PutMapping("/update")
    public Result updateLedBoardPluginSemiProduct(@RequestBody LedBoardPluginSemiProductDTO dto) {
        try {
            if (dto.getId() == null) {
                return ResultUtil.fail("ID不能为空");
            }
            
            LedBoardPluginSemiProduct entity = ledBoardPluginSemiProductService.getById(dto.getId());
            if (entity == null) {
                return ResultUtil.fail("灯板插件半成品不存在");
            }
            
            entity.setSemiProductId(dto.getSemiProductId());
            entity.setLedBoardPluginCode(dto.getLedBoardPluginCode());
            entity.setLedBoardPluginName(dto.getLedBoardPluginName());
            entity.setStatus(dto.getStatus());
            entity.setUpdateTime(LocalDateTime.now());
            
            // 更新文件信息
            if (dto.getFileName() != null && dto.getFileUrl() != null) {
                if (entity.getFileId() != null) {
                    FileInfo fileInfo = fileInfoService.getById(entity.getFileId());
                    if (fileInfo != null) {
                        fileInfo.setFileName(dto.getFileName());
                        fileInfo.setFileUrl(dto.getFileUrl());
                        fileInfoService.updateById(fileInfo);
                    }
                } else {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(dto.getFileName());
                    fileInfo.setFileUrl(dto.getFileUrl());
                    fileInfo.setStatus(1);
                    fileInfo.setCreateTime(LocalDateTime.now());
                    fileInfoService.save(fileInfo);
                    entity.setFileId(fileInfo.getId());
                }
            }
            
            boolean result = ledBoardPluginSemiProductService.updateById(entity);
            return result ? ResultUtil.success("更新成功") : ResultUtil.fail("更新失败");
        } catch (Exception e) {
            return ResultUtil.fail("更新失败：" + e.getMessage());
        }
    }

    /**
     * 查询单个灯板插件半成品
     */
    @GetMapping("/get/{id}")
    public Result getLedBoardPluginSemiProduct(@PathVariable Long id) {
        try {
            LedBoardPluginSemiProduct entity = ledBoardPluginSemiProductService.getById(id);
            if (entity == null) {
                return ResultUtil.fail("灯板插件半成品不存在");
            }
            
            LedBoardPluginSemiProductDTO dto = convertToDTO(entity);
            return ResultUtil.success(dto);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询灯板插件半成品列表
     */
    @GetMapping("/list")
    public Result getLedBoardPluginSemiProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long semiProductId,
            @RequestParam(required = false) String ledBoardPluginCode,
            @RequestParam(required = false) String ledBoardPluginName,
            @RequestParam(required = false) Integer status) {
        try {
            Page<LedBoardPluginSemiProduct> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<LedBoardPluginSemiProduct> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LedBoardPluginSemiProduct::getDeleted, 0);
            
            if (semiProductId != null) {
                queryWrapper.eq(LedBoardPluginSemiProduct::getSemiProductId, semiProductId);
            }
            if (ledBoardPluginCode != null && !ledBoardPluginCode.trim().isEmpty()) {
                queryWrapper.like(LedBoardPluginSemiProduct::getLedBoardPluginCode, ledBoardPluginCode);
            }
            if (ledBoardPluginName != null && !ledBoardPluginName.trim().isEmpty()) {
                queryWrapper.like(LedBoardPluginSemiProduct::getLedBoardPluginName, ledBoardPluginName);
            }
            if (status != null) {
                queryWrapper.eq(LedBoardPluginSemiProduct::getStatus, status);
            }
            
            queryWrapper.orderByDesc(LedBoardPluginSemiProduct::getCreateTime);
            Page<LedBoardPluginSemiProduct> pageResult = ledBoardPluginSemiProductService.page(pageParam, queryWrapper);
            
            List<LedBoardPluginSemiProductDTO> dtoList = new ArrayList<>();
            for (LedBoardPluginSemiProduct entity : pageResult.getRecords()) {
                dtoList.add(convertToDTO(entity));
            }
            
            return ResultUtil.success(dtoList);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据半成品ID查询灯板插件半成品列表
     */
    @GetMapping("/list-by-semi-product/{semiProductId}")
    public Result getLedBoardPluginSemiProductBySemiProductId(@PathVariable Long semiProductId) {
        try {
            LambdaQueryWrapper<LedBoardPluginSemiProduct> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LedBoardPluginSemiProduct::getSemiProductId, semiProductId)
                       .eq(LedBoardPluginSemiProduct::getDeleted, 0)
                       .orderByDesc(LedBoardPluginSemiProduct::getCreateTime);
            
            List<LedBoardPluginSemiProduct> entityList = ledBoardPluginSemiProductService.list(queryWrapper);
            List<LedBoardPluginSemiProductDTO> dtoList = new ArrayList<>();
            for (LedBoardPluginSemiProduct entity : entityList) {
                dtoList.add(convertToDTO(entity));
            }
            
            return ResultUtil.success(dtoList);
        } catch (Exception e) {
            return ResultUtil.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 实体转DTO
     */
    private LedBoardPluginSemiProductDTO convertToDTO(LedBoardPluginSemiProduct entity) {
        LedBoardPluginSemiProductDTO dto = new LedBoardPluginSemiProductDTO();
        dto.setId(entity.getId());
        dto.setSemiProductId(entity.getSemiProductId());
        dto.setLedBoardPluginCode(entity.getLedBoardPluginCode());
        dto.setLedBoardPluginName(entity.getLedBoardPluginName());
        dto.setStatus(entity.getStatus());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        
        // 设置半成品信息
        if (entity.getSemiProductId() != null) {
            SemiProduct semiProduct = semiProductService.getById(entity.getSemiProductId());
            if (semiProduct != null) {
                dto.setSemiProductCode(semiProduct.getSemiProductCode());
                dto.setSemiProductName(semiProduct.getSemiProductName());
            }
        }
        
        // 设置文件信息
        if (entity.getFileId() != null) {
            FileInfo fileInfo = fileInfoService.getById(entity.getFileId());
            if (fileInfo != null) {
                dto.setFileName(fileInfo.getFileName());
                dto.setFileUrl(fileInfo.getFileUrl());
            }
        }
        
        return dto;
    }
}