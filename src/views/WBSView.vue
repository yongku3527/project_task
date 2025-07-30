<template>
  <div class="wbs-container">


    <div class="wbs-content">
      <el-card class="wbs-tree-card">
        <template #header>
          <div class="card-header">
            <span>任务分解结构</span>
            <div class="header-actions">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索任务..."
                style="width: 200px; margin-right: 10px"
                clearable
                @clear="handleSearch"
                @input="handleSearch"
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </div>
        </template>

        <div class="mindmap-container" ref="mindmapRef">
          <div class="mindmap-grid" v-if="wbsData.length > 0">
            <div class="project-tasks-grid">
              <div 
                v-for="project in wbsData" 
                :key="project.id"
                class="project-task-columns"
              >
                <div class="project-header" @click="selectNode(project)">
                  <div class="node-content">
                    <div class="node-title">{{ project.name }}</div>
                    <div class="node-info">
                      <el-tag :type="getStatusType(project.status)" size="small">
                        {{ getStatusText(project.status) }}
                      </el-tag>
                    </div>
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
                        <div class="node-title">{{ taskGroup.name }}</div>
                        <div class="node-info">
                          <el-tag :type="getStatusType(taskGroup.status)" size="small">
                            {{ getStatusText(taskGroup.status) }}
                          </el-tag>
                        </div>
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
                          <div class="task-info">
                            <el-tag :type="getStatusType(task.status)" size="small">
                              {{ getStatusText(task.status) }}
                            </el-tag>
                            <span class="task-assignee">{{ task.assignee || '未分配' }}</span>
                          </div>
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

      <el-card class="wbs-detail-card" v-if="selectedTask">
        <template #header>
          <div class="card-header">
            <span>任务详情</span>
            <el-button link @click="selectedTask = null">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </template>
        
        <div class="task-detail">
          <h3>{{ selectedTask.name }}</h3>
          <el-descriptions :column="2" border>
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
            <el-descriptions-item label="描述" :span="2">
              {{ selectedTask.description || '暂无描述' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { Back, Search, Close } from '@element-plus/icons-vue';

const baseUrl = 'http://192.168.90.64:8083';

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
const searchKeyword = ref('');
const selectedTask = ref<Task | null>(null);

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
    '待开始': 'task-pending',
    '进行中': 'task-progress',
    '已完成': 'task-completed',
    '已暂停': 'task-paused',
    '已取消': 'task-cancelled'
  };
  return classMap[status] || 'task-pending';
};

const selectNode = (data: Task) => {
  selectedTask.value = data;
};

const handleSearch = () => {
  // 思维导图搜索功能将在后续实现
  // 目前保持简单过滤
  if (!searchKeyword.value) {
    return;
  }
  
  const keyword = searchKeyword.value.toLowerCase();
  // 这里可以实现高亮匹配节点等功能
  console.log('搜索关键词:', keyword);
};

const refreshData = () => {
  fetchWBSData();
};

onMounted(() => {
  fetchWBSData();
});
</script>

<style scoped>
.wbs-container {
  height: auto;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}





.wbs-content {
  flex: 1;
  display: flex;
  gap: 20px;
  padding: 20px;
  overflow: hidden;
}

.wbs-tree-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.wbs-detail-card {
  width: 400px;
  max-height: 600px;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
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
  padding: 40px;
}

.mindmap-grid {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: max-content;
  min-height: max-content;
  padding: 20px;
}

.project-tasks-grid {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.project-task-columns {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 40px;
}

.project-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 30px;
  border-radius: 15px;
  font-size: 20px;
  font-weight: bold;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
  cursor: pointer;
  transition: transform 0.3s ease;
  margin-bottom: 30px;
  text-align: center;
  min-width: 220px;
}

.project-header:hover {
  transform: scale(1.05);
}

.task-groups-grid {
  display: flex;
  flex-direction: row;
  gap: 40px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.task-group-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 220px;
  max-width: 280px;
}

.task-group-header {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  color: #333;
  padding: 15px 25px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 3px 10px rgba(0,0,0,0.15);
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  text-align: center;
  min-width: 200px;
}

.task-group-header:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.25);
}

.tasks-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.task-card {
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 200px;
  max-width: 250px;
  text-align: center;
  border: 1px solid transparent;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.task-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.task-title {
  font-weight: 600;
  line-height: 1.3;
  margin-bottom: 4px;
}

.task-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.task-assignee {
  font-size: 12px;
  opacity: 0.8;
}

/* 任务状态颜色 */
.task-pending {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1565c0;
  border-color: #90caf9;
}

.task-progress {
  background: linear-gradient(135deg, #fff3e0 0%, #ffcc02 100%);
  color: #e65100;
  border-color: #ffb300;
}

.task-completed {
  background: linear-gradient(135deg, #e8f5e8 0%, #a5d6a7 100%);
  color: #2e7d32;
  border-color: #66bb6a;
}

.task-paused {
  background: linear-gradient(135deg, #ffebee 0%, #ef9a9a 100%);
  color: #c62828;
  border-color: #e57373;
}

.task-cancelled {
  background: linear-gradient(135deg, #fafafa 0%, #e0e0e0 100%);
  color: #424242;
  border-color: #bdbdbd;
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