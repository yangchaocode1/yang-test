<!--
  用户表单对话框组件
  功能：新增/编辑用户，含用户名、密码（仅新增）、姓名、手机号、邮箱、角色选择、状态、有效期
  Props: visible - 对话框可见性, editData - 编辑时的用户数据（null 表示新增）
  Events: update:visible - 更新可见性, success - 操作成功后触发
-->
<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="560px"
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="90px"
      label-position="right"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          placeholder="请输入用户名"
          :disabled="isEdit"
          maxlength="20"
        />
      </el-form-item>

      <el-form-item v-if="!isEdit" label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          show-password
          maxlength="20"
        />
      </el-form-item>

      <el-form-item label="姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="请输入姓名" maxlength="20" />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
      </el-form-item>

      <el-form-item label="角色" prop="roleIds">
        <el-select
          v-model="form.roleIds"
          multiple
          placeholder="请选择角色"
          style="width: 100%"
        >
          <el-option
            v-for="role in roleOptions"
            :key="role.id"
            :label="role.roleName"
            :value="role.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="有效期" prop="expireTime">
        <el-date-picker
          v-model="form.expireTime"
          type="date"
          placeholder="请选择有效期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          :disabled-date="disablePastDate"
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
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createUser, updateUser, getRoleOptions } from '@/api/user'
import type { UserListItem, RoleOption, CreateUserRequest, UpdateUserRequest } from '@/types'

const props = defineProps<{
  visible: boolean
  editData?: UserListItem | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const roleOptions = ref<RoleOption[]>([])

const isEdit = ref(false)

const defaultForm = () => ({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  roleIds: [] as number[],
  status: 1,
  expireTime: '',
})

const form = reactive(defaultForm())

const phoneValidator = (_rule: any, value: string, callback: (err?: Error) => void) => {
  if (!value) {
    callback()
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

const emailValidator = (_rule: any, value: string, callback: (err?: Error) => void) => {
  if (!value) {
    callback()
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  email: [{ validator: emailValidator, trigger: 'blur' }],
  roleIds: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function disablePastDate(date: Date) {
  return date.getTime() < Date.now() - 86400000
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      loadRoleOptions()
      if (props.editData) {
        isEdit.value = true
        Object.assign(form, {
          username: props.editData.username,
          password: '',
          realName: props.editData.realName,
          phone: props.editData.phone,
          email: props.editData.email,
          roleIds: props.editData.roles?.map(r => r.id) || [],
          status: props.editData.status,
          expireTime: props.editData.expireTime,
        })
      } else {
        isEdit.value = false
        Object.assign(form, defaultForm())
      }
      formRef.value?.clearValidate()
    }
  }
)

async function loadRoleOptions() {
  try {
    const { data } = await getRoleOptions()
    roleOptions.value = data
  } catch {
    roleOptions.value = []
  }
}

function handleClose() {
  emit('update:visible', false)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && props.editData) {
      const payload: UpdateUserRequest = {
        realName: form.realName,
        phone: form.phone,
        email: form.email,
        roleIds: form.roleIds,
        status: form.status,
        expireTime: form.expireTime,
      }
      await updateUser(props.editData.id, payload)
      ElMessage.success('编辑成功')
    } else {
      const payload: CreateUserRequest = {
        username: form.username,
        password: form.password,
        realName: form.realName,
        phone: form.phone,
        email: form.email,
        roleIds: form.roleIds,
        status: form.status,
        expireTime: form.expireTime,
      }
      await createUser(payload)
      ElMessage.success('新增成功')
    }
    emit('success')
    handleClose()
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>
