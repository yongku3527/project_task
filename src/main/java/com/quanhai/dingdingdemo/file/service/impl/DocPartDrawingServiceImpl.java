package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.dto.DocPartDrawingDTO;
import com.quanhai.dingdingdemo.file.mapper.DocPartDrawingMapper;
import com.quanhai.dingdingdemo.file.model.DocPartDrawing;
import com.quanhai.dingdingdemo.file.service.DocPartDrawingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 零件图纸Service实现类
 */
@Service
public class DocPartDrawingServiceImpl extends ServiceImpl<DocPartDrawingMapper, DocPartDrawing> implements DocPartDrawingService {

    @Autowired
    private DocPartDrawingMapper docPartDrawingMapper;

    @Override
    public DocPartDrawingDTO getDocPartDrawingWithFiles(Long id) {
        DocPartDrawingDTO dto = docPartDrawingMapper.selectDocPartDrawingById(id);

        if (dto != null) {
            // 设置DWG文件URL和名称
            if (dto.getDwgFileUrl() == null && dto.getDwgFileName() == null) {
                Long dwgFileId = dto.getDwgFileId();
                if (dwgFileId != null) {
                    dto.setDwgFileUrl(docPartDrawingMapper.selectFileUrlById(dwgFileId));
                    dto.setDwgFileName(docPartDrawingMapper.selectFileNameById(dwgFileId));
                }
            }
            // 设置PDF文件URL和名称
            if (dto.getPdfFileUrl() == null && dto.getPdfFileName() == null) {
                Long pdfFileId = dto.getPdfFileId();
                if (pdfFileId != null) {
                    dto.setPdfFileUrl(docPartDrawingMapper.selectFileUrlById(pdfFileId));
                    dto.setPdfFileName(docPartDrawingMapper.selectFileNameById(pdfFileId));
                }
            }
        }
        return dto;
    }

    @Override
    public List<DocPartDrawingDTO> getDocPartDrawingsByPartId(String partId) {
        List<DocPartDrawingDTO> dtoList = docPartDrawingMapper.selectDocPartDrawingByPartId(partId);
        
        // 为每个DTO设置文件URL和名称
        for (DocPartDrawingDTO dto : dtoList) {
            // 设置DWG文件URL和名称  
            if (dto.getDwgFileUrl() == null && dto.getDwgFileName() == null) {
                Long dwgFileId = dto.getDwgFileId();
                if (dwgFileId != null) {
                    dto.setDwgFileUrl(docPartDrawingMapper.selectFileUrlById(dwgFileId));
                    dto.setDwgFileName(docPartDrawingMapper.selectFileNameById(dwgFileId));
                }
            }
            
            // 设置PDF文件URL和名称
            if (dto.getPdfFileUrl() == null && dto.getPdfFileName() == null) {
                Long pdfFileId = dto.getPdfFileId();
                if (pdfFileId != null) {
                    dto.setPdfFileUrl(docPartDrawingMapper.selectFileUrlById(pdfFileId));
                    dto.setPdfFileName(docPartDrawingMapper.selectFileNameById(pdfFileId));
                }
            }
        }
        
        return dtoList;
    }

    @Override
    public List<DocPartDrawingDTO> getAllDocPartDrawingsWithFiles() {
        // 直接调用Mapper方法获取所有零件图纸及其文件信息
        return docPartDrawingMapper.selectAllDocPartDrawingsWithFiles();
    }
}