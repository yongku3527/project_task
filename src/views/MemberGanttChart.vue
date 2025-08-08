<template>
  <div class="gantt-container">
    <!-- <div class="gantt-header">
      <h1>成员任务甘特图</h1>
    </div> -->
    
    <div class="floating-controls">
      <el-date-picker
        v-model="selectedMonth"
        type="month"
        placeholder="选择月份"
        @change="handleMonthChange"
        :clearable="false"
      />
      <el-button type="primary" @click="refreshData">
        <el-icon><RefreshRight /></el-icon>
        刷新
      </el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      加载中...
    </div>

    <div v-else-if="error" class="error">
      <el-icon><CircleClose /></el-icon>
      {{ error }}
    </div>

    <div v-else class="gantt-content">
      <div class="gantt-chart">
        <div class="gantt-grid">
          <!-- 表头：日期行 -->
          <div class="gantt-header-row">
            <div class="gantt-member-header">成员</div>
            <div class="gantt-date-headers">
              <div 
                v-for="date in dateRange" 
                :key="date.toISOString()"
                class="gantt-date-header"
                :class="{ 'weekend': isWeekend(date) }"
              >
                {{ formatDateHeader(date) }}
              </div>
            </div>
          </div>

          <!-- 成员任务行 -->
          <div 
            v-for="(memberData, memberId) in ganttData" 
            :key="memberId"
            class="gantt-member-row"
          >
            <div class="gantt-member-name" :title="memberId">
              {{ getUserName(memberId) }}
            </div>
            <div class="gantt-task-cells">
              <div 
                v-for="date in dateRange"
                :key="`${memberId}-${date.toISOString()}`"
                class="gantt-task-cell"
                :class="getCellClass(memberData[formatDateKey(date)], date)"
                :title="getCellTitle(memberData[formatDateKey(date)], date)"
              >
                <span v-if="memberData[formatDateKey(date)] && memberData[formatDateKey(date)].length > 0" class="task-count">
                  {{ memberData[formatDateKey(date)].length }}
                </span>
              </div>
            </div>
          </div>
          
          <!-- 无数据提示 -->
          <div v-if="!ganttData || Object.keys(ganttData).length === 0" class="no-data">
            <el-icon><InfoFilled /></el-icon>
            <p>暂无数据，请检查接口返回或选择其他月份</p>
          </div>
        </div>

        <!-- 图例 -->
        <div class="gantt-legend">
          <div class="legend-title">任务数量图例</div>
          <div class="legend-items">
            <div class="legend-item">
              <div class="legend-color" style="background-color: #e6f7ff;"></div>
              <span>0 任务</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background-color: #91d5ff;"></div>
              <span>1-2 任务</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background-color: #40a9ff;"></div>
              <span>3-4 任务</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background-color: #1890ff;"></div>
              <span>5-7 任务</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background-color: #096dd9;"></div>
              <span>8+ 任务</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading, CircleClose, RefreshRight } from '@element-plus/icons-vue';
import axios from 'axios';
import dayjs from 'dayjs';

interface TaskVo {
  taskId: string;
  taskName: string;
  assignee: string;
  dueDate: string;
  priority: string;
  status: string;
}

interface GanttData {
  [memberName: string]: {
    [date: string]: TaskVo[];
  };
}

interface UserMapping {
  [userId: string]: string;
}

// 状态管理
const loading = ref(false);
const error = ref('');
const ganttData = ref<GanttData>({});
const selectedMonth = ref(new Date());
const userMapping = ref<UserMapping>({});

// 计算当前月份的日期范围
const dateRange = computed(() => {
  const start = dayjs(selectedMonth.value).startOf('month');
  const end = dayjs(selectedMonth.value).endOf('month');
  const dates: Date[] = [];
  
  let current = start;
  while (current.isBefore(end) || current.isSame(end, 'day')) {
    dates.push(current.toDate());
    current = current.add(1, 'day');
  }
  
  return dates;
});

// 工具函数
const formatDateHeader = (date: Date): string => {
  return dayjs(date).format('MM/DD');
};

const formatDateKey = (date: Date): string => {
  return dayjs(date).format('YYYY-MM-DD');
};

const isWeekend = (date: Date): boolean => {
  const day = dayjs(date).day();
  return day === 0 || day === 6;
};

const getTaskCount = (tasks?: TaskVo[]): number => {
  return tasks ? tasks.length : 0;
};

const getCellClass = (tasks: TaskVo[] | undefined, date: Date): string => {
  const count = getTaskCount(tasks);
  let className = 'gantt-task-cell ';
  
  if (isWeekend(date)) {
    className += 'weekend ';
  }
  
  if (count === 0) return className + 'level-0';
  if (count <= 2) return className + 'level-1';
  if (count <= 4) return className + 'level-2';
  if (count <= 7) return className + 'level-3';
  return className + 'level-4';
};

const getCellTitle = (tasks: TaskVo[] | undefined, date: Date): string => {
  const count = getTaskCount(tasks);
  const dateStr = dayjs(date).format('YYYY-MM-DD');
  
  if (count === 0) {
    return `${dateStr}: 无任务`;
  } else {
    return `${dateStr}: ${count}个任务\n${tasks?.map(t => t.taskName).join('\n')}`;
  }
};



// 获取用户映射数据
const fetchUserMapping = async () => {
  try {
    const baseUrl = 'http://192.168.90.64:8083';
    const response = await axios.get(`${baseUrl}/basicData/userNameData`);
    
    if (response.data.code === 200) {
      const mapping: UserMapping = {};
      response.data.data.forEach((item: string) => {
        const [userId, userName] = item.split(';');
        mapping[userId] = userName;
      });
      userMapping.value = mapping;
      console.log('用户映射加载完成:', mapping);
    } else {
      console.error('获取用户映射失败:', response.data.msg);
    }
  } catch (error) {
    console.error('获取用户映射异常:', error);
  }
};

// 获取用户名
const getUserName = (userId: string): string => {
  return userMapping.value[userId] || userId;
};

// 数据获取
const fetchGanttData = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    // 先获取用户映射
    if (Object.keys(userMapping.value).length === 0) {
      await fetchUserMapping();
    }
    
    const startDate = dayjs(selectedMonth.value).startOf('month').format('YYYY-MM-DD');
    const endDate = dayjs(selectedMonth.value).endOf('month').format('YYYY-MM-DD');
    
    const baseUrl = 'http://192.168.90.64:8083';
    console.log('请求参数:', { startDate, endDate });
    
    const response = await axios.get(`${baseUrl}/gantt/getGanttData`, {
      params: {
        startDate,
        endDate
      }
    });
    
    if (response.data.code === 200) {
      processGanttData(response.data.data);
    } else {
      throw new Error(response.data.msg || '获取数据失败');
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '获取数据失败';
    ElMessage.error(error.value);
  } finally {
    loading.value = false;
  }
};

const processGanttData = (rawData: any) => {
  const processed: GanttData = {};
  
  console.log('原始数据:', rawData);
  
  // 按成员和日期分组任务
  Object.entries(rawData).forEach(([memberName, memberTasks]: [string, any]) => {
    processed[memberName] = {};
    
    // 确保memberTasks是一个对象
    if (typeof memberTasks === 'object' && memberTasks !== null) {
      Object.entries(memberTasks).forEach(([dateStr, tasks]: [string, any]) => {
        // 确保日期格式一致，处理可能的LocalDate格式
        const date = dayjs(dateStr).format('YYYY-MM-DD');
        processed[memberName][date] = Array.isArray(tasks) ? tasks : [];
        
        // console.log(`成员 ${memberName} 日期 ${date} 任务数:`, processed[memberName][date].length);
      });
    } else {
      console.warn(`成员 ${memberName} 的任务数据格式不正确:`, memberTasks);
    }
  });
  
  console.log('处理后的数据:', processed);
  ganttData.value = processed;
};

// 事件处理
const handleMonthChange = () => {
  fetchGanttData();
};

const refreshData = () => {
  fetchGanttData();
};

// 生命周期
onMounted(() => {
  fetchGanttData();
});
</script>

<style scoped>
.gantt-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.gantt-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.gantt-header h1 {
  margin: 0;
  color: #333;
}

.floating-controls {
  position: fixed;
  top: 10px;
  right: 30px;
  z-index: 1000;
  display: flex;
  gap: 12px;
  align-items: center;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.gantt-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.gantt-chart {
  overflow-x: auto;
}

.gantt-grid {
  min-width: 100%;
  border-collapse: collapse;
}

.gantt-header-row {
  display: flex;
  border-bottom: 2px solid #e8e8e8;
  background-color: #fafafa;
}

.gantt-member-header {
  width: 150px;
  padding: 12px;
  font-weight: bold;
  text-align: center;
  border-right: 1px solid #e8e8e8;
  background-color: #fafafa;
  position: sticky;
  left: 0;
  z-index: 10;
}

.gantt-date-headers {
  display: flex;
  flex: 1;
}

.gantt-date-header {
  width: 40px;
  padding: 8px 4px;
  text-align: center;
  font-size: 12px;
  border-right: 1px solid #e8e8e8;
  font-weight: bold;
}

.gantt-date-header.weekend {
  background-color: #f0f0f0;
  color: #999;
}

.gantt-member-row {
  display: flex;
  border-bottom: 1px solid #e8e8e8;
}

.gantt-member-row:hover {
  background-color: #f5f5f5;
}

.gantt-member-name {
  width: 150px;
  padding: 12px;
  font-weight: bold;
  text-align: center;
  border-right: 1px solid #e8e8e8;
  background-color: white;
  position: sticky;
  left: 0;
  z-index: 5;
}

.gantt-task-cells {
  display: flex;
  flex: 1;
}

.gantt-task-cell {
  width: 40px;
  height: 40px;
  border-right: 1px solid #e8e8e8;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.gantt-task-cell:hover {
  transform: scale(1.1);
  z-index: 5;
}

.gantt-task-cell.weekend {
  background-color: #f9f9f9;
}

.gantt-task-cell.level-0 {
  background-color: #e6f7ff;
}

.gantt-task-cell.level-1 {
  background-color: #91d5ff;
  color: white;
}

.gantt-task-cell.level-2 {
  background-color: #40a9ff;
  color: white;
}

.gantt-task-cell.level-3 {
  background-color: #1890ff;
  color: white;
}

.gantt-task-cell.level-4 {
  background-color: #096dd9;
  color: white;
}

.task-count {
  font-weight: bold;
  font-size: 12px;
}

.gantt-legend {
  margin-top: 20px;
  padding: 15px;
  background-color: #fafafa;
  border-top: 1px solid #e8e8e8;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #999;
}

.no-data .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.no-data p {
  font-size: 16px;
  margin: 0;
}

.legend-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.legend-items {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
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

@media (max-width: 768px) {
  .gantt-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .gantt-member-header,
  .gantt-member-name {
    width: 120px;
  }
  
  .gantt-date-header,
  .gantt-task-cell {
    width: 35px;
  }
}
</style>