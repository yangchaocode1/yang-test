<template>
  <div class="security-config">
    <el-form label-width="160px" label-position="right">
      <h4 class="config-section-title">密码策略设置</h4>

      <el-form-item label="最小密码长度">
        <el-input-number v-model="config.minPasswordLength" :min="6" :max="32" :step="1" />
      </el-form-item>

      <el-form-item label="要求大写字母">
        <el-switch v-model="config.requireUppercase" />
      </el-form-item>

      <el-form-item label="要求数字">
        <el-switch v-model="config.requireDigit" />
      </el-form-item>

      <el-divider />

      <h4 class="config-section-title">登录安全设置</h4>

      <el-form-item label="最大登录尝试次数">
        <el-input-number v-model="config.maxLoginAttempts" :min="1" :max="10" :step="1" />
      </el-form-item>

      <el-form-item label="会话超时时间">
        <el-input-number v-model="config.sessionTimeout" :min="5" :max="1440" :step="5" />
        <span class="unit-text">分钟</span>
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
import { getSecurityConfig, saveSecurityConfig } from '@/api/config'
import type { SecurityConfig } from '@/types'

const config = ref<SecurityConfig>({
  minPasswordLength: 8,
  requireUppercase: true,
  requireDigit: true,
  maxLoginAttempts: 5,
  sessionTimeout: 30,
})

const saving = ref(false)

async function fetchConfig() {
  try {
    const { data } = await getSecurityConfig()
    config.value = data
  } catch {
    config.value = { minPasswordLength: 8, requireUppercase: true, requireDigit: true, maxLoginAttempts: 5, sessionTimeout: 30 }
  }
}

async function handleSave() {
  saving.value = true
  try {
    await saveSecurityConfig(config.value)
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
.security-config {
  max-width: 600px;
}

.config-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  margin: 0 0 20px;
}

.unit-text {
  margin-left: 8px;
  color: #86909c;
  font-size: 14px;
}
</style>
