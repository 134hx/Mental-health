<template>
  <el-header class="psych-header">
    <div class="header-left">
      <span class="title-text">心理支持与院系风险预警平台</span>
    </div>
    <div class="header-right">
      <el-button text class="nav-btn" @click="$router.push('/psych/userinfo')">个人信息</el-button>
      <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
    </div>
  </el-header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import $axios from '@/utils/axios'
const router = useRouter()
const props = defineProps({
  title: {
    type: String,
    default: "心理测评系统"
  }
})
const logout = async () => {
  await $axios.post('/user/logout')
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success("已退出登录")
  router.push('/login')
}
</script>

<style scoped>
.psych-header {
  height: 64px;
  line-height: 64px;
  background: linear-gradient(90deg, #409eff, #722ed1);
  color: #ffffff;
  padding: 0 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}
.header-left .title-text {
  font-size: 20px;
  font-weight: 600;
}
.header-right {
  display: flex;
  gap: 8px;
  align-items: center;
}
.nav-btn {
  color: #fff;
  font-size:15px;
}
.nav-btn:hover {
  color:#e6f4ff !important;
}
</style>




