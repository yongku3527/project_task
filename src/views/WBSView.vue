<template>
  <div class="wbs-container" :class="{ 'fullscreen-mode': isFullscreen }">
    <div class="control-btns">
      <div class="play-pause-btn" @click="toggleAutoSlide">
        <el-button size="small" :type="isPaused ? 'success' : 'warning'">
          <el-icon>
            <VideoPlay v-if="isPaused" />
            <VideoPause v-else />
          </el-icon>
        </el-button>
      </div>
      <div class="next-project-btn" @click="nextProject">
        <el-button size="small" type="primary">
          <el-icon>
            <Back style="transform: rotate(180deg)" />
          </el-icon>
          下一个
        </el-button>
      </div>
      <div class="fullscreen-btn" @click="toggleFullscreen">
        <el-button size="small">
          <el-icon>
            <FullScreen v-if="!isFullscreen" />
            <Close v-else />
          </el-icon>
        </el-button>
      </div>
    </div>

    <div class="legend-panel">
      <div class="legend-item">
        <div class="legend-color status-pending"></div>
        <span>待开始</span>
        &nbsp&nbsp&nbsp
        <div class="legend-color status-completed"></div>
        <span>已完成</span>
      </div>
      <div class="legend-item">
        <div class="legend-color status-in-progress"></div>
        <span>进行中</span>
         &nbsp&nbsp&nbsp
        <div class="legend-color status-delayed"></div>
        <span>已延期</span>
      </div>
      
    </div>


    <div class="wbs-content">
      <el-card class="wbs-tree-card">

        <div class="mindmap-container" ref="mindmapRef">
          <div class="mindmap-grid" v-if="wbsData.length > 0">
            <div class="project-tasks-grid">
              <div 
                v-for="(project, index) in wbsData" 
                :key="project.id"
                class="project-task-columns"
                v-show="index === currentPage"
              >
                <div class="project-header" @click="selectNode(project)">
                  <div class="node-content">
                    <div class="node-title project-title">{{ project.name }}</div>
                  </div>
                </div>
                
                <div class="task-groups-grid" v-if="project.children">
                  <div 
                    v-for="taskGroup in project.children" 
                    :key="taskGroup.id"
                    class="task-group-column"
                  >
                    <div class="task-group-header" @click="selectNode(taskGroup)">
                      <div class="node-content">
                        <div class="node-title task-group-title">{{ taskGroup.name }}</div>
                      </div>
                    </div>
                    
                    <div class="tasks-list" v-if="taskGroup.children">
                      <div 
                        v-for="task in taskGroup.children" 
                        :key="task.id"
                        class="task-card"
                        :class="getTaskStatusClass(task.status)"
                        @click="selectNode(task)"
                      >
                        <div class="task-content">
                          <div class="task-title">{{ task.name }}</div>
                          <div class="task-assignee">{{ task.assignee || '未分配' }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-mindmap">
            <el-empty description="暂无数据" />
          </div>
        </div>
      </el-card>

      <div v-if="selectedTask" class="task-detail-overlay" @click="selectedTask = null">
        <div class="task-detail-popup" @click.stop>
          <div class="popup-header">
            <h3>{{ selectedTask.name }}</h3>
            <el-button link @click="selectedTask = null" class="close-btn">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          
          <div class="task-detail-content">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(selectedTask.status)">
                  {{ getStatusText(selectedTask.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="负责人">
                {{ selectedTask.assignee || '未分配' }}
              </el-descriptions-item>
              <el-descriptions-item label="开始时间">
                {{ selectedTask.startDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="截止时间">
                {{ selectedTask.deadline || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="项目">
                {{ selectedTask.project || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="任务组">
                {{ selectedTask.taskListName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="描述">
                {{ selectedTask.description || '暂无描述' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { Back, Close, FullScreen, VideoPlay, VideoPause } from '@element-plus/icons-vue';

const baseUrl = 'http://192.168.100.125:8083';

interface Task {
  id: string;
  name: string;
  status: string;
  assignee?: string;
  deadline?: string;
  startDate?: string;
  project?: string;
  description?: string;
  children?: Task[];
}

const wbsData = ref<Task[]>([]);
const mindmapRef = ref();
const selectedTask = ref<Task | null>(null);
const isFullscreen = ref(false);
const currentPage = ref(0);
const intervalId = ref<number | null>(null);
const isPaused = ref(false);

// 获取任务数据并构建WBS结构
const fetchWBSData = async () => {
  try {
    const response = await axios.get(`${baseUrl}/dingTask/getTaskInfo`);
    let tasks = [];
    
    if (response.data.code === 200 && Array.isArray(response.data.data)) {
      tasks = response.data.data;
    } else {
      console.warn('API返回数据格式不正确，使用模拟数据');
      tasks = [];
    }
    
    // 按项目名和taskListName分组构建结构
    const projectMap = new Map();
    
    tasks.forEach((task: any) => {
      const project = task.projectName || '未分类项目';
      const taskListName = task.taskListName || '未分类任务组';
      
      if (!projectMap.has(project)) {
        projectMap.set(project, {
          id: `project-${project}`,
          name: project,
          status: '进行中',
          children: []
        });
      }
      
      const projectNode = projectMap.get(project);
      
      // 查找或创建taskListName分组
      let taskListNode = projectNode.children.find((child: any) => child.name === taskListName);
      if (!taskListNode) {
        taskListNode = {
          id: `tasklist-${project}-${taskListName}`,
          name: taskListName,
          status: '进行中',
          children: []
        };
        projectNode.children.push(taskListNode);
      }
      
      // 添加具体任务到对应的taskListName分组下
      taskListNode.children.push({
        id: task.taskId || task.id,
        name: task.taskName || task.name,
        status: task.taskStatus || task.status || '待开始',
        assignee: task.executorName || task.assignee,
        deadline: task.dueDate || task.deadline,
        startDate: task.startDate,
        project: task.projectName || task.project,
        taskListName: task.taskListName || taskListName,
        description: task.description || ''
      });
    });
    
    wbsData.value = Array.from(projectMap.values());
    // 数据加载完成后重置分页
    currentPage.value = 0;
    startAutoSlide();
  } catch (error) {
      console.error('获取WBS数据失败:', error);
      ElMessage.error('获取WBS数据失败');
    
    // 模拟数据
        wbsData.value = [
          {
            id: '1',
            name: '项目管理系统',
            status: '进行中',
            children: [
              {
                id: '1-list-1',
                name: '需求阶段',
                status: '进行中',
                children: [
                  {
                    id: '1-1',
                    name: '需求分析',
                    status: '已完成',
                    assignee: '张三',
                    deadline: '2024-01-15',
                    project: '项目管理系统',
                    taskListName: '需求阶段'
                  },
                  {
                    id: '1-2',
                    name: '系统设计',
                    status: '进行中',
                    assignee: '李四',
                    deadline: '2024-01-30',
                    project: '项目管理系统',
                    taskListName: '需求阶段'
                  }
                ]
              },
              {
                id: '1-list-2',
                name: '开发阶段',
                status: '待开始',
                children: [
                  {
                    id: '1-3',
                    name: '前端开发',
                    status: '待开始',
                    assignee: '王五',
                    deadline: '2024-02-15',
                    project: '项目管理系统',
                    taskListName: '开发阶段'
                  }
                ]
              }
            ]
          }
        ];
  }
};

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    '待开始': '待开始',
    '进行中': '进行中',
    '已完成': '已完成',
    '已暂停': '已暂停',
    '已取消': '已取消'
  };
  return statusMap[status] || status;
};

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '待开始': 'info',
    '进行中': 'warning',
    '已完成': 'success',
    '已暂停': 'danger',
    '已取消': 'info'
  };
  return typeMap[status] || 'info';
};

const getStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    '已完成': 'task-completed',
    '已取消': 'task-cancelled'
  };
  return classMap[status] || '';
};

const getTaskStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    '未接收': 'task-pending',
    '已接收': 'task-pending',
    '进行中': 'task-progress',
    '已完成': 'task-completed',
    '已逾期': 'task-overdue'
  };
  return classMap[status] || 'task-pending';
};

const selectNode = (data: Task) => {
  selectedTask.value = data;
};

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value;
  
  if (isFullscreen.value) {
    document.documentElement.requestFullscreen().catch(err => {
      console.error('全屏失败:', err);
      ElMessage.error('全屏功能不可用');
    });
  } else {
    document.exitFullscreen().catch(err => {
      console.error('退出全屏失败:', err);
    });
  }
};

const nextProject = () => {
  if (wbsData.value.length === 0) return;
  
  currentPage.value = (currentPage.value + 1) % wbsData.value.length;
  
  // 如果正在自动播放，暂停自动播放
  if (!isPaused.value) {
    stopAutoSlide();
    isPaused.value = true;
  }
};

// 监听全屏状态变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement;
};



const refreshData = () => {
  fetchWBSData();
};

const startAutoSlide = () => {
  if (intervalId.value) {
    clearInterval(intervalId.value);
  }
  
  intervalId.value = window.setInterval(() => {
    if (wbsData.value.length > 0 && !isPaused.value) {
      currentPage.value = (currentPage.value + 1) % wbsData.value.length;
    }
  }, 10000);
};

const stopAutoSlide = () => {
  if (intervalId.value) {
    clearInterval(intervalId.value);
    intervalId.value = null;
  }
};

const toggleAutoSlide = () => {
  isPaused.value = !isPaused.value;
  if (isPaused.value) {
    stopAutoSlide();
  } else {
    startAutoSlide();
  }
};

onMounted(() => {
  fetchWBSData();
  document.addEventListener('fullscreenchange', handleFullscreenChange);
  startAutoSlide();
});

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
  stopAutoSlide();
});
</script>

<style scoped>
.wbs-container {
  height: auto;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  position: relative;
}

.control-btns {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1001;
  display: flex;
  gap: 15px;
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.control-btns:hover {
  opacity: 1;
}

.play-pause-btn .el-button,
.next-project-btn .el-button,
.fullscreen-btn .el-button {
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 16px;
  min-width: 50px;
  min-height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fullscreen-mode .control-btns {
  top: 10px;
  right: 10px;
}

.legend-panel {
  position: fixed;
  top: 10px;
  right: 260px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 6px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  min-width: 65px;
  opacity: 0.8;
}

.legend-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #666;
}

.legend-item:last-child {
  margin-bottom: 0;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  margin-right: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.legend-color.status-pending {
  background-color: #ffc107;
}

.legend-color.status-in-progress {
  background-color: #007bff;
}

.legend-color.status-completed {
  background-color: #28a745;
}

.legend-color.status-delayed {
  background-color: #dc3545;
}

.fullscreen-mode {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background-color: #f5f7fa;
  overflow: auto;
}

.fullscreen-mode .wbs-content {
  padding: 10px;
  height: 100vh;
  overflow: auto;
}

.fullscreen-mode .wbs-tree-card {
  border: none;
  box-shadow: none;
  height: 100%;
  overflow: auto;
}

.fullscreen-mode .mindmap-container {
  /* height: calc(100vh - 11vh); */
  overflow: auto;
}





.wbs-content {
  flex: 1;
  padding: 8px;
  overflow: hidden;
}

.wbs-tree-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.task-detail-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.task-detail-popup {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  animation: fadeIn 0.3s ease;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.popup-header h3 {
  margin: 0;
  color: #303133;
}

.close-btn {
  font-size: 20px;
  color: #909399;
}

.task-detail-content {
  padding: 20px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}



.wbs-tree-container {
  flex: 1;
  overflow-y: auto;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px 0;
}

.node-content {
  flex: 1;
}

.node-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.project-title {
  font-size: 25px;
  font-weight: bold;
}

.task-group-title {
  font-size: 14px;
  font-weight: bold;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #606266;
}

.node-assignee {
  color: #409eff;
}

.node-date {
  color: #909399;
}

.task-completed {
  text-decoration: line-through;
  color: #67c23a;
}

.task-cancelled {
  text-decoration: line-through;
  color: #909399;
}

.task-detail h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.mindmap-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: auto;
  padding: 5px;
}

.fullscreen-mode .project-task-columns {
  margin-bottom: 20px;
}

.fullscreen-mode .task-groups-grid {
  overflow-x: auto;
  padding-bottom: 10px;
}

.mindmap-grid {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  min-height: max-content;

  box-sizing: border-box;
}

.project-tasks-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100%;
}

.project-task-columns {
  transition: opacity 0.5s ease-in-out;
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.project-task-columns {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 20px;
}

.project-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: rgb(0, 0, 0);
  padding: 12px 40px;
  border-radius: 10px;
  font-size: 20px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  cursor: pointer;
  transition: transform 0.2s ease;
  margin-bottom: 15px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 180px;
  max-width: 90%;
  margin-left: auto;
  margin-right: auto;
}

.project-header:hover {
  transform: scale(1.02);
}

.task-groups-grid {
  display: flex;
  flex-direction: row;
  gap: 20px;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: center;
  width: 100%;
  box-sizing: border-box;
}

.task-group-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 180px;
  max-width: 220px;
}

.task-group-header {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  color: #333;
  padding: 10px 15px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 12px;
  text-align: center;
  min-width: 160px;
}

.task-group-header:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0,0,0,0.15);
}

.tasks-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;
  width: 100%;
}

.task-card {
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 160px;
  max-width: 160px;
  text-align: center;
  border: 1px solid transparent;
}

.task-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.task-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
}

.task-title {
  font-weight: 600;
  line-height: 1.3;
  margin-bottom: 4px;
  font-size: 15px;
}

.task-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.task-assignee {
  font-size: 12px;
  opacity: 0.9;
}

/* 任务状态颜色 */
.task-pending {
  background: #ffc107;
  color: white;
  border-color: #e0a800;
}

.task-progress {
  background: #007bff;
  color: white;
  border-color: #0056b3;
}

.task-completed {
  background: #28a745;
  color: white;
  border-color: #1e7e34;
  text-decoration: none;
}

.task-overdue {
  background: #dc3545;
  color: white;
  border-color: #bd2130;
}

.node-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.node-title {
  font-weight: 600;
  line-height: 1.3;
}

.node-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.node-assignee {
  font-size: 11px;
  color: #666;
}

.empty-mindmap {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

@media (max-width: 768px) {
  .mindmap-level-1 {
    flex-direction: column;
    gap: 40px;
  }
  
  .mindmap-level-2 {
    margin-top: 20px;
  }
  
  .mindmap-level-3 {
    flex-direction: column;
    gap: 10px;
  }
  
  .wbs-content {
    flex-direction: column;
  }
  
  .wbs-detail-card {
    width: 100%;
    max-height: 300px;
  }
}
</style>