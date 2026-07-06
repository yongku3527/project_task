package com.quanhai.dingdingdemo.model.yiDa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YiDaPrcsCreationLog {
    private Long id;
    /** 物料编码 */
    private String itemCode;
    /** 原流程实例号 */
    private String prcsInstanceCode;
    /** 新创建的流程实例ID */
    private String newInstanceId;
    /** 状态: 0=待创建, 1=创建成功, 2=创建失败 */
    private Integer status;
    /** 错误信息 */
    private String errorMsg;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
