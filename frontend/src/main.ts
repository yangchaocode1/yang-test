/**
 * 应用入口文件
 * 创建 Vue 应用实例，注册全局插件（Pinia、Router、Element Plus）、
 * 注册 Element Plus 图标组件、注册自定义权限指令，并挂载到 DOM
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { setupPermissionDirective } from './directives/permission'

/** 创建 Vue 应用实例 */
const app = createApp(App)

// 全局注册 Element Plus 图标组件，以便在模板中直接使用图标名称
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册全局插件
app.use(createPinia())   // 状态管理
app.use(router)          // 路由
app.use(ElementPlus)     // UI 组件库

// 注册自定义权限指令 v-permission
setupPermissionDirective(app)

// 挂载应用到 #app 节点
app.mount('#app')
