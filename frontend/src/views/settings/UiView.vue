<template>
  <div class="ui-config">
    <el-form label-width="140px" label-position="right">
      <el-form-item label="主题设置">
        <el-radio-group v-model="config.theme">
          <el-radio value="light">默认主题</el-radio>
          <el-radio value="dark">暗色主题</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="布局设置">
        <el-radio-group v-model="config.layout">
          <el-radio value="sidebar">侧边栏布局</el-radio>
          <el-radio value="top">顶部布局</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="语言设置">
        <el-radio-group v-model="config.language">
          <el-radio value="zh">中文</el-radio>
          <el-radio value="en">English</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUiConfig, saveUiConfig } from '@/api/config'
import type { UiConfig } from '@/types'

const config = ref<UiConfig>({
  theme: 'light',
  layout: 'sidebar',
  language: 'zh',
})

const saving = ref(false)

async function fetchConfig() {
  try {
    const { data } = await getUiConfig()
    config.value = data
  } catch {
    config.value = { theme: 'light', layout: 'sidebar', language: 'zh' }
  }
}

async function handleSave() {
  saving.value = true
  try {
    await saveUiConfig(config.value)
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetchConfig)
</script>

<style scoped>
.ui-config {
  max-width: 600px;
}
</style>
