package com.quanhai.dingdingdemo;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponseBody;
import com.aliyun.dingtalkproject_1_0.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.quanhai.dingdingdemo.client.MyDingClient;

import com.quanhai.dingdingdemo.config.DingAppConfig;

import com.quanhai.dingdingdemo.controller.TaskController;
import com.quanhai.dingdingdemo.controller.mes.MesController;
import com.quanhai.dingdingdemo.file.controller.FileInfoController;
import com.quanhai.dingdingdemo.file.dto.DocProdDrawingDTO;
import com.quanhai.dingdingdemo.file.model.DocPartDrawing;
import com.quanhai.dingdingdemo.file.model.FileInfo;
import com.quanhai.dingdingdemo.file.model.Specification;
import com.quanhai.dingdingdemo.file.model.docProdDrawing;
import com.quanhai.dingdingdemo.file.service.*;
import com.quanhai.dingdingdemo.mapper.MeetingMapper;
import com.quanhai.dingdingdemo.model.Project;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultEnum;
import com.quanhai.dingdingdemo.model.TaskVo;
import com.quanhai.dingdingdemo.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest(classes = DingdingDemoApplication.class)
class DingdingDemoApplicationTests {
    @Autowired
    private DingAppConfig dingAppConfig;

    @Autowired
    private MyDingClient myDingClient;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MeetingMapper meetingMapper;
    @Test
    public void testMapper() {
        Map<String, Long> result = new HashMap<>();

        // 每次 SCAN 1000 条，可按需调整
        ScanOptions options = ScanOptions.scanOptions().count(1000).build();

        try (Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(
                (RedisConnection conn) -> conn.scan(options))) {

            while (cursor.hasNext()) {
                byte[] keyBytes = cursor.next();
                String key = new String(keyBytes);

                Long ttl = redisTemplate.getExpire(key); // 单位秒
                long ttl2 = ttl / 3600;
                result.put(key, ttl2);
            }
        }
        System.out.println(result);
    }





    String getAccessToken() throws Exception {

        if (redisTemplate.hasKey("accessToken")) {
            Long Exp = redisTemplate.getExpire("accessToken", TimeUnit.SECONDS);
            if (Exp <= 60 * 20) {
                return getToken();
            }
            return (String) redisTemplate.opsForValue().get("accessToken");
        } else {
            return getToken();
        }

    }


    void setProjectsToRedis() throws Exception {
        com.aliyun.dingtalkproject_1_0.Client projectClient = myDingClient.getProjectClient();
        QueryProjectHeaders queryProjectHeaders = new QueryProjectHeaders();

        queryProjectHeaders.xAcsDingtalkAccessToken = getAccessToken();
        QueryProjectRequest queryProjectRequest = new QueryProjectRequest()
                .setMaxResults(dingAppConfig.getSize());

        QueryProjectResponse queryProjectResponse = projectClient.queryProjectWithOptions(dingAppConfig.getUserid(), queryProjectRequest, queryProjectHeaders, new RuntimeOptions());

        // 存储筛选后的项目
        List<QueryProjectResponseBody.QueryProjectResponseBodyResult> filteredProjects = new ArrayList<>();

        for (QueryProjectResponseBody.QueryProjectResponseBodyResult project : queryProjectResponse.getBody().getResult()) {
            // 筛选条件：未归档（isArchived=false）且可见范围为组织内（visibility=organization）
            if (!project.getIsArchived() && "organization".equals(project.getVisibility())) {
                // 符合条件的项目添加到列表
                filteredProjects.add(project);
                // 可选：打印筛选出的项目信息
                System.out.println("符合条件的项目：");
                System.out.println("项目名称：" + project.getName());
                System.out.println("---------------------");

            }
        }
        redisTemplate.opsForValue().set("projects", filteredProjects, 1, TimeUnit.DAYS);

    }


    @Test
    void getTaskVo() throws Exception {
        com.aliyun.dingtalkproject_1_0.Client projectClient = myDingClient.getProjectClient();

        List<TaskVo> taskVos = new ArrayList<>();

        //获取项目信息
        List<Project> projectList = getProjectsFromRedis();
        if (projectList != null && !projectList.isEmpty()) {
            QueryTaskOfProjectHeaders queryTaskOfProjectHeaders = new QueryTaskOfProjectHeaders();

            queryTaskOfProjectHeaders.xAcsDingtalkAccessToken = getAccessToken();
            QueryTaskOfProjectRequest queryTaskOfProjectRequest = new QueryTaskOfProjectRequest()
                    .setMaxResults(dingAppConfig.getSize());


            for (Project project : projectList) {

                //获取项目id
                String projectId = project.getProjectId();
                QueryTaskOfProjectResponse queryTaskOfProjectResponse = projectClient.queryTaskOfProjectWithOptions(dingAppConfig.getUserid(), projectId, queryTaskOfProjectRequest, queryTaskOfProjectHeaders, new RuntimeOptions());
                List<QueryTaskOfProjectResponseBody.QueryTaskOfProjectResponseBodyResult> taskList = queryTaskOfProjectResponse.getBody().getResult();
                System.out.println(project.getName() + " 项目任务数" + taskList.size());
                for (QueryTaskOfProjectResponseBody.QueryTaskOfProjectResponseBodyResult taskResp : taskList) {
                    TaskVo taskVo = new TaskVo();
                    taskVo.setExecutorId(taskResp.getExecutorId());
                    taskVo.setTaskName(taskResp.getContent());
                    taskVo.setProjectName(project.getName());
                    taskVo.setStartDate(convertUTCTime(taskResp.getStartDate()));
                    taskVo.setDueDate(convertUTCTime(taskResp.getDueDate()));
                    taskVo.setIsDone(taskResp.getIsDone());
                    // 计算剩余天数
                    long remainDays = ChronoUnit.DAYS.between(LocalDate.now(), taskVo.getDueDate());
                    taskVo.setRemainTimeDays((int) remainDays);


                    //传入执行者id获取执行者名称、部门id
                    SetUserInfo(taskResp.getExecutorId(), taskVo);
                    //根据部门id获取部门名称
                    List<String> deptNameList = getDeptInfo(taskVo.getDeptIdList());
                    taskVo.setDeptNameList(deptNameList);
                    //根据状态id获取状态名
                    String statusName = getStatusName(project.getProjectId(), taskResp.getTaskflowstatusId());
                    taskVo.setTaskStatus(statusName);


                    taskVos.add(taskVo);
                }
            }

        }
        System.out.println(taskVos);
        System.out.println("taskVos.size() = " + taskVos.size());

    }


    private List<Project> getProjectsFromRedis() throws Exception {

        List<QueryProjectResponseBody.QueryProjectResponseBodyResult> projects =
                (List<QueryProjectResponseBody.QueryProjectResponseBodyResult>) redisTemplate.opsForValue().get("projects");
        List<Project> projectList = null;
        if (projects != null) {
            projectList = new ArrayList<>();
            for (QueryProjectResponseBody.QueryProjectResponseBodyResult projectResult : projects) {
                Project project = new Project();
                // 先拷贝能自动映射的字段
                BeanUtils.copyProperties(projectResult, project);
                project.setCreated(convertUTCTime(projectResult.getCreated()));
                project.setUpdated(convertUTCTime(projectResult.getUpdated()));
                project.setStartDate(convertUTCTime(projectResult.getStartDate()));
                project.setEndDate(convertUTCTime(projectResult.getEndDate()));
                projectList.add(project);

            }
            return projectList;

        } else {
            setProjectsToRedis();
            return getProjectsFromRedis();
        }

    }


    private String getToken() throws Exception {
        Client oauthClient = myDingClient.getOauthClient();
        // 构建获取access_token的请求
        GetAccessTokenRequest request = new GetAccessTokenRequest()
                .setAppKey(dingAppConfig.getAppkey())
                .setAppSecret(dingAppConfig.getAppsecret());

        // 发送请求获取access_token
        GetAccessTokenResponseBody responseBody = oauthClient.getAccessToken(request).getBody();
        String accessToken = responseBody.getAccessToken();
        redisTemplate.opsForValue().set("accessToken", accessToken, responseBody.getExpireIn(), TimeUnit.SECONDS);
        System.out.println("accessToken: " + accessToken);
        return accessToken;
    }

    // UTC时间转LocalDate
    private LocalDate convertUTCTime(String utcTimeStr) throws ParseException {
        if (utcTimeStr == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date utcTime = sdf.parse(utcTimeStr);
        return utcTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    //获取用户名和部门id列表
    void SetUserInfo(String executorId, TaskVo taskVo) throws Exception {
        // 1. 配置接口参数
        String accessToken = getAccessToken(); // 替换为实际获取的access_token
        String userName = null;
        List<Integer> deptIdList = new ArrayList<>();
        if (redisTemplate.hasKey("executorId_" + executorId) && redisTemplate.hasKey("deptIdList_" + executorId)) {
            userName = (String) redisTemplate.opsForValue().get("executorId_" + executorId);
            deptIdList = (List<Integer>) redisTemplate.opsForValue().get("deptIdList_" + executorId);
        } else {
            // 2. 构建请求URL（包含Query参数access_token）
            String url = "https://oapi.dingtalk.com/topapi/v2/user/get" +
                    "?access_token=" + accessToken;
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
                    System.out.println("接口调用失败: " + jsonObj.getStr("errmsg"));
                    return;
                }

                // 获取用户信息对象
                JSONObject userInfo = jsonObj.getJSONObject("result");
                if (userInfo == null) {
                    System.out.println("用户信息为空");
                    return;
                }

                // 提取用户名称
                userName = userInfo.getStr("name", "未知");

                // 提取部门ID列表并转换为List<String>

                JSONArray deptArray = userInfo.getJSONArray("dept_id_list");
                if (deptArray != null) {
                    for (Object obj : deptArray) {
                        if (obj != null) {
                            deptIdList.add((Integer) obj);
                        }
                    }
                }
                redisTemplate.opsForValue().set("executorId_" + executorId,userName, 1, TimeUnit.DAYS);
                redisTemplate.opsForValue().set("deptIdList_" + executorId,deptIdList, 1, TimeUnit.DAYS);
            } catch (Exception e) {
                System.out.println("解析出错: " + e.getMessage());
            }
        }

        // 输出结果
        System.out.println("用户名称: " + userName);
        System.out.println("所属部门ID列表: " + deptIdList);

        taskVo.setExecutorName(userName);
        taskVo.setDeptIdList(deptIdList);

    }


    //获取部门名称列表
    List<String> getDeptInfo(List<Integer> deptIdList) throws Exception {
        List<String> names = new ArrayList<>();
            String accessToken = getAccessToken();
            for (Integer deptId : deptIdList) {
                String deptName;
                if (redisTemplate.hasKey("deptName_" + deptId) ) {
                    deptName = (String) redisTemplate.opsForValue().get("deptName_" + deptId);
                    names.add(deptName);
                }else {
                    try {
                        String url = "https://oapi.dingtalk.com/topapi/v2/department/get" +
                                "?access_token=" + accessToken;

                        JSONObject body = new JSONObject();
                        body.put("dept_id", deptId);

                        String response = HttpUtil.post(url, body.toString());
                        deptName = parseDeptName(response);
                        redisTemplate.opsForValue().set("deptName_" + deptId, deptName, 1, TimeUnit.DAYS);

                        names.add(deptName);

                    } catch (Exception e) {
                        names.add("查询失败");
                        System.err.printf("部门ID=%d查询失败: %s%n", deptId, e.getMessage());
                    }
                }

            }
        return names;
    }

    private static String parseDeptName(String jsonStr) {
        try {
            JSONObject json = JSONUtil.parseObj(jsonStr);
            if (json.getInt("errcode") != 0) {
                return "错误:" + json.getStr("errmsg");
            }
            return json.getJSONObject("result").getStr("name", "未知部门");
        } catch (Exception e) {
            return "解析异常";
        }
    }

    String getStatusName(String projectId, String taskflowStatusId) throws Exception {
        String name = "";
        if (redisTemplate.hasKey("taskStatusName_" + taskflowStatusId)) {
            name = (String) redisTemplate.opsForValue().get("taskStatusName_" + taskflowStatusId);
        }else {
            com.aliyun.dingtalkproject_1_0.Client client = myDingClient.getProjectClient();
            SearchTaskflowStatusHeaders searchTaskflowStatusHeaders = new SearchTaskflowStatusHeaders();
            searchTaskflowStatusHeaders.xAcsDingtalkAccessToken = getAccessToken();
            SearchTaskflowStatusRequest searchTaskflowStatusRequest = new SearchTaskflowStatusRequest()
                    .setTfsIds(taskflowStatusId);
            SearchTaskflowStatusResponse searchTaskflowStatusResponse = null;
            try {
                searchTaskflowStatusResponse = client.searchTaskflowStatusWithOptions(dingAppConfig.getUserid(), projectId, searchTaskflowStatusRequest, searchTaskflowStatusHeaders, new RuntimeOptions());

            } catch (TeaException err) {
                if (!Common.empty(err.code) && !Common.empty(err.message)) {
                    // err 中含有 code 和 message 属性，可帮助开发定位问题
                    System.out.println("获取状态名失败, code:"+err.code+" message:"+err.message);

                }
            } catch (Exception _err) {
                TeaException err = new TeaException(_err.getMessage(), _err);
                if (!Common.empty(err.code) && !Common.empty(err.message)) {
                    // err 中含有 code 和 message 属性，可帮助开发定位问题
                    System.out.println("获取状态名失败, code:"+err.code+" message:"+err.message);
                }

            }
            if (searchTaskflowStatusResponse == null) {
                return "查询失败";
            }
            SearchTaskflowStatusResponseBody.SearchTaskflowStatusResponseBodyResult searchTaskflowStatusResponseBodyResult =
                    searchTaskflowStatusResponse.getBody().getResult().get(0);

            name = searchTaskflowStatusResponseBodyResult.getName();
            redisTemplate.opsForValue().set("taskStatusName_" + taskflowStatusId, name, 1, TimeUnit.DAYS);

        }

        return name;
    }


    @Autowired
    private MesController mesController;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private DocProdDrawingService docProdDrawingService;
    @Autowired
    private ItemTypeService itemTypeService;

    @Test
    void batchInsertProdDrawing() throws IOException {

        //获取图纸类型
        HashMap<String, String> itemTypeMap = new HashMap<>();
        itemTypeMap.put("CPX", "线束");
        itemTypeMap.put("DAQ", "电器");
        itemTypeMap.put("DMT", "多媒体");
        itemTypeMap.put("QTA", "其他");
        itemTypeMap.put("SFJ", "收放机");
        itemTypeMap.put("SXT", "摄像头");
        itemTypeMap.put("TAX", "天线");
        itemTypeMap.put("YIB", "仪表");
        itemTypeMap.put("YSQ", "扬声器");
        itemTypeMap.put("ZSJ", "注塑成品");
        itemTypeMap.put("LED", "倒车雷达");

        ArrayList<String> failList = new ArrayList<>();

        List<String> fileList = Files.list(Path.of("C:\\Users\\Administrator\\Desktop\\新建文件夹 (2)"))                 // 当前一级目录
                .filter(Files::isRegularFile) // 只保留普通文件
                .filter(p -> p.toString()
                        .toLowerCase()
                        .endsWith(".pdf"))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());

        for (String s : fileList) {
            //去掉文件后缀
            String prodCode = s.substring(0, s.indexOf("."));

            Result<Map<String, Object>> itemInfo = mesController.getItemInfo(prodCode);
            //接口返回失败, 记录失败列表
            if (itemInfo.getCode() != ResultEnum.SUCCESS.code){
                failList.add(prodCode);
                continue;
            }
            Map<String, Object> itemInfoData = itemInfo.getData();

            //创建pdf文件信息，拼接，得到文件ID
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(s);
            fileInfo.setOriginalName(s);
            fileInfo.setFileSuffix(".pdf");
            fileInfo.setFileSize(165600L);
            fileInfo.setFileUrl("/minio/buckets/pdf-files/files/" + s);
            fileInfo.setCreateTime(LocalDateTime.now());
            fileInfo.setStatus(1);
            fileInfoService.saveOrUpdate(fileInfo);
            //创建dwg文件
            FileInfo dwgFileInfo = new FileInfo();
            dwgFileInfo.setFileName(prodCode+".dwg");
            dwgFileInfo.setOriginalName(prodCode+".dwg");
            dwgFileInfo.setFileSuffix(".dwg");
            dwgFileInfo.setFileSize(165600L);
            dwgFileInfo.setFileUrl("/minio/buckets/dwg-files/files/" + prodCode+".dwg");
            dwgFileInfo.setCreateTime(LocalDateTime.now());
            dwgFileInfo.setStatus(1);
            fileInfoService.saveOrUpdate(dwgFileInfo);


            Long pdfFileId = fileInfo.getId();
            Long dwgFileId = dwgFileInfo.getId();

            //获取图纸类型
            //获取编号的前三个字符
            String itemType = itemTypeMap.get(prodCode.substring(0,3));
            if (itemType == null){
                failList.add(prodCode);
                continue;
            }

            //根据物料信息创建成品信息
            docProdDrawing docProdDrawing = new docProdDrawing();
            docProdDrawing.setPid(prodCode);
            docProdDrawing.setDrawingType(itemType);
            docProdDrawing.setItemName((String) itemInfoData.get("itemName"));
            docProdDrawing.setModel((String) itemInfoData.get("itemSpec"));
            docProdDrawing.setPdfFileId(pdfFileId);
            docProdDrawing.setPdfFileUrl(fileInfo.getFileUrl());
            docProdDrawing.setPdfFileName(fileInfo.getOriginalName());
            docProdDrawing.setDwgFileId(dwgFileId);
            docProdDrawing.setDwgFileUrl(dwgFileInfo.getFileUrl());
            docProdDrawing.setDwgFileName(dwgFileInfo.getOriginalName());
            docProdDrawing.setStatus(1);
            docProdDrawing.setCreateTime(LocalDateTime.now());
            docProdDrawing.setUpdateTime(LocalDateTime.now());
            docProdDrawingService.saveOrUpdate(docProdDrawing);


            System.out.println(itemInfoData);
        }

        //输出失败列表
        System.out.println(failList);

    }


    @Autowired
    private DocPartDrawingService docPartDrawingService;

    @Test
    void batchInsertPartDrawing() throws IOException {


        // 2. 数字编码映射
         Map<String, String> itemTypeMap   = new HashMap<>();

        // 数字编码
        itemTypeMap.put("002001", "辅料类");
        itemTypeMap.put("003005", "注塑半成品");
        itemTypeMap.put("003006", "喷涂半成品");
        itemTypeMap.put("003009", "电器类半成品");
        itemTypeMap.put("003008", "壳体加工半成品");
        itemTypeMap.put("036011", "机尾线类");
        itemTypeMap.put("036012", "机尾线类");
        itemTypeMap.put("036013", "机尾线类");
        itemTypeMap.put("036021", "机尾线类");
        itemTypeMap.put("037011", "机尾线类");
        itemTypeMap.put("001015", "机芯传感器类");
        itemTypeMap.put("001017", "塑胶类");
        itemTypeMap.put("001018", "五金螺丝类");
        itemTypeMap.put("001019", "橡胶类");
        itemTypeMap.put("001023", "包材类");
        itemTypeMap.put("001029", "喇叭类");


        ArrayList<String> failList = new ArrayList<>();

        List<String> fileList = Files.list(Path.of("C:\\Users\\Administrator\\Desktop\\新建文件夹 (2)"))                 // 当前一级目录
                .filter(Files::isRegularFile) // 只保留普通文件
                .filter(p -> p.toString()
                        .toLowerCase()
                        .endsWith(".pdf"))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());

        for (String s : fileList) {
            //去掉文件后缀
            String prodCode = s.substring(0, s.indexOf("."));

            Result<Map<String, Object>> itemInfo = mesController.getItemInfo(prodCode);
            //接口返回失败, 记录失败列表
            if (itemInfo.getCode() != ResultEnum.SUCCESS.code){
                failList.add(prodCode);
                continue;
            }
            Map<String, Object> itemInfoData = itemInfo.getData();

            //创建pdf文件信息，拼接，得到文件ID
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(s);
            fileInfo.setOriginalName(s);
            fileInfo.setFileSuffix(".pdf");
            fileInfo.setFileSize(165600L);
            fileInfo.setFileUrl("/minio/buckets/part-pdf/files/" + s);
            fileInfo.setCreateTime(LocalDateTime.now());
            fileInfo.setStatus(1);
            fileInfoService.saveOrUpdate(fileInfo);
            //创建dwg文件
            FileInfo dwgFileInfo = new FileInfo();
            dwgFileInfo.setFileName(prodCode+".dwg");
            dwgFileInfo.setOriginalName(prodCode+".dwg");
            dwgFileInfo.setFileSuffix(".dwg");
            dwgFileInfo.setFileSize(165600L);
            dwgFileInfo.setFileUrl("/minio/buckets/part-dwg/files/" + prodCode+".dwg");
            dwgFileInfo.setCreateTime(LocalDateTime.now());
            dwgFileInfo.setStatus(1);
            fileInfoService.saveOrUpdate(dwgFileInfo);

            Long pdfFileId = fileInfo.getId();
            Long dwgFileId = dwgFileInfo.getId();

            //获取图纸类型
            //获取编号的前三个字符
            String itemType = itemTypeMap.get(prodCode.substring(0,6));
            if (itemType == null){
                failList.add(prodCode);
                continue;
            }

            //根据物料信息创建零件信息
            DocPartDrawing docPartDrawing = new DocPartDrawing();
            docPartDrawing.setPartId(prodCode);
            docPartDrawing.setDrawingType(itemType);
            docPartDrawing.setItemName((String) itemInfoData.get("itemName"));
            docPartDrawing.setModel((String) itemInfoData.get("itemSpec"));
            docPartDrawing.setPdfFileId(pdfFileId);
            docPartDrawing.setPdfFileUrl(fileInfo.getFileUrl());
            docPartDrawing.setPdfFileName(fileInfo.getOriginalName());
            docPartDrawing.setDwgFileId(dwgFileId);
            docPartDrawing.setDwgFileUrl(dwgFileInfo.getFileUrl());
            docPartDrawing.setDwgFileName(dwgFileInfo.getOriginalName());
            docPartDrawing.setStatus(1);
            docPartDrawing.setCreateTime(LocalDateTime.now());
            docPartDrawing.setUpdateTime(LocalDateTime.now());
            docPartDrawingService.saveOrUpdate(docPartDrawing);



            System.out.println(itemInfoData);
        }
        //输出失败列表
        System.out.println(failList);

    }


@Autowired
    private SpecificationService specificationService;


    @Test
    void batchInsertSpecificationDoc() throws IOException {


        // 2. 数字编码映射
        Map<String, String> itemTypeMap   = new HashMap<>();

        // 数字编码

        itemTypeMap.put("001001", "IC类");
        itemTypeMap.put("001002", "二极管类");
        itemTypeMap.put("001003", "三极管类");
        itemTypeMap.put("001004", "电阻类");
        itemTypeMap.put("001005", "电容类");
        itemTypeMap.put("001006", "电感类");
        itemTypeMap.put("001007", "电位器类");
        itemTypeMap.put("001008", "晶振类");
        itemTypeMap.put("001009", "线路板类");
        itemTypeMap.put("001010", "开关类");
        itemTypeMap.put("001011", "插座类");
        itemTypeMap.put("001012", "显示类");
        itemTypeMap.put("001013", "灯类");
        itemTypeMap.put("001014", "保险丝类");
        itemTypeMap.put("001015", "机芯传感器类");
        itemTypeMap.put("001016", "天线类");
        itemTypeMap.put("001018", "五金螺丝类");
        itemTypeMap.put("001019", "橡胶类");
        itemTypeMap.put("001022", "线材类");
        itemTypeMap.put("001029", "喇叭类");
        itemTypeMap.put("002001", "辅料类");



        ArrayList<String> failList = new ArrayList<>();

        List<String> fileList = Files.list(Path.of("C:\\Users\\Administrator\\Desktop\\规格书\\已完成"))                 // 当前一级目录
                .filter(Files::isRegularFile) // 只保留普通文件
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());

        for (String s : fileList) {
            String substring = s.substring(s.indexOf('.'));
            String itemCode = s.split("-")[0];

            Result<Map<String, Object>> itemInfo = mesController.getItemInfo(itemCode);
            //接口返回失败, 记录失败列表
            if (itemInfo.getCode() != ResultEnum.SUCCESS.code){
                failList.add(s);
                continue;
            }
            Map<String, Object> itemInfoData = itemInfo.getData();

            //创建pdf文件信息，拼接，得到文件ID
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(s);
            fileInfo.setOriginalName(s);
            fileInfo.setFileSuffix(substring);
            fileInfo.setFileSize(365600L);
            fileInfo.setFileUrl("/minio/buckets/specification/files/" + s);
            fileInfo.setCreateTime(LocalDateTime.now());
            fileInfo.setStatus(1);
            fileInfoService.saveOrUpdate(fileInfo);

            //获取图纸类型
            //获取编号的前6个字符
            String itemType = itemTypeMap.get(itemCode.substring(0,6));
            if (itemType == null){
                failList.add(s);
                continue;
            }

            Specification specification = new Specification();

            specification.setMaterialId(itemCode);
            specification.setDrawingType(itemType);
            specification.setItemName((String) itemInfoData.get("itemName"));
            specification.setModel((String) itemInfoData.get("itemSpec"));
            specification.setFileId(fileInfo.getId());

            specification.setStatus(1);
            specification.setCreateTime(LocalDateTime.now());
            specification.setUpdateTime(LocalDateTime.now());
            specificationService.saveOrUpdate(specification);


            System.out.println(itemInfoData);
        }
        //输出失败列表
        System.out.println(failList);

    }






















    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskController taskController;
    @Test
    void test22() throws Exception {
        com.aliyun.dingtalkproject_1_0.Client client = myDingClient.getProjectClient();

        com.aliyun.dingtalkproject_1_0.models.GetTaskByIdsHeaders getTaskByIdsHeaders = new com.aliyun.dingtalkproject_1_0.models.GetTaskByIdsHeaders();
        getTaskByIdsHeaders.xAcsDingtalkAccessToken = "d7d897ba6d0f3593b0191499572e8346";
        List<TaskVo> taskVos = (List<TaskVo>) redisTemplate.opsForValue().get("taskVos");
        List<String> taskIdList = new ArrayList<>();
        List<String> taskIdStrList = new ArrayList<>();
        for (TaskVo taskVo : taskVos) {
            taskIdList.add(taskVo.getTaskId());
            if (taskIdList.size() == 81){
                String joinStr = String.join(",", taskIdList);
                taskIdStrList.add(joinStr);
                taskIdList.clear();
            }
        }
        if (!taskIdList.isEmpty()) {
            taskIdStrList.add( String.join(",", taskIdList));
        }


        for (String joinTaskIds : taskIdStrList) {

            com.aliyun.dingtalkproject_1_0.models.GetTaskByIdsRequest getTaskByIdsRequest = new com.aliyun.dingtalkproject_1_0.models.GetTaskByIdsRequest()
                    .setTaskId(joinTaskIds);

                try {
                GetTaskByIdsResponse taskByIdsResponse = client.getTaskByIdsWithOptions("011965232633828543", getTaskByIdsRequest, getTaskByIdsHeaders, new RuntimeOptions());
                List<GetTaskByIdsResponseBody.GetTaskByIdsResponseBodyResult> taskByIdsResponseData = taskByIdsResponse.getBody().getResult();
                for (GetTaskByIdsResponseBody.GetTaskByIdsResponseBodyResult taskInfo : taskByIdsResponseData) {
                    redisTemplate.opsForValue().set("taskInfo_" + taskInfo.getTaskId(), taskInfo,1, TimeUnit.DAYS);
                }
            } catch (TeaException err) {
                System.out.println("err = " + err.getMessage());

            } catch (Exception _err) {
                System.out.println("_err = " + _err.getMessage());

            }

        }



    }




    /**
     * 获取指定时间范围内每个人每天的任务详情
     * @param taskVos 任务列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 包含每个人每天任务统计和详情的Map
     */
    private Map<String, Map<LocalDate, List<TaskVo>>> getDailyTasksByExecutor(
            List<TaskVo> taskVos, LocalDate startDate, LocalDate endDate) {
        
        if (taskVos == null || taskVos.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 创建日期范围列表
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        
        // 按执行者ID分组任务
        Map<String, List<TaskVo>> tasksByExecutor = taskVos.stream()
            .filter(task -> task.getExecutorId() != null && !task.getExecutorId().isEmpty())
            .collect(Collectors.groupingBy(TaskVo::getExecutorId));
        
        // 统计每个人每天的具体任务
        Map<String, Map<LocalDate, List<TaskVo>>> dailyTasksDetail = new HashMap<>();
        
        for (Map.Entry<String, List<TaskVo>> entry : tasksByExecutor.entrySet()) {
            String executorId = entry.getKey();
            List<TaskVo> executorTasks = entry.getValue();
            
            // 初始化该执行者每天的任务列表
            Map<LocalDate, List<TaskVo>> dailyTasks = new HashMap<>();
            for (LocalDate date : dateRange) {
                dailyTasks.put(date, new ArrayList<>());
            }

            // 计算每个任务在日期范围内的天数，并记录具体任务
            for (TaskVo task : executorTasks) {
                LocalDate taskStartDate = task.getStartDate() != null ? task.getStartDate() : startDate;
                LocalDate taskDueDate = task.getDueDate() != null ? task.getDueDate() : endDate;
                
                // 确保日期在范围内
                if (taskStartDate.isAfter(endDate) || taskDueDate.isBefore(startDate)) {
                    continue;
                }
                
                // 限制在日期范围内
                LocalDate actualStart = taskStartDate.isBefore(startDate) ? startDate : taskStartDate;
                LocalDate actualEnd = taskDueDate.isAfter(endDate) ? endDate : taskDueDate;
                
                // 为每个日期记录具体任务
                LocalDate current = actualStart;
                while (!current.isAfter(actualEnd)) {
                    dailyTasks.get(current).add(task);
                    current = current.plusDays(1);
                }
            }
            
            dailyTasksDetail.put(executorId, dailyTasks);
        }
        
        return dailyTasksDetail;
    }

    @Test
    public void gantt() {
        List<TaskVo> taskVos = (List<TaskVo>) redisTemplate.opsForValue().get("taskVos");
        
        if (taskVos == null || taskVos.isEmpty()) {
            System.out.println("未找到任务数据");
            return;
        }
        
        // 获取从今天到下个月的日期范围
        LocalDate today = LocalDate.now();

        LocalDate nextMonth = today.plusMonths(1);
        
        System.out.println("=== 获取每个人从今天到下个月每天的任务详情 ===");
        System.out.println("日期范围: " + today + " 到 " + nextMonth);
        
        // 使用封装的方法获取任务详情
        Map<String, Map<LocalDate, List<TaskVo>>> dailyTasksDetail = 
            getDailyTasksByExecutor(taskVos, today, nextMonth);
        
        // 统计并打印结果
        Map<LocalDate, Long> totalDailyTasks = new HashMap<>();
        
        for (Map.Entry<String, Map<LocalDate, List<TaskVo>>> entry : dailyTasksDetail.entrySet()) {
            String executorId = entry.getKey();
            Map<LocalDate, List<TaskVo>> dailyTasks = entry.getValue();
            
            System.out.println("\n执行者ID: " + executorId);
            
            for (Map.Entry<LocalDate, List<TaskVo>> dailyEntry : dailyTasks.entrySet()) {
                LocalDate date = dailyEntry.getKey();
                List<TaskVo> tasks = dailyEntry.getValue();
                long count = tasks.size();
                
                if (count > 0) {
                    System.out.println("  " + date + ": " + count + " 个任务");
                    
                    // 打印每个任务的详细信息
                    for (TaskVo task : tasks) {
                        System.out.println("    - 任务ID: " + task.getTaskId() +
                                         ", 标题: " + task.getTaskName() +
                                         ", 开始: " + task.getStartDate() + 
                                         ", 截止: " + task.getDueDate());
                    }
                }else {
                    System.out.println("  " + date + ": " + 0 + " 个任务");
                }
                
                // 统计总任务数
                totalDailyTasks.put(date, totalDailyTasks.getOrDefault(date, 0L) + count);
            }
        }
        
        // 统计总览
        System.out.println("\n=== 统计总览 ===");
        System.out.println("总执行者数量: " + dailyTasksDetail.size());
        System.out.println("总任务数量: " + taskVos.size());
        
        // 找出任务最繁忙的日期
        Optional<Map.Entry<LocalDate, Long>> busiestDay = totalDailyTasks.entrySet().stream()
            .max(Map.Entry.comparingByValue());
        
        busiestDay.ifPresent(entry -> 
            System.out.println("最繁忙的一天: " + entry.getKey() + " (" + entry.getValue() + " 个任务)")
        );
    }




}







