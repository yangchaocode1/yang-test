<!--
  角色管理页面
  功能：角色列表展示、搜索筛选（关键词/状态）、新增/编辑/删除角色、分配权限、管理继承关系
-->
<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">角色管理</h2>
    </div>

    <div class="search-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索角色编码/名称"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select
        v-model="query.status"
        placeholder="状态筛选"
        clearable
        style="width: 140px"
        @change="handleSearch"
      >
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border class="data-table">
      <el-table-column prop="code" label="角色编码" min-width="140" />
      <el-table-column prop="name" label="角色名称" min-width="120" />
      <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column prop="parentName" label="父角色" min-width="120">
        <template #default="{ row }">
          {{ row.parentName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="260" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" size="small" @click="handleAssignPermission(row)">分配权限</el-button>
          <el-button link type="primary" size="small" @click="handleManageInheritance(row)">管理继承</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <RoleFormDialog
      v-model:visible="roleFormVisible"
      :edit-data="currentRole"
      @success="fetchData"
    />

    <RolePermissionDialog
      v-model:visible="permissionDialogVisible"
      :role="currentRole"
      @success="fetchData"
    />

    <RoleInheritanceDialog
      v-model:visible="inheritanceDialogVisible"
      :role="currentRole"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Role, RolePageRequest } from '@/types'
import { getRoleList, deleteRole } from '@/api/role'
import RoleFormDialog from './RoleFormDialog.vue'
import RolePermissionDialog from './RolePermissionDialog.vue'
import RoleInheritanceDialog from './RoleInheritanceDialog.vue'

const loading = ref(false)
const tableData = ref<Role[]>([])
const total = ref(0)
const query = reactive<RolePageRequest>({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: null,
})

const roleFormVisible = ref(false)
const permissionDialogVisible = ref(false)
const inheritanceDialogVisible = ref(false)
const currentRole = ref<Role | null>(null)

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const { data } = await getRoleList(query)
    tableData.value = data.list
    total.value = data.total
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchData()
}

function handleReset() {
  query.keyword = ''
  query.status = null
  query.page = 1
  fetchData()
}

function handleAdd() {
  currentRole.value = null
  roleFormVisible.value = true
}

function handleEdit(row: Role) {
  currentRole.value = { ...row }
  roleFormVisible.value = true
}

function handleAssignPermission(row: Role) {
  currentRole.value = { ...row }
  permissionDialogVisible.value = true
}

function handleManageInheritance(row: Role) {
  currentRole.value = { ...row }
  inheritanceDialogVisible.value = true
}

async function handleDelete(row: Role) {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.name}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled or error
  }
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

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.spacer {
  flex: 1;
}

.data-table {
  width: 100%;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
