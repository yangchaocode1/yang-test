<template>
  <div class="notification-config">
    <el-form label-width="140px" label-position="right">
      <el-form-item label="邮件通知">
        <el-switch v-model="config.emailEnabled" />
      </el-form-item>

      <el-form-item label="短信通知">
        <el-switch v-model="config.smsEnabled" />
      </el-form-item>

      <el-form-item label="通知邮箱">
        <el-input v-model="config.notifyEmail" placeholder="请输入通知邮箱地址" style="max-width: 400px" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotificationConfig, saveNotificationConfig } from '@/api/config'
import type { NotificationConfig } from '@/types'

const config = ref<NotificationConfig>({
  emailEnabled: true,
  smsEnabled: false,
  notifyEmail: '',
})

const saving = ref(false)

async function fetchConfig() {
  try {
    const { data } = await getNotificationConfig()
    config.value = data
  } catch {
    config.value = { emailEnabled: true, smsEnabled: false, notifyEmail: '' }
  }
}

async function handleSave() {
  saving.value = true
  try {
    await saveNotificationConfig(config.value)
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
.notification-config {
  max-width: 600px;
}
</style>
