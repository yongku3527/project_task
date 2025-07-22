<template>
  <div class="new-feature-container">
    <!-- 悬浮切换按钮 -->
    <button @click="toggleTaskPanel" class="toggle-btn">{{ showTaskPanel ? '隐藏任务面板' : '显示任务面板' }}</button>
    <!-- 任务管理面板 -->
    <div class="task-panel" v-if="showTaskPanel">
      <h3>任务管理</h3>
      <div class="task-form">
        <el-select v-model="selectedTaskId" class="task-select" :disabled="!chartTasks.length" placeholder="选择任务"
          style="width: 100%">
          <el-option v-for="task in chartTasks" :key="task.taskId" :value="task.taskId">
            <template #default>
              <div class="task-option">
                <span class="project-name">({{ task.projectName }})</span>
                <span class="task-name">{{ task.taskName }}</span>
                <!-- {{ console.log(task) }} -->
                <span class="task-name">{{ task.taskStatus }}</span>
                <span class="task-date">{{ task.dueDate }}</span>
                <el-tag v-if="isTaskInProjectGroups(task.taskId)" size="small" type="success">已添加</el-tag>
              </div>
            </template>
          </el-option>
        </el-select>
        <el-button @click="addTask" class="add-btn" :disabled="!selectedTaskId">添加任务</el-button>
      </div>
      <div v-if="panelLoading" class="panel-loading">加载中...</div>
      <div v-else-if="panelError" class="panel-error">{{ panelError }}</div>
      <div v-else-if="panelTasks.length === 0" class="no-tasks">暂无任务</div>
      <div class="task-list">
        <!-- 项目分组标题 -->
        <div v-for="(tasks, projectName) in taskProjectGroups" :key="projectName" class="project-group">
          <h3 class="project-title">{{ projectName }}</h3>
          <div v-for="task in tasks" :key="task.taskId" class="task-item">

            <span>{{ task.taskName }}</span>
            <button @click="deleteTask(task.taskId)" class="delete-btn">删除</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="chartLoading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">错误: {{ error }}</div>
    <div class="content">
      <div v-for="projectName in projectNames" :key="projectName" class="chart-container">
        <h2>{{ projectName }}</h2>
        <div :id="'taskTimeline-' + projectName" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs';
import { ref, reactive, onMounted, nextTick, watch, set } from 'vue';
import { ElSelect, ElOption, ElTag } from 'element-plus';
const showTaskPanel = ref(false);
const isTaskInProjectGroups = (taskId) => {
  return Object.values(taskProjectGroups).some(projectTasks =>
    projectTasks.some(t => t.taskId === taskId)
  );
};
const toggleTaskPanel = () => showTaskPanel.value = !showTaskPanel.value;
import axios from 'axios';
import * as echarts from 'echarts';
import { tr } from 'element-plus/es/locales.mjs';

const chartTasks = ref<any[]>([]);
const panelTasks = ref<any[]>([]);
const projectNames = ref<string[]>([]);
const projectGroups = reactive<Record<string, any[]>>({});
const taskProjectGroups = reactive<Record<string, any[]>>({});
const chartLoading = ref(true);
const error = ref('');
// 面板相关变量
const selectedTaskId = ref('');
const panelLoading = ref(false);
const panelError = ref('');
const baseUrl = 'http://192.168.70.56:8083'
// 获取面板任务数据 - 仅使用指定接口
const fetchPanelTasks = async () => {
  panelLoading.value = true;
  panelError.value = ''; // 重置错误状态

  try {
    // 严格使用指定接口获取任务数据
    const response = await axios.get(`${baseUrl}/TimeLine/getTimeLineTask`);

    if (response.data.code === 200) {
      panelTasks.value = response.data.data || [];
      console.log('任务数据已从指定接口加载:', panelTasks.value);
    } else {
      panelError.value = `获取任务失败: ${response.data.msg || '未知错误'}`;
      console.error('API返回错误:', response.data);
    }
  } catch (err) {
    panelError.value = '网络错误: 无法连接到任务接口';
    console.error('请求失败详情:', err);
  } finally {
    panelLoading.value = false;
  }
};

// 添加任务
const addTask = async () => {
  if (!selectedTaskId.value) return;
  try {
    panelLoading.value = true;
    console.log("!!!!!")
    console.log(selectedTaskId.value);
    // 调试任务匹配逻辑
    console.log('Selected Task ID:', selectedTaskId.value);
    console.log('Chart Tasks:', chartTasks.value);
    // 转换为相同类型进行比较
    const matchedTask = chartTasks.value.find(t => String(t.taskId) === String(selectedTaskId.value));
    console.log('Matched Task:', matchedTask);
    console.log('projectName:', matchedTask?.projectName);
    const response = await axios.post(baseUrl + '/TimeLine/addTimeLineTask', {

      taskId: selectedTaskId.value,
      taskName: matchedTask?.taskName || '',
      projectName: matchedTask?.projectName || ''

    });


    if (response.data.code === 200) {
      selectedTaskId.value = '';
      await fetchPanelTasks(); // 重新获取任务列表
        nextTick(() => initChart()); // 刷新时间轴
    } else {
      panelError.value = '添加失败: ' + response.data.msg;
    }
  } catch (err) {
    panelError.value = '网络错误: 无法添加任务';
    console.error('添加任务失败:', err);
  } finally {
    panelLoading.value = false;
  }
};

// 删除任务
const deleteTask = async (taskId: string) => {
  console.log("!!!!" + taskId);

  try {
    panelLoading.value = true;
    const response = await axios.delete(baseUrl + '/TimeLine/removeTimeLineTask/' + taskId);

    if (response.data.code === 200) {
      await fetchPanelTasks(); // 重新获取任务列表
      nextTick(() => initChart()); // 刷新时间轴
    } else {
      panelError.value = '删除失败: ' + response.data.msg;
    }
  } catch (err) {
    panelError.value = '网络错误: 无法删除任务';
    console.error('删除任务失败:', err);
  } finally {
    panelLoading.value = false;
  }
};

// 任务面板项目分组逻辑
const updateTaskProjectGroups = () => {
  // 清空现有分组
  Object.keys(taskProjectGroups).forEach(key => delete taskProjectGroups[key]);

  // 按项目名称分组任务
  panelTasks.value.forEach(task => {
    const projectName = task.projectName;
    if (!taskProjectGroups[projectName]) {
      taskProjectGroups[projectName] = [];
    }
    taskProjectGroups[projectName].push(task);
  });

  console.log('任务面板项目分组已更新:', taskProjectGroups);
};

// 监听面板任务变化以更新分组
watch(panelTasks, updateTaskProjectGroups, { immediate: true });

// 获取图表任务数据
const fetchChartTasks = async () => {
  try {
    const response = await axios.get(baseUrl + '/dingTask/getTaskInfo');
    if (response.data.code === 200) {
      chartTasks.value = Array.isArray(response.data.data) ? response.data.data.sort((a, b) => {
        // 先按项目名升序排序
        const projectCompare = a.projectName.localeCompare(b.projectName);
        if (projectCompare !== 0) return projectCompare;
        // 再按截止日期升序排序
        return new Date(a.dueDate) - new Date(b.dueDate);
      }) : [];

    } else {
      error.value = '获取数据失败: ' + response.data.msg;
    }
  } catch (err) {
    error.value = '网络错误: 无法连接到服务器';
    console.error('API请求错误:', err);
  } finally {
    chartLoading.value = false;
  }
};

// 初始化图表
const initChart = () => {
  if (chartTasks.value.length === 0) return;

  // 按项目分组
  // 清空现有项目分组
  Object.keys(projectGroups).forEach(key => delete projectGroups[key]);
  chartTasks.value.forEach(task => {
    const projectName = task.projectName;
    if (!projectGroups[projectName]) {
      projectGroups[projectName] = [];
    }
    projectGroups[projectName].push(task);
  });

  projectNames.value = Object.keys(projectGroups);

  // 为每个项目创建一个nextTick，确保DOM渲染完成
  projectNames.value.forEach(async (projectName) => {
    await nextTick(); // 等待当前项目的DOM渲染完成

    // 收集taskProjectGroups中的所有任务ID
    const taskIds = new Set();
    Object.values(taskProjectGroups).forEach(tasks => {
      tasks.forEach(task => taskIds.add(task.taskId));
    });

    // 准备当前项目的图表数据 - 仅包含taskProjectGroups中的任务
    // 过滤无效日期、排序并匹配任务ID
    const projectTasks = projectGroups[projectName]
      .filter(task => !isNaN(new Date(task.dueDate).getTime()) && taskIds.has(task.taskId))
      .sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime());
    console.log(`项目${projectName}任务数据:`, projectTasks);
    if (projectTasks.length === 0) {
      console.warn(`项目${projectName}没有任务数据`);
      return;
    }

    const currentDate = new Date().toISOString().split('T')[0];
    const seriesData = [{
      name: projectName,
      type: 'line',
      data: projectTasks.map((task) => ([
        new Date(task.dueDate).getTime() || Date.now(),
        0
      ])),
      smooth: false,
      symbol: 'none',
      lineStyle: {
        color: '#8392A5',
        width: 2,
        //透明度设置为零隐藏
        opacity: 0
      },
      emphasis: {
        disabled: true
      },
      z: 1
      ,
      markLine: {
        silent: true,
        animation: true,
        data: [
          {
            xAxis: new Date(new Date(projectTasks[0].dueDate).getTime() - 1 * 24 * 60 * 60 * 1000).getTime(),
            yAxis: 0
          },
          {
            xAxis: new Date(new Date(projectTasks[projectTasks.length - 1].dueDate).getTime() + 1 * 24 * 60 * 60 * 1000).getTime(),
            yAxis: 0
          }
        ],
        smooth: false,
        symbol: 'none',
        lineStyle: {
          color: '#8392A5',
          width: 2,
          type: 'solid'
        },
        emphasis: {
          disabled: true
        },
        z: 1
      }
    },
    {
      name: projectName,
      type: 'scatter',
      symbolSize: 15,
      label: {
        show: true,
        formatter: function (params) {
          const maxLineLength = 6; // 设置每行最大长度
          const name = params.data.name;
          const dueDate = params.data.dueDate;

          let formattedName = '';
          for (let i = 0; i < name.length; i += maxLineLength) {
            formattedName += name.substring(i, i + maxLineLength) + '\n';
          }

          return `${formattedName}\n\n${dueDate}`;
        },
        distance: 23,
        position: 'bottom',
        align: 'center',
        verticalAlign: 'bottom',

        lineHeight: 17,
        textStyle: {
          fontSize: 13,
          color: '#000000'
        },
        z: 2
      },
      //当前标记
      markPoint: {
        symbol: 'pin', // 使用内置pin图标
        symbolSize: 40,
        silent: true,
        animation: true,
        data: [{
          name: '当前日期',
          xAxis: currentDate,
          yAxis: 0,
          itemStyle: {
            color: '#FF69B4'
          },
          label: {
            color: '#A5AAA3',
            fontSize: 12,
            position: 'bottom',
            offset: [0, 10]
          }
        },
        {
          name: '项目开始',
          xAxis: new Date(new Date(projectTasks[0].dueDate).getTime() - 1 * 24 * 60 * 60 * 1000).getTime(),
          yAxis: 0,
          symbol: 'circle',
          symbolSize: 20,
          itemStyle: {
            //灰色
            color: '#A5AAA3'
          },
          label: {
            formatter: function (params) { return dayjs(params.data.xAxis).format('YYYY-MM-DD'); },
            color: '#A5AAA3',
            fontSize: 12,
            position: 'left'
          }
        },
        {
          name: '项目结束',
          xAxis: new Date(new Date(projectTasks[projectTasks.length - 1].dueDate).getTime() + 1 * 24 * 60 * 60 * 1000).getTime(),
          yAxis: 0,
          symbol: 'circle',
          symbolSize: 20,
          itemStyle: {
            color: '#A5AAA3'
          },
          label: {
            formatter: function (params) { return dayjs(params.data.xAxis).format('YYYY-MM-DD'); },
            color: '#A5AAA3',
            fontSize: 12,
            position: 'right'
          }
        }]
      },

      data: projectTasks.map((task, index) => ({
        name: task.taskName,
        value: [
          // 验证日期格式
          new Date(task.dueDate).getTime() || Date.now(),
          0, // 使用任务索引作为Y轴值
          task.remainTimeDays
        ],
        itemStyle: {
          color: getStatusColor(task.taskStatus)
        },
        taskStatus: task.taskStatus,
        executorName: task.executorName,
        startDate: task.startDate,
        dueDate: task.dueDate
      }))
    }];

    // 获取当前项目的图表容器并初始化
    const chartDom = document.getElementById('taskTimeline-' + projectName);
    if (!chartDom) {
      console.warn(`未找到项目${projectName}的图表容器`);
      return;
    }

    const myChart = echarts.init(chartDom);

    // 图表配置
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          const task = params.data;
          return `
            <div style="font-weight: bold;">${task.name}</div>
            <strong>${params.seriesName}</strong><br/>
            <div>负责人: ${task.executorName}</div>
            <div>状态: <span style="color: ${getStatusColor(task.taskStatus)}">${task.taskStatus}</span></div>
            <div>开始日期: ${task.startDate}</div>
            <div>截止日期: ${task.dueDate}</div>
            <div>剩余时间: ${(() => { const due = new Date(task.dueDate); const today = new Date(); const diffTime = due.getTime() - today.getTime(); const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); return diffDays >= 0 ? diffDays + '天' : '已逾期' + Math.abs(diffDays) + '天'; })()}</div>
          `;
        }
      },
      grid: {
        left: '5%',
        right: '5%',
        bottom: '40%',
        top: '0%'
      },
      xAxis: {
        type: 'time',
        name: '截止日期',
        axisLabel: {
          formatter: '{yyyy}-{MM}-{dd}',
          // interval: 'auto',

        },
        axisTick: {
          show: true,
          alignWithLabel: true,
          length: 5
        },
        // 设置X轴范围以确保所有数据可见
        min: projectTasks.length ? Math.min(...projectTasks.map(t => new Date(t.dueDate).getTime())) - 86400000 * 1 : null,
        max: projectTasks.length ? Math.max(...projectTasks.map(t => new Date(t.dueDate).getTime())) + 86400000 * 1 : null,
        // 确保X轴标签不重叠
        interval: 'auto',

      },
      yAxis: {
        type: 'category',
        name: '任务',
        data: ['任务'],
        show: false,
        axisLabel: {
          interval: 0.5,
          rotate: 30
        }
      },
      series: seriesData
    };

    myChart.setOption(option);

    // 响应窗口大小变化
    const handleResize = () => {
      myChart.resize();
    };
    window.addEventListener('resize', handleResize);

    // 存储resize处理函数以便后续清理
    (window as any)[`resizeHandler_${projectName}`] = handleResize;
  });
};

// 根据任务状态获取颜色
const getStatusColor = (status: string): string => {
  switch (status) {
    case '已逾期':
      return '#ff4d4f';
    case '未接收':
      //黄色
      return '#FFC107';
    case '已接收':
      //黄色
      return '#FFC107';
    case '进行中':
      //蓝色
      return '#40A9FF';
    case '已完成':
      //绿色
      return '#52c41a';
      //灰色
    default:
      return '#A5AAA3';
  }
};

// 页面加载时获取数据并初始化图表
onMounted(async () => {
  await Promise.all([fetchChartTasks(), fetchPanelTasks()]);
  nextTick(() => {
    initChart();
  });
});
</script>

<style scoped>
.in-group-indicator {
  color: #4CAF50;
  margin: 0 5px;
  font-weight: bold;
}

.project-group {
  margin-bottom: 20px;
  padding: 15px;
  border-radius: 8px;
  background-color: #f5f5f5;
}

.project-title {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #42b983;
  padding-bottom: 5px;
}

.toggle-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  padding: 8px 12px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}
</style>

<style scoped>
.toggle-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  padding: 8px 12px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.task-panel {
  position: fixed;
  right: 20px;
  top: 20px;
  max-width: 300px;
  width: calc(100% - 1px);
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  overflow-y: auto;
  max-height: calc(100vh - 40px);
}

.task-form {
  display: flex;
  gap: 8px;
  margin: 16px 0;
  flex-wrap: wrap;
}

.task-select {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
  margin-bottom: 15px;
}

.task-input {
  display: none;
}

.task-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}

.project-name {
  color: #606266;
}

.task-name {
  flex: 1;
  color: #303133;
}

.task-date {
  color: #909399;
  font-size: 12px;
}

.add-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.task-list {
  list-style: none;
  padding: 0;
  margin: 16px 0;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.delete-btn {
  color: #ff4d4f;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px 8px;
}

.panel-loading,
.loading {
  color: #666;
  padding: 16px;
  text-align: center;
}

.panel-error,
.error,
.no-tasks {
  color: #ff4d4f;
  padding: 16px;
  text-align: center;
}
</style>

<style scoped>
.new-feature-container {
  padding: 0;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 20px;
  text-align: center;
}

.loading,
.error {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error {
  color: #ff4d4f;
}

.chart-container {
  width: 100%;
  height: 28vh;
  margin: 0;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 5px;
  box-sizing: border-box;
}

.chart-container h2 {
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid #40a9ff;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>