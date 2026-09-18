<template>
  <PsychHeader title="测评报告" />
  <div class="page-wrap">
    <el-card class="report-card" shadow="hover">
      <template #header>
        <div class="report-title">📋 心理测评报告</div>
      </template>
      <div v-if="recordInfo" class="report-body">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="量表名称">{{ recordInfo.scaleName }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ recordInfo.percentScore }} 分</el-descriptions-item>
          <el-descriptions-item label="测评时间">{{ formatTime(recordInfo.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="测评结论">
            <div class="conclusion-text">{{ recordInfo.conclusion }}</div>
          </el-descriptions-item>
        </el-descriptions>
        <div class="btn-wrap">
          <el-button @click="$router.push('/psych/psyindex')">返回测评列表</el-button>
          <el-button type="primary" @click="$router.push('/psych/record')">查看我的历史记录</el-button>
        </div>
      </div>
      <div v-else class="loading-tip">
        <el-skeleton rows="6" animated />
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import $axios from '@/utils/axios'
import PsychHeader from '@/components/header/PsychHeader.vue'
const route = useRoute()
const router = useRouter()
const recordId = route.query.recordId
const recordInfo = ref(null)

const getReport = async ()=>{
  const res = await $axios.get("/scaleRecord/myList")
  const list = res.data.data
  recordInfo.value = list.find(item=> item.id == recordId)
  if(!recordInfo.value){
    router.push("/psych/psyindex")
  }
}

const formatTime = (str)=>{
  if(!str) return ''
  return str.replace('T',' ')
}

onMounted(()=>{
  getReport()
})
</script>
<style scoped>
.page-wrap{
  padding:30px;
  max-width:900px;
  margin:0 auto;
}
.report-card{
  border-radius:12px;
}
.report-title{
  text-align:center;
  font-size:22px;
  font-weight:600;
  color:#2c3e50;
}
.report-body{
  padding:10px;
}
.conclusion-text{
  line-height:1.8;
  font-size:15px;
  color:#303133;
}
.btn-wrap{
  margin-top:30px;
  text-align:center;
  display:flex;
  gap:16px;
  justify-content:center;
}
.loading-tip{
  padding:20px;
}
</style>
