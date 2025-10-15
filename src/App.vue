<template>
  <div class="app-container">
    <el-menu 
      v-if="showNavBar"
      :default-active="route.path" 
      class="main-nav" 
      mode="horizontal"
      background-color="#ffffff"
      text-color="#333333"
      active-text-color="#409EFF"
      router
    >
      <!-- 项目管理类 -->
      <el-sub-menu index="project-management">
        <template #title>
          <Files style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>项目管理</span>
        </template>
        <el-menu-item index="/">
          <Document style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>任务信息</span>
        </el-menu-item>
        <el-menu-item index="/wbs">
          <Grid style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" /> 
          <span>WBS视图</span>
        </el-menu-item>
        <el-menu-item index="/new-feature">
          <Clock style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>任务时间轴</span>
        </el-menu-item>
        <el-menu-item index="/meeting-minutes">
          <Calendar style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>会议纪要</span>
        </el-menu-item>
        <el-menu-item index="/member-gantt">
          <User style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>资源管理</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 开发材料类 -->
      <el-sub-menu index="development-materials">
        <template #title>
          <Folder style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>开发材料</span>
        </template>
        <el-menu-item index="/file-manager">
          <Folder style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>文件管理</span>
        </el-menu-item>
        <el-menu-item index="/circuit-board">
          <Cpu style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>线路板管理</span>
        </el-menu-item>
      </el-sub-menu>
    </el-menu>
    
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
import { onMounted, onUnmounted, ref } from 'vue';
import { House, Menu as MenuIcon, Grid, Folder, Cpu, Document, Calendar, Clock, User, Files } from '@element-plus/icons-vue';

const route = useRoute();
const isFullScreen = ref(false);
const showNavBar = ref(true);

// 监听全屏状态变化
const handleFullScreenChange = () => {
  isFullScreen.value = !!document.fullscreenElement;
};

// 监听自定义事件来控制导航栏显示
const handleToggleNavbar = (event: CustomEvent) => {
  showNavBar.value = event.detail.show;
};

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullScreenChange);
  window.addEventListener('toggle-navbar', handleToggleNavbar as EventListener);
});

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullScreenChange);
  window.removeEventListener('toggle-navbar', handleToggleNavbar as EventListener);
});
</script>

<style scoped>
.app-container {
  min-height: 100vh;
}

.main-nav {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  border-radius: 0;
}

.el-menu-item {
  margin: 0 10px;
}


</style>