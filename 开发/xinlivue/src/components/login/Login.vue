<template>
  <div class="login-bg">
    <div class="login-wrap">
      <div class="login-container">
        <el-card class="login-card" shadow="hover">
          <template #header>
            <div class="title">心理支持与院系风险预警平台</div>
          </template>
          <el-tabs v-model="activeTab" class="login-tabs">
            <el-tab-pane label="登录" name="login">
              <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="110px"
                       @submit.native.prevent
                       @keyup.enter="handleLogin">
                <el-form-item label="账号/手机号" prop="username">
                  <el-input v-model="loginForm.username" placeholder="账号 / 手机号登录" clearable />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                  <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" clearable />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" class="submit-btn" @click="handleLogin">登录</el-button>
                </el-form-item>
                <div class="forget-pwd-row">
                  <span class="forget-text" @click="$router.push('/resetPwd')">忘记密码？</span>
                </div>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="注册账号" name="register">
              <el-form ref="regFormRef" :model="regForm" :rules="regRules" label-width="85px">
                <el-form-item label="登录账号" prop="username">
                  <el-input v-model="regForm.username" placeholder="设置登录用户名" clearable />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="regForm.phone" placeholder="用于登录的手机号" clearable />
                </el-form-item>
                <el-form-item label="设置密码" prop="password">
                  <el-input v-model="regForm.password" type="password" show-password placeholder="密码最少6位" clearable />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPwd">
                  <el-input v-model="regForm.confirmPwd" type="password" show-password placeholder="再次输入密码" clearable />
                </el-form-item>
                <el-form-item>
                  <el-button type="success" class="submit-btn" @click="handleRegister">注册账号</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import $axios from '@/utils/axios'
import { ElMessage } from 'element-plus'
const router = useRouter()
const activeTab = ref('login')
const route = useRoute()
const loginFormRef = ref(null)
const loginForm = reactive({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入账号或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const regFormRef = ref(null)
const regForm = reactive({ username: '', phone: '', password: '', confirmPwd: '' })
const validatePwd = (rule, value, callback) => {
  if (value !== regForm.password) callback(new Error("两次密码输入不一致"))
  else callback()
}
const regRules = {
  username: [{ required: true, message: '请设置登录账号', trigger: 'blur' }],
  phone: [{ required: true, message: '填写手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: "手机号格式错误", trigger: 'blur' }],
  password: [{ required: true, message: '设置密码', trigger: 'blur' }, { min: 6, message: "密码至少6位", trigger: 'blur' }],
  confirmPwd: [{ required: true, message: '确认密码', trigger: 'blur' }, { validator: validatePwd, trigger: 'blur' }]
}
const handleLogin = async () => {
  await loginFormRef.value.validate()
  const res = await $axios.post('/user/login', loginForm)
  if (res.data.code === 200) {
    localStorage.setItem('token', res.data.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.data.user))
    ElMessage.success("登录成功")
    router.push('/psych/index')
  } else {
    ElMessage.error(res.data.msg || "登录失败")
  }
}
const handleRegister = async () => {
  await regFormRef.value.validate()
  const submitData = { username: regForm.username, phone: regForm.phone, password: regForm.password }
  const res = await $axios.post('/user/register', submitData)
  if (res.data.code === 200) {
    ElMessage.success("注册成功，请登录！")
    activeTab.value = 'login'
    loginForm.username = regForm.username
  } else {
    ElMessage.error(res.data.msg)
  }
}
onMounted(()=> {
  if (route.query.resetSuccess === "1") {
    ElMessage.success("密码修改完成，请重新登录")
  }
})
</script>
<style scoped>
.login-bg{
  height: 100vh;
  background-image: url("/bg/XL.jpg");
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  background-color: rgba(0,0,0,0.12);
  background-blend-mode: multiply;
}
.login-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 480px;
  border-radius:14px;
}
.title {
  text-align: center;
  font-size:24px;
  font-weight: 600;
  color:#2c3e50;
}
.submit-btn {
  width: 100%;
  height:42px;
}
.forget-pwd-row{
  text-align:right;
  margin-top:8px;
}
.forget-text{
  color:#409eff;
  cursor:pointer;
  font-size:14px;
}
.forget-text:hover{
  text-decoration:underline;
}
</style>

