/**
 * 权限管理 API 模块
 * 提供权限的增删改查、权限树获取、审计日志查询等接口
 */
import request from '@/utils/request'
import type { Permission, PermissionRequest, PermissionPageRequest, PageResult, AuditLog, AuditLogPageRequest } from '@/types'
import type { Result } from '@/types'

/**
 * 获取权限分页列表
 * @param params 查询参数（含类型、状态、分页信息）
 * @returns 权限分页数据
 */
export function getPermissionList(params: PermissionPageRequest) {
  return request.get<Result<PageResult<Permission>>>('/permissions', { params }) as unknown as Promise<Result<PageResult<Permission>>>
}

/**
 * 获取权限树形结构
 * 用于权限配置页面展示层级关系
 * @returns 权限树形数据（含子节点）
 */
export function getPermissionTree() {
  return request.get<Result<Permission[]>>('/permissions/tree') as unknown as Promise<Result<Permission[]>>
}

/**
 * 根据ID获取权限详情
 * @param id 权限ID
 * @returns 权限详细信息
 */
export function getPermissionById(id: number) {
  return request.get<Result<Permission>>(`/permissions/${id}`) as unknown as Promise<Result<Permission>>
}

/**
 * 创建新权限
 * @param data 创建权限请求参数（含编码、名称、类型、父权限等）
 * @returns 新创建的权限信息
 */
export function createPermission(data: PermissionRequest) {
  return request.post<Result<Permission>>('/permissions', data) as unknown as Promise<Result<Permission>>
}

/**
 * 更新权限信息
 * @param id 权限ID
 * @param data 更新权限请求参数
 * @returns 更新后的权限信息
 */
export function updatePermission(id: number, data: PermissionRequest) {
  return request.put<Result<Permission>>(`/permissions/${id}`, data) as unknown as Promise<Result<Permission>>
}

/**
 * 删除权限
 * @param id 权限ID
 * @returns 无返回数据
 */
export function deletePermission(id: number) {
  return request.delete<Result<void>>(`/permissions/${id}`) as unknown as Promise<Result<void>>
}

/**
 * 获取权限审计日志分页列表
 * @param params 查询参数（含操作人、模块、操作类型、时间范围、分页信息）
 * @returns 审计日志分页数据
 */
export function getAuditLogs(params: AuditLogPageRequest) {
  return request.get<Result<PageResult<AuditLog>>>('/audit-logs', { params }) as unknown as Promise<Result<PageResult<AuditLog>>>
}
