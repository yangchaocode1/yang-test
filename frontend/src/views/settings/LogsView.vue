<template>
  <div class="logs-container">
    <div class="filter-bar">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="操作人">
          <el-input v-model="queryForm.operator" placeholder="请输入操作人" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryForm.operationType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="查询" value="GET" />
            <el-option label="新增" value="POST" />
            <el-option label="修改" value="PUT" />
            <el-option label="删除" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="queryForm.module" placeholder="请选择" clearable style="width: 140px">
            <el-option label="用户管理" value="用户管理" />
            <el-option label="角色管理" value="角色管理" />
            <el-option label="权限管理" value="权限管理" />
            <el-option label="数据模型" value="数据模型" />
            <el-option label="系统设置" value="系统设置" />
            <el-option label="认证" value="认证" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-toolbar">
      <el-button type="success" @click="handleExport">导出日志</el-button>
    </div>

    <el-table :data="logList" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="operator" label="操作人" width="110" />
      <el-table-column prop="operation" label="操作" width="160" />
      <el-table-column prop="module" label="模块" width="110" />
      <el-table-column prop="method" label="请求方法" width="100">
        <template #default="{ row }">
          <el-tag :type="methodTagType(row.method)" size="small">{{ row.method }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ip" label="IP地址" width="140" />
      <el-table-column prop="duration" label="执行时长(ms)" width="120" />
      <el-table-column prop="operateTime" label="操作时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchLogs"
        @current-change="fetchLogs"
      />
    </div>

    <el-dialog v-model="detailVisible" title="日志详情" width="640px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ currentLog.operation }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ currentLog.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.operateTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-content">{{ formatJson(currentLog.params) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <pre class="json-content">{{ formatJson(currentLog.result) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLogList, exportLogs } from '@/api/log'
import type { OperationLog, OperationLogQuery } from '@/types'

const loading = ref(false)
const logList = ref<OperationLog[]>([])
const total = ref(0)
const detailVisible = ref(false)
const dateRange = ref<[string, string] | null>(null)

const queryForm = reactive<OperationLogQuery>({
  page: 1,
  pageSize: 10,
  operator: '',
  operationType: '',
  module: '',
  startTime: '',
  endTime: '',
})

const currentLog = reactive<Partial<OperationLog>>({})

const mockLogs: OperationLog[] = [
  { id: 1, operator: 'admin', operation: '登录系统', module: '认证', method: 'POST', ip: '192.168.1.100', duration: 120, operateTime: '2026-04-22 10:30:00', params: '{"username":"admin"}', result: '{"code":200,"message":"success"}' },
  { id: 2, operator: 'admin', operation: '创建用户', module: '用户管理', method: 'POST', ip: '192.168.1.100', duration: 85, operateTime: '2026-04-22 10:25:00', params: '{"username":"newuser","email":"new@test.com"}', result: '{"code":200,"message":"success"}' },
  { id: 3, operator: 'zhangsan', operation: '修改角色', module: '角色管理', method: 'PUT', ip: '192.168.1.101', duration: 56, operateTime: '2026-04-22 10:20:00', params: '{"id":3,"name":"editor"}', result: '{"code":200,"message":"success"}' },
  { id: 4, operator: 'admin', operation: '删除数据模型', module: '数据模型', method: 'DELETE', ip: '192.168.1.100', duration: 43, operateTime: '2026-04-22 10:15:00', params: '{"id":5}', result: '{"code":200,"message":"success"}' },
  { id: 5, operator: 'lisi', operation: '查看用户列表', module: '用户管理', method: 'GET', ip: '192.168.1.102', duration: 32, operateTime: '2026-04-22 10:10:00', params: '{"page":1,"pageSize":10}', result: '{"code":200,"data":{"list":[],"total":0}}' },
  { id: 6, operator: 'admin', operation: '修改安全设置', module: '系统设置', method: 'PUT', ip: '192.168.1.100', duration: 67, operateTime: '2026-04-22 09:55:00', params: '{"minPasswordLength":10}', result: '{"code":200,"message":"success"}' },
  { id: 7, operator: 'zhangsan', operation: '分配权限', module: '权限管理', method: 'POST', ip: '192.168.1.101', duration: 78, operateTime: '2026-04-22 09:50:00', params: '{"roleId":3,"permissions":[1,2,3]}', result: '{"code":200,"message":"success"}' },
  { id: 8, operator: 'admin', operation: '创建数据模型', module: '数据模型', method: 'POST', ip: '192.168.1.100', duration: 98, operateTime: '2026-04-22 09:45:00', params: '{"name":"Order","fields":[]}', result: '{"code":200,"message":"success"}' },
  { id: 9, operator: 'lisi', operation: '修改通知设置', module: '系统设置', method: 'PUT', ip: '192.168.1.102', duration: 45, operateTime: '2026-04-22 09:40:00', params: '{"emailEnabled":true}', result: '{"code":200,"message":"success"}' },
  { id: 10, operator: 'admin', operation: '修改界面配置', module: '系统设置', method: 'PUT', ip: '192.168.1.100', duration: 38, operateTime: '2026-04-22 09:35:00', params: '{"theme":"dark"}', result: '{"code":200,"message":"success"}' },
]

function methodTagType(method: string) {
  const map: Record<string, string> = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return map[method] || 'info'
}

function formatJson(str: string | undefined) {
  if (!str) return '-'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

function handleDateChange(val: [string, string] | null) {
  if (val) {
    queryForm.startTime = val[0]
    queryForm.endTime = val[1]
  } else {
    queryForm.startTime = ''
    queryForm.endTime = ''
  }
}

function handleSearch() {
  queryForm.page = 1
  fetchLogs()
}

function handleReset() {
  queryForm.operator = ''
  queryForm.operationType = ''
  queryForm.module = ''
  queryForm.startTime = ''
  queryForm.endTime = ''
  dateRange.value = null
  queryForm.page = 1
  fetchLogs()
}

function handleDetail(row: OperationLog) {
  Object.assign(currentLog, row)
  detailVisible.value = true
}

async function handleExport() {
  try {
    const res = await exportLogs(queryForm)
    const blob = new Blob([res as any], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

async function fetchLogs() {
  loading.value = true
  try {
    const { data } = await getLogList(queryForm)
    logList.value = data.list
    total.value = data.total
  } catch {
    logList.value = mockLogs
    total.value = mockLogs.length
  } finally {
    loading.value = false
  }
}

onMounted(fetchLogs)
</script>

<style scoped>
.logs-container {
  padding: 0;
}

.filter-bar {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 12px;
  margin-right: 16px;
}

.table-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.json-content {
  margin: 0;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}
</style>
