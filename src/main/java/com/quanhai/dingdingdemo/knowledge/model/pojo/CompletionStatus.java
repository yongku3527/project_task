package com.quanhai.dingdingdemo.knowledge.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("completion_status")
public class CompletionStatus {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 经验信息id
     */
    @TableField("knowledge_info_id")
    private Long knowledgeInfoId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private String userId;


    /**
     * 完成状态（0待完成、1已学习、2已掌握）
     */
    @TableField("completion_status")
    private Integer completionStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
