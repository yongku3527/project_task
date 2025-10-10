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
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;