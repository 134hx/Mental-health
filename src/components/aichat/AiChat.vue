<template>
  <PsychHeader title="AI心理倾诉" />
  <div class="page-bg">
    <div class="page-wrap">
      <el-card class="chat-root-card" shadow="hover">
        <div class="chat-layout">
          <!-- 左侧会话侧边栏 -->
          <div class="sidebar">
            <div class="sidebar-top">
              <el-button type="success" size="small" @click="createSession" class="new-session-btn">+ 新建对话</el-button>
            </div>
            <div class="session-list">
              <div
                  v-for="item in sessionList"
                  :key="item.id"
                  :class="['session-item', currentSessionId===item.id ? 'active' : '']"
                  @click="switchSession(item.id)">
                <span class="session-title">{{ item.sessionTitle }}</span>
                <el-button type="text" size="small" @click.stop="handleDeleteSession(item.id)">×</el-button>
              </div>
            </div>
          </div>
          <!-- 右侧聊天主窗口 -->
          <div class="chat-main">
            <div class="chat-header-row">
              <span class="title">AI心理倾诉对话</span>
              <el-button size="small" @click="$router.push('/psych/index')">返回首页</el-button>
            </div>
            <div class="chat-box" ref="chatDom">
              <div v-for="msg in msgList" :key="msg.id" :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'">
                <div class="msg-bubble">{{ msg.content }}</div>
              </div>
              <div v-if="loadingAi" class="msg-ai">
                <div class="msg-bubble">AI思考中...</div>
              </div>
            </div>
            <div class="input-area">
              <el-input
                  v-model="inputText"
                  type="textarea"
                  :rows="4"
                  placeholder="写下你的心事，AI会耐心倾听..."
                  @keydown="handleInputKeydown"
              />
              <div class="btn-row">
                <el-button type="primary" @click="sendMsg" :disabled="!currentSessionId || loadingAi">发送</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import $axios from '@/utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import PsychHeader from '@/components/header/PsychHeader.vue'
const router = useRouter()
const chatDom = ref(null)
const currentSessionId = ref(null)
const inputText = ref('')
const msgList = ref([])
const sessionList = ref([])
const loadingAi = ref(false)
// 处理输入框键盘：Enter发送，Shift+Enter换行
const handleInputKeydown = (e) => {
  if (e.isComposing) return
  if (e.key === 'Enter') {
    if (e.shiftKey) {
      const target = e.target
      const pos = target.selectionStart
      const left = inputText.value.substring(0, pos)
      const right = inputText.value.substring(pos)
      inputText.value = left + '\n' + right
      nextTick(() => {
        target.selectionStart = pos + 1
        target.selectionEnd = pos + 1
      })
    } else {
      e.preventDefault()
      sendMsg()
    }
  }
}
// 加载我的全部会话列表
const loadSessionList = async ()=>{
  const res = await $axios.get("/aiSession/myList")
  sessionList.value = res.data.data
}
// 新建会话，默认标题：新对话
const createSession = async () => {
  const res = await $axios.post("/aiSession/create", null, { params: { sessionTitle: "新对话" } })
  currentSessionId.value = res.data.data.id
  msgList.value = []
  ElMessage.success("已开启新对话")
  await loadSessionList()
}
// 切换会话，加载历史消息
const switchSession = async (sid)=>{
  currentSessionId.value = sid
  const res = await $axios.get("/aiMessage/list",{params:{sessionId:sid}})
  msgList.value = res.data.data
  await nextTick()
  chatDom.value.scrollTop = chatDom.value.scrollHeight
}
// 删除会话
const handleDeleteSession = async (sid)=>{
  await ElMessageBox.confirm("确定删除该会话？聊天记录将全部清除。","删除会话",
      {confirmButtonText:"确认",cancelButtonText:"取消",type:"warning"})
  await $axios.post("/aiSession/delete",null,{params:{sessionId:sid}})
  ElMessage.success("会话已删除")
  if(currentSessionId.value === sid){
    currentSessionId.value = null
    msgList.value = []
  }
  await loadSessionList()
}
const getShortTitle = (str)=>{
  if(!str) return "新对话"
  if(str.length <=5){
    return str
  }else{
    return str.substring(0,5)+"..."
  }
}
// 发送消息
const sendMsg = async () => {
  if (!inputText.value.trim()) return ElMessage.warning("请输入内容")
  if(loadingAi.value) return // 加载中禁止重复发送
  loadingAi.value = true
  const userContent = inputText.value.trim()
  // 判断：当前会话标题是否为【新对话】，是就代表第一条消息，自动重命名
  const currentSessionItem = sessionList.value.find(s=> s.id === currentSessionId.value)
  if(currentSessionItem && currentSessionItem.sessionTitle === "新对话"){
    const newTitle = getShortTitle(userContent)
    // 调用后端更新标题接口
    await $axios.post("/aiSession/updateTitle",null,{
      params:{sessionId: currentSessionId.value, sessionTitle: newTitle}
    })
    await loadSessionList() // 刷新侧边栏会话列表，展示新标题
  }
  msgList.value.push({role:"user", content:userContent})
  inputText.value = ''
  await nextTick()
  chatDom.value.scrollTop = chatDom.value.scrollHeight
  try{
    const res = await $axios.post("/aiMessage/send", null, {
      params: { sessionId: currentSessionId.value, content: userContent }
    })
    msgList.value.push(res.data.data)
  }catch (e){
    ElMessage.error("消息发送失败")
  }finally {
    loadingAi.value = false
    await nextTick()
    chatDom.value.scrollTop = chatDom.value.scrollHeight
  }
}
onMounted(async () => {
  await loadSessionList()
  if(sessionList.value.length === 0){
    await createSession()
  }else{
    await switchSession(sessionList.value[0].id)
  }
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
  max-width:1200px;
  margin:0 auto;
}
.chat-root-card {
  border-radius:12px;
}
.chat-layout{
  display:flex;
  height:820px;
}
.sidebar{
  width:220px;
  border-right:1px solid #ebeef5;
  display:flex;
  flex-direction:column;
}
.sidebar-top{
  padding:12px;
}
.new-session-btn{
  width:100%;
}
.session-list{
  flex:1;
  overflow-y:auto;
}
.session-item{
  display:flex;
  justify-content:space-between;
  align-items:center;
  padding:8px 12px;
  cursor:pointer;
  font-size:14px;
}
.session-item:hover{
  background:#f5f7fa;
}
.session-item.active{
  background:#e6f7ff;
}
.session-title{
  overflow:hidden;
  text-overflow:ellipsis;
  white-space:nowrap;
  flex:1;
}
.chat-main{
  flex:1;
  display:flex;
  flex-direction:column;
  padding:12px;
}
.chat-header-row {
  display:flex;
  justify-content:space-between;
  align-items:center;
  gap:12px;
  margin-bottom:10px;
}
.title {
  font-size:18px;
  font-weight:600;
}
.chat-box {
  flex:1;
  overflow-y:auto;
  padding:16px;
  background:#fafbfc;
  border-radius:8px;
}
.msg-user {
  display:flex;
  justify-content:flex-end;
  margin:12px 0;
}
.msg-ai {
  display:flex;
  justify-content:flex-start;
  margin:12px 0;
}
.msg-bubble {
  max-width:72%;
  padding:12px 16px;
  border-radius:14px;
  line-height:1.6;
}
.msg-user .msg-bubble {
  background:#409eff;
  color:#fff;
}
.msg-ai .msg-bubble {
  background:#e9edf2;
  color:#2c3e50;
}
.input-area {
  margin-top:16px;
}
.btn-row {
  margin-top:12px;
  text-align:right;
}
</style>


