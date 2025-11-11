package com.quanhai.dingdingdemo.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.file.dto.DocProdDrawingDTO;
import com.quanhai.dingdingdemo.file.model.docProdDrawing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成品图纸Mapper接口
 */
@Mapper
public interface DocProdDrawingMapper extends BaseMapper<docProdDrawing> {

    /**
     * 根据ID查询成品图纸及其文件信息
     */
    DocProdDrawingDTO selectDocProdDrawingById(@Param("id") Long id);

    /**
     * 根据成品编号查询图纸列表
     */
    List<DocProdDrawingDTO> selectDocProdDrawingByPid(@Param("pid") String pid);

    /**
     * 根据文件ID查询文件URL
     */
    String selectFileUrlById(@Param("fileId") Long fileId);

    /**
     * 根据文件ID查询文件名称
     */
    String selectFileNameById(@Param("fileId") Long fileId);

    /**
     * 查询所有成品图纸及其文件信息
     */
    List<DocProdDrawingDTO> selectAllDocProdDrawingsWithFiles();
    
    /**
     * 根据成品编号查询数量
     */
    int selectCountByPid(@Param("pid") String pid);
}