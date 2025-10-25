<template>
  <div class="app-container">
    <!-- 登录界面不显示导航栏 -->
    <el-menu 
      v-if="showNavBar && !isLoginPage"
      :default-active="route.path" 
      class="main-nav" 
      mode="horizontal"
      background-color="#ffffff"
      text-color="#333333"
      active-text-color="#409EFF"
      router
    >
      <!-- 项目管理类 -->
      <el-sub-menu  index="project-management">
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
      <el-sub-menu v-permission="'doc:menu'" index="development-materials">
        <template #title>
          <Folder style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>开发材料</span>
        </template>
        <el-menu-item v-permission="'doc:view'" index="/file-manager">
          <Folder style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>文件管理</span>
        </el-menu-item>
        <el-menu-item v-permission="'doc:view'" index="/circuit-board">
          <Cpu style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>线路板管理</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 系统管理 -->
      <el-sub-menu v-permission="'permission:menu'" index="system-management">
        <template #title>
          <Setting style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>系统管理</span>
        </template>
        <el-menu-item v-permission="'user:menu'" index="/user-manager">
          <User style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item v-permission="'role:menu'" index="/role-permission">
          <Key style="width: 18px; height: 18px; font-size: 18px; margin-right: 4px;" />
          <span>角色权限</span>
        </el-menu-item>
      </el-sub-menu>
      
      <!-- 用户菜单 -->
      <div class="user-menu">
        <el-dropdown v-if="isLoggedIn" @command="handleUserCommand">
          <span class="el-dropdown-link">
            <el-icon><User /></el-icon>
            {{ username }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-else type="primary" link @click="goToLogin">
          <el-icon><User /></el-icon>
          登录
        </el-button>
      </div>
    </el-menu>
    
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { House, Menu as MenuIcon, Grid, Folder, Cpu, Document, Calendar, Clock, User, Files, ArrowDown, Setting, Key } from '@element-plus/icons-vue';
import PermissionManager from './utils/permission';

const route = useRoute();
const router = useRouter();
const isFullScreen = ref(false);
const showNavBar = ref(true);
const isLoginPage = ref(false);
const isLoggedIn = ref(false);
const username = ref('');

// 监听全屏状态变化
const handleFullScreenChange = () => {
  isFullScreen.value = !!document.fullscreenElement;
};

// 监听自定义事件来控制导航栏显示
const handleToggleNavbar = (event: CustomEvent) => {
  showNavBar.value = event.detail.show;
};

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token');
  const currentUsername = localStorage.getItem('username');
  
  isLoggedIn.value = !!token;
  username.value = currentUsername || '';
  
  // 登录状态检查由路由守卫处理，这里只更新UI状态
};

// 处理用户菜单命令
const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout();
  }
};

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  // 清除权限和角色信息
  PermissionManager.clearPermissions();
  PermissionManager.clearRoles();
  isLoggedIn.value = false;
  username.value = '';
  router.push('/login');
};

// 跳转到登录页
const goToLogin = () => {
  router.push('/login');
};

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullScreenChange);
  window.addEventListener('toggle-navbar', handleToggleNavbar as EventListener);
  
  // 检查是否已登录
  checkLoginStatus();
  
  // 监听路由变化，判断是否登录页面
  watch(() => route.path, (newPath) => {
    isLoginPage.value = newPath === '/login';
  }, { immediate: true });
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
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .user-menu {
    margin-left: auto;
    padding-right: 20px;
  }

  .el-dropdown-link {
    cursor: pointer;
    color: #409EFF;
    display: flex;
    align-items: center;
    gap: 5px;
  }

.el-menu-item {
  margin: 0 10px;
}


</style>