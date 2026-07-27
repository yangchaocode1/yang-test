/**
 * 权限指令模块
 * 提供 v-permission 自定义指令，用于元素级别的权限控制
 * 当用户不具备指定权限时，自动移除 DOM 元素
 */
import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * v-permission 指令定义
 * 使用方式：v-permission="'system:user'" 或 v-permission="['system:user', 'system:role']"
 * 当用户拥有任一指定权限时保留元素，否则移除元素
 */
export const permission: Directive = {
  /**
   * 元素挂载时检查权限
   * @param el 指令绑定的 DOM 元素
   * @param binding 指令绑定值，支持字符串或字符串数组
   */
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const userStore = useUserStore()
    const value = binding.value

    // 未传入权限编码，不做处理
    if (!value) return

    // 统一为数组形式进行判断
    const permissions = Array.isArray(value) ? value : [value]
    // 只要拥有任一权限即视为有权限
    const hasPermission = permissions.some((p) => userStore.hasPermission(p))

    // 无权限则移除元素
    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  },
}

/**
 * 注册权限指令到 Vue 应用实例
 * @param app Vue 应用实例
 */
export function setupPermissionDirective(app: any) {
  app.directive('permission', permission)
}
