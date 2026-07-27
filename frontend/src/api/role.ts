import request from '@/utils/request'
import type { Role, RoleRequest, RolePageRequest, PageResult, Permission } from '@/types'
import type { Result } from '@/types'

export function getRoleList(params: RolePageRequest) {
  return request.get<Result<PageResult<Role>>>('/roles', { params }) as unknown as Promise<Result<PageResult<Role>>>
}

export function getRoleById(id: number) {
  return request.get<Result<Role>>(`/roles/${id}`) as unknown as Promise<Result<Role>>
}

export function createRole(data: RoleRequest) {
  return request.post<Result<Role>>('/roles', data) as unknown as Promise<Result<Role>>
}

export function updateRole(id: number, data: RoleRequest) {
  return request.put<Result<Role>>(`/roles/${id}`, data) as unknown as Promise<Result<Role>>
}

export function deleteRole(id: number) {
  return request.delete<Result<void>>(`/roles/${id}`) as unknown as Promise<Result<void>>
}

export function getRolePermissions(id: number) {
  return request.get<Result<number[]>>(`/roles/${id}/permissions`) as unknown as Promise<Result<number[]>>
}

/**
 * 为角色分配权限
 * 后端路径: POST /roles/{roleId}/permissions
 * 请求体: { permissionIds: number[] }
 */
export function assignRolePermissions(id: number, permissionIds: number[]) {
  return request.post<Result<void>>(`/roles/${id}/permissions`, { permissionIds }) as unknown as Promise<Result<void>>
}

/**
 * 获取角色继承的父角色列表
 * 后端路径: GET /roles/{roleId}/inherited-roles
 */
export function getRoleInheritances(id: number) {
  return request.get<Result<number[]>>(`/roles/${id}/inherited-roles`) as unknown as Promise<Result<number[]>>
}

/**
 * 设置角色继承关系
 * 后端路径: POST /roles/{roleId}/inherit
 * 请求体: { parentRoleIds: number[] }
 */
export function updateRoleInheritances(id: number, parentRoleIds: number[]) {
  return request.post<Result<void>>(`/roles/${id}/inherit`, { parentRoleIds }) as unknown as Promise<Result<void>>
}

/**
 * 移除角色继承关系
 * 后端路径: DELETE /roles/{roleId}/inherit/{parentRoleId}
 */
export function removeRoleInheritance(id: number, parentRoleId: number) {
  return request.delete<Result<void>>(`/roles/${id}/inherit/${parentRoleId}`) as unknown as Promise<Result<void>>
}

export function getAllRoles() {
  return request.get<Result<Role[]>>('/roles/all') as unknown as Promise<Result<Role[]>>
}

export function getPermissionTree() {
  return request.get<Result<Permission[]>>('/permissions/tree') as unknown as Promise<Result<Permission[]>>
}
