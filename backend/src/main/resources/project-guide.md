# 企业后台管理系统 - 项目文档

> 版本: 1.0.1
> 更新时间: 2026-07-27 (CI/CD 测试)

## 启动命令

### 后端启动

```bash
cd backend
./mvnw spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

## 停止命令

### 后端停止

在运行后端的终端中按 `Ctrl + C` 停止 Spring Boot 应用。

如果后台运行，可通过以下命令查找并终止进程：

```bash
# 查找占用 8080 端口的进程
lsof -i :8080
# 终止进程（将 PID 替换为实际进程号）
kill -9 <PID>
```

### 前端停止

在运行前端的终端中按 `Ctrl + C` 停止 Vite 开发服务器。

如果后台运行，可通过以下命令查找并终止进程：

```bash
# 查找占用 5173 端口的进程
lsof -i :5173
# 终止进程（将 PID 替换为实际进程号）
kill -9 <PID>
```

## 访问地址

### 前端页面

| 地址                                            | 说明         |
| --------------------------------------------- | ---------- |
| <http://localhost:5173>                       | 前端主页面（登录页） |
| <http://localhost:5173/dashboard>             | 仪表盘        |
| <http://localhost:5173/users>                 | 用户管理       |
| <http://localhost:5173/roles>                 | 角色管理       |
| <http://localhost:5173/permissions>           | 权限管理       |
| <http://localhost:5173/data-models>           | 数据模型管理     |
| <http://localhost:5173/business-data>         | 业务数据管理     |
| <http://localhost:5173/settings>              | 系统设置       |
| <http://localhost:5173/settings/ui>           | 界面配置       |
| <http://localhost:5173/settings/security>     | 安全设置       |
| <http://localhost:5173/settings/notification> | 通知设置       |
| <http://localhost:5173/settings/logs>         | 日志管理       |

### 后端 API

| 地址                                                     | 说明         |
| ------------------------------------------------------ | ---------- |
| <http://localhost:8080/api>                            | API 根路径    |
| <http://localhost:8080/api/auth/login>                 | 登录接口       |
| <http://localhost:8080/api/auth/logout>                | 登出接口       |
| <http://localhost:8080/api/auth/refresh>               | 刷新令牌       |
| <http://localhost:8080/api/auth/user-info>             | 获取当前用户信息   |
| <http://localhost:8080/api/dashboard/stats>            | 仪表盘统计数据    |
| <http://localhost:8080/api/users>                      | 用户管理 API   |
| <http://localhost:8080/api/roles>                      | 角色管理 API   |
| <http://localhost:8080/api/permissions>                | 权限管理 API   |
| <http://localhost:8080/api/data-models>                | 数据模型 API   |
| <http://localhost:8080/api/business-data>              | 业务数据 API   |
| <http://localhost:8080/api/configs>                    | 系统配置 API   |
| <http://localhost:8080/api/configs/group/UI>           | 界面配置分组 API |
| <http://localhost:8080/api/configs/group/SECURITY>     | 安全配置分组 API |
| <http://localhost:8080/api/configs/group/NOTIFICATION> | 通知配置分组 API |
| <http://localhost:8080/api/operation-logs>             | 操作日志 API   |
| <http://localhost:8080/api/operation-logs/recent>      | 最近操作日志 API |
| <http://localhost:8080/api/audit-logs>                 | 审计日志 API   |

### 工具地址

| 地址                                          | 说明                                                       |
| ------------------------------------------- | -------------------------------------------------------- |
| <http://localhost:8080/api/swagger-ui.html> | Swagger API 文档                                           |
| <http://localhost:8080/api/h2-console>      | H2 数据库控制台（JDBC URL: jdbc:h2:mem:admin\_db，用户名: sa，密码: 空） |

## 登录信息

- 账号: admin
- 密码: admin123

## 日志查看

### 后端日志

#### 1. 控制台日志

后端启动后，所有日志直接输出到启动终端（标准输出），包括：

- Spring Boot 启动日志
- HTTP 请求日志
- SQL 执行日志（MyBatis Plus）
- 异常堆栈信息

#### 2. 日志级别配置

在 `backend/src/main/resources/application.yml` 中配置日志级别：

```yaml
logging:
  level:
    root: INFO                          # 全局日志级别
    com.admin.system: DEBUG             # 项目代码日志级别（开发时建议 DEBUG）
    org.springframework.security: DEBUG # Spring Security 日志（排查权限问题时开启）
    com.baomidou.mybatisplus: DEBUG     # MyBatis Plus SQL 日志
```

#### 3. SQL 日志

开发环境已开启 MyBatis Plus SQL 日志，可在控制台看到完整的 SQL 语句和参数：

```
==>  Preparing: SELECT * FROM sys_user WHERE username = ?
==> Parameters: admin(String)
<==      Total: 1
```

#### 4. H2 数据库控制台

通过 <http://localhost:8080/api/h2-console> 可直接查看和操作数据库：

- JDBC URL: `jdbc:h2:mem:admin_db`
- User Name: `sa`
- Password: （留空）

### 前端日志

#### 1. 浏览器控制台

按 `F12` 或 `Cmd+Option+i` 打开浏览器开发者工具，在 Console 标签页查看：

- API 请求错误日志
- Vue 组件警告
- JavaScript 运行时错误

#### 2. 网络请求日志

在浏览器开发者工具的 Network 标签页查看：

- 所有 API 请求的 URL、状态码、响应时间
- 请求头（含 Authorization Bearer Token）
- 请求体和响应体

#### 3. Vue Devtools

安装 Vue Devtools 浏览器扩展，可查看：

- 组件树和组件状态
- Pinia Store 状态
- 路由信息

## 常见问题排查

### 登录失败

1. 检查后端是否正常启动（访问 <http://localhost:8080/api/swagger-ui.html）>
2. 检查浏览器控制台 Network 标签中 `/auth/login` 请求的响应
3. 确认用户名密码正确（admin / admin123）

### 页面显示 403

1. 检查浏览器 localStorage 中是否有 accessToken
2. 检查用户权限列表是否包含对应权限码
3. 在后端控制台查看 Spring Security 日志

### API 请求 404

1. 检查后端是否启动成功
2. 检查前端 API 路径是否与后端 Controller 路径一致
3. 访问 Swagger 文档确认后端 API 路径

### 数据库数据丢失

H2 内存数据库在应用重启后数据会丢失。如需持久化，切换到 MySQL 数据库：

1. 修改 `application.yml` 中的数据源配置
2. 添加 MySQL 驱动依赖

## 技术栈

- 后端: Java 17 + Spring Boot 3.x + Spring Security + JWT + MyBatis Plus + H2
- 前端: Vue 3 + TypeScript + Element Plus + Pinia + Vue Router + Axios


