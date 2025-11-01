package com.quanhai.dingdingdemo.service.msg;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.quanhai.dingdingdemo.config.msg.MsgConfig;
import com.quanhai.dingdingdemo.model.excption.MyExcption;
import com.quanhai.dingdingdemo.tools.CommonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

@Slf4j
@Service
public class MsgService {

    @Autowired
    private MsgConfig msgConfig;

    @Autowired
    private CommonTools tools;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;



    public String sendMsg(String phone,String variable){

        // 2. 构建请求参数
        JSONObject requestParam = buildRequestParam(phone,variable);

        // 3. 构建请求Header（含鉴权参数）
        String timeStamp = String.valueOf(System.currentTimeMillis()); // 毫秒级时间戳
        String token = tools.getMsgToken(timeStamp);
        // 4. 发送POST请求（JSON格式）
        HttpResponse response = HttpRequest.post(msgConfig.getUrl())
                // 设置公共Header鉴权参数
                .header("userId", msgConfig.getUserId())
                .header("timeStamp", timeStamp)
                .header("token", token)
                // 设置请求体（JSON字符串）
                .body(requestParam.toString())
                // 设置编码（强制UTF-8，避免中文乱码）
                .charset("UTF-8")
                // 执行请求
                .execute();

        // 5. 解析响应结果
        String taskId = parseResponse(response);
        Date date = new Date();
        redisTemplate.opsForHash().put("sendDuanXinTaskIdHash", taskId,phone+"&&"+variable+"&&"+date);
        return taskId;
        
    }



    public String sendCustomMsg(String phone,String variable){

        // 2. 构建请求参数
        JSONObject requestParam = buildCustomRequestParam(phone,variable);

        // 3. 构建请求Header（含鉴权参数）
        String timeStamp = String.valueOf(System.currentTimeMillis()); // 毫秒级时间戳
        String token = tools.getMsgToken(timeStamp);
        // 4. 发送POST请求（JSON格式）
        HttpResponse response = HttpRequest.post(msgConfig.getUrl())
                // 设置公共Header鉴权参数
                .header("userId", msgConfig.getUserId())
                .header("timeStamp", timeStamp)
                .header("token", token)
                // 设置请求体（JSON字符串）
                .body(requestParam.toString())
                // 设置编码（强制UTF-8，避免中文乱码）
                .charset("UTF-8")
                // 执行请求
                .execute();

        // 5. 解析响应结果
        String taskId = parseResponse(response);
        Date date = new Date();
        redisTemplate.opsForHash().put("sendDuanXinTaskIdHash", taskId,phone+"&&"+variable+"&&"+date);
        return taskId;

    }



    /**
     * 构建短信发送接口的业务参数
     */
    private static JSONObject buildCustomRequestParam(String phones,String smsContent) {
        JSONObject param = new JSONObject();
        // 必选参数：手机号集合（英文逗号分隔，最多1000个）
        param.put("phones", phones);
        // 必选参数：短信内容（含【签名】，符合平台规范）
//        param.put("smsContent", "【山东泉海汽车科技有限公司】您的验证码是123456，5分钟内有效，请妥善保管！点击www.baidu.com查询公司详细信息");
        param.put("smsContent", smsContent);
        // 可选参数：定时发送时间（格式yyyyMMddHHmmss，不填则立即发送）
        // param.put("sendtime", "20241231235959");
        // 可选参数：扩展号（最大12位）
        // param.put("sendtermid", "23456");
        // 可选参数：客户自定义任务ID（最大70位，用于关联批次）
        // param.put("customTaskid", "task_20241001_001");
        return param;
    }




    /**
     * 构建短信发送接口的业务参数
     */
    private static JSONObject buildRequestParam(String phones,String variable) {
        JSONObject param = new JSONObject();
        // 必选参数：手机号集合（英文逗号分隔，最多1000个）
        param.put("phones", phones);
        // 必选参数：短信内容（含【签名】，符合平台规范）
//        param.put("smsContent", "【山东泉海汽车科技有限公司】您的验证码是123456，5分钟内有效，请妥善保管！点击www.baidu.com查询公司详细信息");
        param.put("smsContent", "【山东泉海汽车科技有限公司】尊敬的贵宾，欢迎您莅临山东泉海！请点击下方链接，并在页面中选择门禁编号点击“开门”按钮，即可开门进入。\n" +
                "https://quanhaikeji.aliwork.com/o/duibi?password="+variable);
        // 可选参数：定时发送时间（格式yyyyMMddHHmmss，不填则立即发送）
        // param.put("sendtime", "20241231235959");
        // 可选参数：扩展号（最大12位）
        // param.put("sendtermid", "23456");
        // 可选参数：客户自定义任务ID（最大70位，用于关联批次）
        // param.put("customTaskid", "task_20241001_001");
        return param;
    }


    /**
     * 解析接口响应结果
     */
    private  String parseResponse(HttpResponse response) {
        if (response.isOk()) { // HTTP状态码200
            String responseBody = response.body();
            JSONObject result = JSONUtil.parseObj(responseBody);

            // 解析响应码（0为成功）
            int code = result.getInt("code");
            if (code == 0) {
                // 成功：获取taskid（用于后续查询状态报告）
                return result.getJSONObject("data").getStr("taskid");

            } else {
                // 失败：输出错误信息
                String msg = result.getStr("msg");
                log.error("短信发送请求失败！错误信息：" + msg);
                try {
                    tools.send("短信发送失败", "3123544976@qq.com", "错误信息：" + msg);
                    return "-1";
                } catch (EmailException e) {
                    throw new RuntimeException(e.getCause());
                }
            }
        } else {
            // HTTP请求失败（如连接超时、404等）
            log.error("HTTP请求失败！状态码：" + response.getStatus());

            try {
                tools.send("短信发送失败", "3123544976@qq.com", "错误信息：" + "HTTP请求失败");
                return "-1";
            } catch (EmailException e) {
                throw new RuntimeException(e.getCause());
            }
        }

    }

}
