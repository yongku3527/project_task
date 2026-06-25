package com.quanhai.dingdingdemo.bom.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.quanhai.dingdingdemo.bom.service.BomMesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * BOM相关MES接口Service实现类
 */
@Slf4j
@Service
public class BomMesServiceImpl implements BomMesService {

    @Value("${mes.api.url:http://192.168.100.19:8715}")
    private String mesApiUrl;

    @Override
    public Map<String, Object> getItemInfo(String itemCode) {
        Map<String, Object> result = new HashMap<>();
        if (itemCode == null || itemCode.trim().isEmpty()) {
            return result;
        }

        try {
            String url = mesApiUrl + "/dingdingReport/getSingleItemStock";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("itemCode", itemCode.trim().toUpperCase());

            String response = HttpUtil.post(url, paramMap);
            JSONObject jsonResponse = JSONUtil.parseObj(response);

            if (jsonResponse == null) {
                log.warn("MES接口返回空数据，物料编码：{}", itemCode);
                return result;
            }

            Boolean success = jsonResponse.getBool("success");
            if (success == null || !success) {
                String message = jsonResponse.getStr("message");
                log.warn("MES接口调用失败，物料编码：{}，原因：{}", itemCode, message);
                return result;
            }

            JSONObject mesResult = jsonResponse.getJSONObject("result");
            if (mesResult == null) {
                return result;
            }

            result.put("itemCode", mesResult.getStr("itemCode"));
            result.put("itemName", mesResult.getStr("itemName"));
            result.put("itemSpec", mesResult.getStr("itemSpec"));
            return result;

        } catch (Exception e) {
            log.error("调用MES接口失败，物料编码：{}，错误：{}", itemCode, e.getMessage(), e);
            return result;
        }
    }
}
