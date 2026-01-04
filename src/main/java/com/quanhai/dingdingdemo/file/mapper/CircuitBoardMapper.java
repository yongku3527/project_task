package com.quanhai.dingdingdemo.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.file.dto.CircuitBoardDTO;
import com.quanhai.dingdingdemo.file.dto.LedBoardPluginSemiProductDTO;
import com.quanhai.dingdingdemo.file.dto.SemiProductDTO;
import com.quanhai.dingdingdemo.file.model.CircuitBoard;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 线路板Mapper接口
 */
@Mapper
public interface CircuitBoardMapper extends BaseMapper<CircuitBoard> {

    /**
     * 根据ID查询线路板及其嵌套的半成品、灯板插件、文件信息
     */
    CircuitBoardDTO selectCircuitBoardById(@Param("id") Long id);

    /**
     * 根据线路板ID查询半成品列表（嵌套灯板插件及文件信息）
     */
    List<SemiProductDTO> selectSemiProductByBoardId(@Param("boardId") Long boardId);

    /**
     * 根据半成品ID查询灯板插件列表（含文件信息）
     */
    List<LedBoardPluginSemiProductDTO> selectLedPluginBySemiId(@Param("semiId") Long semiId);

    /**
     * 根据文件ID查询文件URL
     */
    String selectFileUrlById(@Param("fileId") Long fileId);

    /**
     * 根据文件ID查询文件名称
     */
    String selectFileNameById(@Param("fileId") Long fileId);

 
    /**
     * 根据文件ID删除线路板、半成品、灯板插件关联记录
     */
    @Delete("DELETE FROM circuit_board WHERE id = #{id}")
    Integer deleteByBoardId(@Param("id") Long id);
}