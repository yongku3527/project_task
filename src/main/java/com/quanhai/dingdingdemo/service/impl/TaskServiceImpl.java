package com.quanhai.dingdingdemo.service.impl;

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

import com.quanhai.dingdingdemo.model.Project;
import com.quanhai.dingdingdemo.model.ProjectTimeInfo;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.StatusInfo;
import com.quanhai.dingdingdemo.model.TaskVo;
import com.quanhai.dingdingdemo.model.excption.MyExcption;
import com.quanhai.dingdingdemo.service.TaskService;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private DingAppConfig dingAppConfig;

    @Autowired
    private MyDingClient myDingClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public void setTaskVoToRedis() throws Exception {
        com.aliyun.dingtalkproject_1_0.Client projectClient = myDingClient.getProjectClient();

        List<TaskVo> taskVos = new ArrayList<>();

        //获取项目信息
        List<Project> projectList = getProjectsFromRedis();
        if (!projectList.isEmpty()) {
            QueryTaskOfProjectHeaders queryTaskOfProjectHeaders = new QueryTaskOfProjectHeaders();

            queryTaskOfProjectHeaders.xAcsDingtalkAccessToken = getAccessToken();
            QueryTaskOfProjectRequest queryTaskOfProjectRequest = new QueryTaskOfProjectRequest()
                    .setMaxResults(dingAppConfig.getSize());


            for (Project project : projectList) {

                //获取项目id
                String projectId = project.getProjectId();

                QueryTaskOfProjectResponse queryTaskOfProjectResponse = null;
                try {
                    queryTaskOfProjectResponse = projectClient.queryTaskOfProjectWithOptions(dingAppConfig.getUserid(), projectId, queryTaskOfProjectRequest, queryTaskOfProjectHeaders, new RuntimeOptions());
                } catch (Exception e) {
                    System.out.println("!!!===>获取项目中任务信息报错，报错项目： "+project.getName());
                    continue;
                }

                List<QueryTaskOfProjectResponseBody.QueryTaskOfProjectResponseBodyResult> taskList = queryTaskOfProjectResponse.getBody().getResult();
                System.out.println(project.getName() + " 项目任务数" + taskList.size());
                for (QueryTaskOfProjectResponseBody.QueryTaskOfProjectResponseBodyResult taskResp : taskList) {
                    TaskVo taskVo = new TaskVo();
                    taskVo.setTaskId(taskResp.getTaskId());
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
                    setUserInfo(taskResp.getExecutorId(), taskVo);
                    //根据部门id获取部门名称
                    List<String> deptNameList = getDeptInfo(taskVo.getDeptIdList());
                    taskVo.setDeptNameList(deptNameList);
                    //根据状态id获取状态名
                    String statusName = getStatusName(project.getProjectId(), taskResp.getTaskflowstatusId());
                    taskVo.setTaskStatus(statusName);

                    taskVos.add(taskVo);
                }

//                 查询key：ProTaskListId_{项目id}是否存在，如果不存在就去调接口存入redis并设置一天的过期时间
//                缓存任务分组详细信息，使用List<SearchTaskListResponseBody.SearchTaskListResponseBodyResult>接收
                if (Boolean.FALSE.equals(redisTemplate.hasKey("ProTaskListId_" + projectId))) {
//                  调接口
                    setTaskGroupList(projectId);
                }

            }

        }

//         创建方法，传入taskVos，方法中拼接taskid，请求查询项目详情接口，解析taskListId，把taskListId填入taskVos，
//         再把redis中的ProTaskListId_*全部获取到内存（需要的话创建个实体类），再次遍历taskVos，把任务分组名称填入taskVos

        //接口查询任务详细信息
        setTaskInfo(taskVos);

        //查询所有任务详情，注入任务分组id
        injectionGropId(taskVos);

        //为taskVos添加任务分组名称
        injectionGropTitle(taskVos);

        redisTemplate.opsForValue().set("taskVos", taskVos);
        System.out.println("taskVos.size() = " + taskVos.size());
    }

    private void setTaskInfo(List<TaskVo> taskVos) throws Exception {
        if (taskVos.size()>redisTemplate.keys("taskInfo_*").size()) {
            System.out.println("====taskVos数量"+taskVos.size()+"大于redis中任务详情的数量"+redisTemplate.keys("taskInfo_*").size()+"，调用查询任务详情接口====");

            com.aliyun.dingtalkproject_1_0.Client client = myDingClient.getProjectClient();
            com.aliyun.dingtalkproject_1_0.models.GetTaskByIdsHeaders getTaskByIdsHeaders = new com.aliyun.dingtalkproject_1_0.models.GetTaskByIdsHeaders();
            getTaskByIdsHeaders.xAcsDingtalkAccessToken = getAccessToken();
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
                    GetTaskByIdsResponse taskByIdsResponse = client.getTaskByIdsWithOptions(dingAppConfig.getUserid(), getTaskByIdsRequest, getTaskByIdsHeaders, new RuntimeOptions());
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

    }

    private void injectionGropId(List<TaskVo> taskVos) {
        System.out.println("====为taskVos注入TaskListId====");

        for (TaskVo taskVo : taskVos) {
            GetTaskByIdsResponseBody.GetTaskByIdsResponseBodyResult taskInfo = (GetTaskByIdsResponseBody.GetTaskByIdsResponseBodyResult) redisTemplate.opsForValue().get("taskInfo_" + taskVo.getTaskId());

            taskVo.setTaskListId(taskInfo.getTaskListId());
            taskVo.setParentTaskId(taskInfo.getTaskId());
        }
    }

    private void injectionGropTitle(List<TaskVo> taskVos) {
        System.out.println("====为taskVos注入任务分组名称====");
        // 获取所有以ProTaskListId_开头的key
        Set<String> keys = redisTemplate.keys("ProTaskListId_*");
        if (keys == null || keys.isEmpty()) {

            return;
        }

        // 收集所有任务分组信息
        List<SearchTaskListResponseBody.SearchTaskListResponseBodyResult> allTaskGroups = new ArrayList<>();
        for (String key : keys) {
            List<SearchTaskListResponseBody.SearchTaskListResponseBodyResult> taskGroupList = 
                (List<SearchTaskListResponseBody.SearchTaskListResponseBodyResult>) redisTemplate.opsForValue().get(key);
            
            if (taskGroupList != null) {
                allTaskGroups.addAll(taskGroupList);
            }
        }

        // 构建taskListId到分组名称的映射
        Map<String, String> taskGroupMap = new HashMap<>();
        for (SearchTaskListResponseBody.SearchTaskListResponseBodyResult taskGroup : allTaskGroups) {
            taskGroupMap.put(taskGroup.getTaskListId(), taskGroup.getTitle());
        }

        // 为taskVos填充任务分组信息
        for (TaskVo taskVo : taskVos) {
            String taskListId = taskVo.getTaskListId();
            if (taskListId != null && taskGroupMap.containsKey(taskListId)) {
                taskVo.setTaskListName(taskGroupMap.get(taskListId));
            }else {
                taskVo.setTaskListName("未分组");
            }
        }
    }

    private void setTaskGroupList(String projectId) throws Exception {
        System.out.println("====缓存任务分组名称====");

        com.aliyun.dingtalkproject_1_0.Client client = null;
        try {
            client = myDingClient.getProjectClient();
        } catch (Exception e) {
            throw new MyExcption("获取ProjectClient失败");
        }

        com.aliyun.dingtalkproject_1_0.models.SearchTaskListHeaders searchTaskListHeaders = new com.aliyun.dingtalkproject_1_0.models.SearchTaskListHeaders();
        searchTaskListHeaders.xAcsDingtalkAccessToken = getAccessToken();
        com.aliyun.dingtalkproject_1_0.models.SearchTaskListRequest searchTaskListRequest = new com.aliyun.dingtalkproject_1_0.models.SearchTaskListRequest()
                .setMaxResults(dingAppConfig.getSize());
        try {
            SearchTaskListResponse searchTaskListResponse = client.searchTaskListWithOptions(dingAppConfig.getUserid(), projectId, searchTaskListRequest, searchTaskListHeaders, new RuntimeOptions());
            List<SearchTaskListResponseBody.SearchTaskListResponseBodyResult> taskGroupList = searchTaskListResponse.getBody().getResult();
            redisTemplate.opsForValue().set("ProTaskListId_"+projectId,taskGroupList);

        } catch (TeaException err) {
            System.out.println("setTaskGroupList TeaException = " + err);

        } catch (Exception _err) {
            System.out.println("setTaskGroupList Exception = " + _err);
        }
    }

    @Override
    public Result getTaskInfo() {
        try {
            List<TaskVo> taskVos = (List<TaskVo>) redisTemplate.opsForValue().get("taskVos");

            if (taskVos == null || taskVos.isEmpty()) {
                setTaskVoToRedis();
            }
            taskVos = (List<TaskVo>) redisTemplate.opsForValue().get("taskVos");

            return ResultUtil.success(taskVos);
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @Override
    public Result getProjectTime() {
        List<TaskVo> tasks;
        try {

            tasks = (List<TaskVo>) redisTemplate.opsForValue().get("taskVos");

        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }

        // 按项目分组并计算最早/最晚截止时间
        Map<String, ProjectTimeInfo> projectTimeMap = tasks.stream()
                .filter(task -> task.getDueDate() != null) // 过滤掉无截止时间的任务
                .collect(Collectors.groupingBy(
                        TaskVo::getProjectName,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                projectTasks -> {
                                    LocalDate earliest = projectTasks.stream()
                                            .map(TaskVo::getDueDate)
                                            .min(LocalDate::compareTo)
                                            .orElse(null);

                                    LocalDate latest = projectTasks.stream()
                                            .map(TaskVo::getDueDate)
                                            .max(LocalDate::compareTo)
                                            .orElse(null);

                                    return new ProjectTimeInfo(earliest, latest);
                                }
                        )
                ));

        return ResultUtil.success(projectTimeMap);
    }

    @Override
    public Result getProjectInfo() throws Exception {
        List<Project> projectsFromRedis = getProjectsFromRedis();
        return ResultUtil.success(projectsFromRedis);
    }

    @Override
    public Result getStatusInfo() {

        List<StatusInfo> statusInfos = new ArrayList<>();

        Set<String> keys = redisTemplate.keys("taskStatusName_*");
        for (String key : keys) {
            StatusInfo statusInfo = new StatusInfo();
            String statusName = (String) redisTemplate.opsForValue().get(key);
            String[] keyArray = key.split("_");

            statusInfo.setStatusName(statusName);
            statusInfo.setProjectId(keyArray[1]);
            statusInfo.setStatusId(keyArray[2]);
            statusInfos.add(statusInfo);

        }
        return ResultUtil.success(statusInfos);
    }


    String getAccessToken() throws Exception {

        if (Boolean.TRUE.equals(redisTemplate.hasKey("accessToken"))) {
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
        redisTemplate.opsForValue().set("projects", filteredProjects, 2, TimeUnit.HOURS);

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
    void setUserInfo(String executorId, TaskVo taskVo) throws Exception {
        // 1. 配置接口参数
        String accessToken = getAccessToken(); // 替换为实际获取的access_token
        String userName = null;
        List<Integer> deptIdList = new ArrayList<>();
        if (Boolean.TRUE.equals(redisTemplate.hasKey("executorId_" + executorId)) && Boolean.TRUE.equals(redisTemplate.hasKey("deptIdList_" + executorId))) {
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
                redisTemplate.opsForValue().set("executorId_" + executorId, userName, 10, TimeUnit.DAYS);
                redisTemplate.opsForValue().set("deptIdList_" + executorId, deptIdList, 10, TimeUnit.DAYS);
            } catch (Exception e) {
                throw new MyExcption("获取用户信息报错：" + e.getMessage());
            }
        }

        // 输出结果

        taskVo.setExecutorName(userName);
        taskVo.setDeptIdList(deptIdList);

    }


    //获取部门名称列表
    List<String> getDeptInfo(List<Integer> deptIdList) throws Exception {
        List<String> names = new ArrayList<>();
        String accessToken = getAccessToken();
        for (Integer deptId : deptIdList) {
            String deptName;
            if (Boolean.TRUE.equals(redisTemplate.hasKey("deptName_" + deptId))) {
                deptName = (String) redisTemplate.opsForValue().get("deptName_" + deptId);
                names.add(deptName);
            } else {
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

    String getStatusNameOld(String projectId, String taskflowStatusId) throws Exception {
        String name = "";
        if (Boolean.TRUE.equals(redisTemplate.hasKey("taskStatusName_" + taskflowStatusId))) {
            name = (String) redisTemplate.opsForValue().get("taskStatusName_" + taskflowStatusId);
        } else {
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
                    throw new MyExcption("获取状态名报错：" + err.message);

                }
            } catch (Exception _err) {
                TeaException err = new TeaException(_err.getMessage(), _err);
                if (!Common.empty(err.code) && !Common.empty(err.message)) {
                    // err 中含有 code 和 message 属性，可帮助开发定位问题
                    throw new MyExcption("获取状态名报错：" + err.message);
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


    String getStatusName(String projectId, String taskflowStatusId) throws Exception {
        String name = "";
        if (Boolean.TRUE.equals(redisTemplate.hasKey("taskStatusName_" + projectId + "_" + taskflowStatusId))) {
            name = (String) redisTemplate.opsForValue().get("taskStatusName_" + projectId + "_" + taskflowStatusId);
        } else {
            com.aliyun.dingtalkproject_1_0.Client client = myDingClient.getProjectClient();
            SearchTaskflowStatusHeaders searchTaskflowStatusHeaders = new SearchTaskflowStatusHeaders();
            searchTaskflowStatusHeaders.xAcsDingtalkAccessToken = getAccessToken();
            SearchTaskflowStatusRequest searchTaskflowStatusRequest = new SearchTaskflowStatusRequest();

            SearchTaskflowStatusResponse searchTaskflowStatusResponse = null;
            try {
                searchTaskflowStatusResponse = client.searchTaskflowStatusWithOptions(dingAppConfig.getUserid(), projectId, searchTaskflowStatusRequest, searchTaskflowStatusHeaders, new RuntimeOptions());

            } catch (TeaException err) {
                if (!Common.empty(err.code) && !Common.empty(err.message)) {
                    // err 中含有 code 和 message 属性，可帮助开发定位问题
                    throw new MyExcption("获取状态名报错：" + err.message);

                }
            } catch (Exception _err) {
                TeaException err = new TeaException(_err.getMessage(), _err);
                if (!Common.empty(err.code) && !Common.empty(err.message)) {
                    // err 中含有 code 和 message 属性，可帮助开发定位问题
                    throw new MyExcption("获取状态名报错：" + err.message);
                }

            }
            if (searchTaskflowStatusResponse == null) {
                return "查询失败";
            }

            List<SearchTaskflowStatusResponseBody.SearchTaskflowStatusResponseBodyResult> result = searchTaskflowStatusResponse.getBody().getResult();

            for (SearchTaskflowStatusResponseBody.SearchTaskflowStatusResponseBodyResult searchTaskflowStatusResponseBodyResult : result) {
                String thisStatuName = searchTaskflowStatusResponseBodyResult.getName();
                redisTemplate.opsForValue().set("taskStatusName_" + projectId + "_" + searchTaskflowStatusResponseBodyResult.getTaskflowStatusId(), thisStatuName, 1, TimeUnit.DAYS);
            }
            name = (String) redisTemplate.opsForValue().get("taskStatusName_" + projectId + "_" + taskflowStatusId);

        }
        return name;
    }


}
