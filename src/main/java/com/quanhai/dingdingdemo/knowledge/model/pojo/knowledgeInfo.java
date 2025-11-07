package com.quanhai.dingdingdemo.knowledge.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 经验库信息实体类
 */
@Data
@TableName("knowledge_info")
public class KnowledgeInfo {

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
    @TableField("product_category")
    private String productCategory;

    /** 失效模式（标准字典值，如 不开机、花屏、烧屏） */
     @TableField("failure_mode")
    private String failureMode;


    /** 问题来源（枚举：IQC、市场、生产线、客退、可靠性实验…） */
     @TableField("issue_source")
    private String issueSource;


    /** 问题描述 */
    @TableField("issue_description")
    private String issueDescription;

    /**
     * 问题附件id
     */
    @TableField("issue_attachments_id")
    private Long issueAttachmentsId;


    /** 根本原因 */
    @TableField("root_cause")
    private String rootCause;

    /** 永久处理措施 */
    @TableField("permanent_action")
    private String permanentAction;


    /** 措施附件id */
    @TableField("action_attachments_id")
    private Long actionAttachmentsId;


    /** 应用场景 */
    @TableField("application_scene")
    private String applicationScene;

    /**
     * 所属角色字段
     */
     @TableField("role")
    private String role;

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