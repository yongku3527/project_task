package com.quanhai.dingdingdemo.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanhai.dingdingdemo.file.dto.DocProdDrawingDTO;
import com.quanhai.dingdingdemo.file.model.docProdDrawing;

import java.util.List;

/**
 * 成品图纸Service接口
 */
public interface DocProdDrawingService extends IService<docProdDrawing> {

    /**
     * 根据ID查询成品图纸及其文件信息
     */
    DocProdDrawingDTO getDocProdDrawingWithFiles(Long id);

    /**
     * 根据成品编号查询图纸列表
     */
    List<DocProdDrawingDTO> getDocProdDrawingsByPid(String pid);

    /**
     * 获取所有成品图纸及其文件信息
     */
    List<DocProdDrawingDTO> getAllDocProdDrawingsWithFiles();
}