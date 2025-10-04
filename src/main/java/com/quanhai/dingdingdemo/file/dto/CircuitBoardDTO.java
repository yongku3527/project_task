package com.quanhai.dingdingdemo.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 线路板DTO，用于前端展示
 */
@Data
public class CircuitBoardDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 线路板编码
     */
    private String boardCode;

    /**
     * 线路板名称
     */
    private String boardName;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 半成品DTO列表
     */
    private List<SemiProductDTO> semiProductDTOList;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


}