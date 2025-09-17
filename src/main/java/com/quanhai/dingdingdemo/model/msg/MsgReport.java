package com.quanhai.dingdingdemo.model.msg;

import lombok.Data;

@Data
public class MsgReport {
    private String id; //消息流水号
    private String taskId; //
    private String phone;
    private String customerTaskId;
    private String result;
    private String resultMsg;
    private String reportTime;
    private String submitTime;
    private String customerExtNum;
    private Object extParam;
}
