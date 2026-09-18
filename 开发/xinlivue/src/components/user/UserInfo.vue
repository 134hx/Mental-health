<template>
  <PsychHeader title="个人信息中心" />
  <div class="page-bg">
    <div class="page-wrap">
      <el-card class="page-card" shadow="hover">
        <template #header>
          <div class="card-header-row">
            <div class="card-header-title">👤 个人资料</div>
            <el-button size="small" @click="$router.push('/psych/index')">返回首页</el-button>
          </div>
        </template>
        <el-form ref="userFormRef" :model="form" label-width="110px">
          <el-form-item label="头像">
            <el-upload
                action="http://localhost:8080/common/upload"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeUpload"
            >
              <img v-if="form.avatar" :src="`http://localhost:8080/${form.avatar}`" class="avatar-img" />
              <div v-else class="avatar-placeholder">
                <el-icon size="40"><User /></el-icon>
              </div>
            </el-upload>
          </el-form-item>
          <el-form-item label="登录账号">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="请设置昵称" />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="form.realName" placeholder="填写真实姓名" />
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="form.sex">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
              <el-option label="保密" value="保密" />
            </el-select>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="手机号" />
          </el-form-item>
          <el-form-item label="身份">
            <el-select v-model="form.role" placeholder="请选择身份">
              <el-option label="学生" :value="0" />
              <el-option label="教师" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitUpdate">保存修改</el-button>
            <el-button type="success" @click="goRecord">查看测评记录</el-button>
            <el-button type="warning" @click="$router.push({path:'/resetPwd', query:{from:'userinfo'}})">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import $axios from '@/utils/axios'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import PsychHeader from '@/components/header/PsychHeader.vue'
const router = useRouter()
const userFormRef = ref(null)
const form = reactive({
  id: null, username: '', nickname: '', realName: '', sex: '', phone: '', avatar: '', role: null
})
const uploadHeaders = computed(() => {
  const tokenVal = localStorage.getItem('token')
  return tokenVal ? { token: tokenVal } : {}
})
const getUserInfo = async () => {
  const res = await $axios.get('/user/info')
  Object.assign(form, res.data.data)
}
const handleAvatarSuccess = (res) => {
  if(res.code === 200){
    form.avatar = res.data
    ElMessage.success("头像上传成功")
  }else{
    ElMessage.error(res.msg || "上传失败")
  }
}
const beforeUpload = (file) => {
  const isImg = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImg) { ElMessage.error("只能上传jpg/png图片"); return false }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { ElMessage.error("图片不能大于5MB"); return false }
  return true
}
const submitUpdate = async () => {
  await $axios.post('/user/updateInfo', form)
  ElMessage.success("个人信息修改成功")
  let localUserStr = localStorage.getItem("user")
  let localUser = localUserStr ? JSON.parse(localUserStr) : {}
  Object.assign(localUser, form)
  localStorage.setItem("user", JSON.stringify(localUser))
}
const goRecord = () => {
  router.push("/psych/record")
}
onMounted(() => {
  getUserInfo()
})
</script>
<style scoped>
.page-bg{
  min-height: calc(100vh - 64px);
  background-image: url("/bg/XL.jpg");
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  background-color: rgba(0,0,0,0.12);
  background-blend-mode: multiply;
}
.page-wrap {
  padding:24px;
  max-width:760px;
  margin:0 auto;
}
.page-card {
  border-radius:12px;
}
.card-header-row{
  display:flex;
  justify-content:space-between;
  align-items:center;
}
.card-header-title {
  font-size:18px;
  font-weight:600;
}
.avatar-img {
  width:110px;
  height:110px;
  object-fit:cover;
  border-radius:10px;
}
.avatar-placeholder {
  width:110px;
  height:110px;
  border:1px dashed #c0c4cc;
  border-radius:10px;
  display:flex;
  align-items:center;
  justify-content:center;
  color:#909399;
}
</style>

