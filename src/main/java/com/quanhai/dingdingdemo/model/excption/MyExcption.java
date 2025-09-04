package com.quanhai.dingdingdemo.model.excption;

import lombok.Data;
import lombok.EqualsAndHashCode;


public class MyExcption extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String msg;


    public MyExcption(String message) {
        this.msg = message;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
