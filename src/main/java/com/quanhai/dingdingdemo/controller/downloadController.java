package com.quanhai.dingdingdemo.controller;

import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

//@RestController
//@RequestMapping("/sendMail")
public class downloadController {

    @PostMapping("/v1")
    public Result sendMailV1(@RequestParam String FileUrl) {
        String fileUrl = FileUrl;
        String savePath = "\\\\192.168.100.12\\信息化部\\IT内容\\李正鑫\\免登录附件文件夹\\123.txt";
        try {
            FileUtils.copyURLToFile(new URL(fileUrl), new File(savePath));
            //成功发邮件
            return ResultUtil.success("文件下载成功！");
        } catch (Exception e) {
            System.out.println("文件下载失败：" + e.getMessage());
            //失败发邮件
            return ResultUtil.fail("文件下载失败！"+e.getMessage());
        }
    }
}
