/**
 * 用户状态管理 Store
 * 管理用户登录态、Token、用户信息、角色和权限等全局状态
 * 使用 Pinia 的组合式 API 风格定义
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import { getUserInfo, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  /** 访问令牌，从 localStorage 初始化 */
  const token = ref<string>(localStorage.getItem('accessToken') || '')
  /** 刷新令牌，从 localStorage 初始化 */
  const refreshTokenVal = ref<string>(localStorage.getItem('refreshToken') || '')
  /** 当前登录用户信息，从 localStorage 初始化 */
  const userInfo = ref<User | null>(
    localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')!) : null
  )
  /** 当前用户的权限编码列表，从 localStorage 初始化 */
  const permissions = ref<string[]>(
    localStorage.getItem('permissions') ? JSON.parse(localStorage.getItem('permissions')!) : []
  )
  /** 当前用户的角色编码列表，从 localStorage 初始化 */
  const roles = ref<string[]>(
    localStorage.getItem('roles') ? JSON.parse(localStorage.getItem('roles')!) : []
  )

  /**
   * 设置 Token 并持久化到 localStorage
   * @param accessToken 访问令牌
   * @param newRefreshToken 刷新令牌
   */
  function setToken(accessToken: string, newRefreshToken: string) {
    token.value = accessToken
    refreshTokenVal.value = newRefreshToken
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', newRefreshToken)
  }

  /**
   * 设置用户信息并持久化角色和权限到 localStorage
   * @param user 用户信息对象
   */
  function setUserInfo(user: User) {
    userInfo.value = user
    roles.value = user.roles || []
    permissions.value = user.permissions || []
    localStorage.setItem('userInfo', JSON.stringify(user))
    localStorage.setItem('roles', JSON.stringify(user.roles || []))
    localStorage.setItem('permissions', JSON.stringify(user.permissions || []))
  }

  /**
   * 从后端获取当前用户信息并更新本地状态
   * 用于页面刷新后恢复用户信息
   */
  async function fetchUserInfo() {
    const { data } = await getUserInfo()
    // 直接从响应中获取角色和权限，确保最新数据
    const userRoles = data.roles || []
    const userPermissions = data.permissions || []
    setUserInfo({
      id: 0,
      username: data.username,
      nickname: data.username,
      email: '',
      phone: '',
      avatar: '',
      status: 1,
      roles: userRoles,
      permissions: userPermissions,
      createdAt: '',
      updatedAt: '',
    })
  }

  /**
   * 判断当前用户是否拥有指定权限
   * @param code 权限编码
   * @returns 是否拥有该权限
   */
  function hasPermission(code: string): boolean {
    return permissions.value.includes(code)
  }

  /**
   * 判断当前用户是否拥有指定角色
   * @param role 角色编码
   * @returns 是否拥有该角色
   */
  function hasRole(role: string): boolean {
    return roles.value.includes(role)
  }

  /**
   * 退出登录
   * 调用后端登出接口，并清除本地所有认证状态
   */
  async function logout() {
    try {
      await logoutApi()
    } finally {
      // 无论后端接口是否成功，都清除本地状态
      token.value = ''
      refreshTokenVal.value = ''
      userInfo.value = null
      permissions.value = []
      roles.value = []
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('roles')
      localStorage.removeItem('permissions')
    }
  }

  return {
    token,
    refreshToken: refreshTokenVal,
    userInfo,
    permissions,
    roles,
    setToken,
    setUserInfo,
    fetchUserInfo,
    hasPermission,
    hasRole,
    logout,
  }
})
