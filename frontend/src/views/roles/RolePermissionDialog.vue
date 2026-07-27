<template>
  <el-dialog
    :model-value="visible"
    title="分配权限"
    width="560px"
    destroy-on-close
    @close="handleClose"
  >
    <div v-if="role" class="role-info">
      <span class="role-label">当前角色：</span>
      <el-tag>{{ role.name }}</el-tag>
      <span class="role-code">{{ role.code }}</span>
    </div>
    <el-tree
      ref="treeRef"
      :data="permissionTree"
      :props="{ label: 'name', children: 'children' }"
      show-checkbox
      node-key="id"
      :default-checked-keys="checkedKeys"
      :default-expand-all="true"
      class="permission-tree"
    >
      <template #default="{ data }">
        <span class="tree-node-label">
          <span>{{ data.name }}</span>
          <el-tag size="small" :type="getTypeTag(data.type)" class="type-tag">
            {{ getTypeLabel(data.type) }}
          </el-tag>
        </span>
      </template>
    </el-tree>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { Role, Permission } from '@/types'
import { getPermissionTree, getRolePermissions, assignRolePermissions } from '@/api/role'

const props = defineProps<{
  visible: boolean
  role: Role | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'success'): void
}>()

const treeRef = ref<any>()
const submitting = ref(false)
const permissionTree = ref<Permission[]>([])
const checkedKeys = ref<number[]>([])

watch(
  () => props.visible,
  async (val) => {
    if (val && props.role) {
      await loadPermissionTree()
      await loadRolePermissions()
    }
  }
)

async function loadPermissionTree() {
  try {
    const { data } = await getPermissionTree()
    permissionTree.value = data
  } catch {
    permissionTree.value = []
  }
}

async function loadRolePermissions() {
  if (!props.role) return
  try {
    const { data } = await getRolePermissions(props.role.id)
    checkedKeys.value = getLeafKeys(permissionTree.value, new Set(data))
  } catch {
    checkedKeys.value = []
  }
}

function getLeafKeys(nodes: Permission[], idSet: Set<number>): number[] {
  const leafKeys: number[] = []
  function walk(list: Permission[]) {
    for (const node of list) {
      if (node.children && node.children.length > 0) {
        walk(node.children)
      } else {
        if (idSet.has(node.id)) {
          leafKeys.push(node.id)
        }
      }
    }
  }
  walk(nodes)
  return leafKeys
}

function getTypeLabel(type: string) {
  const map: Record<string, string> = { MENU: '菜单', BUTTON: '按钮', API: '接口' }
  return map[type] || type
}

function getTypeTag(type: string) {
  const map: Record<string, string> = { MENU: '', BUTTON: 'warning', API: 'success' }
  return map[type] || 'info'
}

function handleClose() {
  checkedKeys.value = []
  permissionTree.value = []
  emit('update:visible', false)
}

async function handleSubmit() {
  if (!props.role) return
  submitting.value = true
  try {
    const checkedNodes = treeRef.value.getCheckedNodes(false, true)
    const permissionIds = checkedNodes.map((n: any) => n.id)
    await assignRolePermissions(props.role.id, permissionIds)
    ElMessage.success('权限分配成功')
    emit('success')
    handleClose()
  } catch {
    ElMessage.error('权限分配失败')
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

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 8px;
}

.tree-node-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.type-tag {
  transform: scale(0.85);
}
</style>
