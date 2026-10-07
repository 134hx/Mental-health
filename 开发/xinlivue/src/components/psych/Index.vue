<template>
  <PsychHeader />
  <div class="home-page">
    <div class="container">
      <h2 class="page-title">心理支持与院系风险预警平台</h2>
      <el-row :gutter="30" class="card-wrap">
        <!-- 第一行 -->
        <el-col :span="8">
          <div class="func-card" @click="$router.push('/psych/psyindex')">
            <div class="card-icon">📝</div>
            <div class="card-title">心理测评</div>
            <div class="card-desc">完成心理量表测评</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="func-card" @click="$router.push('/psych/aichat')">
            <div class="card-icon">💬</div>
            <div class="card-title">AI倾诉</div>
            <div class="card-desc">AI心理对话助手</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="func-card" @click="$router.push('/psych/appointment')">
            <div class="card-icon">📅</div>
            <div class="card-title">线下预约</div>
            <div class="card-desc">心理咨询线下预约登记</div>
          </div>
        </el-col>
        <!-- 第二行 -->
        <el-col :span="8">
          <div class="func-card" @click="$router.push('/psych/community')">
            <div class="card-icon">👥</div>
            <div class="card-title">匿名互助社区</div>
            <div class="card-desc">匿名交流、互相陪伴支持</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="func-card" @click="$router.push('/psych/video')">
            <div class="card-icon">🎬</div>
            <div class="card-title">健康心理视频</div>
            <div class="card-desc">缓解情绪、放松身心</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="func-card" @click="$router.push('/psych/science')">
            <div class="card-icon">🧘</div>
            <div class="card-title">正念训练与科普</div>
            <div class="card-desc">正念练习、心理健康科普</div>
          </div>
        </el-col>
      </el-row>
    </div>
    <el-dialog
        v-model="roleDialogVisible"
        title="请选择您的身份"
        width="420px"
        :show-close="false"
        :close-on-click-modal="false"
    >
      <el-radio-group v-model="tempRole">
        <el-radio label="0">学生</el-radio>
        <el-radio label="1">教师</el-radio>
      </el-radio-group>
      <template #footer>
        <div class="dialog-footer-custom">
          <el-button @click="laterSelect">稍后选择</el-button>
          <el-button type="primary" @click="confirmSelect">确认选择</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import $axios from '@/utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import PsychHeader from '@/components/header/PsychHeader.vue'
const router = useRouter()
const roleDialogVisible = ref(false)
const tempRole = ref("0")
// 获取当前登录用户信息
const getUserInfo = async () => {
  try {
    const res = await $axios.get("/user/info")
    const user = res.data.data
    // role为null，弹出选择身份弹窗
    if(user.role === null){
      roleDialogVisible.value = true
    }
  }catch(e){
    console.error("获取用户信息失败",e)
  }
}
// 稍后选择：只关闭弹窗，不保存，下次进入依旧弹窗
const laterSelect = ()=>{
  roleDialogVisible.value = false
}
// 确认选择身份
const confirmSelect = async () => {
  await ElMessageBox.confirm(
      `确认身份为：${tempRole.value === "0" ? "学生" : "教师"}，选择后可在个人信息页修改，确认提交吗？`,
      "确认身份",
      {confirmButtonText:"确认",cancelButtonText:"取消",type:"warning"}
  )
  await $axios.post("/user/saveRole", {
    role: Number(tempRole.value)
  })
  ElMessage.success("身份选择成功！")
  roleDialogVisible.value = false
  // 更新本地存储user
  const localUser = JSON.parse(localStorage.getItem("user"))
  localUser.role = Number(tempRole.value)
  localStorage.setItem("user", JSON.stringify(localUser))
}
onMounted(()=>{
  getUserInfo()
})
</script>
<style scoped>
.home-page {
  min-height: calc(100vh - 64px);
  background-image: url("/bg/XL.jpg");
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  padding: 60px 20px;
  position: relative;
}
.home-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.08);
  pointer-events: none;
}
.home-page > * {
  position: relative;
  z-index: 1;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
}

.page-title {
  text-align: center;
  font-size: 30px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px rgba(60, 100, 130, 0.35);
  margin-bottom: 48px;
}

.func-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 36px 24px;
  text-align: center;
  cursor: pointer;
  box-shadow: 0 4px 24px rgba(140, 160, 180, 0.12);
  transition: all 0.35s ease;
  margin-bottom: 30px;
  border: 1px solid rgba(255, 255, 255, 0.6);
}
.func-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 36px rgba(126, 200, 227, 0.28);
  background: rgba(255, 255, 255, 0.98);
}
.card-icon {
  font-size: 48px;
  margin-bottom: 16px;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.08));
}
.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c5282;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}
.card-desc {
  font-size: 13px;
  color: #718096;
  line-height: 1.6;
}
</style>