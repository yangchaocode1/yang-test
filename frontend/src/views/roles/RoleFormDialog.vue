<!--
  角色表单对话框组件
  功能：新增/编辑角色，含角色编码、名称、描述、父角色选择（树形）、排序、状态
  Props: visible - 对话框可见性, editData - 编辑时的角色数据（null 表示新增）
  Events: update:visible - 更新可见性, success - 操作成功后触发
-->
<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑角色' : '新增角色'"
    width="520px"
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
      <el-form-item label="角色编码" prop="code">
        <el-input
          v-model="form.code"
          placeholder="请输入角色编码"
          :disabled="isEdit"
          maxlength="50"
        />
      </el-form-item>
      <el-form-item label="角色名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入角色名称" maxlength="50" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入角色描述"
          :rows="3"
          maxlength="200"
        />
      </el-form-item>
      <el-form-item label="父角色" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="parentRoleOptions"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="请选择父角色"
          clearable
          check-strictly
          :render-after-expand="false"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
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
import type { Role, RoleRequest } from '@/types'
import { createRole, updateRole, getAllRoles } from '@/api/role'

const props = defineProps<{
  visible: boolean
  editData: Role | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const isEdit = ref(false)
const parentRoleOptions = ref<any[]>([])

const form = reactive<RoleRequest>({
  name: '',
  code: '',
  description: '',
  parentId: null,
  sort: 0,
  status: 1,
})

const rules: FormRules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码仅支持大写字母和下划线', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

watch(
  () => props.visible,
  async (val) => {
    if (val) {
      isEdit.value = !!props.editData
      if (props.editData) {
        Object.assign(form, {
          name: props.editData.name,
          code: props.editData.code,
          description: props.editData.description,
          parentId: props.editData.parentId,
          sort: props.editData.sort,
          status: props.editData.status,
        })
      } else {
        Object.assign(form, {
          name: '',
          code: '',
          description: '',
          parentId: null,
          sort: 0,
          status: 1,
        })
      }
      await loadParentRoles()
    }
  }
)

async function loadParentRoles() {
  try {
    const { data } = await getAllRoles()
    const filtered = props.editData
      ? data.filter((r) => r.id !== props.editData!.id)
      : data
    parentRoleOptions.value = buildTree(filtered)
  } catch {
    parentRoleOptions.value = []
  }
}

function buildTree(roles: Role[]): any[] {
  const map = new Map<number, any>()
  const roots: any[] = []
  roles.forEach((r) => map.set(r.id, { ...r, children: [] }))
  roles.forEach((r) => {
    const node = map.get(r.id)!
    if (r.parentId && map.has(r.parentId)) {
      map.get(r.parentId)!.children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

function handleClose() {
  formRef.value?.resetFields()
  emit('update:visible', false)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && props.editData) {
      await updateRole(props.editData.id, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createRole({ ...form })
      ElMessage.success('创建成功')
    }
    emit('success')
    handleClose()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}
</script>
