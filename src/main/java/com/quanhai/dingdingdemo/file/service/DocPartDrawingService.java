package com.quanhai.dingdingdemo.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.file.dto.DocPartDrawingDTO;
import com.quanhai.dingdingdemo.file.model.DocPartDrawing;

import java.util.List;

/**
 * 零件图纸Service接口
 */
public interface DocPartDrawingService extends IService<DocPartDrawing> {

    /**
     * 根据ID查询零件图纸及其文件信息
     */
    DocPartDrawingDTO getDocPartDrawingWithFiles(Long id);

    /**
     * 根据零件编号查询图纸列表
     */
    List<DocPartDrawingDTO> getDocPartDrawingsByPartId(String partId);

    /**
     * 获取所有零件图纸及其文件信息
     */
    List<DocPartDrawingDTO> getAllDocPartDrawingsWithFiles();
}