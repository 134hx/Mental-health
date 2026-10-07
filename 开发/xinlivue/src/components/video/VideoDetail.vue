<template>
  <div class="page-bg">
    <div class="page-wrap">
      <el-card shadow="hover">
        <template #header>
          <div class="header-box">
            <div>
              <h2>{{ currentVideo.title }}</h2>
              <div class="video-meta" v-if="currentVideo.id">
                <span>发布者：{{ currentVideo.uploaderName || '未知老师' }}</span>
                <span>发布日期：{{ formatDate(currentVideo.createTime) }}</span>
              </div>
            </div>
            <el-button @click="$router.push('/psych/video')">返回视频列表</el-button>
          </div>
        </template>

        <div class="play-container">
          <div class="player-wrap">
            <video
                v-if="currentVideo.url"
                :src="currentVideo.url"
                :poster="currentVideo.poster"
                controls
                style="width:100%;border-radius:8px;background:#000;"
            ></video>
            <div v-else class="empty-tip">视频信息不存在</div>
          </div>

          <div class="video-desc-box">
            <h3>视频简介</h3>
            <p>{{ currentVideo.description }}</p>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import $axios from '@/utils/axios'

const route = useRoute()
const currentVideo = ref({})

const formatDate = (t) => {
  if (!t) return ''
  return String(t)
}

const loadVideo = async () => {
  const vid = Number(route.params.id)
  const res = await $axios.get(`/psych/video/get?id=${vid}`)
  if (res.data.code === 200) {
    currentVideo.value = res.data.data
  }
}

onMounted(() => {
  loadVideo()
})
watch(() => route.params.id, () => {
  loadVideo()
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
.page-wrap {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}
.header-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.video-meta {
  margin-top: 6px;
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #888;
}
.play-container {
  width: 100%;
}
.empty-tip {
  height: 420px;
  background: #fafafa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}
.video-desc-box {
  margin-top: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}
.video-desc-box h3 {
  margin: 0 0 8px;
}
.video-desc-box p {
  margin: 0;
  color: #555;
  line-height: 1.7;
}
</style>