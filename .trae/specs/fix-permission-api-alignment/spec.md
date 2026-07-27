# 修复权限与API对接问题 Spec

## Why
登录后导航栏点击各功能模块均显示"无权限"或403错误，前端API路径与后端不匹配导致功能不可用，代码缺少注释，文档缺少日志查看说明。

## What Changes
- 修复前端路由权限码与后端权限码的映射关系
- 修复前端 API 路径与后端 Controller 路径的对应关系
- 补充后端缺失的 API（仪表盘统计、最近日志、配置分组接口等）
- 为所有后端和前端代码添加详细中文注释
- 更新 project-guide.md 添加日志查看说明

## Impact
- Affected specs: build-admin-system
- Affected code: 前端全部 API 模块、路由、后端部分 Controller、Service

## ADDED Requirements

### Requirement: 前后端权限码对齐
系统 SHALL 确保前端路由的 permission 字段与后端数据库中的权限码一致。

#### Scenario: 用户访问用户管理页面
- **WHEN** admin 用户登录后点击"用户管理"导航
- **THEN** 系统允许访问，不跳转403页面

#### Scenario: 用户访问所有功能页面
- **WHEN** admin 用户登录后点击任意导航菜单
- **THEN** 系统允许访问所有功能页面

### Requirement: 前后端API路径对齐
系统 SHALL 确保前端所有 API 请求路径与后端 Controller 路径完全匹配。

#### Scenario: 仪表盘加载
- **WHEN** 用户访问仪表盘页面
- **THEN** 统计数据和最近日志正常加载，无404错误

#### Scenario: 系统配置加载
- **WHEN** 用户访问系统设置页面
- **THEN** 界面配置、安全设置、通知设置数据正常加载

### Requirement: 代码注释
系统代码 SHALL 包含详细的中文注释，说明类、方法、关键逻辑的用途。

#### Scenario: 阅读后端代码
- **WHEN** 开发者打开任意后端 Java 文件
- **THEN** 类和关键方法均有中文注释说明用途

#### Scenario: 阅读前端代码
- **WHEN** 开发者打开任意前端 Vue/TS 文件
- **THEN** 组件和关键函数均有中文注释说明用途

### Requirement: 日志查看文档
project-guide.md SHALL 包含后端和前端运行日志的查看方法。

#### Scenario: 查看后端日志
- **WHEN** 开发者需要排查后端问题
- **THEN** 可按文档指引查看控制台日志或日志文件

## MODIFIED Requirements

### Requirement: 前端路由权限
前端路由 meta.permission 字段 SHALL 使用后端 data.sql 中定义的权限码：
- 用户管理: `system:user`
- 角色管理: `system:role`
- 权限管理: `system:permission`
- 数据模型管理: `system:data-model`
- 业务数据管理: `system:data`
- 界面配置: `settings:ui`
- 安全设置: `settings:security`
- 通知设置: `settings:notification`
- 日志管理: `settings:log`
