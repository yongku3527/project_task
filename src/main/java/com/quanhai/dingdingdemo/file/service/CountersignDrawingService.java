package com.quanhai.dingdingdemo.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.file.dto.CountersignDrawingDTO;
import com.quanhai.dingdingdemo.file.model.CountersignDrawing;

import java.util.List;

/**
 * 会签图纸Service接口
 */
public interface CountersignDrawingService extends IService<CountersignDrawing> {

    /**
     * 根据ID查询会签图纸及其文件信息
     */
    CountersignDrawingDTO getCountersignDrawingWithFiles(Long id);

    /**
     * 根据零部件号查询会签图纸列表
     */
    List<CountersignDrawingDTO> getCountersignDrawingsByPartNo(String partNo);

    /**
     * 获取所有会签图纸及其文件信息
     */
    List<CountersignDrawingDTO> getAllCountersignDrawingsWithFiles();
}