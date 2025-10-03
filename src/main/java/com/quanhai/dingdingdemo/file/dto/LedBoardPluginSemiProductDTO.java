package com.quanhai.dingdingdemo.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 灯板插件半成品DTO，用于前端展示
 */
@Data

public class LedBoardPluginSemiProductDTO {


    private Long id;


    private Long semiProductId;


    private String semiProductCode;


    private String semiProductName;


    private String ledBoardPluginCode;


    private String ledBoardPluginName;



    private String fileUrl;


    private String fileName;


    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}