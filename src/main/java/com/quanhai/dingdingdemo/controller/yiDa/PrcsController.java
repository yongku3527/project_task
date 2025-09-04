package com.quanhai.dingdingdemo.controller.yiDa;

import cn.hutool.log.Log;
import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.mapper.yiDa.PrcsMapper;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.yiDa.CodeAndPrcsIns;
import com.quanhai.dingdingdemo.service.yiDa.PrcsServiceImpl;
import com.quanhai.dingdingdemo.task.yiDaTask;
import com.quanhai.dingdingdemo.tools.CommonTools;
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

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



    @PostMapping("/addOrSave")
    public Result addOrSave( String itemCode, String prcsInstance) {
        //传入   物料编码、流程实例
        BigDecimal itemNum = prcsService.getItemNum(itemCode);
        if(itemNum.compareTo(new BigDecimal(0)) == 0){
            //直接存数据库 记日志 返回
            prcsMapper.insert(new CodeAndPrcsIns(null,itemCode,prcsInstance));

            logger.info("工单在产数为零，存数据库");
            return ResultUtil.success("工单在产数为零，存数据库");
        }else {
            //工单在产数不为零
            //查询表单详情，
            try {
                prcsService.creatNewInterface(prcsInstance);
            } catch (Exception e) {
                logger.error(e.getMessage());
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


}
