<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索模型编码或名称"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="primary" @click="handleAdd">新增数据模型</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="modelCode" label="模型编码" width="150" />
      <el-table-column prop="modelName" label="模型名称" width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="tableName" label="表名" width="150" />
      <el-table-column prop="fieldCount" label="字段数量" width="100" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleViewFields(row)">查看字段</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <DataModelFormDialog
      v-model:visible="formDialogVisible"
      :model-id="currentModelId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDataModelPage, deleteDataModel } from '@/api/dataModel'
import DataModelFormDialog from './components/DataModelFormDialog.vue'
import type { DataModel } from '@/types'

const loading = ref(false)
const tableData = ref<DataModel[]>([])
const formDialogVisible = ref(false)
const currentModelId = ref<number | null>(null)

const searchForm = reactive({
  keyword: '',
  status: null as number | null,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const { data } = await getDataModelPage({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status,
    })
    tableData.value = data.list
    pagination.total = data.total
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  currentModelId.value = null
  formDialogVisible.value = true
}

function handleEdit(row: DataModel) {
  currentModelId.value = row.id
  formDialogVisible.value = true
}

function handleViewFields(row: DataModel) {
  currentModelId.value = row.id
  formDialogVisible.value = true
}

async function handleDelete(row: DataModel) {
  try {
    await ElMessageBox.confirm(`确定要删除数据模型"${row.modelName}"吗？`, '提示', {
      type: 'warning',
    })
    await deleteDataModel(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    if (arguments[0] !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function handleFormSuccess() {
  fetchData()
}
</script>

<style scoped>
.page-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
