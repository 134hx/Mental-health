<template>
  <PsychHeader title="匿名互助社区" />
  <div class="page-bg">
    <div class="page-wrap">
      <el-card shadow="hover">
        <template #header>
          <div class="header-box">
            <div>
              <h2>匿名互助社区</h2>
              <p class="tip-text">tips:&nbsp;&nbsp;在该社区你将会始终以匿名的形式活动，你可以畅所欲言，但请绿色发言，维护社区良好风气。</p>
              <p class="tip-text">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;唯有老师在该社区实名，点击老师头像可直接预约老师。</p>
            </div>
            <!-- 右侧垂直容器 -->
            <div class="header-right-wrap">
              <!-- 第一排按钮 -->
              <div style="display:flex;gap:12px;justify-content:flex-end;">
                <el-button type="primary" @click="openPostDialog()">发布帖子</el-button>
                <el-button @click="$router.push('/psych/index')">返回首页</el-button>
              </div>
              <!-- 第二排：下拉选择框 -->
              <div style="margin-top:10px;">
                <el-select v-model="postFilterType" size="small" style="width:90px;" @change="val=>console.log('筛选变更',val)">
                  <el-option
                      v-for="opt in filterOptions"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                  />
                </el-select>
              </div>
            </div>
          </div>
        </template>
        <!--帖子列表 使用计算属性showPostList-->
        <div class="post-list">
          <el-card class="post-item" v-for="item in showPostList" :key="item.id">
            <div class="post-header">
              <el-avatar
                  :size="44"
                  :src="getAvatarSrc(item.showAvatar, item.role)"
                  :style="item.role===1 ? 'cursor:pointer' : ''"
                  @click="item.role===1 && openTeacherDialog(item)"
                  fit="cover"
              >
                {{ item.role === 1 ? item.realName?.charAt(0) : '' }}
              </el-avatar>
              <div class="post-info">
                <div class="name-row">
                  <span class="nick">{{ item.showNickname }}</span>
                  <span v-if="item.role ===1" class="tag-teacher">老师</span>
                </div>
                <div class="time">{{ item.createTime }}</div>
              </div>
            </div>
            <div class="post-title">{{ item.title }}</div>
            <div class="post-content">{{ item.content }}
              <div class="post-content-footer">
                <el-button
                    v-if="item.isMine"
                    link
                    type="danger"
                    size="small"
                    @click="delPost(item.id)"
                >
                  删除帖子
                </el-button>
              </div>
            </div>
            <!--评论区域-->
            <div class="comment-wrap">
              <div class="comment-input-row">
                <el-input v-model="tempCommentMap[item.id]" placeholder="写下你的评论..." size="small"></el-input>
                <el-button size="small" type="primary" @click="submitComment(item.id)">发表</el-button>
              </div>
              <div v-if="commentMap[item.id] && commentMap[item.id].length>0">
                <template v-if="showAllCommentMap[item.id]">
                  <div class="comment-list">
                    <div class="comment-item" v-for="c in commentMap[item.id]" :key="c.id">
                      <el-avatar
                          :size="32"
                          :src="getAvatarSrc(c.showAvatar,c.role)"
                          :style="c.role===1 ? 'cursor:pointer' : ''"
                          @click="c.role===1 && openTeacherDialog(c)"
                          fit="cover"
                      >
                        {{ c.role ===1 ? c.realName?.charAt(0):'' }}
                      </el-avatar>
                      <div class="comment-body">
                        <div class="comment-header">
                          <div class="name-row">
                            <span class="nick">{{ c.showNickname }}</span>
                            <span v-if="c.role ===1" class="tag-teacher">老师</span>
                          </div>
                          <span class="time">{{ c.createTime }}</span>
                          <el-button v-if="c.isMine" link type="danger" size="small" @click="delComment(c.id,item.id)">删除</el-button>
                        </div>
                        <div class="comment-text">{{ c.content }}</div>
                      </div>
                    </div>
                  </div>
                </template>
                <template v-else>
                  <div class="comment-list">
                    <div class="comment-item" v-for="c in commentMap[item.id].slice(0,3)" :key="c.id">
                      <el-avatar
                          :size="32"
                          :src="getAvatarSrc(c.showAvatar,c.role)"
                          :style="c.role===1 ? 'cursor:pointer' : ''"
                          @click="c.role===1 && openTeacherDialog(c)"
                          fit="cover"
                      >
                        {{ c.role ===1 ? c.realName?.charAt(0):'' }}
                      </el-avatar>
                      <div class="comment-body">
                        <div class="comment-header">
                          <div class="name-row">
                            <span class="nick">{{ c.showNickname }}</span>
                            <span v-if="c.role ===1" class="tag-teacher">老师</span>
                          </div>
                          <span class="time">{{ c.createTime }}</span>
                          <el-button v-if="c.isMine" link type="danger" size="small" @click="delComment(c.id,item.id)">删除</el-button>
                        </div>
                        <div class="comment-text">{{ c.content }}</div>
                      </div>
                    </div>
                  </div>
                </template>
                <div v-if="commentMap[item.id].length > 3" class="expand-btn-wrap">
                  <el-button link size="small" @click="toggleCommentExpand(item.id)">
                    {{ showAllCommentMap[item.id] ? '收起评论' : `展开全部${commentMap[item.id].length}条评论` }}
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-card>
      <!-- ==========发帖弹窗========== -->
      <el-dialog v-model="postDialogVisible" title="发布帖子" width="600px">
        <el-form :model="postForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="postForm.title" placeholder="请输入标题" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="postForm.content" type="textarea" rows="8" placeholder="写下你的困惑，学生身份将匿名发布" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="postDialogVisible=false">取消</el-button>
          <el-button type="primary" @click="submitPost">发布</el-button>
        </template>
      </el-dialog>
      <!-- ==========老师资料卡弹窗========== -->
      <el-dialog v-model="dialogTeacherInfo" title="老师资料卡" width="440px">
        <div style="text-align:center;margin-bottom:16px;">
          <el-avatar :size="80" :src="getAvatarSrc(currTeacher.avatar,1)" fit="cover">
            {{ currTeacher.realName?.charAt(0) }}
          </el-avatar>
          <div style="font-size:18px;font-weight:bold;margin-top:8px;">{{ currTeacher.realName }}</div>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="性别">{{ currTeacher.gender || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="擅长方向">{{ currTeacher.goodAt || "未填写" }}</el-descriptions-item>
          <el-descriptions-item label="简介">{{ currTeacher.introduction || "未填写" }}</el-descriptions-item>
          <el-descriptions-item label="咨询地址">{{ currTeacher.address || "未填写" }}</el-descriptions-item>
        </el-descriptions>
        <template #footer>
          <el-button @click="dialogTeacherInfo = false">关闭</el-button>
          <el-button v-if="loginUserRole === 0" type="primary" @click="handleGoBook">预约该老师</el-button>
        </template>
      </el-dialog>
      <!-- ==========预约咨询弹窗========== -->
      <el-dialog v-model="bookDialogVisible" title="预约咨询" width="580px">
        <el-form ref="formRef" :model="bookForm" label-width="110px">
          <el-form-item label="预约老师">
            <el-input v-model="bookForm.teacherName" disabled></el-input>
          </el-form-item>
          <el-form-item label="预约日期">
            <el-date-picker v-model="bookForm.appointDate" type="date" placeholder="选择预约日期" style="width:100%" @change="onDateChange" />
          </el-form-item>
          <el-form-item label="可选时间段">
            <el-select v-model="bookForm.slotId" placeholder="请先选择日期" style="width:100%">
              <el-option v-for="opt in slotOptions" :key="opt.id" :label="`${opt.weekDay} ${opt.startTime}~${opt.endTime}`" :value="opt.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="咨询问题备注">
            <el-input v-model="bookForm.remark" type="textarea" rows="4" placeholder="简单描述你的困扰，方便老师提前了解" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="bookDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitBook">提交预约申请</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>
<script setup>
import PsychHeader from '@/components/header/PsychHeader.vue'
import {ref,onMounted, computed} from 'vue'
import { useRouter } from 'vue-router'
import $axios from '@/utils/axios'
import { imgUrl } from '@/utils/imgUrl'
import {ElMessage, ElMessageBox} from 'element-plus'
const router = useRouter()
const loginUserRole = ref(localStorage.getItem("userRole") ? Number(localStorage.getItem("userRole")) : null)
// 头像工具函数：统一走 imgUrl
const getAvatarSrc = (avatarStr, role) => {
  if (role === 0) {
    return '/vo/niming.png'
  }
  if (!avatarStr) {
    return '/vo/default-teacher.png'
  }
  return imgUrl(avatarStr)
}
//帖子
const postList = ref([])
//帖子过滤：all全部 / mine我的帖子
const postFilterType = ref('all')
const filterOptions = ref([
  { label: '全部帖子', value: 'all' },
  { label: '我的帖子', value: 'mine' }
])
const showPostList = computed(()=>{
  if(postFilterType.value === 'mine'){
    return postList.value.filter(p=> p.isMine)
  }
  return postList.value
})
const postDialogVisible = ref(false)
const postForm = ref({ title:'', content:'' })
//评论
const commentMap = ref({})
const tempCommentMap = ref({})
const showAllCommentMap = ref({})
//老师弹窗
const dialogTeacherInfo = ref(false)
const currTeacher = ref({})
//预约弹窗
const bookDialogVisible = ref(false)
const bookForm = ref({
  teacherId: null,
  teacherName: '',
  appointDate: null,
  slotId: null,
  remark: ''
})
const allTeacherSlots = ref([])
const slotOptions = ref([])
const toggleCommentExpand = (postId)=>{
  showAllCommentMap.value[postId] = !showAllCommentMap.value[postId]
}
const loadComment = async (postId)=>{
  const res = await $axios.get('/community/comment/list',{params:{postId}})
  if(res.data.code===200){
    commentMap.value[postId] = res.data.data
    showAllCommentMap.value[postId] = false
  }
}
const submitComment = async (postId)=>{
  const content = tempCommentMap.value[postId]
  if(!content || !content.trim()){
    return ElMessage.warning("请输入评论内容")
  }
  try{
    await $axios.post('/community/comment/add',{postId,content:content.trim()})
    tempCommentMap.value[postId]=''
    ElMessage.success("评论成功")
    await loadComment(postId)
  }catch(err){
    if(err.msg){
      ElMessage.error(err.msg)
    }else{
      ElMessage.error("网络异常，请稍后重试")
    }
  }
}
const delComment = async (commentId,postId)=>{
  await ElMessageBox.confirm("确认删除这条评论？","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:'warning'})
  await $axios.post(`/community/comment/delete/${commentId}`)
  ElMessage.success("已删除")
  await loadComment(postId)
}
const loadPostList = async ()=>{
  const res = await $axios.get('/community/post/list')
  if(res.data.code ===200){
    postList.value = res.data.data
    for(let p of postList.value){
      await loadComment(p.id)
    }
  }
}
const openPostDialog = ()=>{
  postForm.value.title=''
  postForm.value.content=''
  postDialogVisible.value=true
}
const submitPost = async ()=>{
  if(!postForm.value.title){
    return ElMessage.warning("请输入标题")
  }
  if(!postForm.value.content){
    return ElMessage.warning("请输入帖子内容")
  }
  try{
    await $axios.post('/community/post/save',postForm.value)
    ElMessage.success("发帖成功")
    postDialogVisible.value = false
    await loadPostList()
  }catch(err){
    if(err.msg){
      ElMessage.error(err.msg)
    }else{
      ElMessage.error("网络异常，请稍后重试")
    }
  }
}
const delPost = async (postId)=>{
  await ElMessageBox.confirm("确定删除该帖子？帖子下所有评论也将不再展示。","删除帖子",
      {confirmButtonText:"确认",cancelButtonText:"取消",type:"warning"})
  await $axios.post(`/community/post/delete/${postId}`)
  ElMessage.success("帖子已删除")
  await loadPostList()
}
const openTeacherDialog = (teacherItem)=>{
  currTeacher.value = {
    userId: teacherItem.userId,
    avatar: teacherItem.showAvatar || teacherItem.avatar || '',
    realName: teacherItem.realName,
    gender: teacherItem.gender,
    goodAt: teacherItem.goodAt,
    introduction: teacherItem.introduction,
    address: teacherItem.address
  }
  dialogTeacherInfo.value = true
}
const handleGoBook = async ()=>{
  dialogTeacherInfo.value = false
  bookForm.value.teacherId = currTeacher.value.userId
  bookForm.value.teacherName = currTeacher.value.realName
  bookForm.value.appointDate = null
  bookForm.value.slotId = null
  bookForm.value.remark = ''
  slotOptions.value = []
  const res = await $axios.get(`/teacherSlot/getByTeacherId/${currTeacher.value.userId}`)
  if(res.data.code ===200){
    allTeacherSlots.value = res.data.data
  }
  bookDialogVisible.value = true
}
const onDateChange = ()=>{
  slotOptions.value = []
  bookForm.value.slotId = null
  if(!bookForm.value.appointDate) return
  const d = new Date(bookForm.value.appointDate)
  const weekIdx = d.getDay()
  const weekMap = ['周日','周一','周二','周三','周四','周五','周六']
  const selectWeek = weekMap[weekIdx]
  slotOptions.value = allTeacherSlots.value.filter(item=> item.weekDay === selectWeek)
}
const submitBook = async ()=>{
  if(!bookForm.value.appointDate){
    return ElMessage.warning("请选择预约日期")
  }
  if(!bookForm.value.slotId){
    return ElMessage.warning("请选择该日期下的可预约时间段")
  }
  const d = new Date(bookForm.value.appointDate)
  const year = d.getFullYear()
  const m = String(d.getMonth()+1).padStart(2,'0')
  const day = String(d.getDate()).padStart(2,'0')
  const body = {
    teacherId: bookForm.value.teacherId,
    appointDate: `${year}-${m}-${day}`,
    slotId: bookForm.value.slotId,
    remark: bookForm.value.remark
  }
  try {
    await $axios.post('/appointment/save', body)
    ElMessage.success("预约申请提交成功")
    bookDialogVisible.value = false
  }catch (err){
    console.error(err)
    ElMessage.error("预约提交失败："+(err.response?.data?.msg||"服务器异常"))
  }
}
onMounted(()=>{
  loadPostList()
})
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
  max-width:1000px;
  margin:0 auto;
}
.card-header-bar{
  display:flex;
  justify-content:space-between;
  align-items:center;
}
.post-list{
  display:flex;
  flex-direction:column;
  gap:14px;
  margin-top:12px;
}
.post-item{
  padding:12px;
}
.post-header{
  display:flex;
  gap:12px;
  align-items:center;
  margin-bottom:10px;
}
.post-info{
  display:flex;
  flex-direction:column;
}
.nick{
  font-weight:500;
}
.time{
  font-size:12px;
  color:#999;
}
.post-title{
  font-size:16px;
  font-weight:bold;
  margin-bottom:8px;
}
.post-content{
  color:#444;
  line-height:1.6;
}
.post-content-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 2px;
}
.comment-wrap{
  margin-top:14px;
  border-top:1px solid #eee;
  padding-top:12px;
}
.comment-input-row{
  display:flex;
  gap:8px;
  align-items:center;
  margin-bottom:10px;
}
.comment-list{
  display:flex;
  flex-direction:column;
  gap:10px;
}
.comment-item{
  display:flex;
  gap:8px;
  align-items:flex-start;
}
.comment-body{
  flex:1;
}
.comment-header{
  display:flex;
  gap:10px;
  align-items:center;
  margin-bottom:4px;
}
.comment-header .nick{
  font-weight:500;
  font-size:13px;
}
.comment-header .time{
  font-size:12px;
  color:#999;
}
.comment-text{
  font-size:13px;
  color:#444;
}
.expand-btn-wrap{
  margin-top:6px;
}
.header-box {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.tip-text {
  color:#666;
  font-size:13px;
  margin:4px 0 0;
}
.name-row {
  display:flex;
  align-items: baseline;
  gap: 6px;
}
.tag-teacher {
  font-size:11px;
  color:#409eff;
  border:1px solid #409eff;
  padding:1px 4px;
  border-radius:4px;
}
.header-right-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
</style>