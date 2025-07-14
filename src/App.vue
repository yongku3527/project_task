<script setup>
import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue';
// import { ElMessage, ElSpin } from 'element-plus';
import { FullScreen, Filter } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';



// 获取全局属性
const internalInstance = getCurrentInstance();
const axios = internalInstance?.appContext.config.globalProperties.$axios;

// 状态管理
const showFilters = ref(false);
const tasks = ref([]);
const departments = ref(['所有部门']);
const projects = ref([]);
const projectsData = ref([]); // 存储原始项目数据
const selectedDept = ref('所有部门');
const selectedProject = ref('所有项目');
const activeTab = ref('filters');
const activeFilters = ref(['basic']);
const selectedStatus = ref([]);

const lastRefreshTime = ref('');
const isLoading = ref(false);

// 筛选面板控制
const toggleFilters = () => {
  showFilters.value = !showFilters.value;
};

// 全屏控制
const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(err => {
      ElMessage.error(`全屏请求失败: ${err.message}`);
    });
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    }
  }
};
let refreshInterval = null;
const API_URL = '/dingTask/getTaskInfo';

// 获取项目数据
const fetchProjects = async () => {
  if (!axios) {
    ElMessage.error('Axios未正确初始化');
    return;
  }

  try {
    const response = await axios.get('http://192.168.100.43:5173/dingTask/getProjectInfo');
    if (response.data.code === 200) {
      projectsData.value = response.data.data;
      
      // 按创建时间排序，最新的项目排在前面
      projectsData.value.sort((a, b) => new Date(b.created) - new Date(a.created));
      
      // 更新项目筛选选项
      projects.value = ['所有项目', ...projectsData.value.map(p => p.name)];
      
      // 默认选中最新创建的项目
      if (projectsData.value.length > 0) {
        selectedProject.value = projectsData.value[0].name;
      }
    } else {
      ElMessage.warning(`获取项目数据失败: ${response.data.msg || '未知错误'}`);
    }
  } catch (error) {
    console.error('获取项目数据失败:', error);
    ElMessage.error('网络错误，无法获取项目信息');
  }
};

// 获取任务数据
const fetchTasks = async () => {
  if (!axios) {
    ElMessage.error('Axios未正确初始化');
    return;
  }

  isLoading.value = true;
  try {
    const response = await axios.get(API_URL);
    if (response.data.code === 200) {
      tasks.value = response.data.data;
      updateFilters();
      updateLastRefreshTime();

      console.info('数据刷新成功');
    } else {
      ElMessage.warning(`获取数据失败: ${response.data.msg || '未知错误'}`);
    }
  } catch (error) {
    console.error('获取任务数据失败:', error);
    ElMessage.error('网络错误，无法连接到服务器');
  } finally {
    isLoading.value = false;
  }
};

// 更新筛选器选项
const updateFilters = () => {
  // 提取所有部门
  const deptSet = new Set();
  tasks.value.forEach(task => {
    task.deptNameList.forEach(dept => deptSet.add(dept));
  });
  departments.value = ['所有部门', ...Array.from(deptSet)];

  // 项目筛选选项已通过fetchProjects更新
};

// 更新最后刷新时间
const updateLastRefreshTime = () => {
  const now = new Date();
  lastRefreshTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 筛选任务
const filteredTasks = () => {
  return tasks.value.filter(task => {
    const deptMatch = selectedDept.value === '所有部门' || task.deptNameList.includes(selectedDept.value);
    const projectMatch = selectedProject.value === '所有项目' || task.projectName === selectedProject.value;
    const statusMap = { unreceived: '未接收', inProgress: '进行中', completed: '已完成', delayed: '已逾期' };
  const statusMatch = selectedStatus.value.length === 0 || selectedStatus.value.some(status => statusMap[status] === task.taskStatus);
    return deptMatch && projectMatch && statusMatch;
  });
};

// 按人员分组任务
const groupedTasks = () => {
  const groups = {};
  filteredTasks().forEach(task => {
    if (!groups[task.executorId]) {
      groups[task.executorId] = {
        executorId: task.executorId,
        executorName: task.executorName,
        tasks: [],
        completedCount: 0
      };
    }
    // 分离已完成任务
    if (task.taskStatus === '已完成') {
      groups[task.executorId].completedCount++;
    } else {
      groups[task.executorId].tasks.push(task);
    }
  });
  // 按项目创建时间升序排序，再按任务剩余时间升序排列
  Object.values(groups).forEach(group => {
    // 创建项目名称到创建时间的映射
    const projectCreationMap = {};
    projectsData.value.forEach(project => {
      projectCreationMap[project.name] = project.created;
    });

    group.tasks.sort((a, b) => {
      // 获取项目创建时间
      const aProjectCreated = new Date(projectCreationMap[a.projectName] || 0);
      const bProjectCreated = new Date(projectCreationMap[b.projectName] || 0);

      // 按项目创建时间升序排序（早创建的在前）
      if (aProjectCreated.getTime() !== bProjectCreated.getTime()) {
        return bProjectCreated - aProjectCreated;
      }

      // 同一项目内按剩余时间升序排序
      return new Date(a.dueDate) - new Date(b.dueDate);
    });
  });

  return Object.values(groups).sort((a, b) => b.tasks.length - a.tasks.length);
};

// 获取任务状态样式和文本
const getStatusInfo = (status) => {
  switch (status) {
    case '未接收':
      return { class: 'status-pending', text: '未接收', icon: 'clock' };
    case '已接收':
      return { class: 'status-received', text: '已接收', icon: 'check-circle' };
    case '进行中':
      return { class: 'status-progress', text: '进行中', icon: 'loading' };
    case '已完成':
      return { class: 'status-completed', text: '已完成', icon: 'check' };
    default:
      return { class: 'status-default', text: status, icon: 'question' };
  }
};

const completedTasks = () => {
  return tasks.value.filter(task => task.taskStatus === '已完成').length;
};

// 组件生命周期
onMounted(() => {
  // 立即获取项目和任务数据
  fetchProjects();
  fetchTasks();
  // 设置30秒刷新一次
  refreshInterval = setInterval(() => {
      fetchTasks();
    }, 30000);
});

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval);
  }
});
</script>

<template>
  <div class="app-container" style="max-width: 100vw; overflow-x: hidden;">
    <div class="button-group">
  <el-button @click="toggleFilters" class="filter-toggle-btn">
    <el-icon><Filter /></el-icon> 筛选
  </el-button>
  <el-button @click="toggleFullScreen" class="filter-toggle-btn">
    <el-icon><FullScreen /></el-icon>
  </el-button>
</div>
    <!-- 筛选区域 -->
<div class="filter-overlay" :class="{'active': showFilters}" @click="toggleFilters"></div>
<div class="filter-panel" :class="{'active': showFilters}">
      <div class="filter-panel-header">
        <h3>筛选条件</h3>
        <!-- <button @click="toggleFilters" class="close-btn">×</button> -->
      </div>
      <ElTabs v-model="activeTab" type="card" class="filter-tabs">
        <!-- 筛选条件选项卡 -->
        <ElTabPane label="筛选条件" name="filters">
          <div class="filter-group">
              <div class="filter-item">
                <label class="filter-label">部门筛选:</label>
                <ElSelect v-model="selectedDept" placeholder="选择部门" class="filter-select" style="width: 100%">
                  <ElOption v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
                </ElSelect>
              </div>
              <div class="filter-item">
                <label class="filter-label">项目筛选:</label>
                <ElSelect v-model="selectedProject" placeholder="选择项目" class="filter-select" style="width: 100%">
                  <ElOption v-for="project in projects" :key="project" :label="project" :value="project" />
                </ElSelect>
              </div>
              <div class="filter-item">
                <label class="filter-label">任务状态:</label>
                <ElCheckboxGroup v-model="selectedStatus" class="status-checkbox-group">
                  <ElCheckbox label="未接收" value="unreceived" />
                  <ElCheckbox label="进行中" value="inProgress" />
                  <ElCheckbox label="已完成" value="completed" />
                  <ElCheckbox label="已逾期" value="delayed" />
                </ElCheckboxGroup>
              </div>
            </div>
        </ElTabPane>

        <!-- 统计信息选项卡 -->
        <ElTabPane label="任务统计" name="statistics">
          <div class="stats-container">
            <div class="stat-card">
              <div class="stat-title">总任务数</div>
              <div class="stat-value">{{ tasks.length }}</div>
              <div class="stat-desc">系统中所有任务总量</div>
            </div>
            <div class="stat-card">
              <div class="stat-title">筛选后任务数</div>
              <div class="stat-value">{{ filteredTasks().length }}</div>
              <div class="stat-desc">当前筛选条件下的任务量</div>
            </div>
            <div class="stat-card">
              <div class="stat-title">完成率</div>
              <div class="stat-value">{{ Math.round((completedTasks()/tasks.length)*100) }}%</div>
              <div class="stat-desc">已完成任务占比</div>
            </div>
            <div class="refresh-info">
              <i class="el-icon-time"></i>
              <span>后台数据每10分钟刷新一次</span>
            </div>
          </div>
        </ElTabPane>
      </ElTabs>
    </div>

    <!-- 任务卡片区域 -->
    <main class="task-grid" :class="{'filter-active': showFilters}" >
      <ElSpin v-if="isLoading" class="page-loading" size="large" />
      <div v-for="person in groupedTasks()" :key="person.executorId" class="person-task-group">
          <div class="person-header">
            <!-- <ElAvatar class="person-avatar">
              {{ person.executorName ? person.executorName.charAt(0) : '?' }}
            </ElAvatar> -->
            <div class="person-info">
              <h3 class="person-name">{{ person.executorName || '未知用户' }}</h3>
              <div class="task-count">未完成 {{ person.tasks.length }} 个，已完成 {{ person.completedCount }} 个</div>
            </div>
          </div>
          <div style="overflow-x: auto;">
            <ElTable
              :data="person.tasks"
              border
              size="small"
              max-height="380"
              class="task-table"
              :scroll="{ x: 'max-content' }"
            >
            <ElTableColumn prop="taskName" label="任务名称" :width="200"></ElTableColumn>

            <ElTableColumn label="状态" :width="100">
              <template #default="{ row }">
                <div class="task-status-badge" :class="getStatusInfo(row.taskStatus).class">
                  <i :class="'el-icon-' + getStatusInfo(row.taskStatus).icon"></i>
                  <span>{{ getStatusInfo(row.taskStatus).text }}</span>
                </div>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="projectName" label="项目" :width="130"></ElTableColumn>

            <ElTableColumn label="部门" :width="150">
              <template #default="{ row }">
                <div class="dept-tags">
                  <ElTag v-for="dept in row.deptNameList" :key="dept" size="small">{{ dept }}</ElTag>
                </div>
              </template>
            </ElTableColumn>
<ElTableColumn label="开始时间" :width="110">
              <template #default="{ row }">
                {{ row.startDate ? row.startDate : '未设置' }}
              </template>
            </ElTableColumn>
            <ElTableColumn prop="dueDate" label="截止日期" :width="110"></ElTableColumn>
            <ElTableColumn label="剩余时间" :width="100">
              <template #default="{ row }">
                <span class="remain-time" :class="row.remainTimeDays <= 3 ? 'urgent' : ''">
                  {{ row.remainTimeDays }} 天
                </span>
              </template>
            </ElTableColumn>
          </ElTable>
          </div>
        </div>

      <div v-if="!isLoading && groupedTasks().length === 0" class="no-tasks">
        <div class="no-tasks-icon"><i class="el-icon-search"></i></div>
        <div class="no-tasks-text">没有找到匹配的任务</div>
        <ElButton @click="manualRefresh" size="small">重新加载</ElButton>
      </div>
    </main>

  </div>
</template>

<style scoped>
/* 全局样式 */
.app-container {
  width: 100vw;
  max-width: 100vw;
  min-height: 100vh;
  margin: 0;
  padding: 0 ; /* 保留合理内边距，避免内容贴边 */
  overflow-x: hidden;
  box-sizing: border-box;
  /* TODO */
  background-color: #145fce;
  /* background-color: #f5f7fa; */
}

.button-group { display: flex; gap: 12px; position: fixed; top: 20px; right: 20px; z-index: 1000; }





/* 头部样式 */
.app-header {
  background: linear-gradient(135deg, #1e88e5 0%, #3949ab 100%);
  color: white;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  height: 70px;
}

.app-title {
  font-size: 1.5rem;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 10px;
  font-size: 1.8rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.refresh-time {
  font-size: 0.9rem;
  opacity: 0.9;
}

.refresh-btn {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.3s;
}

.refresh-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}

/* 筛选面板样式 */
.filter-panel {
  position: fixed;
  right: 0;
  top: 0;
  height: 100vh;
  width: 280px;
  background: #fff;
  padding: 24px;
  z-index: 1001;
  box-shadow: -2px 0 15px rgba(0, 0, 0, 0.15);
  transform: translateX(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.filter-panel.active {
  transform: translateX(0);
}

.filter-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.filter-panel-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1f2d3d;
  font-weight: 600;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #909399;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #f5f5f5;
  color: #606266;
}

.filter-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.filter-overlay.active {
  opacity: 1;
  visibility: visible;
}

.filter-panel.active {
  transform: translateX(0);
}

.filter-toggle-btn {
  margin-right: 15px;
  padding: 6px 12px;
  background-color: #1e88e5;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.filter-tabs { margin-bottom: 16px; }

.filter-group { padding: 10px 0; }

.status-checkbox-group { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 8px; }

.filter-item { margin-bottom: 16px; display: flex; flex-direction: column; gap: 8px; }

.filter-label { font-size: 0.9rem; color: #606266; font-weight: 500; }

.filter-select { width: 100%; }

.stats-container { padding: 10px 0; }

.stat-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  text-align: center;
}

.stat-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1f2d3d;
  margin-bottom: 4px;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
}

.refresh-info {
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
}

/* 任务网格样式 */
.task-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%,320px), 1fr));
  gap: 16px;
  padding: 0 12px;
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  padding: 0 12px;
  width: 100%;
  max-width: 100vw;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  padding: 0 12px;
  width: 100%;
  max-width: 100%;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
  padding: 0 16px;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  padding: 0 16px;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  display: grid;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box !important;
  grid-template-columns: 1fr 1fr !important;
  gap: 8px;
  padding: 8px 8px 40px;
  position: relative;
  width: 100%;
  max-height: calc(100vh - 5px);
  overflow-y: auto;
  transition: margin-left 0.3s ease;
}



.filter-panel {
  transition: all 0.3s ease;
}

@media (min-width: 1200px) {
  .task-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .task-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 767px) {
  .task-grid {
    grid-template-columns: 1fr;
  }
}

/* 人员任务组样式 */
.person-task-group {
  max-width: 100%;
  box-sizing: border-box;
  padding: 8px;
  margin-bottom: 10px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  /* TODO 固定的卡片宽度 */
  height: 520px;
  overflow-y: auto;
}

.person-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.person-avatar {
  width: 40px;
  height: 40px;
  background-color: #1e88e5;
  color: white;
  font-size: 18px;
  margin-right: 12px;
}

.person-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
}

.person-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 3px 0;
}

.task-count {
  color: #718096;
  font-size: 0.8rem;
  margin: 0;
}

.person-tasks-table {
  margin-top: 10px;
}

.task-table .el-table__cell {
  padding: 8px 10px;
  font-size: 0.9rem;
  min-width: 0;
  box-sizing: border-box !important;
  word-wrap: break-word;
}

.task-table .el-table__header th {
  padding: 8px 10px;
  font-size: 0.85rem;
  background-color: #f5f7fa;
  box-sizing: border-box !important;
}

.task-table {
  width: 100% !important;
  table-layout: fixed;
  --el-table-border-color: #ebeef5;
  overflow: hidden;
  width: 100% !important;
  table-layout: auto;
  --el-table-border-color: #ebeef5;
  overflow: hidden;
  width: 100% !important;
  table-layout: auto;
  --el-table-border-color: #ebeef5;
  overflow: hidden;
  width: 100% !important;
  table-layout: auto;
  min-width: 100%;
  --el-table-border-color: #ebeef5;
  overflow: hidden;
  width: 100% !important;
  table-layout: auto;
  min-width: unset;
  --el-table-header-bg-color: #f8f9fa;
  width: 100% !important;
  table-layout: auto;
  min-width: unset;
  --el-table-header-bg-color: #f8f9fa;
  width: 100% !important;
  min-width: unset;
  table-layout: auto;
  width: 100% !important;
  max-width: 100%;
  width: auto !important;
  min-width: 900px;
  table-layout: fixed;
}

.task-table-container {
  overflow-x: auto;
  width: 100%;
  padding: 0 20px;
}

.task-grid.fullscreen {
  grid-template-columns: 1fr !important;
}

.task-grid.fullscreen .task-table {
  width: auto !important;
}

.task-grid.fullscreen .person-task-group {
  width: 100%;
  padding: 0;
}

.task-table-container {
  overflow-x: auto;
  width: 100%;
}



.task-table .el-table__header th {
  padding: 8px 10px;
  font-size: 0.85rem;
  background-color: #f5f7fa;
}

.page-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* 任务卡片样式 */
.task-card {
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.task-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.1);
}

.task-header {
  padding: 15px 20px;
  background-color: #f8f9fa;
  margin: -20px -20px 15px;
}

.task-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
  margin-bottom: 10px;
}

.status-pending {
  background-color: #f7fafc;
  color: #718096;
}
.status-received {
  background-color: #fff3bf;
  color: #d69e2e;
}
.status-progress {
  background-color: #ebf8ff;
  color: #3182ce;
}

.status-completed {
  background-color: #f0fff4;
  color: #38a169;
}

.status-default {
  background-color: #fff5f5;
  color: #e53e3e;
}

.task-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 5px;
  line-height: 1.4;
}

.executor-name {
  color: #4a5568;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 5px;
}

.task-details {
  padding: 0 5px;

}

.task-meta {
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  margin-bottom: 10px;
  align-items: flex-start;
}

.meta-icon {
  color: #718096;
  margin-right: 8px;
  margin-top: 3px;
  font-size: 0.9rem;
}

.meta-label {
  color: #718096;
  font-size: 0.85rem;
  width: 50px;
}

.meta-value {
  color: #2d3748;
  font-size: 0.9rem;
  flex: 1;
}

.dept-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.task-time-info {
  border-top: 1px dashed #e2e8f0;
  padding-top: 15px;
  margin-top: 10px;
}

.time-item {
  display: flex;
  margin-bottom: 8px;
}

.remain-time {
  font-weight: 600;
}

.urgent {
  color: #e53e3e;
}

/* 无任务状态 */
.no-tasks {
  text-align: center;
  padding: 80px 0;
  color: #718096;
  background-color: white;
  border-radius: 12px;
  margin: 20px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.no-tasks-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  color: #cbd5e0;
}

.no-tasks-text {
  font-size: 1.1rem;
  margin-bottom: 20px;
}

/* 页脚样式 */
.app-footer {
  background-color: white;
  padding: 20px 0;
  margin-top: 30px;
  border-top: 1px solid #e2e8f0;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
  color: #718096;
  font-size: 0.9rem;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .stats-info {
    margin-top: 15px;
  }
  .auto-refresh-info {
    margin-left: 0;
    margin-top: 10px;
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .task-grid {
    grid-template-columns: 1fr;
    padding: 0 8px;
  }
  .el-table__body-wrapper {
    overflow-x: hidden !important;
  }
  .el-table-column {
    min-width: 80px !important;
  }
  .person-name {
    font-size: 14px;
  }
  .task-grid {
    grid-template-columns: 1fr;
    padding: 0 8px;
  }
  .el-table-column {
    min-width: 80px !important;
  }
  .person-name {
    font-size: 14px;
  }
  .el-table__body-wrapper {
    overflow-x: hidden !important;
  }
  .el-table-column {
    min-width: 80px !important;
  }
  .person-name {
    font-size: 14px;
  }
  .task-grid {
    grid-template-columns: 1fr;
    padding: 0 8px;
  }
  .el-table-column {
    min-width: 80px !important;
  }
  .person-name {
    font-size: 14px;
  }
  .el-table__cell {
    padding: 8px 5px;
  }
  .person-name {
    font-size: 14px;
  }
  .person-name {
    font-size: 16px;
  }
  .task-count {
    font-size: 12px;
  }
  .el-table__cell {
    padding: 8px 6px;
  }
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 15px 0;
    gap: 10px;
  }
  .header-actions {
    width: 100%;
    justify-content: center;
  }
  .task-title {
    font-size: 1rem;
  }
}
</style>
