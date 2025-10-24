import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue()
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 8081,
    host: '0.0.0.0', // 允许局域网访问
    proxy: {
      '/api': {
        target: 'http://192.168.90.64:8083',
        changeOrigin: true,
        secure: false,
        credentials: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
