package com.quanhai.dingdingdemo.service.yiDa;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.dingtalkyida_2_0.Client;
import com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdResponse;
import com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdResponseBody;
import com.aliyun.dingtalkyida_2_0.models.StartInstanceResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.quanhai.dingdingdemo.client.MyDingClient;
import com.quanhai.dingdingdemo.config.DingAppConfig;

import com.quanhai.dingdingdemo.mapper.yiDa.PrcsMapper;
import com.quanhai.dingdingdemo.model.excption.MyExcption;
import com.quanhai.dingdingdemo.tools.CommonTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class PrcsServiceImpl {
    @Autowired
    private DingAppConfig dingAppConfig;

    @Autowired
    private MyDingClient myDingClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CommonTools commonTools;

    @Autowired
    private PrcsMapper prcsMapper;

    private Logger logger = LoggerFactory.getLogger(PrcsServiceImpl.class);


    @Transactional
    public String creatNewInterface(String prcsInterface) throws Exception {
        //获取详情
        GetInstanceByIdResponseBody interfaceInfo = getInterfaceInfo(prcsInterface);
        if(interfaceInfo == null){
            return "null";
        }
        //创建新流程
        String InterfaceId = addInterface(interfaceInfo);

        return InterfaceId ;

    }

    public GetInstanceByIdResponseBody getInterfaceInfo(String prcsInterface) throws Exception {

        Client client = myDingClient.getyidaClient();
        com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdHeaders getInstanceByIdHeaders = new com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdHeaders();
        getInstanceByIdHeaders.xAcsDingtalkAccessToken = commonTools.getAccessToken();
        com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdRequest getInstanceByIdRequest = new com.aliyun.dingtalkyida_2_0.models.GetInstanceByIdRequest()
                .setSystemToken("8D7668A1LCL2GTN39RHSGD04QLXV3UY0J396LXWI")
                .setFormUuid("FORM-41085269FC7642EAB6E3745A67C6EC0DPUGV")
                .setUserId(dingAppConfig.getUserid())
                .setAppType("APP_XZT8TFAQ7QCQQW4QQPP1")
                .setUseAlias(true);


            GetInstanceByIdResponse  instanceByIdWithOptions = client.getInstanceByIdWithOptions(prcsInterface, getInstanceByIdRequest, getInstanceByIdHeaders, new RuntimeOptions());
            return instanceByIdWithOptions.getBody();


    }



    public String addInterface(GetInstanceByIdResponseBody interfaceInfo) {

        Client client = myDingClient.getyidaClient();
        com.aliyun.dingtalkyida_2_0.models.StartInstanceHeaders startInstanceHeaders = new com.aliyun.dingtalkyida_2_0.models.StartInstanceHeaders();
        startInstanceHeaders.xAcsDingtalkAccessToken = commonTools.getAccessToken();
        //表单组件数据
        Map<String,Object> data = replaceEmployeeFieldIds((Map<String, Object>) interfaceInfo.getData());

        System.out.println("formData = " + data);
        //TODO 需要修改


        String formDataJson = JSONUtil.toJsonStr(data);
        //获取创建人
        String OriginatorUserId = interfaceInfo.getOriginator().getUserId();


        com.aliyun.dingtalkyida_2_0.models.StartInstanceRequest startInstanceRequest = new com.aliyun.dingtalkyida_2_0.models.StartInstanceRequest()
                .setSystemToken("8D7668A1LCL2GTN39RHSGD04QLXV3UY0J396LXWI")
                .setFormUuid("FORM-EE8496CDE0A34B969FD34A6590EBCCD8KSFZ")
                .setUserId(OriginatorUserId)
                .setAppType("APP_XZT8TFAQ7QCQQW4QQPP1")
                .setFormDataJson(formDataJson)
                .setUseAlias(true);
        try {
            StartInstanceResponse startInstanceResponse = client.startInstanceWithOptions(startInstanceRequest, startInstanceHeaders, new RuntimeOptions());
            String interfaceId = startInstanceResponse.getBody().getResult();

            return interfaceId;

        } catch (Exception _err) {
            logger.error("流程创建失败 msg: "+_err.getMessage());
            throw new RuntimeException(_err);
        }
    }


    public BigDecimal getItemNum(String ItemCode) {
        // 1. 定义文件上传接口URL
        String url = "http://192.168.100.19:8715/dingdingReport/getSingleItemStock";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("itemCode", ItemCode);

        String Response = HttpUtil.post(url, paramMap);


        JSONObject jsonResponse = JSONUtil.parseObj(Response);

        JSONObject result = jsonResponse.getJSONObject("result");
        Object projectNum = result.get("projectNum");
       if (result.isEmpty()){
           logger.error(" 未找到物料编号:"+ItemCode);
           return BigDecimal.ZERO;
       }

        return new BigDecimal(projectNum.toString());
    }


    public Map replaceEmployeeFieldIds(Map<String, Object> data){
        // 预编译正则，提高效率
        Pattern pattern = Pattern.compile("^employeeField_.+_id$");

        data.entrySet()
                .stream()
                .filter(e -> pattern.matcher(e.getKey()).matches()) // 只处理 *id 字段
                .forEach(e -> {
                    String idKey   = e.getKey();                       // employeeField_xxx_id
                    String valKey  = idKey.substring(0, idKey.length() - 3); // 去掉 “_id”
                    Object idValue = e.getValue();                     // 可能是 List 或单个值

                    // 如果存在对应的无后缀字段，则覆盖
                    if (data.containsKey(valKey)) {
                        data.put(valKey, idValue);
                    }
                });

        return data;
    }


}
