<!--
  主布局组件
  功能：侧边栏导航（可折叠）、顶部导航栏（用户信息、通知、退出登录）、
  底部移动端导航栏、主内容区域（router-view）
  支持响应式布局，移动端自动隐藏侧边栏并显示底部导航
-->
<template>
  <div class="layout">
    <!-- 侧边栏导航 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <span v-if="!isCollapsed">管理系统</span>
        <span v-else>MS</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 含多个子路由的菜单项显示为子菜单 -->
          <el-sub-menu v-if="route.children && route.children.length > 1" :index="route.path">
            <template #title>
              <el-icon><component :is="route.meta?.icon" /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="`${route.path}/${child.path}`"
            >
              {{ child.meta?.title }}
            </el-menu-item>
          </el-sub-menu>
          <!-- 单子路由或无子路由的菜单项 -->
          <el-menu-item v-else :index="getMenuIndex(route)">
            <el-icon><component :is="route.meta?.icon" /></el-icon>
            <template #title>{{ route.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>

    <!-- 主内容区域 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header-left">
          <!-- 侧边栏折叠/展开按钮 -->
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <!-- 通知徽标 -->
          <el-badge :value="notificationCount" class="notification-badge">
            <el-icon class="icon-btn"><Bell /></el-icon>
          </el-badge>
          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 路由视图内容区 -->
      <main class="content">
        <router-view />
      </main>
    </div>

    <!-- 移动端底部导航栏（仅小屏幕显示） -->
    <nav class="bottom-nav">
      <div
        v-for="item in bottomNavItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: activeMenu === item.path }"
        @click="navigateTo(item.path)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </div>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Fold, Expand, Bell, ArrowDown, Odometer, User, UserFilled, Lock, Grid, Document, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 侧边栏是否折叠 */
const isCollapsed = ref(false)
/** 通知数量（模拟数据） */
const notificationCount = ref(3)

/** 从路由配置中提取菜单项，过滤隐藏路由 */
const menuRoutes = computed(() => {
  const routes = router.options.routes.find((r) => r.path === '/')?.children || []
  return routes.filter((r) => !r.meta?.hidden)
})

/** 当前激活的菜单项（基于路由路径） */
const activeMenu = computed(() => route.path)

/** 移动端底部导航项配置 */
const bottomNavItems = [
  { path: '/dashboard', title: '首页', icon: Odometer },
  { path: '/users', title: '用户', icon: User },
  { path: '/roles', title: '角色', icon: UserFilled },
  { path: '/settings', title: '设置', icon: Setting },
]

/**
 * 获取菜单项的路由索引
 * 单子路由时直接使用子路由路径，否则使用父路由路径
 * @param route 路由配置对象
 * @returns 菜单索引路径
 */
function getMenuIndex(route: any) {
  if (route.children && route.children.length === 1) {
    return `/${route.path}/${route.children[0].path}`
  }
  return `/${route.path}`
}

/** 切换侧边栏折叠/展开状态 */
function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}

/**
 * 导航到指定路径
 * @param path 目标路径
 */
function navigateTo(path: string) {
  router.push(path)
}

/**
 * 处理用户下拉菜单命令
 * @param command 命令类型（profile-个人中心，logout-退出登录）
 */
async function handleCommand(command: string) {
  if (command === 'logout') {
    await userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.sidebar {
  width: 220px;
  background-color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  transition: width 0.3s;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  border-bottom: 1px solid #e4e7ed;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 60px);
}

.main-container {
  flex: 1;
  margin-left: 220px;
  transition: margin-left 0.3s;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.sidebar.collapsed + .main-container {
  margin-left: 64px;
}

.header {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 99;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.icon-btn:hover {
  color: #409eff;
}

.notification-badge {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  color: #606266;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.bottom-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  color: #909399;
  font-size: 12px;
}

.nav-item.active {
  color: #409eff;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }

  .main-container {
    margin-left: 0;
  }

  .bottom-nav {
    display: flex;
  }

  .content {
    padding-bottom: 80px;
  }
}
</style>
