package com.quanhai.dingdingdemo.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.file.dto.CountersignDrawingDTO;
import com.quanhai.dingdingdemo.file.model.CountersignDrawing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会签图纸Mapper接口
 */
@Mapper
public interface CountersignDrawingMapper extends BaseMapper<CountersignDrawing> {

    /**
     * 根据ID查询会签图纸及其文件信息
     */
    CountersignDrawingDTO selectCountersignDrawingById(@Param("id") Long id);

    /**
     * 根据零部件号查询会签图纸列表
     */
    List<CountersignDrawingDTO> selectCountersignDrawingsByPartNo(@Param("partNo") String partNo);

    /**
     * 查询所有会签图纸及其文件信息
     */
    List<CountersignDrawingDTO> selectAllCountersignDrawingsWithFiles();
}