<template>
  <el-dialog
    :model-value="visible"
    title="管理继承"
    width="520px"
    destroy-on-close
    @close="handleClose"
  >
    <div v-if="role" class="role-info">
      <span class="role-label">当前角色：</span>
      <el-tag>{{ role.name }}</el-tag>
      <span class="role-code">{{ role.code }}</span>
    </div>
    <div class="inherit-section">
      <div class="section-title">选择可继承的父角色</div>
      <el-checkbox-group v-model="selectedParentIds" class="parent-list">
        <el-checkbox
          v-for="item in availableParents"
          :key="item.id"
          :value="item.id"
          :label="item.id"
          class="parent-item"
        >
          <span class="parent-name">{{ item.name }}</span>
          <span class="parent-code">{{ item.code }}</span>
        </el-checkbox>
      </el-checkbox-group>
      <el-empty v-if="availableParents.length === 0" description="暂无可继承的父角色" :image-size="60" />
    </div>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { Role } from '@/types'
import { getAllRoles, getRoleInheritances, updateRoleInheritances } from '@/api/role'

const props = defineProps<{
  visible: boolean
  role: Role | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'success'): void
}>()

const submitting = ref(false)
const availableParents = ref<Role[]>([])
const selectedParentIds = ref<number[]>([])

watch(
  () => props.visible,
  async (val) => {
    if (val && props.role) {
      await loadAvailableParents()
      await loadInheritances()
    }
  }
)

async function loadAvailableParents() {
  if (!props.role) return
  try {
    const { data } = await getAllRoles()
    availableParents.value = data.filter((r) => r.id !== props.role!.id)
  } catch {
    availableParents.value = []
  }
}

async function loadInheritances() {
  if (!props.role) return
  try {
    const { data } = await getRoleInheritances(props.role.id)
    selectedParentIds.value = data
  } catch {
    selectedParentIds.value = []
  }
}

function handleClose() {
  selectedParentIds.value = []
  availableParents.value = []
  emit('update:visible', false)
}

async function handleSubmit() {
  if (!props.role) return
  submitting.value = true
  try {
    await updateRoleInheritances(props.role.id, selectedParentIds.value)
    ElMessage.success('继承关系更新成功')
    emit('success')
    handleClose()
  } catch {
    ElMessage.error('继承关系更新失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.role-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.role-label {
  color: #606266;
  font-size: 14px;
}

.role-code {
  color: #909399;
  font-size: 13px;
}

.inherit-section {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
}

.parent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.parent-item {
  display: flex;
  align-items: center;
  height: auto;
}

.parent-name {
  font-size: 14px;
  color: #303133;
}

.parent-code {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
</style>
