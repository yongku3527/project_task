<template>
  <div class="new-feature-container">
    <h1>项目任务时间轴</h1>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">错误: {{ error }}</div>
    <div v-else class="content">
      <div class="chart-container">
        <div id="taskTimeline" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';

const tasks = ref<any[]>([]);
const loading = ref(true);
const error = ref('');

// 获取任务数据
const fetchTasks = async () => {
  try {
    const response = await axios.get('http://192.168.100.43:8083/dingTask/getTaskInfo');
    if (response.data.code === 200) {
      tasks.value = Array.isArray(response.data.data) ? response.data.data : [];
    } else {
      error.value = '获取数据失败: ' + response.data.msg;
    }
  } catch (err) {
    error.value = '网络错误: 无法连接到服务器';
    console.error('API请求错误:', err);
  } finally {
    loading.value = false;
  }
};

// 初始化图表
const initChart = () => {
  if (tasks.value.length === 0) return;

  // 按项目分组
  const projects: Record<string, any[]> = {};
  tasks.value.forEach(task => {
    if (!projects[task.projectName]) {
      projects[task.projectName] = [];
    }
    projects[task.projectName].push(task);
  });

  // 准备图表数据
  const projectNames = Object.keys(projects);
  const seriesData = projectNames.map(projectName => {
    const projectTasks = projects[projectName].sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime());
    return {
      name: projectName,
      type: 'scatter',
      data: projectTasks.map(task => ({
        name: task.taskName,
        value: [
          new Date(task.dueDate).getTime(),
          projectNames.indexOf(projectName),
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
    };
  });

  // 获取图表容器并初始化
  const chartDom = document.getElementById('taskTimeline');
  if (!chartDom) return;

  const myChart = echarts.init(chartDom);

  // 图表配置
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: params => {
        const task = params.data;
        return `
          <div style="font-weight: bold;">${task.name}</div>
          <div>项目: ${params.seriesName}</div>
          <div>负责人: ${task.executorName}</div>
          <div>状态: <span style="color: ${getStatusColor(task.taskStatus)}">${task.taskStatus}</span></div>
          <div>开始日期: ${task.startDate}</div>
          <div>截止日期: ${task.dueDate}</div>
          <div>剩余时间: ${task.remainTimeDays >= 0 ? task.remainTimeDays + '天' : '已逾期' + Math.abs(task.remainTimeDays) + '天'}</div>
        `;
      }
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      top: '15%'
    },
    xAxis: {
      type: 'time',
      name: '截止日期',
      axisLabel: {
        formatter: '{yyyy}-{MM}-{dd}'
      }
    },
    yAxis: {
      type: 'category',
      name: '项目名称',
      data: projectNames,
      axisLabel: {
        interval: 0
      }
    },
    series: seriesData
  };

  myChart.setOption(option);

  // 响应窗口大小变化
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

// 根据任务状态获取颜色
const getStatusColor = (status: string): string => {
  switch (status) {
    case '已逾期':
      return '#ff4d4f';
    case '未接收':
      return '#faad14';
    default:
      return '#1890ff';
  }
};

// 页面加载时获取数据并初始化图表
onMounted(async () => {
  await fetchTasks();
  initChart();
});
</script>

<style scoped>
.new-feature-container {
  padding: 20px;
  max-width: 1200px;
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
  height: 600px;
  margin-top: 20px;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>