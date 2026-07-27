<!--
  根组件
  作为 Vue 应用的顶层组件，仅包含 router-view 用于路由视图渲染
  同时定义全局基础样式（重置样式、字体、背景色等）
-->
<template>
  <router-view />
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 全局字体和渲染优化 */
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #303133;
  background-color: #f5f7fa;
}

/* 页面容器通用样式 */
.page-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: 200px;
}
</style>
