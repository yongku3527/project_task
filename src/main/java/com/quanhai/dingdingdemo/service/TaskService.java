package com.quanhai.dingdingdemo.service;

import com.quanhai.dingdingdemo.model.Resp.Result;

public interface TaskService {

    void setTaskVoToRedis() throws Exception;

    Result getTaskInfo();

}
