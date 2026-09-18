import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // ============ 新增内网访问 + 后端代理配置 ============
  server: {
    host: '0.0.0.0', // 允许局域网其他设备访问
    port: 5173,
    proxy: {
      '/api': {
        // 改成本机内网IP+后端端口8080
        target: 'http://172.22.156.141:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
