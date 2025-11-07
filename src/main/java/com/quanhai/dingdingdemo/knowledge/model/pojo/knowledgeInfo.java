package com.quanhai.dingdingdemo.knowledge.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 线路板实体类
 */
@Data
@TableName("knowledge_nfo")
public class knowledgeInfo {

    /**
     * 主键ID
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 产品机型
     */
    @TableField("product_model")
    private String productModel;

    /** 产品分类 */
    private String productCategory;

    /** 失效模式（标准字典值，如 不开机、花屏、烧屏） */
    private String failureMode;


    /** 问题来源（枚举：IQC、市场、生产线、客退、可靠性实验…） */
    private String issueSource;


    /** 问题描述 */
    private String issueDescription;


    /**
     * 问题附件
     */
    @TableField("dwg_file_id")
    private Long issueAttachments;


    /** 根本原因 */
    private String rootCause;

    /** 永久处理措施 */
    private String permanentAction;


    /** 措施附件 */
    private String actionAttachments;


    /** 应用场景 */
    private String applicationScene;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}