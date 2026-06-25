package com.quanhai.dingdingdemo.bom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.bom.model.BomOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * BOM操作日志表Mapper
 */
@Mapper
public interface BomOperationLogMapper extends BaseMapper<BomOperationLog> {
}
