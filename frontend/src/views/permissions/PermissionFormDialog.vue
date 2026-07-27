<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑权限' : '新增权限'"
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
      <el-form-item label="权限编码" prop="code">
        <el-input
          v-model="form.code"
          placeholder="请输入权限编码"
          :disabled="isEdit"
          maxlength="100"
        />
      </el-form-item>
      <el-form-item label="权限名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入权限名称" maxlength="50" />
      </el-form-item>
      <el-form-item label="权限类型" prop="type">
        <el-radio-group v-model="form.type" :disabled="isEdit">
          <el-radio value="MENU">菜单</el-radio>
          <el-radio value="BUTTON">按钮</el-radio>
          <el-radio value="API">接口</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="父权限" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="parentPermissionOptions"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="请选择父权限"
          clearable
          check-strictly
          :render-after-expand="false"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item v-if="form.type === 'MENU'" label="路径" prop="path">
        <el-input v-model="form.path" placeholder="请输入菜单路径" maxlength="200" />
      </el-form-item>
      <el-form-item v-if="form.type === 'MENU'" label="图标" prop="icon">
        <el-input v-model="form.icon" placeholder="请输入图标名称" maxlength="50" />
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
import type { Permission, PermissionRequest } from '@/types'
import { getPermissionTree, createPermission, updatePermission } from '@/api/permission'

const props = defineProps<{
  visible: boolean
  editData: Permission | null
  defaultParentId?: number | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const isEdit = ref(false)
const parentPermissionOptions = ref<any[]>([])

const form = reactive<PermissionRequest>({
  name: '',
  code: '',
  type: 'MENU',
  parentId: null,
  path: '',
  icon: '',
  sort: 0,
  status: 1,
})

const rules: FormRules = {
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z:_-]+$/, message: '权限编码仅支持字母、冒号、下划线和连字符', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
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
          type: props.editData.type,
          parentId: props.editData.parentId,
          path: props.editData.path,
          icon: props.editData.icon,
          sort: props.editData.sort,
          status: props.editData.status,
        })
      } else {
        Object.assign(form, {
          name: '',
          code: '',
          type: 'MENU',
          parentId: props.defaultParentId || null,
          path: '',
          icon: '',
          sort: 0,
          status: 1,
        })
      }
      await loadParentPermissions()
    }
  }
)

async function loadParentPermissions() {
  try {
    const { data } = await getPermissionTree()
    const filtered = props.editData
      ? filterNode(data, props.editData.id)
      : data
    parentPermissionOptions.value = filtered
  } catch {
    parentPermissionOptions.value = []
  }
}

function filterNode(nodes: any[], excludeId: number): any[] {
  return nodes
    .filter((n) => n.id !== excludeId)
    .map((n) => ({
      ...n,
      children: n.children ? filterNode(n.children, excludeId) : [],
    }))
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
      await updatePermission(props.editData.id, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createPermission({ ...form })
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
