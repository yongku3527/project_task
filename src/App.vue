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
      <!-- <el-menu-item index="/">
        <House />
        <span>首页</span>
      </el-menu-item> -->
      <el-menu-item index="/">
        <MenuIcon />
        <span>任务信息</span>
      </el-menu-item>
      <el-menu-item index="/wbs">
        <Grid />
        <span>WBS视图</span>
      </el-menu-item>
      <el-menu-item index="/new-feature">
        <Grid />
        <span>任务时间轴</span>
      </el-menu-item>
      <el-menu-item index="/meeting-minutes">
        <Grid />
        <span>会议纪要</span>
      </el-menu-item>
    </el-menu>
    
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
import { onMounted, onUnmounted, ref } from 'vue';
import { House, Menu as MenuIcon, Grid } from '@element-plus/icons-vue';

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