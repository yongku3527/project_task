package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.dto.CountersignDrawingDTO;
import com.quanhai.dingdingdemo.file.mapper.CountersignDrawingMapper;
import com.quanhai.dingdingdemo.file.model.CountersignDrawing;
import com.quanhai.dingdingdemo.file.service.CountersignDrawingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会签图纸Service实现类
 */
@Service
public class CountersignDrawingServiceImpl extends ServiceImpl<CountersignDrawingMapper, CountersignDrawing> implements CountersignDrawingService {

    @Autowired
    private CountersignDrawingMapper countersignDrawingMapper;

    /**
     * 根据ID查询会签图纸及其文件信息
     */
    @Override
    public CountersignDrawingDTO getCountersignDrawingWithFiles(Long id) {
        return countersignDrawingMapper.selectCountersignDrawingById(id);
    }

    /**
     * 根据零部件号查询会签图纸列表
     */
    @Override
    public List<CountersignDrawingDTO> getCountersignDrawingsByPartNo(String partNo) {
        return countersignDrawingMapper.selectCountersignDrawingsByPartNo(partNo);
    }

    /**
     * 获取所有会签图纸及其文件信息
     */
    @Override
    public List<CountersignDrawingDTO> getAllCountersignDrawingsWithFiles() {
        return countersignDrawingMapper.selectAllCountersignDrawingsWithFiles();
    }
}