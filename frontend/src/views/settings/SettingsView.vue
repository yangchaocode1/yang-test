<template>
  <div class="settings-container">
    <el-tabs v-model="activeTab" type="card" class="settings-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="界面配置" name="ui" />
      <el-tab-pane label="安全设置" name="security" />
      <el-tab-pane label="通知设置" name="notification" />
      <el-tab-pane label="日志管理" name="logs" />
    </el-tabs>
    <div class="settings-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const tabNameMap: Record<string, string> = {
  ui: '/settings/ui',
  security: '/settings/security',
  notification: '/settings/notification',
  logs: '/settings/logs',
}

const pathToTab: Record<string, string> = {
  '/settings/ui': 'ui',
  '/settings/security': 'security',
  '/settings/notification': 'notification',
  '/settings/logs': 'logs',
}

const activeTab = ref(pathToTab[route.path] || 'ui')

watch(() => route.path, (path) => {
  const tab = pathToTab[path]
  if (tab) {
    activeTab.value = tab
  }
})

function handleTabChange(tab: string | number) {
  const path = tabNameMap[tab as string]
  if (path) {
    router.push(path)
  }
}
</script>

<style scoped>
.settings-container {
  padding: 0;
}

.settings-tabs {
  margin-bottom: 20px;
}

.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.settings-content {
  background: #fff;
  border-radius: 0 0 12px 12px;
  padding: 24px;
  border: 1px solid #e4e7ed;
  border-top: none;
}
</style>
