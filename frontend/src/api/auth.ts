/**
 * 认证授权 API 模块
 * 提供用户登录、登出、短信验证码、Token 刷新、用户信息获取等接口
 */
import request from '@/utils/request'
import type { LoginRequest, SmsLoginRequest, LoginResponse, RefreshTokenResponse } from '@/types'
import type { Result } from '@/types'

/**
 * 账号密码登录
 * @param data 登录请求参数（含用户名和密码）
 * @returns 登录响应数据（含 accessToken、refreshToken、用户角色和权限信息）
 */
export function login(data: LoginRequest) {
  return request.post<Result<LoginResponse>>('/auth/login', data) as unknown as Promise<Result<LoginResponse>>
}

/**
 * 手机短信验证码登录
 * @param data 短信登录请求参数（含手机号和验证码）
 * @returns 登录响应数据（含 accessToken、refreshToken、用户角色和权限信息）
 */
export function loginBySms(data: SmsLoginRequest) {
  return request.post<Result<LoginResponse>>('/auth/login/sms', data) as unknown as Promise<Result<LoginResponse>>
}

/**
 * 发送短信验证码
 * @param phone 手机号码
 * @returns 无返回数据
 */
export function sendSmsCode(phone: string) {
  return request.post<Result<void>>('/auth/sms/send', { phone }) as unknown as Promise<Result<void>>
}

/**
 * 退出登录
 * 调用后端接口使当前 Token 失效
 * @returns 无返回数据
 */
export function logout() {
  return request.post<Result<void>>('/auth/logout') as unknown as Promise<Result<void>>
}

/**
 * 刷新访问令牌
 * 当 accessToken 过期时，使用 refreshToken 获取新的令牌对
 * @param refreshToken 刷新令牌
 * @returns 新的令牌数据（含新的 accessToken 和 refreshToken）
 */
export function refreshTokenApi(refreshToken: string) {
  return request.post<Result<RefreshTokenResponse>>('/auth/refresh', { refreshToken }) as unknown as Promise<Result<RefreshTokenResponse>>
}

/**
 * 获取当前登录用户信息
 * @returns 用户信息（含用户名、角色、权限列表）
 */
export function getUserInfo() {
  return request.get<Result<LoginResponse>>('/auth/user-info') as unknown as Promise<Result<LoginResponse>>
}

/**
 * 获取当前登录用户的权限编码列表
 * @returns 权限编码字符串数组
 */
export function getUserPermissions() {
  return request.get<Result<string[]>>('/auth/permissions') as unknown as Promise<Result<string[]>>
}
