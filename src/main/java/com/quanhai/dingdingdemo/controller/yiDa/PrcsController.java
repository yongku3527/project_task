package com.quanhai.dingdingdemo.controller.yiDa;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.mapper.yiDa.PrcsMapper;
import com.quanhai.dingdingdemo.mapper.yiDa.ReminderInfoMapper;
import com.quanhai.dingdingdemo.mapper.yiDa.YiDaPrcsCreationLogMapper;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.yiDa.CodeAndPrcsIns;
import com.quanhai.dingdingdemo.model.yiDa.ReminderInfo;
import com.quanhai.dingdingdemo.model.yiDa.ReminderInfoDto;
import com.quanhai.dingdingdemo.model.yiDa.YiDaPrcsCreationLog;
import com.quanhai.dingdingdemo.service.TaskService;
import com.quanhai.dingdingdemo.service.yiDa.PrcsServiceImpl;
import com.quanhai.dingdingdemo.task.yiDaTask;
import com.quanhai.dingdingdemo.tools.CommonTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.jacoco.agent.rt.internal_035b120.core.internal.flow.IFrame;
import org.simpleframework.xml.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prcs")
public class PrcsController {

    @Autowired
    private PrcsServiceImpl prcsService;

    @Autowired
    private PrcsMapper prcsMapper;

    @Autowired
    private CommonTools commonTools;

    @Autowired
    private MailConfig mailConfig;

    private Logger logger = LoggerFactory.getLogger(PrcsController.class);



    @Autowired
    private YiDaPrcsCreationLogMapper prcsCreationLogMapper;

    @PostMapping("/addOrSave")
    public Result addOrSave( String itemCode, String prcsInstance) {
        //传入   物料编码、流程实例
        BigDecimal itemNum = prcsService.getItemNum(itemCode);

        if(itemNum.compareTo(new BigDecimal(0)) == 0){
            //直接存数据库 记日志 返回
            prcsMapper.insert(new CodeAndPrcsIns(null,itemCode,prcsInstance));

            // 记录到流程创建日志表（待创建状态）
            YiDaPrcsCreationLog creationLog = new YiDaPrcsCreationLog();
            creationLog.setItemCode(itemCode);
            creationLog.setPrcsInstanceCode(prcsInstance);
            creationLog.setStatus(0);
            creationLog.setCreateTime(LocalDateTime.now());
            prcsCreationLogMapper.insert(creationLog);

            logger.info("工单在产数为零，存数据库");
            return ResultUtil.success("工单在产数为零，存数据库");
        }else {
            //工单在产数不为零
            //查询表单详情，
            try {
                String newInstanceId = prcsService.creatNewInterface(prcsInstance);

                // 记录到流程创建日志表（创建成功）
                YiDaPrcsCreationLog creationLog = new YiDaPrcsCreationLog();
                creationLog.setItemCode(itemCode);
                creationLog.setPrcsInstanceCode(prcsInstance);
                creationLog.setNewInstanceId(newInstanceId);
                creationLog.setStatus(1);
                creationLog.setCreateTime(LocalDateTime.now());
                prcsCreationLogMapper.insert(creationLog);

            } catch (Exception e) {
                logger.error(e.getMessage());

                // 记录到流程创建日志表（创建失败）
                YiDaPrcsCreationLog creationLog = new YiDaPrcsCreationLog();
                creationLog.setItemCode(itemCode);
                creationLog.setPrcsInstanceCode(prcsInstance);
                creationLog.setStatus(2);
                creationLog.setErrorMsg(e.getMessage());
                creationLog.setCreateTime(LocalDateTime.now());
                prcsCreationLogMapper.insert(creationLog);

                try {
                    commonTools.send("接口创建新流程失败",mailConfig.getBadTo(),"参数详情：itemCode:"+itemCode+"prcsInstance:"+prcsInstance+"\n报错信息： "+e.getMessage());
                } catch (EmailException ex) {
                    logger.error("报错邮件发送失败(接口) ："+ex.getMessage()+"\n");
                }
                return ResultUtil.fail(e.getMessage());
            }

            logger.info("工单在产数不为零,已创建新流程");
            return ResultUtil.success("工单在产数不为零,已创建新流程");
        }
    }

    @Autowired
    private ReminderInfoMapper reminderInfoMapper;

    @Autowired
    private TaskService taskService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;






    /**
     * 保存定时提醒信息
     * @return
     */
    @PostMapping("/saveTimedReminderInfo")
    public Result saveTimedReminderInfo(
            @RequestParam(required = false) String yltExecutorIdStr,
            @RequestParam(required = false) String pcbExecutorIdStr,
            @RequestParam String pcbName) {
        logger.info("保存定时提醒信息: 原理图审批人字符串 = " + yltExecutorIdStr + ", pcb审批人字符串 = " + pcbExecutorIdStr + ", 线路板名称 = " + pcbName);

        if (StringUtils.isNotBlank(yltExecutorIdStr)) {
            saveRedisAndDb(yltExecutorIdStr, pcbName);
        }
        if (StringUtils.isNotBlank(pcbExecutorIdStr)) {
            saveRedisAndDb(pcbExecutorIdStr, pcbName);
        }
        return ResultUtil.success("保存成功");
    }

    private void saveRedisAndDb(String executorIdStr, String pcbName) {
        JSONArray executorIdListJSON = JSONUtil.parseArray(executorIdStr);
        ArrayList<String> executorIdList = executorIdListJSON.stream().map(Object::toString).collect(Collectors.toCollection(ArrayList::new));


        //对钉钉用户手机号预处理
        preloadMobile(executorIdList);

        //创建提醒信息列表
        List<ReminderInfo> reminderInfoList = new ArrayList<>();
        for (String executorId : executorIdList) {
            reminderInfoList.add(new ReminderInfo(null, pcbName,executorId,LocalDateTime.now()));
        }
        reminderInfoMapper.insert(reminderInfoList);
    }


    /**
     * 某人审核通过后 删除该条提醒信息
     * @param executorId 审核通过的用户
     * @param pcbName 线路板名称
     * 根据PCB名称和审核通过的用户 删除该条提醒信息
     * @return
     */
    @PostMapping("/deleteReminderInfoOne")
    public Result deleteReminderInfoOne(@RequestParam String executorId, @RequestParam String pcbName) {

        logger.info("一条节点审核通过 删除该条提醒信息： executorId = " + executorId + ", pcbName = " + pcbName);
        String replace = "";

        if (!StringUtils.isBlank(executorId)) {
             replace = executorId.replace("[", "").replace("]", "").replace("\"", "");
        }
        System.out.println("replace = " + replace);

        reminderInfoMapper.delete(new LambdaQueryWrapper<ReminderInfo>()
                .eq(ReminderInfo::getPcbName,pcbName)
                .in(ReminderInfo::getExecutorId,replace));

        return ResultUtil.success("删除成功");
    }


    /**
     * 某人审核退回后 删除和线路板名称相关的提醒信息
     * @param pcbName 线路板名称
     * @return
     */
    @PostMapping("/deleteReminderInfoAll")
    public Result deleteReminderInfoAll(@RequestParam String pcbName) {

        System.out.println("pcbName = " + pcbName);
        logger.info("某节点审核退回 删除所有相关信息： pcbName = " + pcbName);

        reminderInfoMapper.delete(new LambdaQueryWrapper<ReminderInfo>()
                .eq(ReminderInfo::getPcbName,pcbName));

        return ResultUtil.success("删除成功");
    }



    private void preloadMobile(ArrayList<String> executorIdList) {


        if (!executorIdList.isEmpty()) {

            // 2. 构建请求URL（包含Query参数access_token）
            String url = "https://oapi.dingtalk.com/topapi/v2/user/get" +
                    "?access_token=" + taskService.getAccessToken();

            for (String executorId : executorIdList) {
                //redis中存在就不查了
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Reminder:" + executorId))) {
                    continue;
                }

                // 3. 构建Body参数（JSON格式）
                JSONObject bodyJson = new JSONObject();
                bodyJson.put("userid", executorId);
                // 4. 发送POST请求并获取响应
                String result = HttpUtil.post(url, bodyJson.toString());
                // 5. 处理响应结果（此处仅打印，实际可解析JSON）
                System.out.println("接口响应：" + result);
                try {
                    JSONObject jsonObj = JSONUtil.parseObj(result);

                    // 检查接口调用状态
                    if (jsonObj.getInt("errcode") != 0) {
                        logger.error("查询员工详细信息接口调用失败: " + jsonObj.getStr("errmsg"));
                        continue;
                    }

                    // 获取用户信息对象
                    JSONObject userInfo = jsonObj.getJSONObject("result");
                    if (userInfo == null) {
                        logger.error("用户信息为空");
                        continue;
                    }

                    // 提取用户名称
                    String userName = userInfo.getStr("name", "未知");
                    String mobile = userInfo.getStr("mobile", "000");

                    redisTemplate.opsForValue().set("Reminder:" + executorId, userName+","+mobile);

                } catch (Exception e) {
                    logger.error(" 查询员工详细信息接口调用失败 ："+e.getMessage());
                }
            }

        }
    }


}
