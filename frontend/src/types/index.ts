export interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar: string
  status: number
  expireTime: string
  mustChangePassword: number
  roles: UserRole[]
  createdTime: string
  updatedTime: string
}

export interface UserRole {
  id: number
  roleCode: string
  roleName: string
}

export interface UserListItem {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar: string
  status: number
  expireTime: string
  mustChangePassword: number
  roles: UserRole[]
  createdTime: string
  updatedTime: string
}

export interface UserQuery extends PageRequest {
  status?: number
}

export interface CreateUserRequest {
  username: string
  password: string
  realName: string
  phone: string
  email: string
  roleIds: number[]
  status: number
  expireTime: string
}

export interface UpdateUserRequest {
  realName: string
  phone: string
  email: string
  roleIds: number[]
  status: number
  expireTime: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface ResetPasswordResponse {
  defaultPassword: string
}

export interface ImportResult {
  successCount: number
  failCount: number
  errors: Array<{ row: number; message: string }>
}

export interface RoleOption {
  id: number
  roleName: string
  roleCode: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface SmsLoginRequest {
  phone: string
  code: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  username: string
  roles: string[]
  permissions: string[]
}

export interface RefreshTokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

export interface Role {
  id: number
  roleCode: string
  roleName: string
  description: string
  parentId: number | null
  parentName?: string
  sortOrder: number
  status: number
  permissionIds: number[]
  createdTime: string
  updatedTime: string
}

export interface RoleRequest {
  roleCode: string
  roleName: string
  description: string
  parentId: number | null
  sortOrder: number
  status: number
}

export interface RolePageRequest extends PageRequest {
  status?: number | null
}

export interface Permission {
  id: number
  permissionCode: string
  permissionName: string
  permissionType: string
  parentId: number | null
  path: string
  icon: string
  sortOrder: number
  status: number
  children?: Permission[]
}

export interface PermissionRequest {
  permissionCode: string
  permissionName: string
  permissionType: string
  parentId: number | null
  path: string
  icon: string
  sortOrder: number
  status: number
}

export interface PermissionPageRequest extends PageRequest {
  type?: string | null
  status?: number | null
}

export interface AuditLog {
  id: number
  userId: number
  username: string
  operation: string
  module: string
  targetType: string
  targetId: string
  oldValue: string
  newValue: string
  ipAddress: string
  createdTime: string
}

export interface AuditLogPageRequest extends PageRequest {
  userId?: number
  module?: string
  operation?: string
  targetType?: string
  startTime?: string
  endTime?: string
}

export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface PageRequest {
  pageNum: number
  pageSize: number
  keyword?: string
}

export interface DashboardStats {
  userCount: number
  roleCount: number
  dataModelCount: number
  todayOperationCount: number
}

export interface OperationLog {
  id: number
  userId: number
  username: string
  operation: string
  method: string
  params: string
  result: string
  ipAddress: string
  duration: number
  createdTime: string
}

export interface OperationLogQuery extends PageRequest {
  operator?: string
  operationType?: string
  module?: string
  startTime?: string
  endTime?: string
}

export interface UiConfig {
  theme: 'light' | 'dark'
  layout: 'sidebar' | 'top'
  language: 'zh' | 'en'
}

export interface SecurityConfig {
  minPasswordLength: number
  requireUppercase: boolean
  requireDigit: boolean
  maxLoginAttempts: number
  sessionTimeout: number
}

export interface NotificationConfig {
  emailEnabled: boolean
  smsEnabled: boolean
  notifyEmail: string
}

export type FieldType = 'TEXT' | 'NUMBER' | 'DATE' | 'SELECT' | 'REFERENCE'

export interface DataModelField {
  id?: number
  modelId?: number
  fieldCode: string
  fieldName: string
  fieldType: string
  required: number
  uniqueFlag: number
  referenceModelId?: number | null
  referenceModelName?: string
  options?: string[]
  sortOrder: number
  createdTime?: string
  updatedTime?: string
}

export interface DataModel {
  id: number
  modelCode: string
  modelName: string
  description: string
  tableName: string
  status: number
  fields: DataModelField[]
  createdTime: string
  updatedTime: string
}

export interface DataModelPageRequest extends PageRequest {
  status?: number | null
}

export interface BusinessDataPageRequest extends PageRequest {
  modelId: number
  conditions?: Record<string, any>
}

export interface BusinessDataItem {
  id: number
  modelId: number
  modelName: string
  data: Record<string, any>
  createdTime: string
  updatedTime: string
}

export interface ReferenceInfo {
  sourceModelId: number
  sourceModelName: string
  sourceFieldCode: string
  sourceDataId: number
  sourceDataLabel: string
}

export interface DataReferenceDetail {
  currentData: BusinessDataItem
  referencedBy: ReferenceInfo[]
  references: ReferenceInfo[]
}
