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
import com.quanhai.dingdingdemo.file.dto.CircuitBoardDTO;
import com.quanhai.dingdingdemo.file.mapper.CircuitBoardMapper;
import com.quanhai.dingdingdemo.file.mapper.ItemTypeMapper;
import com.quanhai.dingdingdemo.file.model.ItemType;
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

    @Autowired
    private CircuitBoardMapper circuitBoardMapper;


    @Autowired
    private ItemTypeMapper itemTypeMapper;

    @Test
    @Transactional
    public void getItemInfoForMES() throws Exception {

        ItemType itemType = itemTypeMapper.selectOne(new LambdaQueryWrapper<ItemType>().eq(ItemType::getItemPrefix, "SFJ"));
        System.out.println("itemType = " + itemType);
    }

}
