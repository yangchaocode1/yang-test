# Tasks

- [x] Task 1: 搭建后端项目骨架
  - [x] SubTask 1.1: 创建 Spring Boot 3.x 项目，配置 Maven/Gradle 依赖（Spring Web、Spring Security、MyBatis Plus、H2、Swagger、Lombok 等）
  - [x] SubTask 1.2: 配置 H2 内存数据库数据源，编写数据库初始化脚本（schema.sql、data.sql）
  - [x] SubTask 1.3: 实现统一响应封装（Result<T>）与全局异常处理
  - [x] SubTask 1.4: 集成 Swagger/SpringDoc 生成 API 文档
  - [x] SubTask 1.5: 配置跨域与代理设置

- [x] Task 2: 实现认证与授权基础
  - [x] SubTask 2.1: 实现 JWT 工具类（生成、解析、刷新令牌）
  - [x] SubTask 2.2: 实现 Spring Security 配置（认证过滤器、授权规则、无状态会话）
  - [x] SubTask 2.3: 实现登录/登出/令牌刷新 API
  - [x] SubTask 2.4: 实现基于 RBAC 的权限模型（User-Role-Permission 表结构与实体）

- [x] Task 3: 实现用户管理模块
  - [x] SubTask 3.1: 创建用户实体、Mapper、Service、Controller
  - [x] SubTask 3.2: 实现用户 CRUD API（含分页、搜索、排序）
  - [x] SubTask 3.3: 实现用户状态管理（启用/禁用、账号有效期）
  - [x] SubTask 3.4: 实现密码管理（密码重置、密码强度验证）
  - [x] SubTask 3.5: 实现用户数据 Excel 导入导出

- [x] Task 4: 实现角色与权限管理模块
  - [x] SubTask 4.1: 创建角色、权限实体与关联表，实现 Mapper、Service、Controller
  - [x] SubTask 4.2: 实现角色 CRUD API
  - [x] SubTask 4.3: 实现权限分配 API（为角色分配/移除权限）
  - [x] SubTask 4.4: 实现权限继承机制（角色间继承关系）
  - [x] SubTask 4.5: 实现权限审计日志记录

- [x] Task 5: 实现业务数据管理模块
  - [x] SubTask 5.1: 实现数据模型定义功能（动态表结构或预定义模型）
  - [x] SubTask 5.2: 实现业务数据 CRUD API（含复杂查询条件）
  - [x] SubTask 5.3: 实现后端数据验证（Spring Validation + 业务规则验证）
  - [x] SubTask 5.4: 实现数据关联关系管理

- [x] Task 6: 实现系统设置模块
  - [x] SubTask 6.1: 实现系统配置实体与 CRUD API（界面配置、安全设置、通知设置）
  - [x] SubTask 6.2: 实现操作日志记录（AOP 切面自动记录）
  - [x] SubTask 6.3: 实现日志查询与导出 API

- [x] Task 7: 搭建前端项目骨架
  - [x] SubTask 7.1: 创建 Vue 3 + Vite 项目，配置 Element Plus、Pinia、Vue Router、Axios
  - [x] SubTask 7.2: 实现响应式布局框架（侧边栏、顶部导航、底部导航栏）
  - [x] SubTask 7.3: 实现 Axios 封装（请求/响应拦截、JWT 令牌注入、错误处理）
  - [x] SubTask 7.4: 实现路由守卫与权限指令（按钮级权限控制）

- [x] Task 8: 实现前端认证页面
  - [x] SubTask 8.1: 实现登录页面（用户名密码登录、手机验证码登录）
  - [x] SubTask 8.2: 实现令牌管理（存储、刷新、过期处理）

- [x] Task 9: 实现前端用户管理界面
  - [x] SubTask 9.1: 实现用户列表页面（表格、搜索、分页、批量操作）
  - [x] SubTask 9.2: 实现用户新增/编辑表单（含表单验证）
  - [x] SubTask 9.3: 实现用户状态切换与密码重置操作
  - [x] SubTask 9.4: 实现用户 Excel 导入导出功能

- [x] Task 10: 实现前端角色与权限管理界面
  - [x] SubTask 10.1: 实现角色列表页面与角色编辑表单
  - [x] SubTask 10.2: 实现权限配置界面（树形结构展示权限，支持分配）
  - [x] SubTask 10.3: 实现权限继承配置界面
  - [x] SubTask 10.4: 实现权限审计日志查看界面

- [x] Task 11: 实现前端业务数据管理界面
  - [x] SubTask 11.1: 实现数据模型管理页面
  - [x] SubTask 11.2: 实现业务数据列表页面（含复杂查询条件）
  - [x] SubTask 11.3: 实现业务数据新增/编辑表单（含实时验证）
  - [x] SubTask 11.4: 实现数据关联关系管理界面

- [x] Task 12: 实现前端系统设置界面
  - [x] SubTask 12.1: 实现仪表盘页面（关键指标展示与快捷操作入口）
  - [x] SubTask 12.2: 实现界面配置页面（主题、布局、语言）
  - [x] SubTask 12.3: 实现安全设置页面（密码策略、登录限制、会话超时）
  - [x] SubTask 12.4: 实现通知设置页面
  - [x] SubTask 12.5: 实现日志管理页面（查看、筛选、导出）

# Task Dependencies
- [Task 2] depends on [Task 1]
- [Task 3] depends on [Task 2]
- [Task 4] depends on [Task 2]
- [Task 5] depends on [Task 2]
- [Task 6] depends on [Task 2]
- [Task 8] depends on [Task 7]
- [Task 9] depends on [Task 8]
- [Task 10] depends on [Task 8]
- [Task 11] depends on [Task 8]
- [Task 12] depends on [Task 8]
- [Task 7] 与 [Task 1] 可并行
