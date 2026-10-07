<template>
  <PsychHeader title="我的测评记录" />
  <div class="page-bg">
    <div class="page-wrap">
      <el-card class="page-card" shadow="hover">
        <template #header>
          <div class="card-header-row">
            <div class="card-header-title">📄 历史测评报告</div>
            <el-button size="small" @click="$router.push('/psych/psyindex')">返回</el-button>
          </div>
        </template>
        <el-table :data="recordList" border stripe style="width:100%" class="table-style">
          <el-table-column prop="scaleName" label="量表名称" min-width="180" />
          <el-table-column label="原始分" width="100" align="center">
            <template #default="scope">
              {{ scope.row.totalScore }}
            </template>
          </el-table-column>
          <el-table-column label="严重指数" width="100" align="center">
            <template #default="scope">
              <span :class="severityClass(scope.row.percentScore)">
                {{ scope.row.percentScore }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="conclusion" label="测评结论" min-width="360" />
          <el-table-column label="测评时间" width="180" :formatter="formatTime" />
        </el-table>
        <el-empty v-if="recordList.length === 0" description="暂无测评记录，请前往首页完成测评"></el-empty>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import $axios from '@/utils/axios'
import PsychHeader from '@/components/header/PsychHeader.vue'
const recordList = ref([])

const formatTime = (row)=>{
  if(!row.createTime) return ''
  return row.createTime.replace('T',' ')
}

// ★ 严重指数：越高越严重，配色分级
const severityClass = (percent) => {
  if (percent == null) return ''
  if (percent <= 20) return 'sev-low'
  if (percent <= 45) return 'sev-mild'
  if (percent <= 70) return 'sev-mid'
  return 'sev-high'
}

const getRecordList = async () => {
  const res = await $axios.get("/scaleRecord/myList")
  recordList.value = res.data.data
}
onMounted(() => {
  getRecordList()
})
</script>
<style scoped>
.page-wrap {
  padding:24px;
  max-width:1200px;
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
.sev-low  { color: #67c23a; font-weight: 600; }
.sev-mild { color: #e6a23c; font-weight: 600; }
.sev-mid  { color: #f56c6c; font-weight: 600; }
.sev-high { color: #c03639; font-weight: 700; }
</style>