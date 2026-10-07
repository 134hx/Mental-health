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
  try {
    await loginFormRef.value.validate()
  } catch (e) {
    return
  }

  try {
    const res = await $axios.post('/user/login', loginForm)
    localStorage.setItem('token', res.data.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.data.user))
    localStorage.setItem('bgm_playing', '1')
    window.dispatchEvent(new Event('bgm-start'))
    ElMessage.success("登录成功")
    router.push('/psych/index')
  } catch (err) {
    ElMessage.error(err?.msg || "登录失败，请检查账号或密码")
  }
}

const handleRegister = async () => {
  try {
    await regFormRef.value.validate()
  } catch (e) {
    return
  }

  const submitData = { username: regForm.username, phone: regForm.phone, password: regForm.password }
  try {
    await $axios.post('/user/register', submitData)
    ElMessage.success("注册成功，请登录！")
    activeTab.value = 'login'
    loginForm.username = regForm.username
  } catch (err) {
    ElMessage.error(err?.msg || "注册失败，请稍后重试")
  }
}

onMounted(()=> {
  if (route.query.resetSuccess === "1") {
    ElMessage.success("密码修改完成，请重新登录")
  }
})
</script>
<style scoped>
.login-bg {
  height: 100vh;
  background-image: url("/bg/XL.jpg");
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  position: relative;
}
.login-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.08);
  pointer-events: none;
}
.login-wrap {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 480px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 40px rgba(126, 200, 227, 0.2);
  border: none;
}
.title {
  text-align: center;
  font-size: 22px;
  font-weight: 600;
  color: #2c5282;
  letter-spacing: 1px;
}
.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 12px;
  font-size: 16px;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #7ec8e3, #5ba8d4);
  border: none;
}
.submit-btn:hover {
  background: linear-gradient(135deg, #6db8d8, #4d9bc7);
}
.forget-pwd-row {
  text-align: right;
  margin-top: 8px;
}
.forget-text {
  color: #5ba8d4;
  cursor: pointer;
  font-size: 14px;
}
.forget-text:hover {
  text-decoration: underline;
}
</style>