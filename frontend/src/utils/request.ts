/**
 * Axios 请求封装模块
 * 基于 Axios 创建统一的 HTTP 请求实例，配置请求/响应拦截器
 * 功能：自动附加 Token、响应错误处理、401 自动刷新 Token、请求队列管理
 */
import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { Result } from '@/types'

/** 创建 Axios 实例，基础路径为 /api，超时时间 15 秒 */
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

/** 是否正在刷新 Token 的标志，防止并发刷新 */
let isRefreshing = false
/** Token 刷新期间暂存的请求队列，刷新成功后依次重发 */
let pendingRequests: Array<(token: string) => void> = []

/**
 * 请求拦截器
 * 自动在请求头中附加 Bearer Token
 */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 处理业务错误码、HTTP 状态码错误、401 自动刷新 Token
 */
service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data
    // 业务错误码非 200/0 时，视为业务异常
    if (res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res as any
  },
  async (error) => {
    const { response } = error
    // 网络异常（无响应）
    if (!response) {
      ElMessage.error('网络异常，请检查网络连接')
      return Promise.reject(error)
    }

    const { status, data } = response

    // 401 未授权：尝试使用 refreshToken 刷新令牌
    if (status === 401) {
      const storedRefreshToken = localStorage.getItem('refreshToken')
      // 无 refreshToken，直接跳转登录页
      if (!storedRefreshToken) {
        clearAuthAndRedirect()
        return Promise.reject(error)
      }

      // 首个 401 请求负责刷新 Token
      if (!isRefreshing) {
        isRefreshing = true
        try {
          const newToken = await refreshAccessToken(storedRefreshToken)
          isRefreshing = false
          // 刷新成功后，重发队列中所有暂存请求
          pendingRequests.forEach((cb) => cb(newToken))
          pendingRequests = []
          // 重发当前请求
          error.config.headers.Authorization = `Bearer ${newToken}`
          return service(error.config)
        } catch {
          // 刷新失败，清除认证状态并跳转登录页
          isRefreshing = false
          pendingRequests = []
          clearAuthAndRedirect()
          return Promise.reject(error)
        }
      } else {
        // 已有刷新请求进行中，将当前请求加入等待队列
        return new Promise((resolve) => {
          pendingRequests.push((token: string) => {
            error.config.headers.Authorization = `Bearer ${token}`
            resolve(service(error.config))
          })
        })
      }
    }

    // 403 禁止访问 - 只在非认证接口时跳转
    if (status === 403 && !error.config?.url?.includes('/auth/')) {
      ElMessage.error('无权限访问')
      router.push('/403')
    } else if (status === 500) {
      // 500 服务器内部错误
      ElMessage.error(data?.message || '服务器内部错误')
    } else {
      // 其他 HTTP 错误
      ElMessage.error(data?.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

/**
 * 使用 refreshToken 刷新访问令牌
 * @param refreshToken 刷新令牌
 * @returns 新的访问令牌
 */
async function refreshAccessToken(refreshToken: string): Promise<string> {
  const { data } = await axios.post<Result<{ accessToken: string; refreshToken: string }>>('/api/auth/refresh', {
    refreshToken,
  })
  const newToken = data.data.accessToken
  const newRefreshToken = data.data.refreshToken
  // 持久化新令牌
  localStorage.setItem('accessToken', newToken)
  if (newRefreshToken) {
    localStorage.setItem('refreshToken', newRefreshToken)
  }
  return newToken
}

/**
 * 清除本地所有认证状态并跳转到登录页
 * 用于 Token 刷新失败或无有效 Token 时
 */
function clearAuthAndRedirect() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('roles')
  localStorage.removeItem('permissions')
  router.push('/login')
}

export default service
