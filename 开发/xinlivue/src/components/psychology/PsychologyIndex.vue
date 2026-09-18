<template>
  <PsychHeader title="心理测评首页" />
  <div class="page-bg">
    <div class="page-wrap">
      <el-card class="page-card" shadow="hover">
        <template #header>
          <div class="card-header-row">
            <div class="card-header-title">📋 心理自评量表列表</div>
            <el-button size="small" @click="$router.push('/psych/index')">返回首页</el-button>
          </div>
        </template>
        <div class="scale-wrap">
          <el-card v-for="item in scaleList" :key="item.id" class="scale-card" shadow="hover">
            <h3 class="scale-title">{{item.scaleName}}</h3>
            <p class="desc">{{item.description}}</p>
            <div class="card-footer">
              <el-button type="primary" @click="goTest(item.id)">开始测评</el-button>
            </div>
          </el-card>
        </div>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import $axios from '@/utils/axios'
import PsychHeader from '@/components/header/PsychHeader.vue'
const router = useRouter()
const scaleList = ref([])
const getScaleList = async () => {
  const res = await $axios.get('/scale/list')
  scaleList.value = res.data.data
}
const goTest = (scaleId) => {
  router.push({ path: '/psych/test', query: { scaleId } })
}
onMounted(() => {
  getScaleList()
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
  padding: 24px;
  max-width: 1200px;
  margin:0 auto;
}
.page-card {
  border-radius: 12px;
}
.card-header-row{
  display:flex;
  justify-content:space-between;
  align-items:center;
}
.card-header-title {
  font-size:18px;
  font-weight:600;
  color:#2c3e50;
}
.scale-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
.scale-card {
  width: 340px;
  border-radius:10px;
  transition: all 0.25s ease;
}
.scale-card:hover {
  transform: translateY(-4px);
}
.scale-title {
  margin: 0 0 10px 0;
  color:#2c3e50;
}
.desc {
  color:#666;
  font-size:14px;
  min-height:48px;
}
.card-footer {
  margin-top:16px;
  text-align:right;
}
</style>
