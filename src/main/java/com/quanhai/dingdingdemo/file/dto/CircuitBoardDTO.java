package com.quanhai.dingdingdemo.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 线路板DTO，用于前端展示
 */
@Data

public class CircuitBoardDTO {


    private Long id;

    private String boardCode;


    private String boardName;


    private String fileUrl;


    private String fileName;


    private Integer status;

    /**
     * 半成品DTO列表
     */
    private List<SemiProductDTO> semiProductDTOList;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}