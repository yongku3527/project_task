package com.quanhai.dingdingdemo.controller.msg;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.msg.MsgReport;
import com.quanhai.dingdingdemo.service.msg.MsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/msg")
public class MsgController {

    @Autowired
    private MsgService msgService;

    //开门短信发送
    @PostMapping("/sendMessage")
    public Result sendMessage(String phone,String variable) {

        //传入手机号和内容
        String taskId = msgService.sendMsg(phone, variable);

        return ResultUtil.success(taskId);
    }

//    @PostMapping("/receiveTaskIdStatus")
//    public Result receiveTaskIdStatus(ArrayList<MsgReport> reports) {
//
//
//
//
//        return ResultUtil.success("ok");
//    }


}
