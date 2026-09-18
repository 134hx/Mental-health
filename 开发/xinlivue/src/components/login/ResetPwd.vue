<template>
  <div class="reset-bg">
    <div class="reset-wrap">
      <div class="reset-container">
        <el-card class="reset-card" shadow="hover">
          <template #header>
            <div class="title">修改密码</div>
          </template>
          <!-- 第一步：输入账号/手机号 -->
          <el-form v-if="step===1" ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="120px">
            <el-form-item label="账号/手机号" prop="account">
              <el-input v-model="accountForm.account" placeholder="请输入账号或者绑定手机号" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="submit-btn" @click="checkAccountNext">下一步</el-button>
            </el-form-item>
            <div class="back-row">
              <el-button @click="handleCancel">取消</el-button>
            </div>
          </el-form>
          <!--第二步：新密码、确认密码-->
          <el-form v-if="step===2" ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="密码至少6位" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPwd">
              <el-input v-model="pwdForm.confirmPwd" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button @click="backStep1">上一步</el-button>
              <el-button @click="handleCancel">取消</el-button>
              <el-button type="primary" @click="submitReset">确认修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>
<script setup>
import {ref,reactive,computed} from 'vue'
import {useRouter,useRoute} from 'vue-router'
import $axios from '@/utils/axios'
import {ElMessage} from 'element-plus'
const router=useRouter()
const route = useRoute()
//步骤 1输入账号/手机号，2设置新密码
const step=ref(1)
const tempAccount=ref('') //保存校验通过的账号/手机号
// 判断来源：是否来自个人信息页面
const isFromUserInfo = computed(()=>{
  return route.query.from === 'userinfo'
})
//第一步表单 ref和模板保持一致 accountFormRef
const accountFormRef=ref(null)
const accountForm=reactive({account:''})
const accountRules={
  account:[
    {required:true,message:"请输入账号或手机号",trigger:"blur"}
  ]
}
//第二步表单
const pwdFormRef=ref(null)
const pwdForm=reactive({newPassword:'',confirmPwd:''})
const validateConfirm=(rule,value,callback)=>{
  if(value!==pwdForm.newPassword){
    callback(new Error("两次输入密码不一致"))
  }else{
    callback()
  }
}
const pwdRules={
  newPassword:[
    {required:true,message:"请输入新密码",trigger:"blur"},
    {min:6,message:"密码至少6位",trigger:"blur"}
  ],
  confirmPwd:[
    {required:true,message:"请确认密码",trigger:"blur"},
    {validator:validateConfirm,trigger:"blur"}
  ]
}
// 统一取消按钮逻辑：根据来源跳转不同页面
const handleCancel = ()=>{
  if(isFromUserInfo.value){
    // 个人信息过来 → 返回个人信息页
    router.push('/psych/userinfo')
  }else{
    // 登录忘记密码过来 → 返回登录页
    router.push('/login')
  }
}
//校验账号/手机号，进入第二步
const checkAccountNext=async ()=>{
  if(!accountFormRef.value) return
  try {
    await accountFormRef.value.validate()
    const res=await $axios.post("/user/checkAccount", accountForm)
    if(res.data.code===200){
      tempAccount.value = accountForm.account
      step.value=2
      ElMessage.success("请设置新密码")
    }else{
      ElMessage.warning(res.data.msg || "账号不存在，请重新输入")
    }
  }catch(err){
    ElMessage.error("校验失败，请检查输入")
  }
}
//返回第一步
const backStep1=()=>{
  step.value=1
  pwdForm.newPassword=''
  pwdForm.confirmPwd=''
}
//提交重置密码：无论来自哪里，修改成功全部清空本地存储，跳登录页重新登录
const submitReset=async ()=>{
  if(!pwdFormRef.value) return
  await pwdFormRef.value.validate()
  const body={
    account: tempAccount.value,
    newPassword: pwdForm.newPassword
  }
  await $axios.post("/user/resetPassword",body)
  // 清空本地token和用户信息，强制重新登录
  localStorage.removeItem("token")
  localStorage.removeItem("user")
  ElMessage.success("密码修改成功，请重新登录")
  // 统一跳登录页
  router.push({path:"/login", query:{resetSuccess:"1"}})
}
</script>
<style scoped>
.reset-bg{
  height:100vh;
  background-image: url("/bg/XL.jpg");
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  background-color: rgba(0,0,0,0.12);
  background-blend-mode: multiply;
}
.reset-wrap{
  height:100%;
  display:flex;
  align-items:center;
  justify-content:center;
}
.reset-card{
  width:460px;
  border-radius:14px;
}
.title{
  text-align:center;
  font-size:22px;
  font-weight:600;
  color:#2c3e50;
}
.submit-btn{
  width:100%;
}
.back-row{
  text-align:right;
  margin-top:8px;
}
</style>

