<!--
  仪表盘页面
  功能：展示系统统计卡片（用户总数、角色数量、数据模型数量、今日操作次数）、
  快捷操作入口、最近操作日志列表
  统计数据支持变化率展示，接口异常时使用模拟数据兜底
-->
<template>
  <div class="dashboard-container">
    <!-- 统计卡片行 -->
    <div class="stats-row">
      <el-card v-for="stat in statsCards" :key="stat.label" class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" :style="{ background: stat.bgColor }">
            <el-icon :size="24" :color="stat.iconColor"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div v-if="stat.change !== undefined" class="stat-change" :class="stat.change >= 0 ? 'up' : 'down'">
              <el-icon v-if="stat.change >= 0"><Top /></el-icon>
              <el-icon v-else><Bottom /></el-icon>
              {{ Math.abs(stat.change) }}%
              <span class="change-text">较昨日</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <div class="action-row">
        <el-card v-for="action in quickActions" :key="action.label" class="action-card" shadow="hover" @click="router.push(action.path)">
          <div class="action-content">
            <div class="action-icon" :style="{ background: action.bgColor }">
              <el-icon :size="28" :color="action.iconColor"><component :is="action.icon" /></el-icon>
            </div>
            <div class="action-label">{{ action.label }}</div>
          </div>
        </el-card>
      </div>
    </div>

    <div class="recent-logs">
      <h3 class="section-title">最近操作日志</h3>
      <el-card shadow="never">
        <el-table :data="recentLogs" stripe style="width: 100%">
          <el-table-column prop="username" label="操作人" width="120" />
          <el-table-column prop="operation" label="操作" width="160" />
          <el-table-column prop="method" label="方法" width="100" />
          <el-table-column prop="ipAddress" label="IP地址" width="140" />
          <el-table-column prop="duration" label="耗时(ms)" width="100" />
          <el-table-column prop="createdTime" label="操作时间" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, Grid, Timer, Top, Bottom, Setting, Lock, DataAnalysis } from '@element-plus/icons-vue'
import { getDashboardStats, getRecentLogs } from '@/api/log'
import type { DashboardStats, OperationLog } from '@/types'

const router = useRouter()

/** 仪表盘统计数据 */
const stats = ref<DashboardStats>({
  userCount: 0,
  roleCount: 0,
  dataModelCount: 0,
  todayOperationCount: 0,
})
/** 最近操作日志列表 */
const recentLogs = ref<OperationLog[]>([])

/** 统计卡片配置（将数据映射为展示格式） */
const statsCards = computed(() => [
  {
    label: '用户总数',
    value: stats.value.userCount,
    icon: User,
    bgColor: '#ecf5ff',
    iconColor: '#409eff',
  },
  {
    label: '角色数量',
    value: stats.value.roleCount,
    icon: UserFilled,
    bgColor: '#f0f9eb',
    iconColor: '#67c23a',
  },
  {
    label: '数据模型数量',
    value: stats.value.dataModelCount,
    icon: Grid,
    bgColor: '#fdf6ec',
    iconColor: '#e6a23c',
  },
  {
    label: '今日操作次数',
    value: stats.value.todayOperationCount,
    icon: Timer,
    bgColor: '#fef0f0',
    iconColor: '#f56c6c',
  },
])

/** 快捷操作配置 */
const quickActions = [
  { label: '用户管理', path: '/users', icon: User, bgColor: '#ecf5ff', iconColor: '#409eff' },
  { label: '角色管理', path: '/roles', icon: Lock, bgColor: '#f0f9eb', iconColor: '#67c23a' },
  { label: '数据管理', path: '/data-models', icon: DataAnalysis, bgColor: '#fdf6ec', iconColor: '#e6a23c' },
  { label: '系统设置', path: '/settings', icon: Setting, bgColor: '#fef0f0', iconColor: '#f56c6c' },
]

/**
 * 获取仪表盘数据
 * 分别获取统计数据和最近日志，接口异常时使用模拟数据兜底
 */
async function fetchData() {
  try {
    const { data: statsData } = await getDashboardStats()
    stats.value = statsData
  } catch {
    stats.value = { userCount: 1286, roleCount: 8, dataModelCount: 15, todayOperationCount: 342 }
  }
  try {
    const { data: logsData } = await getRecentLogs(10)
    recentLogs.value = logsData
  } catch {
    recentLogs.value = [
      { id: 1, userId: 1, username: 'admin', operation: '登录系统', method: 'POST', params: '', result: '', ipAddress: '192.168.1.100', duration: 120, createdTime: '2026-04-22 10:30:00' },
      { id: 2, userId: 1, username: 'admin', operation: '创建用户', method: 'POST', params: '', result: '', ipAddress: '192.168.1.100', duration: 85, createdTime: '2026-04-22 10:25:00' },
      { id: 3, userId: 2, username: 'zhangsan', operation: '修改角色', method: 'PUT', params: '', result: '', ipAddress: '192.168.1.101', duration: 56, createdTime: '2026-04-22 10:20:00' },
      { id: 4, userId: 1, username: 'admin', operation: '删除数据模型', method: 'DELETE', params: '', result: '', ipAddress: '192.168.1.100', duration: 43, createdTime: '2026-04-22 10:15:00' },
      { id: 5, userId: 3, username: 'lisi', operation: '导出日志', method: 'GET', params: '', result: '', ipAddress: '192.168.1.102', duration: 230, createdTime: '2026-04-22 10:10:00' },
      { id: 6, userId: 1, username: 'admin', operation: '修改安全设置', method: 'PUT', params: '', result: '', ipAddress: '192.168.1.100', duration: 67, createdTime: '2026-04-22 09:55:00' },
      { id: 7, userId: 2, username: 'zhangsan', operation: '查看用户列表', method: 'GET', params: '', result: '', ipAddress: '192.168.1.101', duration: 32, createdTime: '2026-04-22 09:50:00' },
      { id: 8, userId: 1, username: 'admin', operation: '创建数据模型', method: 'POST', params: '', result: '', ipAddress: '192.168.1.100', duration: 98, createdTime: '2026-04-22 09:45:00' },
      { id: 9, userId: 3, username: 'lisi', operation: '修改通知设置', method: 'PUT', params: '', result: '', ipAddress: '192.168.1.102', duration: 45, createdTime: '2026-04-22 09:40:00' },
      { id: 10, userId: 1, username: 'admin', operation: '分配权限', method: 'POST', params: '', result: '', ipAddress: '192.168.1.100', duration: 78, createdTime: '2026-04-22 09:35:00' },
    ]
  }
}

onMounted(fetchData)
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #86909c;
  margin-top: 4px;
}

.stat-change {
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-change.up {
  color: #67c23a;
}

.stat-change.down {
  color: #f56c6c;
}

.change-text {
  color: #c0c4cc;
  margin-left: 4px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  margin: 0 0 16px;
}

.quick-actions {
  margin-bottom: 24px;
}

.action-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-card {
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.action-card:hover {
  transform: translateY(-2px);
}

.action-card :deep(.el-card__body) {
  padding: 24px;
}

.action-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-label {
  font-size: 14px;
  color: #4e5969;
  font-weight: 500;
}

.recent-logs :deep(.el-card) {
  border-radius: 12px;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .action-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
  .action-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
