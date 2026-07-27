<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">权限管理</h2>
    </div>

    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="权限配置" name="permissions">
        <div class="permission-layout">
          <div class="tree-panel">
            <div class="panel-header">
              <span class="panel-title">权限树</span>
              <div class="panel-actions">
                <el-button size="small" @click="toggleExpandAll">
                  {{ isAllExpanded ? '全部折叠' : '全部展开' }}
                </el-button>
                <el-button type="primary" size="small" @click="handleAddPermission()">新增权限</el-button>
              </div>
            </div>
            <el-tree
              ref="treeRef"
              :data="permissionTree"
              :props="{ label: 'name', children: 'children' }"
              node-key="id"
              highlight-current
              :default-expand-all="isAllExpanded"
              class="permission-tree"
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <span class="tree-node">
                  <span class="node-label">{{ data.name }}</span>
                  <el-tag size="small" :type="getTypeTag(data.type)" class="type-tag">
                    {{ getTypeLabel(data.type) }}
                  </el-tag>
                  <span class="node-actions">
                    <el-button link type="primary" size="small" @click.stop="handleAddPermission(data.id)">新增子权限</el-button>
                    <el-button link type="primary" size="small" @click.stop="handleEditPermission(data)">编辑</el-button>
                    <el-button link type="danger" size="small" @click.stop="handleDeletePermission(data)">删除</el-button>
                  </span>
                </span>
              </template>
            </el-tree>
            <el-empty v-if="permissionTree.length === 0 && !treeLoading" description="暂无权限数据" />
          </div>

          <div class="detail-panel">
            <template v-if="selectedPermission">
              <div class="panel-header">
                <span class="panel-title">权限详情</span>
              </div>
              <el-descriptions :column="1" border class="detail-desc">
                <el-descriptions-item label="权限编码">{{ selectedPermission.code }}</el-descriptions-item>
                <el-descriptions-item label="权限名称">{{ selectedPermission.name }}</el-descriptions-item>
                <el-descriptions-item label="权限类型">
                  <el-tag size="small" :type="getTypeTag(selectedPermission.type)">
                    {{ getTypeLabel(selectedPermission.type) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="父权限">{{ selectedPermission.parentId || '无' }}</el-descriptions-item>
                <el-descriptions-item label="路径">{{ selectedPermission.path || '-' }}</el-descriptions-item>
                <el-descriptions-item label="图标">{{ selectedPermission.icon || '-' }}</el-descriptions-item>
                <el-descriptions-item label="排序">{{ selectedPermission.sort }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="selectedPermission.status === 1 ? 'success' : 'danger'" size="small">
                    {{ selectedPermission.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
              <div class="detail-actions">
                <el-button type="primary" @click="handleEditPermission(selectedPermission)">编辑</el-button>
              </div>
            </template>
            <el-empty v-else description="请在左侧选择权限节点查看详情" />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="审计日志" name="audit">
        <div class="audit-section">
          <div class="search-bar">
            <el-input
              v-model="auditQuery.operator"
              placeholder="操作人"
              clearable
              style="width: 140px"
            />
            <el-select
              v-model="auditQuery.module"
              placeholder="模块"
              clearable
              style="width: 140px"
            >
              <el-option label="角色管理" value="ROLE" />
              <el-option label="权限管理" value="PERMISSION" />
              <el-option label="用户管理" value="USER" />
            </el-select>
            <el-select
              v-model="auditQuery.operationType"
              placeholder="操作类型"
              clearable
              style="width: 140px"
            >
              <el-option label="创建" value="CREATE" />
              <el-option label="更新" value="UPDATE" />
              <el-option label="删除" value="DELETE" />
              <el-option label="分配" value="ASSIGN" />
            </el-select>
            <el-date-picker
              v-model="auditTimeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px"
            />
            <el-button type="primary" @click="handleAuditSearch">查询</el-button>
            <el-button @click="handleAuditReset">重置</el-button>
          </div>

          <el-table :data="auditData" v-loading="auditLoading" stripe border>
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="createdAt" label="操作时间" min-width="170" />
            <el-table-column prop="module" label="模块" width="120">
              <template #default="{ row }">
                {{ getModuleLabel(row.module) }}
              </template>
            </el-table-column>
            <el-table-column prop="operationType" label="操作类型" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="getOpTypeTag(row.operationType)">
                  {{ getOpTypeLabel(row.operationType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="targetName" label="目标对象" min-width="140" show-overflow-tooltip />
            <el-table-column prop="content" label="变更内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="ip" label="IP地址" width="140" />
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="auditQuery.page"
              v-model:page-size="auditQuery.pageSize"
              :total="auditTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchAuditData"
              @current-change="fetchAuditData"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <PermissionFormDialog
      v-model:visible="permissionFormVisible"
      :edit-data="editingPermission"
      :default-parent-id="defaultParentId"
      @success="handlePermissionFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Permission, AuditLog, AuditLogPageRequest } from '@/types'
import { getPermissionTree, deletePermission, getAuditLogs } from '@/api/permission'
import PermissionFormDialog from './PermissionFormDialog.vue'

const activeTab = ref('permissions')
const treeRef = ref<any>()
const treeLoading = ref(false)
const isAllExpanded = ref(true)
const permissionTree = ref<Permission[]>([])
const selectedPermission = ref<Permission | null>(null)

const permissionFormVisible = ref(false)
const editingPermission = ref<Permission | null>(null)
const defaultParentId = ref<number | null>(null)

const auditLoading = ref(false)
const auditData = ref<AuditLog[]>([])
const auditTotal = ref(0)
const auditTimeRange = ref<string[]>([])
const auditQuery = reactive<AuditLogPageRequest>({
  page: 1,
  pageSize: 10,
  operator: '',
  module: '',
  operationType: '',
  startTime: '',
  endTime: '',
})

onMounted(() => {
  fetchPermissionTree()
  fetchAuditData()
})

async function fetchPermissionTree() {
  treeLoading.value = true
  try {
    const { data } = await getPermissionTree()
    permissionTree.value = data
  } catch {
    permissionTree.value = []
  } finally {
    treeLoading.value = false
  }
}

function toggleExpandAll() {
  isAllExpanded.value = !isAllExpanded.value
  const tree = treeRef.value
  if (!tree) return
  const nodes = tree.store._getAllNodes()
  nodes.forEach((node: any) => {
    node.expanded = isAllExpanded.value
  })
}

function handleNodeClick(data: Permission) {
  selectedPermission.value = data
}

function getTypeLabel(type: string) {
  const map: Record<string, string> = { MENU: '菜单', BUTTON: '按钮', API: '接口' }
  return map[type] || type
}

function getTypeTag(type: string) {
  const map: Record<string, string> = { MENU: '', BUTTON: 'warning', API: 'success' }
  return map[type] || 'info'
}

function handleAddPermission(parentId?: number) {
  editingPermission.value = null
  defaultParentId.value = parentId || null
  permissionFormVisible.value = true
}

function handleEditPermission(data: Permission) {
  editingPermission.value = { ...data }
  defaultParentId.value = null
  permissionFormVisible.value = true
}

async function handleDeletePermission(data: Permission) {
  try {
    await ElMessageBox.confirm(`确定删除权限「${data.name}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await deletePermission(data.id)
    ElMessage.success('删除成功')
    if (selectedPermission.value?.id === data.id) {
      selectedPermission.value = null
    }
    fetchPermissionTree()
  } catch {
    // cancelled or error
  }
}

function handlePermissionFormSuccess() {
  fetchPermissionTree()
  selectedPermission.value = null
}

async function fetchAuditData() {
  auditLoading.value = true
  try {
    const params = { ...auditQuery }
    if (auditTimeRange.value && auditTimeRange.value.length === 2) {
      params.startTime = auditTimeRange.value[0]
      params.endTime = auditTimeRange.value[1]
    } else {
      params.startTime = ''
      params.endTime = ''
    }
    const { data } = await getAuditLogs(params)
    auditData.value = data.list
    auditTotal.value = data.total
  } catch {
    auditData.value = []
    auditTotal.value = 0
  } finally {
    auditLoading.value = false
  }
}

function handleAuditSearch() {
  auditQuery.page = 1
  fetchAuditData()
}

function handleAuditReset() {
  auditQuery.operator = ''
  auditQuery.module = ''
  auditQuery.operationType = ''
  auditQuery.startTime = ''
  auditQuery.endTime = ''
  auditQuery.page = 1
  auditTimeRange.value = []
  fetchAuditData()
}

function getModuleLabel(module: string) {
  const map: Record<string, string> = { ROLE: '角色管理', PERMISSION: '权限管理', USER: '用户管理' }
  return map[module] || module
}

function getOpTypeLabel(type: string) {
  const map: Record<string, string> = { CREATE: '创建', UPDATE: '更新', DELETE: '删除', ASSIGN: '分配' }
  return map[type] || type
}

function getOpTypeTag(type: string) {
  const map: Record<string, string> = { CREATE: 'success', UPDATE: '', DELETE: 'danger', ASSIGN: 'warning' }
  return map[type] || 'info'
}
</script>

<style scoped>
.page-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
}

.main-tabs {
  margin-top: 4px;
}

.permission-layout {
  display: flex;
  gap: 16px;
  min-height: 500px;
}

.tree-panel {
  width: 400px;
  flex-shrink: 0;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.panel-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.panel-actions {
  display: flex;
  gap: 8px;
}

.permission-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  padding-right: 8px;
}

.node-label {
  flex: 1;
  font-size: 14px;
}

.type-tag {
  transform: scale(0.85);
  flex-shrink: 0;
}

.node-actions {
  display: none;
  gap: 2px;
  flex-shrink: 0;
}

.tree-node:hover .node-actions {
  display: flex;
}

.detail-panel {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
}

.detail-desc {
  margin: 16px;
}

.detail-actions {
  padding: 0 16px 16px;
}

.audit-section {
  margin-top: 8px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
