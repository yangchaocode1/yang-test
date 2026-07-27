import request from '@/utils/request'
import type { Result } from '@/types'

/**
 * 获取UI配置
 * 后端路径: GET /configs/group/UI
 * 返回类型: Result<Map<String, String>>，即键值对映射
 */
export function getUiConfig() {
  return request.get<Result<Record<string, string>>>('/configs/group/UI') as unknown as Promise<Result<Record<string, string>>>
}

/**
 * 保存UI配置
 * 后端路径: POST /configs/group/UI
 * 请求体: Map<String, String>，即键值对映射
 */
export function saveUiConfig(data: Record<string, string>) {
  return request.post<Result<void>>('/configs/group/UI', data) as unknown as Promise<Result<void>>
}

/**
 * 获取安全配置
 * 后端路径: GET /configs/group/SECURITY
 * 返回类型: Result<Map<String, String>>，即键值对映射
 */
export function getSecurityConfig() {
  return request.get<Result<Record<string, string>>>('/configs/group/SECURITY') as unknown as Promise<Result<Record<string, string>>>
}

/**
 * 保存安全配置
 * 后端路径: POST /configs/group/SECURITY
 * 请求体: Map<String, String>，即键值对映射
 */
export function saveSecurityConfig(data: Record<string, string>) {
  return request.post<Result<void>>('/configs/group/SECURITY', data) as unknown as Promise<Result<void>>
}

/**
 * 获取通知配置
 * 后端路径: GET /configs/group/NOTIFICATION
 * 返回类型: Result<Map<String, String>>，即键值对映射
 */
export function getNotificationConfig() {
  return request.get<Result<Record<string, string>>>('/configs/group/NOTIFICATION') as unknown as Promise<Result<Record<string, string>>>
}

/**
 * 保存通知配置
 * 后端路径: POST /configs/group/NOTIFICATION
 * 请求体: Map<String, String>，即键值对映射
 */
export function saveNotificationConfig(data: Record<string, string>) {
  return request.post<Result<void>>('/configs/group/NOTIFICATION', data) as unknown as Promise<Result<void>>
}
