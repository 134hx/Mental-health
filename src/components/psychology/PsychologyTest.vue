<template>
  <PsychHeader title="量表答题" />
  <div class="page-wrap">
    <el-card class="page-card" shadow="hover">
      <template #header>
        <div class="card-header-row">
          <div class="card-header-title">请认真完成题目</div>
          <div v-if="questionList.length" class="tip-index">第 {{ currentIndex+1 }} 题 / {{ questionList.length }} 题</div>
          <el-button type="danger" size="small" @click="giveUpTest">放弃测评</el-button>
        </div>
      </template>
      <!-- 没有题目显示加载，有题目才渲染题目区域 -->
      <div v-if="questionList.length">
        <div class="q-item">
          <p class="q-text">{{ currentIndex + 1 }}、{{ currentQuestion.questionContent }}</p>
          <!-- 使用 radio‑button，纵向布局，矩形选框，无圆点 -->
          <el-radio-group v-model="answerMap[currentQuestion.id]" class="radio-group">
            <el-radio-button
                v-for="(opt,optIdx) in parseOption(currentQuestion.optionJson)"
                :key="opt.score"
                :label="opt.score">
              {{ getAbcLabel(optIdx) }}、{{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>

        <div class="btn-area">
          <el-button :disabled="currentIndex === 0" @click="prevQuestion">上一题</el-button>
          <!-- 不是最后一题：显示下一题；最后一题：显示提交 -->
          <el-button v-if="currentIndex < questionList.length -1" type="primary" @click="nextQuestion">下一题</el-button>
          <el-button v-else type="success" @click="submitTest">提交测评</el-button>
        </div>
      </div>
      <div v-else class="loading-wrap">
        <el-skeleton rows="4" animated />
        <div class="loading-text">正在加载测评题目...</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import $axios from '@/utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import PsychHeader from '@/components/header/PsychHeader.vue'

const route = useRoute()
const router = useRouter()
const scaleId = route.query.scaleId

const questionList = ref([])
const answerMap = reactive({})
const currentIndex = ref(0)

// 当前题目计算属性
const currentQuestion = computed(()=>{
  return questionList.value[currentIndex.value]
})

// ABCD标签生成
const getAbcLabel = (idx)=>{
  const arr = ['A','B','C','D','E','F']
  return arr[idx]
}

const parseOption = (jsonStr) => {
  return JSON.parse(jsonStr)
}

const getQuestion = async () => {
  const res = await $axios.get(`/scale/question/${scaleId}`)
  questionList.value = res.data.data
}

// 上一题：不再校验是否作答，直接切换
const prevQuestion = ()=>{
  if(currentIndex.value > 0){
    currentIndex.value --
  }
}

// 下一题：不再校验是否作答，可以直接翻页浏览
const nextQuestion = ()=>{
  if(currentIndex.value < questionList.value.length - 1){
    currentIndex.value ++
  }
}

// 放弃测评：弹窗确认，跳转到测评列表页 /psych/psyindex
const giveUpTest = async ()=>{
  await ElMessageBox.confirm(
      '确定要放弃本次测评吗？作答内容不会保存。',
      '放弃测评',
      { confirmButtonText:'确定放弃', cancelButtonText:'继续答题', type:'warning' }
  )
  router.push('/psych/psyindex')
}

// 提交全部答卷：提交时校验【所有题目必须全部完成】
// 提交全部答卷：提交时校验【所有题目必须全部完成】
const submitTest = async () => {
  // 提交前校验全部题目，而不是单题
  let unAnsweredCount = 0
  let userRawTotal = 0
  let maxTotal = 0
  for(let q of questionList.value){
    maxTotal += 3 // 每题满分固定为3
    if(answerMap[q.id] === undefined){
      unAnsweredCount ++
    }else{
      userRawTotal += Number(answerMap[q.id])
    }
  }
  if(unAnsweredCount > 0){
    return ElMessage.warning(`还有 ${unAnsweredCount} 道题目未完成，请完成全部题目后再提交！`)
  }

  // 百分制计算，四舍五入取整
  const percentScore = Math.round( 100 - ( userRawTotal / maxTotal * 100 ) );

  await ElMessageBox.confirm(
      '确认提交答卷？提交后不可修改。',
      '提交确认',
      { confirmButtonText:'确认提交', cancelButtonText:'取消', type:'info' }
  )

  const res = await $axios.post("/scaleRecord/submit", null, {
    params: {
      scaleId: scaleId,
      totalScore: userRawTotal,
      percentScore: percentScore
    }
  })
  // 拿到生成的测评记录id，跳转到独立报告页面
  const recordId = res.data.data.id
  ElMessage.success("测评提交成功")
  router.push({ path:'/psych/report', query:{ recordId } })
}

onMounted(() => {
  getQuestion()
})
</script>

<style scoped>
.page-wrap {
  padding: 24px;
  max-width: 860px;
  margin:0 auto;
}
.page-card {
  border-radius:12px;
}
.card-header-row{
  display:flex;
  justify-content:space-between;
  align-items:center;
  gap:16px;
}
.tip-index{
  font-size:16px;
  font-weight:600;
  color:#409eff;
}
.card-header-title {
  font-size:18px;
  font-weight:600;
}
.q-item {
  padding:30px 10px;
  min-height:260px;
}
.q-text {
  font-size:18px;
  margin-bottom:24px;
  color:#303133;
  line-height:1.6;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.radio-group :deep(.el-radio-button) {
  width: 100%;
}

.radio-group :deep(.el-radio-button__inner) {
  width: 100%;
  justify-content: flex-start;
  text-align: left;
  padding-left: 18px;
}

.btn-area{
  display:flex;
  justify-content:center;
  gap:20px;
  padding:20px 0;
  border-top:1px solid #ebeef5;
}
.loading-wrap{
  padding:40px 20px;
  text-align:center;
}
.loading-text{
  margin-top:12px;
  color:#909399;
}
</style>
