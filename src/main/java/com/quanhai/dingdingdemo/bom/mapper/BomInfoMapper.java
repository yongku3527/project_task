package com.quanhai.dingdingdemo.bom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanhai.dingdingdemo.bom.model.BomInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * BOM信息主表Mapper
 */
@Mapper
public interface BomInfoMapper extends BaseMapper<BomInfo> {
}
