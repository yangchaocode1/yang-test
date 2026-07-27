/**
 * 用户管理 API 模块
 * 提供用户的增删改查、状态管理、密码操作、导入导出等接口
 */
import request from '@/utils/request'
import type {
  Result,
  PageResult,
  UserListItem,
  UserQuery,
  CreateUserRequest,
  UpdateUserRequest,
  ChangePasswordRequest,
  ResetPasswordResponse,
  ImportResult,
  RoleOption,
} from '@/types'

/**
 * 获取用户分页列表
 * @param params 查询参数（含关键词、状态、分页信息）
 * @returns 用户分页数据
 */
export function getUserList(params: UserQuery) {
  return request.get<Result<PageResult<UserListItem>>>('/users', { params }) as unknown as Promise<Result<PageResult<UserListItem>>>
}

/**
 * 获取用户详情
 * @param id 用户ID
 * @returns 用户详细信息
 */
export function getUserDetail(id: number) {
  return request.get<Result<UserListItem>>(`/users/${id}`) as unknown as Promise<Result<UserListItem>>
}

/**
 * 创建新用户
 * @param data 创建用户请求参数（含用户名、密码、姓名、角色等）
 * @returns 新创建的用户信息
 */
export function createUser(data: CreateUserRequest) {
  return request.post<Result<UserListItem>>('/users', data) as unknown as Promise<Result<UserListItem>>
}

/**
 * 更新用户信息
 * @param id 用户ID
 * @param data 更新用户请求参数（不含用户名和密码）
 * @returns 更新后的用户信息
 */
export function updateUser(id: number, data: UpdateUserRequest) {
  return request.put<Result<UserListItem>>(`/users/${id}`, data) as unknown as Promise<Result<UserListItem>>
}

/**
 * 删除单个用户
 * @param id 用户ID
 * @returns 无返回数据
 */
export function deleteUser(id: number) {
  return request.delete<Result<void>>(`/users/${id}`) as unknown as Promise<Result<void>>
}

/**
 * 批量删除用户
 * 后端路径: DELETE /users/batch
 * @param ids 待删除的用户ID数组
 * @returns 无返回数据
 */
export function batchDeleteUsers(ids: number[]) {
  return request.delete<Result<void>>('/users/batch', { data: ids }) as unknown as Promise<Result<void>>
}

/**
 * 切换用户状态（启用/禁用）
 * 后端路径: PUT /users/{id}/status（toggle模式，无需传status参数）
 * @param id 用户ID
 * @returns 无返回数据
 */
export function changeUserStatus(id: number) {
  return request.put<Result<void>>(`/users/${id}/status`) as unknown as Promise<Result<void>>
}

/**
 * 重置用户密码
 * 管理员操作，将用户密码重置为系统默认密码
 * @param id 用户ID
 * @returns 重置结果（含默认密码）
 */
export function resetPassword(id: number) {
  return request.put<Result<ResetPasswordResponse>>(`/users/${id}/reset-password`) as unknown as Promise<Result<ResetPasswordResponse>>
}

/**
 * 修改当前用户密码
 * 用户自行修改密码，需验证旧密码
 * @param data 修改密码请求参数（含旧密码和新密码）
 * @returns 无返回数据
 */
/**
 * 修改密码
 * 后端路径: PUT /users/{id}/change-password
 */
export function changePassword(id: number, data: ChangePasswordRequest) {
  return request.put<Result<void>>(`/users/${id}/change-password`, data) as unknown as Promise<Result<void>>
}

/**
 * 导出用户列表为 Excel 文件
 * @param params 可选的查询参数，用于筛选导出范围
 * @returns Excel 文件的 Blob 数据
 */
export function exportUsers(params?: UserQuery) {
  return request.get<Blob>('/users/export', { params, responseType: 'blob' }) as unknown as Promise<Blob>
}

/**
 * 导入用户数据（Excel 文件）
 * @param file 待导入的 Excel 文件
 * @returns 导入结果（含成功数、失败数和错误详情）
 */
export function importUsers(file: File) {
  // 使用 FormData 封装文件数据，以 multipart/form-data 格式上传
  const formData = new FormData()
  formData.append('file', file)
  return request.post<Result<ImportResult>>('/users/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }) as unknown as Promise<Result<ImportResult>>
}

/**
 * 获取角色选项列表
 * 用于用户表单中的角色选择下拉框
 * @returns 角色选项数组（仅含 id、name、code）
 */
export function getRoleOptions() {
  return request.get<Result<RoleOption[]>>('/roles/options') as unknown as Promise<Result<RoleOption[]>>
}
