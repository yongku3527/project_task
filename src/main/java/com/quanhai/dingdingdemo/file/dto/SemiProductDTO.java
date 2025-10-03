package com.quanhai.dingdingdemo.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 半成品DTO，用于前端展示
 */
@Data

public class SemiProductDTO {


    private Long id;

    private Long circuitBoardId;

    private String circuitBoardCode;

    private String circuitBoardName;

    private String semiProductCode;


    private String semiProductName;


    private String schematicFileUrl;


    private String schematicFileName;


    private String smtFileUrl;


    private String smtFileName;


    private Integer status;

    /**
     * 灯板插件半成品DTO列表
     */
    private List<LedBoardPluginSemiProductDTO> ledBoardPluginSemiProductDTOList;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}