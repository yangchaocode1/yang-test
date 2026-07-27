# 安全审查报告

## 项目概览

**项目名称**: admin-system (企业级后台管理系统)
**技术栈**: Spring Boot 3.2.5 + Spring Security + MyBatis Plus 3.5.6 + H2 Database + JWT (jjwt 0.12.5) + SpringDoc OpenAPI 2.5.0
**依赖数量**: 约 13 个主要 Maven 依赖
**认证方式**: JWT Token 认证 (无状态)
**前端**: 本项目仅包含后端代码，无前端代码

---

## 严重漏洞（必须立即修复）

| 编号 | 漏洞类型 | 位置（文件路径+行号） | 风险描述 | 修复方案 | CVSS评分 |
|------|----------|----------------------|----------|----------|----------|
| CRIT-001 | H2控制台对外开放 + 空密码 | `application.yml:16-21` | H2数据库控制台已启用且允许远程访问(`web-allow-others: true`)，默认用户`sa`密码为空，攻击者可直接登录数据库获取/篡改所有数据，甚至执行系统命令 | 1. 生产环境禁用H2控制台: `spring.h2.console.enabled: false`<br>2. 设置强密码<br>3. 如必须启用，增加IP白名单和Spring Security保护 | 9.8 |
| CRIT-002 | CORS配置不安全 - 通配符+允许凭证 | `CorsConfig.java:17,20` | 同时使用 `setAllowedOriginPatterns("*")` 和 `setAllowCredentials(true)`，虽使用patterns而非origins在技术上可共存，但允许任意来源携带凭证(Cookie/Token)发起跨域请求，存在CSRF和数据泄露风险 | 1. 明确配置允许的来源域名列表，禁止使用`*`<br>2. 对内部接口禁用跨域或严格限定来源 | 8.1 |
| CRIT-003 | 缺少基于角色的访问控制 (RBAC未生效) | `SecurityConfig.java:40-50` | Spring Security配置中所有接口仅要求`authenticated()`，未对管理接口（用户管理、角色管理、权限管理、配置管理）设置角色/权限限制。任何登录用户均可访问所有管理功能，包括创建用户、分配角色、修改系统配置等 | 1. 使用`requestMatchers("/users/**", "/roles/**", "/permissions/**").hasRole("SYSTEM_ADMIN")`等方式限制管理接口<br>2. 在Controller或Service层添加`@PreAuthorize`注解 | 8.8 |
| CRIT-004 | JWT密钥硬编码在配置文件中 | `application.yml:51-52` | JWT签名密钥以明文形式硬编码在application.yml中，且提交至代码仓库。密钥泄露后攻击者可伪造任意用户的JWT令牌，实现越权访问 | 1. 使用环境变量或外部配置中心(如Nacos/Vault)管理密钥<br>2. 密钥长度至少256位<br>3. 生产环境必须轮换密钥 | 7.5 |
| CRIT-005 | 登录接口无暴力破解防护 | `AuthController.java:48-62` | 登录接口无速率限制、无验证码、无登录失败次数锁定机制。攻击者可通过暴力破解获取用户账号密码 | 1. 实现登录失败次数限制（如5次失败锁定账号15分钟）<br>2. 添加IP级别的速率限制<br>3. 考虑添加验证码 | 7.5 |
| CRIT-006 | 排序参数SQL注入风险 | `UserService.java:64-68`<br>`BusinessDataService.java:56-60`<br>`DataModelService.java:42-46` | `orderBy`参数直接传入MyBatis Plus的`OrderItem.asc/desc()`，未做白名单校验。攻击者可通过构造恶意的orderBy值进行SQL注入 | 1. 对orderBy字段做白名单校验，只允许预设的列名<br>2. 使用正则表达式验证orderBy值格式 | 7.2 |

---

## 高危漏洞（尽快修复）

| 编号 | 漏洞类型 | 位置 | 风险描述 | 修复方案 |
|------|----------|------|----------|----------|
| HIGH-001 | JWT登出后令牌未失效 | `AuthService.java:42-44` | 登出仅清除SecurityContext，JWT令牌本身仍然有效。令牌泄露后即使登出，攻击者仍可继续使用该令牌访问系统 | 1. 实现Token黑名单机制（Redis存储已注销Token）<br>2. 缩短AccessToken有效期<br>3. 使用Refresh Token轮换机制 |
| HIGH-002 | Refresh Token无轮换且可无限刷新 | `AuthService.java:54-70` | 刷新令牌接口每次生成新的access token和refresh token，但旧refresh token仍然有效，可无限循环刷新。且未验证refresh token是否已被使用/吊销 | 1. 实现Refresh Token单次使用机制（使用后立即失效）<br>2. 维护refresh token白名单/黑名单<br>3. 限制refresh token最大使用次数 |
| HIGH-003 | 修改密码接口存在越权风险 | `UserController.java:179-185` | 修改密码接口路径包含用户ID参数`/{id}/change-password`，普通用户可修改其他用户的密码（只要知道ID），存在水平越权漏洞 | 1. 验证当前登录用户只能修改自己的密码，或需管理员权限<br>2. 普通用户改密接口不接受ID参数，从SecurityContext获取当前用户 |
| HIGH-004 | 用户管理接口存在越权风险 | `UserController.java:56-260` | 用户的增删改查、重置密码、批量删除等管理接口无权限校验，任何登录用户均可操作。普通用户可创建管理员账号、重置其他用户密码等 | 1. 添加`@PreAuthorize("hasRole('SYSTEM_ADMIN')")`注解<br>2. 在SecurityConfig中配置路径权限规则 |
| HIGH-005 | 角色/权限管理接口无权限控制 | `RoleController.java`<br>`PermissionController.java` | 角色管理、权限管理的全部接口无权限校验，任何登录用户均可创建角色、分配权限、修改权限体系 | 1. 添加管理员权限校验注解<br>2. 配置Spring Security路径规则 |
| HIGH-006 | 系统配置管理接口无权限控制 | `ConfigController.java` | 系统配置的增删改查、批量保存接口无权限校验，任何登录用户均可修改系统安全配置（如密码策略、登录尝试次数等） | 1. 添加管理员权限校验<br>2. 敏感配置项需单独权限控制 |
| HIGH-007 | 操作日志记录敏感信息 | `OperationLogSaveService.java:54-65` | 操作日志AOP将所有请求参数序列化后存入数据库，包含登录密码、修改密码等敏感信息。日志泄露将导致密码泄露 | 1. 对敏感字段（password、token等）做脱敏处理<br>2. 提供注解标记敏感参数不记录<br>3. 加密存储日志中的敏感数据 |
| HIGH-008 | 全局异常泄露内部错误信息 | `GlobalExceptionHandler.java:70-74` | 未捕获的异常直接返回`e.getMessage()`给前端，可能泄露数据库结构、内部路径、堆栈信息等敏感内容，辅助攻击者进行针对性攻击 | 1. 生产环境仅返回通用错误信息，不暴露详情<br>2. 详细错误记录到日志，不返回给前端 |
| HIGH-009 | 文件上传无类型/大小限制 | `UserController.java:211-217` | 用户导入接口接收MultipartFile但未校验文件类型和大小，攻击者可上传超大文件导致DoS，或上传恶意文件利用POI漏洞 | 1. 限制文件大小（spring.servlet.multipart.max-file-size）<br>2. 校验文件类型（白名单，仅允许xlsx/xls）<br>3. 校验文件头内容而非仅依赖扩展名 |
| HIGH-010 | 数据模型/业务数据接口无权限控制 | `DataModelController.java`<br>`BusinessDataController.java` | 数据模型管理和业务数据的全部接口无权限校验，任何登录用户均可创建数据模型、删除业务数据 | 1. 添加相应的权限校验<br>2. 按数据模型实现细粒度权限控制 |

---

## 中危漏洞（计划修复）

| 编号 | 漏洞类型 | 位置 | 风险描述 | 修复方案 |
|------|----------|------|----------|----------|
| MED-001 | Access Token有效期过长 | `application.yml:53` | Access Token有效期为2小时(7200000ms)，远超建议的15-30分钟，令牌泄露后可被长时间利用 | 缩短AccessToken有效期至15-30分钟，配合Refresh Token使用 |
| MED-002 | 测试接口暴露 | `UserController.java:225-260` | `/users/test/batch-create`和`/users/test/clear`等测试接口在生产环境中也可访问，可被用于批量创建用户或清空数据 | 1. 使用`@Profile("!prod")`限制仅非生产环境可用<br>2. 或添加管理员权限校验 |
| MED-003 | Swagger文档公开访问 | `SecurityConfig.java:44-46` | Swagger UI和API文档接口设置为permitAll，生产环境中暴露完整API结构，辅助攻击者发现弱点 | 1. 生产环境禁用Swagger<br>2. 或添加认证保护，仅允许内部访问 |
| MED-004 | 默认管理员密码 | `data.sql:1-2` | 初始管理员账号admin的密码为固定值（从BCrypt哈希可判断为弱密码），部署后未强制修改 | 1. 首次登录强制修改密码<br>2. 文档明确要求部署后修改默认密码<br>3. 提供随机初始密码生成机制 |
| MED-005 | 重置密码为固定弱密码 | `UserService.java:276-287` | 重置密码功能将密码重置为固定值"123456"，虽标记需下次修改，但仍存在窗口期被利用的风险 | 1. 生成随机临时密码并通过安全渠道通知<br>2. 缩短必须修改密码的窗口期 |
| MED-006 | 缺少请求参数校验注解 | `UserController.java:90`<br>`DataModelController.java:86`<br>`BusinessDataController.java:86` | 部分PUT接口（如updateUser、update数据模型、更新业务数据）的@RequestBody参数缺少`@Valid`注解，后端验证失效 | 1. 为所有@RequestBody参数添加@Valid注解<br>2. 确保DTO中有完整的校验规则 |
| MED-007 | 查询请求DTO无校验注解 | `UserQueryRequest.java`<br>`DataModelQueryRequest.java`<br>`BusinessDataQueryRequest.java` | 查询类DTO完全没有校验注解，pageSize可被设置为极大值导致内存溢出，keyword无长度限制可能影响查询性能 | 1. 添加@Size、@Max、@Min等校验注解<br>2. 限制pageSize最大值（如100）<br>3. 限制keyword最大长度 |
| MED-008 | 审计日志记录不完整 | `AuditLogService` 相关 | 审计日志仅覆盖角色操作，用户、权限、配置、数据模型等关键操作缺少审计记录 | 1. 为所有敏感操作添加审计日志<br>2. 包含操作人、时间、IP、操作类型、新旧值等 |
| MED-009 | 缺少Actuator安全配置 | 未引入spring-boot-starter-actuator | 当前未引入Actuator依赖，风险较低。但若后续引入需注意安全配置 | 引入Actuator后需配置management.endpoints.web.exposure.include和安全认证 |
| MED-010 | 邮箱/手机号格式无校验 | `UserCreateRequest.java`<br>`UserUpdateRequest` | 用户的邮箱、手机号字段缺少格式校验（@Email、@Pattern），可能导致脏数据或被用于注入攻击 | 添加@Email和手机号格式正则校验 |
| MED-011 | 数据库SQL日志输出到控制台 | `application.yml:31-32` | MyBatis Plus配置了`log-impl: StdOutImpl`，SQL语句及参数直接输出到控制台，生产环境可能泄露敏感数据 | 生产环境关闭SQL日志或使用日志框架配置合适的级别 |

---

## 安全评分（1-10 分）

**当前安全评分: 3.5 / 10**

评分说明:
- 认证基础框架尚可（JWT + BCrypt），但关键安全机制缺失
- 权限控制几乎为零，RBAC仅停留在数据层面未落地到接口
- 存在多个严重漏洞（H2控制台、越权、SQL注入风险）
- 安全编码意识不足，敏感信息保护不到位

---

## 最关键的安全风险

**H2数据库控制台对外暴露且空密码，结合所有管理接口无权限控制，攻击者可在无需认证的情况下直接访问数据库获取全部数据，或通过越权访问以普通用户身份执行管理员操作，系统完全失控。**
