package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.dto.FileInfoDTO;
import com.quanhai.dingdingdemo.file.dto.SpecificationDTO;
import com.quanhai.dingdingdemo.file.mapper.SpecificationMapper;
import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.file.model.Specification;
import com.quanhai.dingdingdemo.file.service.FileInfoService;
import com.quanhai.dingdingdemo.file.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规格书Service实现类
 */
@Service
public class SpecificationServiceImpl extends ServiceImpl<SpecificationMapper, Specification> implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * 根据ID查询规格书及其文件信息
     */
    @Override
    public SpecificationDTO getSpecificationWithFiles(Long id) {
        SpecificationDTO dto = specificationMapper.selectSpecificationById(id);
        if (dto != null) {
            fillFileList(dto);
        }
        return dto;
    }

    /**
     * 根据原材料ID查询规格书列表
     */
    @Override
    public List<SpecificationDTO> getSpecificationsByMaterialId(String materialId) {
        List<SpecificationDTO> list = specificationMapper.selectSpecificationsByMaterialId(materialId);
        list.forEach(this::fillFileList);
        return list;
    }

    /**
     * 获取所有规格书及其文件信息
     */
    @Override
    public List<SpecificationDTO> getAllSpecificationsWithFiles() {
        List<SpecificationDTO> list = specificationMapper.selectAllSpecificationsWithFiles();
        list.forEach(this::fillFileList);
        return list;
    }

    /**
     * 填充文件列表信息
     */
    private void fillFileList(SpecificationDTO dto) {
        if (!StringUtils.hasText(dto.getFileId())) {
            return;
        }

        // 解析逗号分隔的文件ID
        List<Long> fileIds = Arrays.stream(dto.getFileId().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        if (fileIds.isEmpty()) {
            return;
        }

        // 查询文件信息
        List<FileInfo> fileInfos = fileInfoService.listByIds(fileIds);

        // 转换为DTO
        List<FileInfoDTO> fileList = fileInfos.stream().map(fileInfo -> {
            FileInfoDTO fileInfoDTO = new FileInfoDTO();
            fileInfoDTO.setId(fileInfo.getId());
            fileInfoDTO.setFileName(fileInfo.getFileName());
            fileInfoDTO.setFileUrl(fileInfo.getFileUrl());
            fileInfoDTO.setOriginalName(fileInfo.getOriginalName());
            fileInfoDTO.setFileSuffix(fileInfo.getFileSuffix());
            return fileInfoDTO;
        }).collect(Collectors.toList());

        dto.setFileList(fileList);

        // 设置第一个文件为默认显示（兼容旧逻辑）
        if (!fileList.isEmpty()) {
            dto.setFileUrl(fileList.get(0).getFileUrl());
            dto.setFileName(fileList.get(0).getFileName());
        }
    }
}
