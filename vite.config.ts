import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
 
  ],
  server: {
    host: '192.168.100.43',
    proxy: {
      '/dingTask': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      }
    }
  }
})
