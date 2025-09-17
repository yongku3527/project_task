package com.quanhai.dingdingdemo;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.aliyun.dingtalkyida_2_0.Client;
import com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdResponse;
import com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdResponseBody;
import com.aliyun.dingtalkyida_2_0.models.StartInstanceResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quanhai.dingdingdemo.client.MyDingClient;
import com.quanhai.dingdingdemo.config.DingAppConfig;
import com.quanhai.dingdingdemo.config.msg.MsgConfig;
import com.quanhai.dingdingdemo.controller.yiDa.DownloadController;
import com.quanhai.dingdingdemo.controller.yiDa.PrcsController;
import com.quanhai.dingdingdemo.mapper.yiDa.PrcsMapper;
import com.quanhai.dingdingdemo.model.yiDa.CodeAndPrcsIns;
import com.quanhai.dingdingdemo.service.msg.MsgService;
import com.quanhai.dingdingdemo.service.yiDa.PrcsServiceImpl;
import com.quanhai.dingdingdemo.tools.CommonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.EmailException;
import org.apache.ibatis.annotations.Lang;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@SpringBootTest(classes = DingdingDemoApplication.class)
public class testdemo {
    @Autowired
    private DingAppConfig dingAppConfig;

    @Autowired
    private MyDingClient myDingClient;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CommonTools commonTools;


    @Test
    public void addInterface() throws Exception {


        Client client = myDingClient.getyidaClient();
        com.aliyun.dingtalkyida_2_0.models.StartInstanceHeaders startInstanceHeaders = new com.aliyun.dingtalkyida_2_0.models.StartInstanceHeaders();
        startInstanceHeaders.xAcsDingtalkAccessToken = commonTools.getAccessToken();


        GetInstanceByIdResponseBody interfaceInfo = prcsServiceImpl.getInterfaceInfo("00223da0-995c-408e-8070-8d55e1bf1a4c");
        Map map = prcsServiceImpl.replaceEmployeeFieldIds((Map<String, Object>) interfaceInfo.getData());
        System.out.println("map = " + map);
        String formDataJson = JSONUtil.toJsonStr(map);

        com.aliyun.dingtalkyida_2_0.models.StartInstanceRequest startInstanceRequest = new com.aliyun.dingtalkyida_2_0.models.StartInstanceRequest()
                .setSystemToken("8D7668A1LCL2GTN39RHSGD04QLXV3UY0J396LXWI")
                .setFormUuid("FORM-EFEFFE4B77BA46919ED0BB37443069AA7FGB")
                .setUserId("011965232633828543")
                .setAppType("APP_XZT8TFAQ7QCQQW4QQPP1")
                .setFormDataJson(formDataJson)
                .setUseAlias(true);
        try {
            StartInstanceResponse startInstanceResponse = client.startInstanceWithOptions(startInstanceRequest, startInstanceHeaders, new RuntimeOptions());
            System.out.println(startInstanceResponse.getBody().getResult());
        } catch (Exception _err) {
           _err.printStackTrace();
        }


    }




    @Test
    public void saveFile() {
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

    @Test
    public void getInterfaceInfoTest() throws Exception {
//        Map interfaceInfo = getInterfaceInfo();
        getItemInfoForMES();
    }

    private Map getInterfaceInfo() throws Exception {
        Client client = myDingClient.getyidaClient();
        com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdHeaders getInstanceByIdHeaders = new com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdHeaders();
        getInstanceByIdHeaders.xAcsDingtalkAccessToken = commonTools.getAccessToken();
        com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdRequest getInstanceByIdRequest = new com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdRequest()
                .setSystemToken("8D7668A1LCL2GTN39RHSGD04QLXV3UY0J396LXWI")
                .setFormUuid("FORM-EFEFFE4B77BA46919ED0BB37443069AA7FGB")
                .setUserId("02186700545626304822")
                .setAppType("APP_XZT8TFAQ7QCQQW4QQPP1")
                .setUseAlias(true);

            GetInstanceByIdResponse instanceByIdWithOptions = client.getInstanceByIdWithOptions("00223da0-995c-408e-8070-8d55e1bf1a4c", getInstanceByIdRequest, getInstanceByIdHeaders, new RuntimeOptions());
            return instanceByIdWithOptions.getBody().getData();

    }


    @Autowired
    private PrcsServiceImpl prcsServiceImpl;
    @Autowired
    private PrcsMapper prcsMapper;

    @Autowired
    private PrcsController prcsController;

    @Autowired
    private DownloadController downloadController;
    @Autowired
    private CommonTools tools;


    @Autowired
    private MsgService msgService;

    @Autowired
    private MsgConfig msgConfig;

    @Test
    @Transactional
    public void getItemInfoForMES() throws Exception {
//      prcsServiceImpl.getItemNum("0050021129");
//        prcsController.jiekou1("0010090865","00223da0-995c-408e-8070-8d55e1bf1a4c");


//            prcsServiceImpl.creatNewInterface("367679f6-e06c-4980-a8b1-68c8e177b4a8");
//        prcsServiceImpl.getInterfaceInfo("123");

//        commonTools.send("subject","msg");
//        downloadController.sendMailV1("https://tianshu-vpc-private.oss-cn-shanghai.aliyuncs.com/APP_XZT8TFAQ7QCQQW4QQPP1_MTEwNTM0MTc1NTg0NzI1Ml9RODY2NjdCMUFCRlkzU0lPOUcwSUs5M1JIOTg2M0RBNUpFM0ZNT0Q%24.zip?Expires=1756965080&OSSAccessKeyId=LTAITJPdNYBKla7D&Signature=jBHuf6h5JpsCCusyHK%2By4Z00OAc%3D");

        prcsServiceImpl.creatNewInterface("14bf8029-0436-42aa-a538-9bab061c2a72");

//        prcsServiceImpl.getItemNum("0010090872");

//        downloadController.sendMailV2("{\"FileUrl\":\"https://tianshu-vpc-private.oss-cn-shanghai.aliyuncs.com/APP_XZT8TFAQ7QCQQW4QQPP1_MDExOTY1MjMyNjMzODI4NTQzX1E4NjY2N0IxTzJHWUpPTFpBQjMxTDdVMjZJOTAzRkxWQTY2Rk1WRw%24%24.mp3?Expires=1757123207&OSSAccessKeyId=LTAITJPdNYBKla7D&Signature=zL7RQRp%2FVu33w9e1TjylaUw4Uy4%3D\"}",
//                "123.mp3",
//                "线路板名称测试测\n试测试测试测试",
//                "钢网名称测试测试测\n试测试测试",
//                "钢网编号测试测\n试测试测试");

//        try {
//            tools.send("ceshi ","3123544976@qq.com","内容测试");
//        } catch (EmailException e) {
//            e.getCause().printStackTrace();
//
//        }
//        String msgToken = tools.getMsgToken();
//        System.out.println("msgToken = " + msgToken);

//        msgService.sendMsg("13326351595","【山东泉海汽车科技有限公司】尊敬的贵宾，欢迎您光临山东泉海，请点击链接，点击“打开”按钮开门。https://quanhaikeji.aliwork.com/o/duibi?password=6sa1df6a1sdf54aw6e4f31sd5f4a6wef");


    }

}
