import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import Home from '../views/Home.vue';
import TaskInfo from '../views/TaskInfo.vue';
import NewFeature from '../views/NewFeature.vue';
import WBSView from '../views/WBSView.vue';

const routes: Array<RouteRecordRaw> = [
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
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;