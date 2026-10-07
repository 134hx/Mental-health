<template>
  <router-view />

  <audio ref="audioRef" :src="musicSrc" loop preload="auto"></audio>

  <div
      v-if="showMusicBtn"
      class="global-music-btn"
      @click="toggleMusic"
      :title="musicPlaying ? '暂停音乐' : '播放音乐'"
  >
    <span v-if="musicPlaying">🎵</span>
    <span v-else>🔇</span>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const musicSrc = '/music/bgm.mp3'
const audioRef = ref(null)
const musicPlaying = ref(false)
const showMusicBtn = ref(false)

const isLoggedIn = () => !!localStorage.getItem('token')

const playMusic = async () => {
  const el = audioRef.value
  if (!el) return
  try {
    el.volume = 0.4
    await el.play()
    musicPlaying.value = true
    localStorage.setItem('bgm_playing', '1')
  } catch (e) {
    console.warn('音乐播放被拦截:', e.message)
    musicPlaying.value = false
  }
}

const pauseMusic = () => {
  const el = audioRef.value
  if (!el) return
  el.pause()
  musicPlaying.value = false
  localStorage.setItem('bgm_playing', '0')
}

const toggleMusic = () => {
  if (musicPlaying.value) pauseMusic()
  else playMusic()
}

const syncMusic = async () => {
  if (!isLoggedIn()) {
    showMusicBtn.value = false
    pauseMusic()
    return
  }
  showMusicBtn.value = true
  if (localStorage.getItem('bgm_playing') === '0') return
  await playMusic()
}

window.addEventListener('bgm-start', () => {
  playMusic()
})

onMounted(() => {
  syncMusic()
})

watch(() => route.path, () => {
  syncMusic()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* ★ 只设 min-height，不加 flex */
html, body, #app {
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #303133;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
}

/* ★ 全局柔和滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-thumb {
  background: rgba(140, 160, 180, 0.3);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(140, 160, 180, 0.5);
}
::-webkit-scrollbar-track {
  background: transparent;
}

/* ★ 全局页面背景：所有页面统一用 .page-bg */
.page-bg {
  position: relative;
  min-height: calc(100vh - 64px);
  background-image: url("/bg/XL.jpg");
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  background-color: #f5f7fa;
}
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

/* ★ Element Plus 全局微调 */
.el-card {
  border-radius: 16px !important;
  border: none !important;
  box-shadow: 0 4px 20px rgba(140, 160, 180, 0.08) !important;
  transition: box-shadow 0.3s ease !important;
}
.el-card:hover {
  box-shadow: 0 6px 28px rgba(140, 160, 180, 0.12) !important;
}

.el-button {
  border-radius: 10px !important;
  transition: all 0.25s ease !important;
}

.el-input__wrapper,
.el-textarea__inner {
  border-radius: 10px !important;
}

.el-dialog {
  border-radius: 16px !important;
  overflow: hidden;
}

.el-tabs__item {
  font-size: 15px;
}

/* 音乐按钮 */
.global-music-btn {
  position: fixed;
  top: 80px;
  right: 24px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  cursor: pointer;
  user-select: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 9999;
}
.global-music-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.35);
}
.global-music-btn:active {
  transform: scale(0.95);
}
</style>