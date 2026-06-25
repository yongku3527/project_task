package com.quanhai.dingdingdemo.bom.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * BOM操作日志表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bom_operation_log")
public class BomOperationLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("bom_id")
    private Long bomId;

    @TableField("detail_id")
    private Long detailId;

    @TableField("operation_type")
    private String operationType;

    @TableField("field_name")
    private String fieldName;

    @TableField("old_value")
    private String oldValue;

    @TableField("new_value")
    private String newValue;

    @TableField("operate_by")
    private String operateBy;

    @TableField(value = "operate_time", fill = FieldFill.INSERT)
    private LocalDateTime operateTime;

    @TableField("remark")
    private String remark;
}
