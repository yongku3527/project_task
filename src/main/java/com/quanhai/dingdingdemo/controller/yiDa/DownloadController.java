package com.quanhai.dingdingdemo.controller.yiDa;

import cn.hutool.json.JSONUtil;
import com.quanhai.dingdingdemo.config.MailConfig;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.excption.MyExcption;
import com.quanhai.dingdingdemo.model.yiDa.SendMailDto;
import com.quanhai.dingdingdemo.tools.CommonTools;
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/sendMail")
public class DownloadController {

    @Autowired
    private CommonTools commonTools;
    @Autowired
    private MailConfig mailConfig;

    private Logger logger = LoggerFactory.getLogger(DownloadController.class);

//    @PostMapping("/v1/{FileName}/{pcbName}/{gangWangName}/{gangWangCode}")
    public Result sendMailV1(@RequestBody String FileUrl,
                             @PathVariable String FileName,
                             @PathVariable String pcbName,
                             @PathVariable String gangWangName,
                             @PathVariable String gangWangCode) throws EmailException {
//        logger.info();
        String fileUrl = (String) JSONUtil.parseObj(FileUrl).get("FileUrl");


        String msg = "尊敬的供应商朋友：\n" +
                "以下为"+pcbName+"的钢网文件，钢网名称为："+gangWangName+"，钢网编号为："+gangWangCode+"\n" +
                "该邮箱不回复任何邮件，如有疑问请联系我司工程师处理，感谢支持。";

        try {
            commonTools.send(fileUrl,FileName,"钢网邮件",mailConfig.getTo(),msg);

        } catch (Exception e) {

            logger.error("钢网邮件发送失败："+e.getMessage());
            commonTools.send("钢网邮件发送失败",mailConfig.getBadTo(),msg);

        }

        return ResultUtil.success("ok");
    }


//    @PostMapping("/v1/{FileName}/{pcbName}/{gangWangName}/")
    public Result sendMailGWCodeIsNull(@RequestBody String FileUrl,@PathVariable String FileName,@PathVariable String pcbName,@PathVariable String gangWangName) throws EmailException {
        return sendMailV1(FileUrl,FileName,pcbName,gangWangName,"null");
    }




    @PostMapping("/v2")
    public Result sendMailV2(@RequestBody String FileUrl, String FileName, String pcbName, String gangWangName, String gangWangCode, String originatorUser) throws EmailException {
        logger.info("FileUrl = " + FileUrl +
                ", FileName = " + FileName +
                ", pcbName = " + pcbName +
                ", gangWangName = " + gangWangName +
                ", gangWangCode = " + gangWangCode+
                ", originatorUser = " + originatorUser);

        String userId = originatorUser.replace("\"", "").replace("[", "").replace("]", "");
        String userName = commonTools.getUserName(userId);


        String fileUrl = (String) JSONUtil.parseObj(FileUrl).get("FileUrl");

        pcbName = pcbName != null ? pcbName.replace("\n", "、") : "";
        gangWangName = gangWangName != null ? gangWangName.replace("\n", "、") : "";
        gangWangCode = gangWangCode != null ? gangWangCode.replace("\n", "、") : "";



        String msg = "尊敬的供应商朋友：\n" +
                "附件为 "+pcbName+" 的钢网文件，钢网名称为："+gangWangName+"，钢网编号为："+gangWangCode+"\n" +
                "责任工程师："+userName+"\n"+
                "该邮箱不回复任何邮件，如有疑问请联系我司工程师处理，感谢支持。";


        if ("未知".equals(userName)) {
            msg = "尊敬的供应商朋友：\n" +
                    "附件为 "+pcbName+" 的钢网文件，钢网名称为："+gangWangName+"，钢网编号为："+gangWangCode+"\n" +
                    "该邮箱不回复任何邮件，如有疑问请联系我司工程师处理，感谢支持。";
        }

        try {
            commonTools.send(fileUrl,FileName,"钢网邮件",mailConfig.getTo(),msg);

        } catch (Exception e) {

            logger.error("钢网邮件发送失败："+e.getMessage()+e.getMessage());
            commonTools.send("钢网邮件发送失败",mailConfig.getBadTo(),"报错信息: "+e.getMessage()+"\n"+"内容:\n"+msg);
        }

        return ResultUtil.defineSuccess(200,"ok");
    }




    @PostMapping("/v3")
    public Result sendMailV3(@RequestBody String FileUrl,String FileName, String pcbName,String gangWangName, String gangWangCode) throws EmailException {
        logger.info("FileUrl = " + FileUrl +
                ", FileName = " + FileName +
                ", pcbName = " + pcbName +
                ", gangWangName = " + gangWangName +
                ", gangWangCode = " + gangWangCode);


        String fileUrl = (String) JSONUtil.parseObj(FileUrl).get("FileUrl");

        pcbName = pcbName != null ? pcbName.replace("\n", "、") : "";
        gangWangName = gangWangName != null ? gangWangName.replace("\n", "、") : "";
        gangWangCode = gangWangCode != null ? gangWangCode.replace("\n", "、") : "";



        String msg = "尊敬的供应商朋友：\n" +
                "附件为 "+pcbName+" 的钢网文件，钢网名称为："+gangWangName+"，钢网编号为："+gangWangCode+"\n" +
                "该邮箱不回复任何邮件，如有疑问请联系我司工程师处理，感谢支持。";

        try {
            commonTools.send(fileUrl,FileName,"钢网邮件",mailConfig.getTo(),msg);

        } catch (Exception e) {

            logger.error("钢网邮件发送失败："+e.getMessage());
            commonTools.send("钢网邮件发送失败",mailConfig.getBadTo(),msg);
        }

        return ResultUtil.defineSuccess(200,"ok");
    }






}
