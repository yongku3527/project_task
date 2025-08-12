<template>
  <div class="gantt-container">
    <!-- <div class="gantt-header">
      <h1>成员任务甘特图</h1>
    </div> -->
    
    <div class="floating-controls">
      <el-date-picker
        v-model="startDate"
        type="date"
        placeholder="开始日期"
        @change="fetchGanttData"
        :clearable="false"
        style="width: 150px"
      />
      <el-date-picker
        v-model="endDate"
        type="date"
        placeholder="结束日期"
        @change="fetchGanttData"
        :clearable="false"
        style="width: 150px"
      />

      <el-button type="primary" @click="fetchGanttData" :loading="loading">
          <el-icon><RefreshRight /></el-icon>
          刷新
        </el-button>
        <el-button 
          type="warning" 
          @click="clearMemberSortOrder"
          :disabled="memberSortOrder.length === 0"
        >
          <el-icon><CircleClose /></el-icon>
          重置排序
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
    <div class="gantt-grid" ref="ganttGridRef">
          <!-- 表头：日期行 -->
          <div class="gantt-header-row">
            <div class="gantt-member-header frozen-col">成员</div>
            <div class="gantt-date-headers frozen-row">
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
            v-for="memberId in sortedMembers" 
            :key="memberId"
            class="gantt-member-row"
            :class="{
              'dragging': draggedMember === memberId,
              'drag-over': dragOverMember === memberId
            }"
            draggable="true"
            @dragstart="handleDragStart(memberId)"
            @dragenter="handleDragEnter(memberId)"
            @dragleave="handleDragLeave"
            @dragover.prevent
            @drop="handleDrop(memberId)"
            @dragend="handleDragEnd"
          >
            <div class="gantt-member-name frozen-col" :title="memberId">
              <el-icon class="drag-handle"><Rank /></el-icon>
              {{ getUserName(memberId) }}
            </div>
            <div class="gantt-task-cells">
              <div 
                v-for="date in dateRange"
                :key="`${memberId}-${date.toISOString()}`"
                class="gantt-task-cell"
                :class="getCellClass(ganttData[memberId]?.[formatDateKey(date)], date)"
                :title="getCellTitle(ganttData[memberId]?.[formatDateKey(date)], date)"
              >
                <span v-if="ganttData[memberId]?.[formatDateKey(date)] && ganttData[memberId]?.[formatDateKey(date)].length > 0" class="task-count">
                  {{ ganttData[memberId]?.[formatDateKey(date)].length }}
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
import { ref, onMounted, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading, CircleClose, RefreshRight, Rank } from '@element-plus/icons-vue';
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

// 常量
const MEMBER_SORT_STORAGE_KEY = 'member_gantt_sort_order';

// 状态管理
const loading = ref(false);
const error = ref('');
const ganttData = ref<GanttData>({});
const startDate = ref(new Date());
const endDate = ref(dayjs().add(1, 'month').toDate());
const userMapping = ref<UserMapping>({});
const memberSortOrder = ref<string[]>([]);
const availableMembers = ref<string[]>([]);

// 计算日期范围
const dateRange = computed(() => {
  const start = dayjs(startDate.value);
  const end = dayjs(endDate.value);
  const dates: Date[] = [];
  
  let current = start;
  while (current.isBefore(end) || current.isSame(end, 'day')) {
    dates.push(current.toDate());
    current = current.add(1, 'day');
  }
  
  return dates;
});

// 排序后的人员列表
const sortedMembers = computed(() => {
  const members = Object.keys(ganttData.value);
  if (memberSortOrder.value.length === 0) {
    return members;
  }
  
  // 先按自定义排序，其余按原顺序追加
  const sorted = [...memberSortOrder.value];
  const remaining = members.filter(m => !memberSortOrder.value.includes(m));
  return [...sorted, ...remaining];
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
    const baseUrl = 'http://192.168.100.125:8083';
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



// 拖拽排序相关变量
const draggedMember = ref<string | null>(null);
const dragOverMember = ref<string | null>(null);

// 拖拽开始
const handleDragStart = (memberId: string) => {
  draggedMember.value = memberId;
};

// 拖拽进入
const handleDragEnter = (memberId: string) => {
  dragOverMember.value = memberId;
};

// 拖拽离开
const handleDragLeave = () => {
  dragOverMember.value = null;
};

// 拖拽放置
const handleDrop = (targetMemberId: string) => {
  if (!draggedMember.value || draggedMember.value === targetMemberId) return;
  
  const members = [...sortedMembers.value];
  const draggedIndex = members.indexOf(draggedMember.value);
  const targetIndex = members.indexOf(targetMemberId);
  
  if (draggedIndex === -1 || targetIndex === -1) return;
  
  // 重新排序
  const newOrder = [...members];
  const [removed] = newOrder.splice(draggedIndex, 1);
  newOrder.splice(targetIndex, 0, removed);
  
  // 更新排序状态
  memberSortOrder.value = newOrder;
  
  // 清理拖拽状态
  draggedMember.value = null;
  dragOverMember.value = null;
};

// 拖拽结束
const handleDragEnd = () => {
  draggedMember.value = null;
  dragOverMember.value = null;
};

// 本地存储相关函数
const saveMemberSortOrder = () => {
  try {
    localStorage.setItem(MEMBER_SORT_STORAGE_KEY, JSON.stringify(memberSortOrder.value));
    console.log('人员排序已保存到本地存储');
  } catch (error) {
    console.error('保存人员排序失败:', error);
  }
};

const loadMemberSortOrder = (): string[] => {
  try {
    const stored = localStorage.getItem(MEMBER_SORT_STORAGE_KEY);
    return stored ? JSON.parse(stored) : [];
  } catch (error) {
    console.error('加载人员排序失败:', error);
    return [];
  }
};

const clearMemberSortOrder = () => {
  try {
    localStorage.removeItem(MEMBER_SORT_STORAGE_KEY);
    memberSortOrder.value = [];
    console.log('人员排序已清除');
  } catch (error) {
    console.error('清除人员排序失败:', error);
  }
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
    
    const startDateStr = dayjs(startDate.value).format('YYYY-MM-DD');
    const endDateStr = dayjs(endDate.value).format('YYYY-MM-DD');
    
    const baseUrl = 'http://192.168.100.125:8083';
    console.log('请求参数:', { startDate: startDateStr, endDate: endDateStr });
    
    const response = await axios.get(`${baseUrl}/gantt/getGanttData`, {
      params: {
        startDate: startDateStr,
        endDate: endDateStr
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

  // 更新可用人员列表
  const members = Object.keys(processed);
  availableMembers.value = members;

  // 清理不存在的成员的排序
  memberSortOrder.value = memberSortOrder.value.filter(m => members.includes(m));

  // 如果是首次加载数据，从本地存储加载排序
  if (memberSortOrder.value.length === 0) {
    const savedOrder = loadMemberSortOrder();
    memberSortOrder.value = savedOrder.filter(member => members.includes(member));
  }
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

// 监听排序变化并自动保存
watch(memberSortOrder, (newOrder) => {
  if (newOrder.length > 0) {
    saveMemberSortOrder();
  }
}, { deep: true });
</script>

<style scoped>
.gantt-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
  background-color: #f5f5f5;
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
  top: 100px;
  right: 30px;
  z-index: 1000;
  display: flex;
  gap: 12px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  flex-wrap: wrap;
  max-width: 400px;
  align-items: flex-start;
}

.gantt-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.gantt-chart {
  flex: 1;
  overflow: auto;
  position: relative;
}

.gantt-grid {
  position: relative;
  min-width: 100%;
  display: inline-block;
}

/* 冻结行列样式 */
.frozen-col {
  position: sticky;
  left: 0;
  z-index: 3;
  background: white;
  border-right: 2px solid #e0e0e0;
}

.frozen-row {
  position: sticky;
  top: 0;
  z-index: 2;
  background: #f8f9fa;
}

.gantt-header.frozen-col {
  z-index: 4;
  background: #f8f9fa;
}

.gantt-member-name.frozen-col {
  z-index: 3;
  background: white;
}

.gantt-grid {
  min-width: 100%;
  border-collapse: collapse;
}

.gantt-header-row {
  display: flex;
  border-bottom: 2px solid #e0e0e0;
  background: #f8f9fa;
}

.gantt-member-header {
  width: 150px;
  min-width: 110px;
  height: 42px;
  padding: 12px;
  font-weight: bold;
  text-align: center;
  border-right: 1px solid #e8e8e8;
  background-color: #fafafa;
  position: sticky;
  left: 0;
  z-index: 10;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gantt-date-headers {
  display: flex;
  flex: 1;
}

.gantt-date-header {
  width: 50px;
  padding: 8px 4px;
  text-align: center;
  font-size: 12px;
  border-right: 1px solid #e8e8e8;
  font-weight: bold;
  box-sizing: border-box;
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

.gantt-member-row.dragging {
  opacity: 0.5;
}

.gantt-member-row.dragging .gantt-member-name,
.gantt-member-row.dragging .gantt-task-cells {
  background-color: #e3f2fd;
}

.gantt-member-row.drag-over .gantt-member-name,
.gantt-member-row.drag-over .gantt-task-cells {
  background-color: #bbdefb;
}

.gantt-member-row.drag-over .gantt-member-name {
  border-left: 3px solid #2196f3;
}

.drag-handle {
  margin-right: 8px;
  cursor: move;
  color: #999;
  font-size: 12px;
}

.gantt-member-name:hover .drag-handle {
  color: #666;
}

.gantt-member-name {
  width: 150px;
  min-width: 110px;
  height: 42px;
  padding: 12px;
  font-weight: bold;
  text-align: center;
  border-right: 1px solid #e8e8e8;
  background-color: white;
  position: sticky;
  left: 0;
  z-index: 5;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gantt-task-cells {
  display: flex;
  flex: 1;
}

.gantt-task-cell {
  width: 50px;
  height: 42px;
  border-right: 1px solid #e8e8e8;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-sizing: border-box;
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