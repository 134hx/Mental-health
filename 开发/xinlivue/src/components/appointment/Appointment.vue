<template>
  <PsychHeader title="线下心理咨询预约" />
  <div class="page-bg">
    <div class="page-wrap">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header-bar">
            <span></span>
            <el-button size="small" @click="$router.push('/psych/index')">返回首页</el-button>
          </div>
        </template>
        <el-tabs v-model="activeTab">
          <!-- 0、老师资料卡列表【默认进入】 -->
          <el-tab-pane label="心理老师列表" name="teacherList">
            <div class="profile-card-wrap">
              <div v-if="profileList.length ===0" class="empty-tip">
                暂未老师发布咨询资料卡
              </div>
              <el-card class="teacher-card" v-for="item in profileList" :key="item.id" shadow="hover">
                <div class="card-row">
                  <img v-if="item.avatar" :src="imgUrl(item.avatar)" class="avatar" />
                  <div v-else class="avatar-placeholder">👨‍⚕️</div>
                  <div class="info">
                    <div class="name">{{ item.realName }}</div>
                    <div class="meta">性别：{{ item.gender }}</div>
                    <div class="meta">电话：{{ item.phone ? (item.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')) : '未填写' }}</div>
                    <div class="meta">咨询地址：{{ item.address || '暂未填写' }}</div>
                    <div class="meta">擅长：{{ item.goodAt || "暂无填写" }}</div>
                    <div class="meta">简介：{{ item.intro || "暂无简介" }}</div>
                  </div>
                </div>
                <div class="card-footer">
                  <el-button v-if="userRole !== 1" type="primary" size="small" @click="openBookDialog(item)">预约该老师</el-button>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
          <!-- 老师专属tab：编辑自己的资料卡 -->
          <el-tab-pane v-if="userRole ===1" label="我的资料卡" name="editProfile">
            <el-form ref="profileFormRef" :model="profileForm" label-width="120px">
              <el-form-item label="真实姓名">
                <el-input :value="userInfo.realName || '未填写'" disabled></el-input>
              </el-form-item>
              <el-form-item label="性别">
                <el-input :value="userInfo.sex || '未填写'" disabled></el-input>
              </el-form-item>
              <el-form-item label="手机号">
                <el-input :value="userInfo.phone || '未填写'" disabled></el-input>
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input v-model="profileForm.intro" type="textarea" rows="5" placeholder="介绍自己的咨询方向、工作经历" />
              </el-form-item>
              <el-form-item label="擅长方向">
                <el-input v-model="profileForm.goodAt" placeholder="填写自己擅长的方向，例：学业压力,情绪调节,人际困扰" />
              </el-form-item>
              <el-form-item label="咨询地址">
                <el-input v-model="profileForm.address" placeholder="填写线下咨询地址" />
              </el-form-item>

              <el-divider />
              <div>
                <div style="display:flex;gap:24px;align-items:center;margin-bottom:10px;">
                  <span style="font-weight:bold">可预约时间段</span>
                  <el-button type="success" size="small" @click="addTimeSlot">新增时间段</el-button>
                </div>
                <el-form-item>
                  <div v-for="(slot,idx) in slotList" :key="idx" style="display:flex;gap:12px;align-items:center;margin-bottom:8px;flex-wrap:wrap;">
                    <el-select v-model="slot.weekDay" style="width:110px">
                      <el-option label="周一" value="周一"/>
                      <el-option label="周二" value="周二"/>
                      <el-option label="周三" value="周三"/>
                      <el-option label="周四" value="周四"/>
                      <el-option label="周五" value="周五"/>
                      <el-option label="周六" value="周六"/>
                      <el-option label="周日" value="周日"/>
                    </el-select>
                    <el-time-picker v-model="slot.startTime" format="HH:mm" value-format="HH:mm" placeholder="开始时间" />
                    <span>—</span>
                    <el-time-picker v-model="slot.endTime" format="HH:mm" value-format="HH:mm" placeholder="结束时间" />
                    <el-button size="small" type="danger" @click="removeTimeSlot(idx)">删除</el-button>
                  </div>
                  <div v-if="slotList.length===0" style="color:#999;margin:8px 0">尚未设置时间段，学生将无法预约您</div>
                </el-form-item>
              </div>
            </el-form>
            <div style="display:flex;justify-content:flex-end;padding-right:20px;margin-top:10px;">
              <el-button type="primary" @click="saveProfile">保存并发布资料卡</el-button>
            </div>
          </el-tab-pane>
          <!-- 学生：我的预约记录 -->
          <el-tab-pane v-if="userRole === 0" label="我的预约记录" name="my">
            <el-table :data="myList" border stripe>
              <el-table-column prop="teacherName" label="预约老师" />
              <el-table-column label="老师联系电话">
                <template #default="scope">
                  <span v-if="scope.row.status === 1 || scope.row.status === 2">
                    {{ scope.row.teacherPhone || '未填写' }}
                  </span>
                  <span v-else>
                    {{ scope.row.teacherPhone ? scope.row.teacherPhone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '未填写' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="teacherAddress" label="咨询地址">
                <template #default="scope">
                  {{ scope.row.teacherAddress || "暂未填写" }}
                </template>
              </el-table-column>
              <el-table-column label="预约时间">
                <template #default="scope">
                  {{ scope.row.appointTime || '' }}
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" />
              <el-table-column label="状态">
                <template #default="scope">
                  <span :class="statusClass(scope.row.status)">{{ statusText(scope.row.status) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button
                      v-if="scope.row.status === 0"
                      size="small"
                      type="danger"
                      @click="cancelAppoint(scope.row.id)"
                  >取消预约</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="myList.length === 0" description="暂无预约记录"></el-empty>
          </el-tab-pane>
          <!-- 老师视图：收到的预约申请 -->
          <el-tab-pane v-if="userRole === 1" label="收到预约申请" name="teacher">
            <el-table :data="teacherReceiveList" border stripe>
              <el-table-column label="预约时间">
                <template #default="scope">
                  {{ scope.row.appointTime || '' }}
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="学生备注" />
              <el-table-column label="状态">
                <template #default="scope">
                  <span :class="statusClass(scope.row.status)">{{ statusText(scope.row.status) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button v-if="scope.row.status ===0" size="small" type="success" @click="agree(scope.row.id)">同意预约</el-button>
                  <el-button v-if="scope.row.status ===1" size="small" type="primary" @click="finish(scope.row.id)">完成预约</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="teacherReceiveList.length===0" description="暂无预约申请"></el-empty>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
    <!-- ========== 预约弹窗 ========== -->
    <el-dialog v-model="bookDialogVisible" title="预约咨询" width="580px">
      <el-form ref="formRef" :model="form" label-width="110px">
        <el-form-item label="预约老师">
          <el-input v-model="form.teacherName" disabled></el-input>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker v-model="form.appointDate" type="date" placeholder="选择预约日期" style="width:100%" @change="onDateChange"/>
        </el-form-item>
        <el-form-item label="可选时间段">
          <el-select v-model="form.slotId" placeholder="请先选择日期" style="width:100%">
            <el-option v-for="opt in slotOptions" :key="opt.id" :label="`${opt.weekDay} ${opt.startTime}~${opt.endTime}`" :value="opt.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="咨询问题备注">
          <el-input v-model="form.remark" type="textarea" rows="4" placeholder="简单描述你的困扰，方便老师提前了解" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bookDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">提交预约申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import $axios from '@/utils/axios'
import { imgUrl } from '@/utils/imgUrl'
import { ElMessage, ElMessageBox } from 'element-plus'
import PsychHeader from '@/components/header/PsychHeader.vue'
const route = useRoute()
const activeTab = ref('teacherList')
const userRole = ref(0)
const bookDialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  teacherId: null,
  teacherName: '',
  appointDate: null,
  slotId: null,
  remark: ''
})
const userInfo = ref({})
const profileFormRef = ref(null)
const profileForm = ref({
  intro:'',
  goodAt:'',
  address:''
})
const slotList = ref([])
const profileList = ref([])
const myList = ref([])
const teacherReceiveList = ref([])
const allTeacherSlots = ref([])
const slotOptions = ref([])

const onDateChange = ()=>{
  slotOptions.value = []
  form.value.slotId = null
  if(!form.value.appointDate) return
  const d = new Date(form.value.appointDate)
  const weekIdx = d.getDay()
  const weekMap = ['周日','周一','周二','周三','周四','周五','周六']
  const selectWeek = weekMap[weekIdx]
  slotOptions.value = allTeacherSlots.value.filter(item=> item.weekDay === selectWeek)
}

onMounted(async ()=>{
  if(route.query.teacherId){
    const targetTeacher = profileList.value.find(item=> item.userId === Number(route.query.teacherId))
    if(targetTeacher){
      openBookDialog(targetTeacher)
    }
  }
  const localUser = JSON.parse(localStorage.getItem('user')||'{}')
  userRole.value = localUser.role ?? 0
  const userRes = await $axios.get('/user/info')
  if(userRes.data.code ===200){
    userInfo.value = userRes.data.data
  }
  await loadTeacherProfileList()
  await loadMyAppointment()
  if(userRole.value ===1){
    await loadTeacherReceive()
    await loadMyProfile()
    await loadMySlot()
  }
})

const openBookDialog = async (teacherItem)=>{
  form.value.teacherId = teacherItem.userId
  form.value.teacherName = teacherItem.realName
  form.value.appointDate = null
  form.value.slotId = null
  form.value.remark = ''
  slotOptions.value = []
  bookDialogVisible.value = true
  const res = await $axios.get(`/teacherSlot/getByTeacherId/${teacherItem.userId}`)
  if(res.data.code ===200){
    allTeacherSlots.value = res.data.data
  }
}

const loadMySlot = async ()=>{
  const res = await $axios.get("/teacherSlot/my")
  if(res.data.code ===200){
    slotList.value = res.data.data
  }
}
const addTimeSlot = ()=>{
  slotList.value.push({weekDay:'周一',startTime:'09:00',endTime:'10:00'})
}
const removeTimeSlot = (idx)=>{
  slotList.value.splice(idx,1)
}

const loadTeacherProfileList = async ()=>{
  const res = await $axios.get('/appointment/profile/list')
  if(res.data.code ===200){
    profileList.value = res.data.data
  }
}
const loadMyProfile = async ()=>{
  const res = await $axios.get('/appointment/profile/my')
  if(res.data.code ===200 && res.data.data){
    profileForm.value.intro = res.data.data.intro
    profileForm.value.goodAt = res.data.data.goodAt
    profileForm.value.address = res.data.data.address
  }
}
const saveProfile = async ()=>{
  if(!userInfo.value.realName){
    return ElMessage.warning("请先前往个人中心完善真实姓名！")
  }
  if(!userInfo.value.sex){
    return ElMessage.warning("请先前往个人中心完善性别！")
  }
  if(!userInfo.value.phone){
    return ElMessage.warning("请先前往个人中心完善手机号！")
  }
  await $axios.post('/appointment/profile/save',profileForm.value)
  await $axios.post("/teacherSlot/saveBatch",slotList.value)
  ElMessage.success("资料卡保存发布成功！学生端即可看见你的资料")
  await loadTeacherProfileList()
}
const loadMyAppointment = async ()=>{
  const res = await $axios.get('/appointment/myList')
  if(res.data.code ===200){
    myList.value = res.data.data
  }
}
const loadTeacherReceive = async ()=>{
  const res = await $axios.get('/appointment/teacherList')
  if(res.data.code ===200){
    teacherReceiveList.value = res.data.data
  }
}
const submit = async ()=>{
  if(!form.value.appointDate){
    return ElMessage.warning("请选择预约日期")
  }
  if(!form.value.slotId){
    return ElMessage.warning("请选择该日期下的可预约时间段")
  }
  const d = new Date(form.value.appointDate)
  const year = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const body = {
    teacherId: form.value.teacherId,
    appointDate: `${year}-${m}-${day}`,
    slotId: form.value.slotId,
    remark: form.value.remark
  }
  try {
    await $axios.post('/appointment/save', body)
    ElMessage.success("预约申请提交成功")
    bookDialogVisible.value = false
    await loadMyAppointment()
  } catch (err) {
    console.error(err)
    ElMessage.error("预约提交失败：" + (err.response?.data?.msg || "服务器异常"))
  }
}

const cancelAppoint = async (id)=>{
  await ElMessageBox.confirm('确认取消这条预约？','提示',{
    type:'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  await $axios.post('/appointment/updateStatus',null,{params:{id,status:3}})
  ElMessage.success("已取消预约")
  await loadMyAppointment()
}
const agree = async (id)=>{
  await $axios.post('/appointment/updateStatus',null,{params:{id,status:1}})
  ElMessage.success("已同意预约")
  await loadTeacherReceive()
}
const finish = async (id)=>{
  await $axios.post('/appointment/updateStatus',null,{params:{id,status:2}})
  ElMessage.success("标记完成")
  await loadTeacherReceive()
}

const statusText = (s)=>{
  const map = {0:'待确认',1:'已同意',2:'已完成',3:'已取消'}
  return map[s]||'未知'
}
const statusClass = (s)=>{
  const clsMap = {
    0:'text-orange-500',
    1:'text-green-600',
    2:'text-blue-600',
    3:'text-gray-500'
  }
  return clsMap[s]||''
}
</script>
<style scoped>
.page-bg {
  min-height: calc(100vh - 64px);
  background-image: url("/bg/XL.jpg");
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  background-color: #f5f7fa;
  padding: 24px 0;
  position: relative;
}
/* ★ 只压一点亮度，不模糊 */
.page-bg::before {
  content: '';
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.08);
  pointer-events: none;
  z-index: 0;
}
.page-bg > * {
  position: relative;
  z-index: 1;
}
.page-wrap{
  padding:24px;
  max-width:1100px;
  margin:0 auto;
}
.card-header-bar{
  display:flex;
  justify-content: space-between;
  align-items:center;
}
.profile-card-wrap{
  display:flex;
  flex-wrap:wrap;
  gap:18px;
  padding:10px 0;
}
.teacher-card{
  width:340px;
}
.card-row{
  display:flex;
  gap:14px;
}
.avatar{
  width:70px;
  height:70px;
  border-radius:50%;
  object-fit:cover;
}
.avatar-placeholder{
  width:70px;
  height:70px;
  border-radius:50%;
  background:#eee;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size:28px;
}
.info{
  flex:1;
}
.name{
  font-size:17px;
  font-weight:600;
  margin-bottom:4px;
}
.meta{
  font-size:14px;
  color:#555;
  margin-bottom:3px;
}
.card-footer{
  margin-top:14px;
  text-align:right;
}
.empty-tip{
  width:100%;
  text-align:center;
  padding:40px 0;
  color:#999;
}
</style>