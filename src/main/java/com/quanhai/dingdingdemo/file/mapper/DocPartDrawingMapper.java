package com.quanhai.dingdingdemo.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.file.dto.DocPartDrawingDTO;
import com.quanhai.dingdingdemo.file.model.DocPartDrawing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 零件图纸Mapper接口
 */
@Mapper
public interface DocPartDrawingMapper extends BaseMapper<DocPartDrawing> {

    /**
     * 根据ID查询零件图纸及其文件信息
     */
    DocPartDrawingDTO selectDocPartDrawingById(@Param("id") Long id);

    /**
     * 根据零件编号查询图纸列表
     */
    List<DocPartDrawingDTO> selectDocPartDrawingByPartId(@Param("partId") String partId);

    /**
     * 根据文件ID查询文件URL
     */
    String selectFileUrlById(@Param("fileId") Long fileId);

    /**
     * 根据文件ID查询文件名称
     */
    String selectFileNameById(@Param("fileId") Long fileId);

    /**
     * 查询所有零件图纸及其文件信息
     */
    List<DocPartDrawingDTO> selectAllDocPartDrawingsWithFiles();
}