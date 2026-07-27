/**
 * 路由配置模块
 * 定义应用的路由表、导航守卫（权限校验、登录状态检查）
 */
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

/** 主布局组件，懒加载 */
const Layout = () => import('@/layout/Layout.vue')

/** 路由配置表 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { requiresAuth: false }, // 不需要登录认证
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/ForbiddenView.vue'),
    meta: { requiresAuth: false }, // 不需要登录认证
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard', // 默认重定向到仪表盘
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/UsersView.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'system:user' },
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/roles/RolesView.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', permission: 'system:role' },
      },
      {
        path: 'permissions',
        name: 'Permissions',
        component: () => import('@/views/permissions/PermissionsView.vue'),
        meta: { title: '权限管理', icon: 'Lock', permission: 'system:permission' },
      },
      {
        path: 'data-models',
        name: 'DataModels',
        component: () => import('@/views/data-models/DataModelsView.vue'),
        meta: { title: '数据模型管理', icon: 'Grid', permission: 'biz:model' },
      },
      {
        path: 'business-data',
        name: 'BusinessData',
        component: () => import('@/views/business-data/BusinessDataView.vue'),
        meta: { title: '业务数据管理', icon: 'Document', permission: 'biz' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/SettingsView.vue'),
        meta: { title: '系统设置', icon: 'Setting' },
        redirect: '/settings/ui', // 默认显示界面配置
        children: [
          {
            path: 'ui',
            name: 'SettingsUI',
            component: () => import('@/views/settings/UiView.vue'),
            meta: { title: '界面配置', permission: 'settings:ui' },
          },
          {
            path: 'security',
            name: 'SettingsSecurity',
            component: () => import('@/views/settings/SecurityView.vue'),
            meta: { title: '安全设置', permission: 'settings:security' },
          },
          {
            path: 'notification',
            name: 'SettingsNotification',
            component: () => import('@/views/settings/NotificationView.vue'),
            meta: { title: '通知设置', permission: 'settings:notification' },
          },
          {
            path: 'logs',
            name: 'SettingsLogs',
            component: () => import('@/views/settings/LogsView.vue'),
            meta: { title: '日志管理', permission: 'settings:log' },
          },
        ],
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: { requiresAuth: false }, // 不需要登录认证
  },
]

/** 创建路由实例 */
const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 全局前置导航守卫
 * 执行顺序：免认证检查 -> 登录状态检查 -> 用户信息恢复 -> 权限校验
 */
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  // 不需要认证的页面直接放行
  if (to.meta.requiresAuth === false) {
    next()
    return
  }

  // 未登录则跳转登录页，并记录原始目标路径
  if (!userStore.token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 页面刷新后 userInfo 丢失或权限列表为空，从后端重新获取
  if (!userStore.userInfo || userStore.permissions.length === 0) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 获取失败（token过期、用户不存在等）则清除状态并跳转登录页
      await userStore.logout()
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  // 路由级权限校验：无权限则跳转 403 页面
  // admin 角色拥有所有权限，直接放行
  if (to.meta.permission) {
    if (userStore.roles.includes('SYSTEM_ADMIN') || userStore.hasPermission(to.meta.permission as string)) {
      next()
      return
    }
    next('/403')
    return
  }

  next()
})

export default router
