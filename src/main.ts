import { createApp } from 'vue'
import './style.css'
// import App from './TaskManagementSystem.vue'
import App from './App.vue' 
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/theme-chalk/index.css'
import axios from 'axios'
import router from './router'

const app = createApp(App)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.config.globalProperties.$axios = axios
app.mount('#app')
