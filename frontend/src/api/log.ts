/**
 * 日志与仪表盘 API 模块
 * 提供仪表盘统计数据、操作日志查询和导出接口
 */
import request from '@/utils/request'
import type { Result, PageResult, OperationLog, OperationLogQuery, DashboardStats } from '@/types'

/**
 * 获取仪表盘统计数据
 * 后端路径: GET /dashboard/stats
 * @returns 统计数据（含用户总数、角色数量、数据模型数量、今日操作次数）
 */
export function getDashboardStats() {
  return request.get<Result<DashboardStats>>('/dashboard/stats') as unknown as Promise<Result<DashboardStats>>
}

/**
 * 获取最近操作日志
 * 后端路径: GET /operation-logs/recent
 * @param limit 返回条数，默认10条
 * @returns 最近的操作日志列表
 */
export function getRecentLogs(limit: number = 10) {
  return request.get<Result<OperationLog[]>>('/operation-logs/recent', { params: { limit } }) as unknown as Promise<Result<OperationLog[]>>
}

/**
 * 获取操作日志分页列表
 * 后端路径: GET /operation-logs
 * @param params 查询参数（含操作人、操作类型、模块、时间范围、分页信息）
 * @returns 操作日志分页数据
 */
export function getLogList(params: OperationLogQuery) {
  return request.get<Result<PageResult<OperationLog>>>('/operation-logs', { params }) as unknown as Promise<Result<PageResult<OperationLog>>>
}

/**
 * 导出操作日志为 Excel 文件
 * 后端路径: GET /operation-logs/export
 * @param params 查询参数，用于筛选导出范围
 * @returns Excel 文件的 Blob 数据
 */
export function exportLogs(params: OperationLogQuery) {
  return request.get('/operation-logs/export', { params, responseType: 'blob' })
}
