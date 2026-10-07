import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import service from './utils/axios'
// 引入中文包
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const app = createApp(App)
app.config.globalProperties.$axios = service

// 注册图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 挂载ElementPlus，传入locale开启中文（关键改动）
app.use(router)
    .use(ElementPlus, {
        locale: zhCn
    })
    .mount('#app')

