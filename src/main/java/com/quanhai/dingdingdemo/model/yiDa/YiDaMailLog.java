package com.quanhai.dingdingdemo.model.yiDa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YiDaMailLog {
    private Long id;
    /** 文件URL */
    private String fileUrl;
    /** 文件名 */
    private String fileName;
    /** PCB名称 */
    private String pcbName;
    /** 钢网名称 */
    private String gangWangName;
    /** 钢网编号 */
    private String gangWangCode;
    /** 发起用户ID */
    private String originatorUser;
    /** 责任工程师姓名 */
    private String userName;
    /** 收件人 */
    private String recipient;
    /** 邮件主题 */
    private String subject;
    /** 状态: 1=发送成功, 2=发送失败 */
    private Integer status;
    /** 错误信息 */
    private String errorMsg;
    /** 发送时间 */
    private LocalDateTime sendTime;
}
