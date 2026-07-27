<template>
  <div class="error-page">
    <h1>403</h1>
    <p>无权限访问</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  color: #909399;
}
.error-page h1 {
  font-size: 72px;
  margin-bottom: 10px;
  color: #e6a23c;
}
</style>
