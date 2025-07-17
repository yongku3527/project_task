import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import Home from '../views/Home.vue';
import TaskInfo from '../views/TaskInfo.vue';
import NewFeature from '../views/NewFeature.vue';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/task-info',
    name: 'TaskInfo',
    component: TaskInfo
  },
  {
    path: '/new-feature',
    name: 'NewFeature',
    component: NewFeature
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;