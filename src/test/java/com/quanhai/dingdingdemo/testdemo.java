package com.quanhai.dingdingdemo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@SpringBootTest(classes = DingdingDemoApplication.class)
public class testdemo {


    @Test
    public void test11() {
        String fileUrl = "https://down.sandai.net/thunder11/XunLeiWebSetup12.4.1.3670xl11.exe";

        String savePath = "src/main/resources/static/3670xl11.exe";

        try {
            FileUtils.copyURLToFile(new URL(fileUrl), new File(savePath));
            System.out.println("文件下载成功！");
        } catch (IOException e) {
            System.out.println("文件下载失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

}
