<template>
  <div class="page-container">
    <div class="search-bar">
      <el-select
        v-model="selectedModelId"
        placeholder="选择数据模型"
        style="width: 220px"
        @change="handleModelChange"
      >
        <el-option
          v-for="m in modelList"
          :key="m.id"
          :label="m.modelName"
          :value="m.id"
        />
      </el-select>
    </div>

    <template v-if="currentModel">
      <div class="dynamic-search">
        <el-form :inline="true" :model="searchConditions" @submit.prevent="handleSearch">
          <el-form-item
            v-for="field in searchableFields"
            :key="field.fieldCode"
            :label="field.fieldName"
          >
            <el-input
              v-if="field.fieldType === 'TEXT'"
              v-model="searchConditions[field.fieldCode]"
              :placeholder="`搜索${field.fieldName}`"
              clearable
              style="width: 180px"
            />
            <el-input-number
              v-else-if="field.fieldType === 'NUMBER'"
              v-model="searchConditions[field.fieldCode]"
              :placeholder="`搜索${field.fieldName}`"
              controls-position="right"
              style="width: 180px"
            />
            <el-date-picker
              v-else-if="field.fieldType === 'DATE'"
              v-model="searchConditions[field.fieldCode]"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px"
            />
            <el-select
              v-else-if="field.fieldType === 'SELECT'"
              v-model="searchConditions[field.fieldCode]"
              :placeholder="`选择${field.fieldName}`"
              clearable
              style="width: 180px"
            >
              <el-option
                v-for="opt in field.selectOptions || []"
                :key="opt"
                :label="opt"
                :value="opt"
              />
            </el-select>
            <el-select
              v-else-if="field.fieldType === 'REFERENCE'"
              v-model="searchConditions[field.fieldCode]"
              :placeholder="`选择${field.fieldName}`"
              clearable
              style="width: 180px"
            >
              <el-option
                v-for="item in referenceDataMap[field.fieldCode] || []"
                :key="item.id"
                :label="getReferenceLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleResetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="action-bar">
        <el-button type="primary" @click="handleAdd">新增数据</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column
          v-for="field in displayFields"
          :key="field.fieldCode"
          :prop="`data.${field.fieldCode}`"
          :label="field.fieldName"
          :min-width="getFieldMinWidth(field)"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <template v-if="field.fieldType === 'REFERENCE'">
              {{ getReferenceDisplayValue(row.data[field.fieldCode], field) }}
            </template>
            <template v-else-if="field.fieldType === 'DATE'">
              {{ row.data[field.fieldCode] || '-' }}
            </template>
            <template v-else>
              {{ row.data[field.fieldCode] ?? '-' }}
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button link type="primary" @click="handleViewReferences(row)">查看关联</el-button>
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
    </template>

    <el-empty v-else description="请选择数据模型" />

    <BusinessDataFormDialog
      v-model:visible="formDialogVisible"
      :model="currentModel"
      :data-id="currentDataId"
      @success="handleFormSuccess"
    />

    <DataReferenceDialog
      v-model:visible="referenceDialogVisible"
      :model-id="selectedModelId"
      :data-id="currentDataId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDataModelList } from '@/api/dataModel'
import { getBusinessDataPage, deleteBusinessData, batchDeleteBusinessData, getReferenceModelData } from '@/api/businessData'
import BusinessDataFormDialog from './components/BusinessDataFormDialog.vue'
import DataReferenceDialog from './components/DataReferenceDialog.vue'
import type { DataModel, DataModelField, BusinessDataItem } from '@/types'

const loading = ref(false)
const tableData = ref<BusinessDataItem[]>([])
const modelList = ref<DataModel[]>([])
const selectedModelId = ref<number | null>(null)
const formDialogVisible = ref(false)
const referenceDialogVisible = ref(false)
const currentDataId = ref<number | null>(null)
const selectedIds = ref<number[]>([])
const searchConditions = ref<Record<string, any>>({})
const referenceDataMap = ref<Record<string, BusinessDataItem[]>>({})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const currentModel = computed(() => {
  return modelList.value.find((m) => m.id === selectedModelId.value) || null
})

const searchableFields = computed(() => {
  if (!currentModel.value) return []
  return currentModel.value.fields.filter((f) => f.fieldType !== 'SELECT' || (f.selectOptions && f.selectOptions.length > 0))
})

const displayFields = computed(() => {
  if (!currentModel.value) return []
  return [...currentModel.value.fields].sort((a, b) => a.sort - b.sort)
})

onMounted(async () => {
  await fetchModelList()
})

async function fetchModelList() {
  try {
    const { data } = await getDataModelList()
    modelList.value = data
  } catch {
    modelList.value = []
  }
}

async function handleModelChange() {
  searchConditions.value = {}
  pagination.page = 1
  referenceDataMap.value = {}
  if (currentModel.value) {
    await loadReferenceData()
    await fetchData()
  } else {
    tableData.value = []
    pagination.total = 0
  }
}

async function loadReferenceData() {
  if (!currentModel.value) return
  const refFields = currentModel.value.fields.filter((f) => f.fieldType === 'REFERENCE' && f.referenceModelId)
  for (const field of refFields) {
    try {
      const { data } = await getReferenceModelData(field.referenceModelId!)
      referenceDataMap.value[field.fieldCode] = data
    } catch {
      referenceDataMap.value[field.fieldCode] = []
    }
  }
}

async function fetchData() {
  if (!selectedModelId.value) return
  loading.value = true
  try {
    const conditions: Record<string, any> = {}
    for (const [key, val] of Object.entries(searchConditions.value)) {
      if (val !== null && val !== undefined && val !== '') {
        conditions[key] = val
      }
    }
    const { data } = await getBusinessDataPage({
      page: pagination.page,
      pageSize: pagination.pageSize,
      modelId: selectedModelId.value,
      conditions: Object.keys(conditions).length > 0 ? conditions : undefined,
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

function handleResetSearch() {
  searchConditions.value = {}
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  currentDataId.value = null
  formDialogVisible.value = true
}

function handleEdit(row: BusinessDataItem) {
  currentDataId.value = row.id
  formDialogVisible.value = true
}

async function handleDelete(row: BusinessDataItem) {
  try {
    await ElMessageBox.confirm('确定要删除该数据吗？', '提示', { type: 'warning' })
    await deleteBusinessData(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    if (arguments[0] !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条数据吗？`, '提示', { type: 'warning' })
    await batchDeleteBusinessData(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchData()
  } catch {
    if (arguments[0] !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

function handleSelectionChange(rows: BusinessDataItem[]) {
  selectedIds.value = rows.map((r) => r.id)
}

function handleViewReferences(row: BusinessDataItem) {
  currentDataId.value = row.id
  referenceDialogVisible.value = true
}

function handleFormSuccess() {
  fetchData()
}

function getFieldMinWidth(field: DataModelField) {
  const widthMap: Record<string, number> = {
    TEXT: 150,
    NUMBER: 120,
    DATE: 140,
    SELECT: 120,
    REFERENCE: 150,
  }
  return widthMap[field.fieldType] || 120
}

function getReferenceLabel(item: BusinessDataItem) {
  const data = item.data
  const keys = Object.keys(data)
  if (keys.length > 0) {
    return `${item.id} - ${Object.values(data).find((v) => v !== null && v !== undefined) || item.id}`
  }
  return String(item.id)
}

function getReferenceDisplayValue(value: any, field: DataModelField) {
  if (value === null || value === undefined) return '-'
  const refData = referenceDataMap.value[field.fieldCode] || []
  const found = refData.find((item) => item.id === value)
  return found ? getReferenceLabel(found) : value
}
</script>

<style scoped>
.page-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.search-bar {
  margin-bottom: 16px;
}

.dynamic-search {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafbfc;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.action-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
