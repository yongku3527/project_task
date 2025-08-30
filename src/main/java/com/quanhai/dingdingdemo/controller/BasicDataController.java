package com.quanhai.dingdingdemo.controller;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/basicData")
public class BasicDataController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("userNameData")
    public Result userNameData() {

        List<String> idNameList = new ArrayList<>();

        for (Object key : redisTemplate.keys("executorId_*")) {
            String redisKey = key.toString();
            String userId = redisKey.split("_")[1];
            String userName = (String) redisTemplate.opsForValue().get(redisKey);

            idNameList.add(userId+";"+userName);
        }

        System.out.println(idNameList);

        return ResultUtil.success(idNameList);
    }
}
