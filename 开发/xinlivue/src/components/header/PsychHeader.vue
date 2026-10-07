<template>
  <el-header class="psych-header">
    <div class="header-left">
      <span class="title-text">心理支持与院系风险预警平台</span>
    </div>

    <div class="header-right">
      <div class="user-entry" @click="$router.push('/psych/userinfo')">
        <img
            v-if="avatarUrl"
            :src="avatarUrl"
            class="user-avatar"
            alt="头像"
        />
        <div v-else class="user-avatar user-avatar-placeholder">
          {{ userInitial }}
        </div>
        <span class="user-name">个人信息</span>
      </div>

      <el-button type="danger" plain size="small" class="logout-btn" @click="logout">
        退出登录
      </el-button>
    </div>
  </el-header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import $axios from '@/utils/axios'
import { imgUrl } from '@/utils/imgUrl'

const router = useRouter()

const currentUser = ref(JSON.parse(localStorage.getItem('user') || 'null'))

const refreshUser = () => {
  currentUser.value = JSON.parse(localStorage.getItem('user') || 'null')
}

const avatarUrl = computed(() => {
  const a = currentUser.value?.avatar
  return a ? imgUrl(a) : ''
})

const userInitial = computed(() => {
  const u = currentUser.value
  if (!u) return '?'
  const name = u.nickname || u.realName || u.username || ''
  return name.charAt(0).toUpperCase() || '?'
})

const logout = async () => {
  try {
    await $axios.post('/user/logout')
  } catch (e) { /* 忽略 */ }
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('bgm_playing')
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  window.addEventListener('user-updated', refreshUser)
})
onUnmounted(() => {
  window.removeEventListener('user-updated', refreshUser)
})
</script>

<style scoped>
.psych-header {
  height: 64px;
  line-height: 64px;
  /* ★ 改成深青蓝渐变：白字清晰 */
  background: linear-gradient(90deg, #4a90b8 0%, #5ba8d4 50%, #7ec8e3 100%);
  color: #ffffff;
  padding: 0 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(74, 144, 184, 0.25);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left .title-text {
  font-size: 19px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);  /* ★ 加阴影，白字更清晰 */
}

.header-right {
  display: flex;
  gap: 16px;
  align-items: center;
}

/* ★ 用户入口（头像 + 文字一块点击） */
.user-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  cursor: pointer;
  transition: background 0.2s ease;
  user-select: none;
}
.user-entry:hover {
  background: rgba(255, 255, 255, 0.18);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.75);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  background: #fff;
  flex-shrink: 0;
}
.user-avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #4a90b8;
  background: rgba(255, 255, 255, 0.92);
  line-height: 1;
}

.user-name {
  color: #ffffff;
  font-size: 15px;
  font-weight: 500;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.logout-btn {
  background: rgba(255, 255, 255, 0.18) !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
  color: #fff !important;
}
.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  border-color: rgba(255, 255, 255, 0.7) !important;
  color: #fff !important;
}
</style>