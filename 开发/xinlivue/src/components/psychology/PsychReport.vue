<template>
  <PsychHeader title="测评报告" />
  <div class="page-bg">
    <div class="page-wrap">
      <el-card class="report-card" shadow="hover">
        <template #header>
          <div class="report-title">📋 心理测评报告</div>
        </template>
        <div v-if="recordInfo" class="report-body">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="量表名称">{{ recordInfo.scaleName }}</el-descriptions-item>
            <el-descriptions-item label="原始得分">
              {{ recordInfo.totalScore }}
            </el-descriptions-item>
            <el-descriptions-item label="严重指数">
              <span :class="severityClass(recordInfo.percentScore)" class="severity-text">
                {{ recordInfo.percentScore }}%
              </span>
              <span class="severity-hint">（越高代表症状越明显，满分 100%）</span>
            </el-descriptions-item>
            <el-descriptions-item label="测评时间">{{ formatTime(recordInfo.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="测评结论">
              <div class="conclusion-text">{{ recordInfo.conclusion }}</div>
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="isSevere" class="warning-box">
            <div class="warning-title">⚠️ 温馨提示</div>
            <div class="warning-text">
              本结果仅作为初步自我觉察，不构成临床诊断。若近期持续感到困扰，建议主动联系学校心理咨询中心或专业心理医生，获得面对面的支持。
            </div>
          </div>

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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
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

const severityClass = (percent) => {
  if (percent == null) return ''
  if (percent <= 20) return 'sev-low'
  if (percent <= 45) return 'sev-mild'
  if (percent <= 70) return 'sev-mid'
  return 'sev-high'
}

// 严重指数 > 70% 时提示就医
const isSevere = computed(() => {
  return recordInfo.value && recordInfo.value.percentScore > 70
})

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
.severity-text {
  font-size: 16px;
}
.severity-hint {
  color: #909399;
  font-size: 12px;
  margin-left: 6px;
}
.sev-low  { color: #67c23a; font-weight: 600; }
.sev-mild { color: #e6a23c; font-weight: 600; }
.sev-mid  { color: #f56c6c; font-weight: 600; }
.sev-high { color: #c03639; font-weight: 700; }
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
.warning-box {
  margin-top: 20px;
  padding: 16px 20px;
  background: #fff8e6;
  border-left: 4px solid #e6a23c;
  border-radius: 8px;
}
.warning-title {
  font-weight: 600;
  color: #b88230;
  margin-bottom: 6px;
}
.warning-text {
  font-size: 13px;
  color: #8a6a2f;
  line-height: 1.7;
}
</style>