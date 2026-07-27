<!--
  登录页面
  功能：账号密码登录、手机短信验证码登录、记住用户名、登录后跳转
  支持两种登录方式的 Tab 切换，短信登录含倒计时验证码发送
-->
<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 登录页头部：Logo、标题 -->
      <div class="login-header">
        <div class="login-logo">
          <el-icon :size="36" color="#409eff"><Monitor /></el-icon>
        </div>
        <h2 class="login-title">企业后台管理系统</h2>
        <p class="login-subtitle">Enterprise Management System</p>
      </div>

      <!-- 登录方式 Tab 切换 -->
      <el-tabs v-model="activeTab" class="login-tabs" stretch>
        <!-- 账号密码登录 Tab -->
        <el-tab-pane label="账号密码登录" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="0"
            size="large"
          >
            <el-form-item prop="username">
              <el-input
                v-model="passwordForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="passwordForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handlePasswordLogin"
              />
            </el-form-item>
            <el-form-item>
              <div class="login-options">
                <el-checkbox v-model="passwordForm.remember">记住我</el-checkbox>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                class="login-btn"
                :loading="loading"
                @click="handlePasswordLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 手机短信验证码登录 Tab -->
        <el-tab-pane label="手机验证码登录" name="sms">
          <el-form
            ref="smsFormRef"
            :model="smsForm"
            :rules="smsRules"
            label-width="0"
            size="large"
          >
            <el-form-item prop="phone">
              <el-input
                v-model="smsForm.phone"
                placeholder="请输入手机号"
                :prefix-icon="Phone"
                clearable
                maxlength="11"
              />
            </el-form-item>
            <el-form-item prop="code">
              <div class="sms-input-wrapper">
                <el-input
                  v-model="smsForm.code"
                  placeholder="请输入验证码"
                  :prefix-icon="Message"
                  clearable
                  maxlength="6"
                  @keyup.enter="handleSmsLogin"
                />
                <!-- 验证码发送按钮，倒计时期间禁用 -->
                <el-button
                  class="sms-btn"
                  :disabled="smsCountdown > 0"
                  :loading="smsSending"
                  @click="handleSendCode"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s 后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                class="login-btn"
                :loading="loading"
                @click="handleSmsLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 页脚版权信息 -->
    <div class="login-footer">
      <span>Copyright &copy; 2026 企业后台管理系统</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login, loginBySms, sendSmsCode } from '@/api/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { User, Lock, Phone, Message, Monitor } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

/** 当前激活的登录 Tab（password-密码登录，sms-短信登录） */
const activeTab = ref('password')
/** 登录按钮加载状态 */
const loading = ref(false)
/** 短信发送中状态 */
const smsSending = ref(false)
/** 短信验证码倒计时（秒），0 表示可发送 */
const smsCountdown = ref(0)
/** 倒计时定时器引用 */
let smsTimer: ReturnType<typeof setInterval> | null = null

/** 密码登录表单引用 */
const passwordFormRef = ref<FormInstance>()
/** 短信登录表单引用 */
const smsFormRef = ref<FormInstance>()

/** 密码登录表单数据 */
const passwordForm = reactive({
  username: localStorage.getItem('rememberedUsername') || '',
  password: '',
  remember: !!localStorage.getItem('rememberedUsername'),
})

/** 短信登录表单数据 */
const smsForm = reactive({
  phone: '',
  code: '',
})

/**
 * 手机号格式校验器
 * 校验规则：非空 + 1开头+第二位3-9+9位数字
 */
const phoneValidator = (_rule: any, value: string, callback: (err?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

/** 密码登录表单校验规则 */
const passwordRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

/** 短信登录表单校验规则 */
const smsRules = {
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
}

/**
 * 启动短信验证码倒计时
 * 倒计时 60 秒，期间禁止重复发送
 */
function startCountdown() {
  smsCountdown.value = 60
  smsTimer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      if (smsTimer) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }
  }, 1000)
}

/**
 * 发送短信验证码
 * 先校验手机号格式，再调用发送接口，成功后启动倒计时
 */
async function handleSendCode() {
  if (!smsForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }
  smsSending.value = true
  try {
    await sendSmsCode(smsForm.phone)
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch {
    ElMessage.error('验证码发送失败')
  } finally {
    smsSending.value = false
  }
}

/**
 * 处理账号密码登录
 * 校验表单 -> 调用登录接口 -> 存储 Token 和用户信息 -> 处理记住我 -> 跳转目标页
 */
async function handlePasswordLogin() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { data } = await login({
      username: passwordForm.username,
      password: passwordForm.password,
    })
    // 存储 Token 到状态管理和 localStorage
    userStore.setToken(data.accessToken, data.refreshToken)
    // 存储用户信息（含角色和权限）
    userStore.setUserInfo({
      id: 0,
      username: data.username,
      nickname: data.username,
      email: '',
      phone: '',
      avatar: '',
      status: 1,
      roles: data.roles,
      permissions: data.permissions,
      createdAt: '',
      updatedAt: '',
    })

    // 处理"记住我"功能
    if (passwordForm.remember) {
      localStorage.setItem('rememberedUsername', passwordForm.username)
    } else {
      localStorage.removeItem('rememberedUsername')
    }

    // 跳转到登录前的目标页或默认仪表盘
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
    ElMessage.success('登录成功')
  } catch {
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

/**
 * 处理手机短信验证码登录
 * 校验表单 -> 调用短信登录接口 -> 存储 Token 和用户信息 -> 跳转目标页
 */
async function handleSmsLogin() {
  const valid = await smsFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { data } = await loginBySms({
      phone: smsForm.phone,
      code: smsForm.code,
    })
    userStore.setToken(data.accessToken, data.refreshToken)
    userStore.setUserInfo({
      id: 0,
      username: data.username,
      nickname: data.username,
      email: '',
      phone: smsForm.phone,
      avatar: '',
      status: 1,
      roles: data.roles,
      permissions: data.permissions,
      createdAt: '',
      updatedAt: '',
    })

    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
    ElMessage.success('登录成功')
  } catch {
    ElMessage.error('登录失败，请检查验证码')
  } finally {
    loading.value = false
  }
}

/** 组件卸载时清除倒计时定时器，防止内存泄漏 */
onUnmounted(() => {
  if (smsTimer) {
    clearInterval(smsTimer)
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f5ff 50%, #eef0f8 100%);
  padding: 20px;
}

.login-card {
  width: 420px;
  padding: 40px 36px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #ecf5ff, #d9ecff);
  margin-bottom: 16px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d2129;
  margin: 0 0 6px;
}

.login-subtitle {
  font-size: 13px;
  color: #86909c;
  margin: 0;
  letter-spacing: 0.5px;
}

.login-tabs {
  margin-top: 4px;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  color: #86909c;
}

.login-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}

.sms-input-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}

.sms-input-wrapper .el-input {
  flex: 1;
}

.sms-btn {
  flex-shrink: 0;
  width: 120px;
  border-radius: 8px;
}

.login-footer {
  margin-top: 32px;
  color: #c0c4cc;
  font-size: 12px;
}
</style>
