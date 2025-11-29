package com.quanhai.dingdingdemo.knowledge.model.vo;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 经验库信息实体类
 */
@Data
public class KnowledgeInfoVo {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 产品机型
     */

    private String productModel;

    /**
     * 产品分类
     */

    private String productCategory;

    /**
     * 失效模式（标准字典值，如 不开机、花屏、烧屏）
     */

    private String failureMode;


    /**
     * 问题来源（枚举：IQC、市场、生产线、客退、可靠性实验…）
     */

    private String issueSource;


    /**
     * 问题描述
     */

    private String issueDescription;

    /**
     * 问题附件id
     */

    private Long issueAttachmentsId;


    /**
     * 根本原因
     */

    private String rootCause;

    /**
     * 永久处理措施
     */

    private String permanentAction;


    /**
     * 措施附件id
     */
    private Long actionAttachmentsId;


    /**
     * 应用场景
     */

    private String applicationScene;

    /**
     * 所属角色字段
     */

    private String role;


    /**
     * 创建时间
     */

    private LocalDateTime createTime;

    /**
     * 修改时间
     */

    private LocalDateTime updateTime;

    //下面是vo特有的字段

    /**
     * 完成状态（0待完成、1已学习、2已掌握）
     */
    private Integer completionStatus;

    /**
     * 问题附件文件名
     *
     */
    private String issueAttachmentsFileName;
    /**
     * 问题附件URL
     */
    private String issueAttachmentsFileUrl;

    /**
     * 措施附件文件名
     */
    private String actionAttachmentsFileName;
    /**
     * 措施附件URL
     */
    private String actionAttachmentsFileUrl;
}