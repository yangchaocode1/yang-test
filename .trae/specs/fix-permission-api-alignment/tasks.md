# Tasks

- [ ] Task 1: 修复前端路由权限码与后端对齐
  - [ ] SubTask 1.1: 修改 router/index.ts 中所有路由的 meta.permission 字段，与后端 data.sql 权限码对齐（system:user, system:role, system:permission, system:data-model, system:data, settings:ui, settings:security, settings:notification, settings:log）
  - [ ] SubTask 1.2: 移除仪表盘和系统设置父级路由的 permission 限制（这些页面本身不需要权限码）

- [ ] Task 2: 补充后端缺失的 API
  - [ ] SubTask 2.1: 在 OperationLogController 或新建 DashboardController 中添加仪表盘统计 API（GET /dashboard/stats）和最近日志 API（GET /logs/recent）
  - [ ] SubTask 2.2: 在 ConfigController 中添加配置分组 API（GET /config/ui, POST /config/ui, GET /config/security, POST /config/security, GET /config/notification, POST /config/notification）
  - [ ] SubTask 2.3: 在 RoleController 中添加获取角色选项 API（GET /roles/options 和 GET /roles/all）

- [ ] Task 3: 修复前端 API 路径与后端对齐
  - [ ] SubTask 3.1: 修复 api/log.ts 路径（/logs/recent → /operation-logs/recent, /logs → /operation-logs, /logs/export → /operation-logs/export, /dashboard/stats 对齐后端）
  - [ ] SubTask 3.2: 修复 api/config.ts 路径（/config/ui → /configs/group/UI, /config/security → /configs/group/SECURITY, /config/notification → /configs/group/NOTIFICATION）
  - [ ] SubTask 3.3: 修复 api/role.ts 路径（/roles/all → /roles/all, /roles/options → /roles/all）
  - [ ] SubTask 3.4: 修复 api/permission.ts 审计日志路径（/permissions/audit-logs → /audit-logs）
  - [ ] SubTask 3.5: 修复 api/user.ts 批量删除路径（/users/batch-delete → /users/batch）

- [ ] Task 4: 为后端代码添加详细中文注释
  - [ ] SubTask 4.1: 为所有 Controller 添加类注释和方法注释
  - [ ] SubTask 4.2: 为所有 Service 添加类注释和方法注释
  - [ ] SubTask 4.3: 为所有 Entity 和 DTO 添加类注释和字段注释
  - [ ] SubTask 4.4: 为 Config、Security、Aspect 等配置类添加类注释和方法注释

- [ ] Task 5: 为前端代码添加详细中文注释
  - [ ] SubTask 5.1: 为所有 API 模块添加函数注释
  - [ ] SubTask 5.2: 为所有 Vue 组件添加组件注释和关键逻辑注释
  - [ ] SubTask 5.3: 为 stores、utils、directives 添加注释

- [ ] Task 6: 更新 project-guide.md
  - [ ] SubTask 6.1: 添加后端日志查看方法（控制台日志、日志级别配置、H2 SQL日志）
  - [ ] SubTask 6.2: 添加前端日志查看方法（浏览器控制台、Vue Devtools）
  - [ ] SubTask 6.3: 添加常见问题排查指引

# Task Dependencies
- [Task 3] depends on [Task 2]
- [Task 4] 与 [Task 5] 可并行
- [Task 1] 与 [Task 2] 可并行
