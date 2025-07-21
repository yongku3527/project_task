<template>
  <div class="new-feature-container">

    <div v-if="loading" class="loading">加载中...</div>
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
import { ref, onMounted, nextTick } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';
import { tr } from 'element-plus/es/locales.mjs';

const tasks = ref<any[]>([]);
const projectNames = ref<string[]>([]);
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

  projectNames.value = Object.keys(projects);

  // 为每个项目创建一个nextTick，确保DOM渲染完成
  projectNames.value.forEach(async (projectName) => {
    await nextTick(); // 等待当前项目的DOM渲染完成

    // 准备当前项目的图表数据
    // 过滤无效日期并排序
    const projectTasks = projects[projectName]
      .filter(task => !isNaN(new Date(task.dueDate).getTime()))
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
        opacity: 0.9
      },
      emphasis: {
        disabled: true
      },
      z: 1
    },
    {
      name: projectName,
      type: 'scatter',
      symbolSize: 25,
      label: {
        show: true,
        formatter: function (params) {
          return `${params.data.name}\n\n${params.data.dueDate}`;
        },
        distance: 33,
        position: 'bottom',
        align: 'center',
        verticalAlign: 'bottom',

        lineHeight: 30,
        textStyle: {
          fontSize: 12,
        }
      },
      //当前标记
      markPoint: {
        symbol: 'pin', // 使用内置pin图标
        symbolSize: 25,
        silent: true,
        animation: true,
        data: [{
          name: '当前日期',
          xAxis: currentDate,
          yAxis: 0,
          itemStyle: {
            color: '#ff4d4f'
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
          symbolSize: 18,
          itemStyle: {
            color: '#52c41a'
          },
          label: {
            formatter: function (params) { return dayjs(params.data.xAxis).format('YYYY-MM-DD'); },
            color: '#52c41a',
            fontSize: 12,
            position: 'left'
          }
        },
        {
          name: '项目结束',
          xAxis: new Date(new Date(projectTasks[projectTasks.length - 1].dueDate).getTime() + 1 * 24 * 60 * 60 * 1000).getTime(),
          yAxis: 0,
          symbol: 'circle',
          symbolSize: 18,
          itemStyle: {
            color: '#ff4d4f'
          },
          label: {
            formatter: function (params) { return dayjs(params.data.xAxis).format('YYYY-MM-DD'); },
            color: '#ff4d4f',
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
            <div>项目: ${params.seriesName}</div>
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
        bottom: '50%',
        top: '10%'
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
      //灰色
      return '#A5AAA3';
    case '已接收':
      //黄色
      return '#FFC107';
    case '进行中':
      //蓝色
      return '#40A9FF';
    default:
      return '#1890ff';
  }
};

// 页面加载时获取数据并初始化图表
onMounted(async () => {
  await fetchTasks();
  nextTick(() => {
    initChart();
  });
});
</script>

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
  height: 30vh;
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