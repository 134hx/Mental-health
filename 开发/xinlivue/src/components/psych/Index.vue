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
          <div class="func-card">
            <div class="card-icon">🚨</div>
            <div class="card-title">危机识别转介</div>
            <div class="card-desc">心理危机求助与转介通道</div>
          </div>
        </el-col>
        <!-- 第二行 -->
        <el-col :span="8">
          <div class="func-card">
            <div class="card-icon">👥</div>
            <div class="card-title">匿名互助社区</div>
            <div class="card-desc">匿名交流、互相陪伴支持</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="func-card">
            <div class="card-icon">📅</div>
            <div class="card-title">线下预约</div>
            <div class="card-desc">心理咨询线下预约登记</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="func-card">
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
.home-page{
  min-height: calc(100vh - 64px);
  /* ==========背景图片配置========== */
  background-image: url("/bg/XL.jpg");
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  background-color: rgba(0,0,0,0.15);
  background-blend-mode: multiply;

  padding:40px 20px;
}
.container{
  max-width: 1100px;
  margin:0 auto;
}
.page-title{
  text-align:center;
  font-size:28px;
  color:#ffffff; /* 标题改成白色，适配深色背景图 */
  text-shadow: 0 1px 6px rgba(0,0,0,0.25); /* 文字阴影，防止看不清 */
  margin-bottom:40px;
}
.func-card{
  background:#ffffff;
  border-radius:14px;
  padding:30px 20px;
  text-align:center;
  cursor:pointer;
  box-shadow:0 2px 12px rgba(0,0,0,0.08);
  transition: all 0.25s;
  margin-bottom:30px;
}
.func-card:hover{
  transform: translateY(-6px);
  box-shadow:0 8px 20px rgba(64,158,255,0.18);
}
.card-icon{
  font-size:44px;
  margin-bottom:12px;
}
.card-title{
  font-size:18px;
  font-weight:600;
  color:#303133;
  margin-bottom:6px;
}
.card-desc{
  font-size:14px;
  color:#909399;
}
.dialog-footer-custom{
  display:flex;
  justify-content: space-between;
  align-items: center;
}
</style>

