package com.quanhai.dingdingdemo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanhai.dingdingdemo.file.dto.DocProdDrawingDTO;
import com.quanhai.dingdingdemo.file.mapper.DocProdDrawingMapper;
import com.quanhai.dingdingdemo.file.model.docProdDrawing;
import com.quanhai.dingdingdemo.file.service.DocProdDrawingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成品图纸Service实现类
 */
@Service
public class DocProdDrawingServiceImpl extends ServiceImpl<DocProdDrawingMapper, docProdDrawing> implements DocProdDrawingService {

    @Autowired
    private DocProdDrawingMapper docProdDrawingMapper;

    @Override
    public DocProdDrawingDTO getDocProdDrawingWithFiles(Long id) {
        DocProdDrawingDTO dto = docProdDrawingMapper.selectDocProdDrawingById(id);

        if (dto != null) {
            // 设置DWG文件URL和名称
            if (dto.getDwgFileUrl() == null && dto.getDwgFileName() == null) {
                Long dwgFileId = dto.getDwgFileId();
                if (dwgFileId != null) {
                    dto.setDwgFileUrl(docProdDrawingMapper.selectFileUrlById(dwgFileId));
                    dto.setDwgFileName(docProdDrawingMapper.selectFileNameById(dwgFileId));
                }
            }
            // 设置PDF文件URL和名称
            if (dto.getPdfFileUrl() == null && dto.getPdfFileName() == null) {
                Long pdfFileId = dto.getPdfFileId();
                if (pdfFileId != null) {
                    dto.setPdfFileUrl(docProdDrawingMapper.selectFileUrlById(pdfFileId));
                    dto.setPdfFileName(docProdDrawingMapper.selectFileNameById(pdfFileId));
                }
            }
        }
        return dto;
    }

    @Override
    public List<DocProdDrawingDTO> getDocProdDrawingsByPid(String pid) {
        List<DocProdDrawingDTO> dtoList = docProdDrawingMapper.selectDocProdDrawingByPid(pid);
        
        // 为每个DTO设置文件URL和名称
        for (DocProdDrawingDTO dto : dtoList) {
            // 设置DWG文件URL和名称  
            if (dto.getDwgFileUrl() == null && dto.getDwgFileName() == null) {
                Long dwgFileId = dto.getDwgFileId();
                if (dwgFileId != null) {
                    dto.setDwgFileUrl(docProdDrawingMapper.selectFileUrlById(dwgFileId));
                    dto.setDwgFileName(docProdDrawingMapper.selectFileNameById(dwgFileId));
                }
            }
            
            // 设置PDF文件URL和名称
            if (dto.getPdfFileUrl() == null && dto.getPdfFileName() == null) {
                Long pdfFileId = dto.getPdfFileId();
                if (pdfFileId != null) {
                    dto.setPdfFileUrl(docProdDrawingMapper.selectFileUrlById(pdfFileId));
                    dto.setPdfFileName(docProdDrawingMapper.selectFileNameById(pdfFileId));
                }
            }
        }
        
        return dtoList;
    }

    @Override
    public List<DocProdDrawingDTO> getAllDocProdDrawingsWithFiles() {
        // 直接调用Mapper方法获取所有成品图纸及其文件信息
        return docProdDrawingMapper.selectAllDocProdDrawingsWithFiles();
    }

    @Override
    public boolean isPidExists(String pid) {
        // 检查指定的成品编号是否已存在
        int count = docProdDrawingMapper.selectCountByPid(pid);
        return count > 0;
    }
    
}