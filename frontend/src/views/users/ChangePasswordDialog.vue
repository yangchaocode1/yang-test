<!--
  修改密码对话框组件
  功能：修改当前用户密码，含旧密码验证、新密码强度检测、确认密码一致性校验
  Props: visible - 对话框可见性
  Events: update:visible - 更新可见性
-->
<template>
  <el-dialog
    :model-value="visible"
    title="修改密码"
    width="440px"
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      label-position="right"
    >
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入旧密码"
          show-password
          maxlength="20"
        />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
          maxlength="20"
        />
        <div class="strength-bar" v-if="form.newPassword">
          <div class="strength-item" v-for="i in 4" :key="i">
            <div
              class="strength-segment"
              :class="{
                active: i <= strengthLevel,
                [`level-${strengthLevel}`]: i <= strengthLevel,
              }"
            ></div>
          </div>
          <span class="strength-text" :class="`level-${strengthLevel}`">{{ strengthLabel }}</span>
        </div>
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
          maxlength="20"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { changePassword } from '@/api/user'

const props = defineProps<{
  visible: boolean
  userId?: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const strengthLevel = computed(() => {
  const pwd = form.newPassword
  if (!pwd) return 0
  let level = 0
  if (pwd.length >= 6) level++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) level++
  if (/\d/.test(pwd)) level++
  if (/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(pwd)) level++
  return level
})

const strengthLabel = computed(() => {
  const labels = ['', '弱', '一般', '较强', '强']
  return labels[strengthLevel.value] || ''
})

const confirmPasswordValidator = (_rule: any, value: string, callback: (err?: Error) => void) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
  confirmPassword: [{ validator: confirmPasswordValidator, trigger: 'blur' }],
}

function handleClose() {
  emit('update:visible', false)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await changePassword(props.userId!, {
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
    })
    ElMessage.success('密码修改成功')
    handleClose()
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.strength-bar {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  width: 100%;
}

.strength-item {
  flex: 1;
}

.strength-segment {
  height: 4px;
  border-radius: 2px;
  background: #e4e7ed;
  transition: background 0.3s;
}

.strength-segment.active.level-1 {
  background: #f56c6c;
}

.strength-segment.active.level-2 {
  background: #e6a23c;
}

.strength-segment.active.level-3 {
  background: #409eff;
}

.strength-segment.active.level-4 {
  background: #67c23a;
}

.strength-text {
  font-size: 12px;
  margin-left: 8px;
  white-space: nowrap;
}

.strength-text.level-1 {
  color: #f56c6c;
}

.strength-text.level-2 {
  color: #e6a23c;
}

.strength-text.level-3 {
  color: #409eff;
}

.strength-text.level-4 {
  color: #67c23a;
}
</style>
