<!--
  用户管理页面
  功能：用户列表展示、搜索筛选（关键词/状态）、新增/编辑/删除用户、
  批量删除、状态切换（启用/禁用）、密码重置、Excel 导入导出
-->
<template>
  <div class="page-container">
    <!-- 搜索筛选栏 -->
      <el-form :model="query" inline>
        <el-form-item>
          <el-input
            v-model="query.keyword"
            placeholder="用户名/姓名/手机号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
        <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
      <div class="action-right">
        <el-button @click="handleExport">导出 Excel</el-button>
        <el-button @click="importVisible = true">导入 Excel</el-button>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      @selection-change="handleSelectionChange"
      style="width: 100%"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column prop="username" label="用户名" min-width="110" show-overflow-tooltip />
      <el-table-column prop="realName" label="姓名" min-width="100" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机号" min-width="120" show-overflow-tooltip />
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expireTime" label="账号有效期" min-width="110" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.expireTime || '永久' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          <el-button link type="warning" size="small" @click="handleResetPassword(row)">
            重置密码
          </el-button>
          <el-button
            link
            :type="row.status === 1 ? 'danger' : 'success'"
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        background
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <UserFormDialog
      v-model:visible="formVisible"
      :edit-data="editData"
      @success="fetchData"
    />

    <ChangePasswordDialog v-model:visible="passwordVisible" />

    <el-dialog v-model="importVisible" title="导入用户" width="480px" destroy-on-close>
      <el-upload
        ref="uploadRef"
        drag
        action=""
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-exceed="() => ElMessage.warning('只能上传一个文件')"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx / .xls 格式文件</div>
        </template>
      </el-upload>
      <div v-if="importResult" class="import-result">
        <el-alert type="success" :closable="false" :title="`成功导入 ${importResult.successCount} 条`" />
        <el-alert
          v-if="importResult.failCount > 0"
          type="error"
          :closable="false"
          :title="`失败 ${importResult.failCount} 条`"
          class="import-fail-alert"
        />
        <div v-if="importResult.errors?.length" class="import-errors">
          <p v-for="(err, idx) in importResult.errors" :key="idx" class="error-item">
            第{{ err.row }}行：{{ err.message }}
          </p>
        </div>
      </div>
      <template #footer>
        <el-button @click="importVisible = false">关闭</el-button>
        <el-button type="primary" :loading="importing" :disabled="!importFile" @click="handleImport">
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'
import {
  getUserList,
  deleteUser,
  batchDeleteUsers,
  changeUserStatus,
  resetPassword,
  exportUsers,
  importUsers,
} from '@/api/user'
import type { UserListItem, UserQuery, ImportResult } from '@/types'
import UserFormDialog from './UserFormDialog.vue'
import ChangePasswordDialog from './ChangePasswordDialog.vue'

/** 表格加载状态 */
const loading = ref(false)
/** 用户列表数据 */
const tableData = ref<UserListItem[]>([])
/** 数据总条数 */
const total = ref(0)
/** 已选中的用户ID列表（用于批量操作） */
const selectedIds = ref<number[]>([])

/** 查询参数 */
const query = reactive<UserQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: undefined,
})

/** 用户表单对话框可见性 */
const formVisible = ref(false)
/** 当前编辑的用户数据（null 表示新增） */
const editData = ref<UserListItem | null>(null)
/** 修改密码对话框可见性 */
const passwordVisible = ref(false)

/** 导入对话框可见性 */
const importVisible = ref(false)
/** 导入中状态 */
const importing = ref(false)
/** 待导入的文件 */
const importFile = ref<File | null>(null)
/** 导入结果数据 */
const importResult = ref<ImportResult | null>(null)

onMounted(() => {
  fetchData()
})

/** 获取用户列表数据 */
async function fetchData() {
  loading.value = true
  try {
    const { data } = await getUserList(query)
    tableData.value = data.records
    total.value = data.total
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

/** 搜索（重置到第一页） */
function handleSearch() {
  query.page = 1
  fetchData()
}

/** 重置搜索条件 */
function handleReset() {
  query.keyword = ''
  query.status = undefined
  query.pageNum = 1
  fetchData()
}

/** 表格选择变更事件 */
function handleSelectionChange(rows: UserListItem[]) {
  selectedIds.value = rows.map((r) => r.id)
}

/** 新增用户 */
function handleAdd() {
  editData.value = null
  formVisible.value = true
}

/** 编辑用户 */
function handleEdit(row: UserListItem) {
  editData.value = { ...row }
  formVisible.value = true
}

/** 删除单个用户（需确认） */
async function handleDelete(row: UserListItem) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.realName || row.username}」吗？`, '提示', {
      type: 'warning',
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled or error
  }
}

/** 批量删除用户（需确认） */
async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个用户吗？`, '提示', {
      type: 'warning',
    })
    await batchDeleteUsers(selectedIds.value)
    ElMessage.success('批量删除成功')
    fetchData()
  } catch {
    // cancelled or error
  }
}

/** 切换用户状态（启用/禁用） */
async function handleToggleStatus(row: UserListItem) {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${label}用户「${row.realName || row.username}」吗？`, '提示', {
      type: 'warning',
    })
    await changeUserStatus(row.id)
    ElMessage.success(`${label}成功`)
    fetchData()
  } catch {
    // cancelled or error
  }
}

/** 重置用户密码（管理员操作，需确认） */
async function handleResetPassword(row: UserListItem) {
  try {
    await ElMessageBox.confirm(
      `确定重置用户「${row.realName || row.username}」的密码吗？`,
      '提示',
      { type: 'warning' }
    )
    const { data } = await resetPassword(row.id)
    ElMessageBox.alert(
      `密码已重置，新默认密码为：${data.defaultPassword}`,
      '重置成功',
      { type: 'success' }
    )
  } catch {
    // cancelled or error
  }
}

/** 导出用户列表为 Excel 文件 */
async function handleExport() {
  try {
    const blob = await exportUsers(query)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `用户列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    // error handled by interceptor
  }
}

/** 导入文件选择变更事件 */
function handleFileChange(file: UploadFile) {
  if (file.raw) {
    importFile.value = file.raw
    importResult.value = null
  }
}

/** 执行导入操作 */
async function handleImport() {
  if (!importFile.value) return
  importing.value = true
  try {
    const { data } = await importUsers(importFile.value)
    importResult.value = data
    if (data.failCount === 0) {
      ElMessage.success(`导入成功，共 ${data.successCount} 条`)
      fetchData()
    }
  } catch {
    // error handled by interceptor
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.page-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.search-bar {
  margin-bottom: 16px;
}

.search-bar :deep(.el-form-item) {
  margin-bottom: 0;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.action-left,
.action-right {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.import-result {
  margin-top: 16px;
}

.import-fail-alert {
  margin-top: 8px;
}

.import-errors {
  margin-top: 8px;
  max-height: 150px;
  overflow-y: auto;
}

.error-item {
  margin: 4px 0;
  font-size: 13px;
  color: #f56c6c;
}
</style>
