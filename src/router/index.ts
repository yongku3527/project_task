import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import Home from '../views/Home.vue';
import TaskInfo from '../views/TaskInfo.vue';
import NewFeature from '../views/NewFeature.vue';
import WBSView from '../views/WBSView.vue';
import MeetingMinutes from '../views/MeetingMinutes.vue';
import MemberGanttChart from '../views/MemberGanttChart.vue';
import FileManager from '../views/FileManager.vue';
import CircuitBoardManager from '../views/CircuitBoardManager.vue';
import DocProdDrawingManager from '../views/DocProdDrawingManager.vue';
import Login from '../views/Login.vue';
import TestLogin from '../views/TestLogin.vue';
import CookieTest from '../views/CookieTest.vue';
import UserManager from '../views/UserManager.vue';
import RolePermissionManager from '../views/RolePermissionManager.vue';
import ItemTypeManager from '../views/ItemTypeManager.vue';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  // {
  //   path: '/',
  //   name: 'Home',
  //   component: Home
  // },
  {
    path: '/',
    name: 'TaskInfo',
    component: TaskInfo
  },
  {
    path: '/new-feature',
    name: 'NewFeature',
    component: NewFeature
  },
  {
    path: '/wbs',
    name: 'WBSView',
    component: WBSView
  },
  {
    path: '/meeting-minutes',
    name: 'MeetingMinutes',
    component: MeetingMinutes
  },
  {
    path: '/member-gantt',
    name: 'MemberGanttChart',
    component: MemberGanttChart
  },
  {
    path: '/file-manager',
    name: 'FileManager',
    component: FileManager
  },
  {
    path: '/circuit-board',
    name: 'CircuitBoardManager',
    component: CircuitBoardManager
  },
  {
    path: '/doc-prod-drawing',
    name: 'DocProdDrawingManager',
    component: DocProdDrawingManager
  },
  {
    path: '/test-login',
    name: 'TestLogin',
    component: TestLogin
  },
  {
    path: '/cookie-test',
    name: 'CookieTest',
    component: CookieTest
  },
  {
    path: '/user-manager',
    name: 'UserManager',
    component: UserManager
  },
  {
    path: '/role-permission',
    name: 'RolePermissionManager',
    component: RolePermissionManager
  },
  {
    path: '/item-type',
    name: 'ItemTypeManager',
    component: ItemTypeManager
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  
  // 如果访问登录页且有token，跳转到首页
  if (to.path === '/login' && token) {
    next('/');
    return;
  }

    // 如果访问其他页面且没有token，跳转到登录页
  if (to.path !== '/login' && !token) {
    next('/login');
    return;
  }
  
  // // 需要token验证的路径列表
  // const protectedRoutes = ['/file-manager', '/circuit-board', '/user-manager', '/role-permission'];
  
  // // 如果访问需要token的路径但没有token，跳转到登录页
  // if (protectedRoutes.includes(to.path) && !token) {
  //   next('/login');
  //   return;
  // }
  
  // 首页不需要token验证，允许直接访问
  next();
});

export default router;