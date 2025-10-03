package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.mapper.FileInfoMapper;
import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.file.service.FileInfoService;
import org.springframework.stereotype.Service;

/**
 * 文件信息Service实现类
 */
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {
}