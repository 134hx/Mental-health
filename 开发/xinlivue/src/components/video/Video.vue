<template>
  <PsychHeader title="健康心理视频库" />
  <div class="page-bg">
    <div class="page-wrap">
      <el-card shadow="hover" :body-style="{ paddingBottom: '0' }">
        <template #header>
          <div class="header-box">
            <div>
              <h2>健康心理视频库</h2>
              <p class="tip-text">科普心理知识，学习情绪调节、压力缓解小技巧</p>
            </div>

            <div class="header-actions">
              <el-button @click="$router.push('/psych/index')">返回首页</el-button>
              <el-button v-if="isTeacher" type="primary" @click="openUpload">上传视频</el-button>
              <el-select
                  v-if="isTeacher"
                  v-model="videoFilterType"
                  size="small"
                  style="width: 90px;"
              >
                <el-option label="全部视频" value="all" />
                <el-option label="我的上传" value="mine" />
              </el-select>
            </div>
          </div>
        </template>

        <div class="search-bar">
          <el-input
              v-model="keyword"
              placeholder="搜索视频名称或发布者"
              clearable
              style="max-width: 320px;"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>

        <el-dialog v-model="openUploadDialog" title="上传心理视频" width="620px">
          <el-form :model="form" label-width="90px">
            <el-form-item label="视频标题">
              <el-input v-model="form.title" placeholder="请输入视频标题"></el-input>
            </el-form-item>
            <el-form-item label="视频简介">
              <el-input v-model="form.description" type="textarea" rows="3"></el-input>
            </el-form-item>
            <el-form-item label="MP4视频文件">
              <el-upload
                  action="#"
                  :http-request="handleUploadVideoFile"
                  accept=".mp4"
                  :file-list="videoFileList"
                  :limit="1"
              >
                <el-button>选择mp4文件</el-button>
              </el-upload>
            </el-form-item>
            <el-form-item label="封面图片">
              <el-upload
                  action="#"
                  :http-request="handleUploadPoster"
                  accept="image/*"
                  :file-list="posterFileList"
                  :limit="1"
              >
                <el-button>选择封面图</el-button>
              </el-upload>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="closeDialog">取消</el-button>
            <el-button type="primary" @click="submitAdd">保存视频</el-button>
          </template>
        </el-dialog>

        <div class="video-card-grid">
          <el-card
              v-for="item in showVideoList"
              :key="item.id"
              shadow="hover"
              class="video-card"
              :body-style="{ padding: '0' }"
              @click="goDetail(item.id)"
          >
            <div class="card-cover">
              <img v-if="item.poster" :src="item.poster" alt="封面" />
              <div v-else class="no-poster">暂无封面</div>

              <el-button
                  v-if="item.isMine"
                  class="card-delete-btn"
                  size="small"
                  circle
                  @click.stop="delVideo(item.id)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>

            <div class="card-title">{{ item.title }}</div>
            <div class="card-meta">
              <span>发布者：{{ item.uploaderName || '未知老师' }}</span>
              <span class="meta-time">{{ formatDate(item.createTime) }}</span>
            </div>
          </el-card>
        </div>

        <el-empty
            v-if="showVideoList.length === 0"
            description="暂无视频"
            style="margin-top: 24px;"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import PsychHeader from '@/components/header/PsychHeader.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Search } from '@element-plus/icons-vue'
import $axios from '@/utils/axios'

const router = useRouter()

const currentUser = computed(() => {
  const userStr = localStorage.getItem("user")
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch (e) { return null }
})

const isTeacher = computed(() => currentUser.value?.role === 1)

const formatDate = (t) => {
  if (!t) return ''
  return String(t)
}

const keyword = ref('')
const activeKeyword = ref('')

const handleSearch = () => {
  activeKeyword.value = keyword.value.trim()
}

const openUploadDialog = ref(false)
let folderUuid = ref('')
const videoList = ref([])
const videoFilterType = ref('all')

const showVideoList = computed(() => {
  const uid = currentUser.value?.id
  let list = videoList.value.map(v => ({
    ...v,
    isMine: v.userId != null && v.userId === uid
  }))

  if (videoFilterType.value === 'mine') {
    list = list.filter(v => v.isMine)
  }

  const k = activeKeyword.value.toLowerCase()
  if (k) {
    list = list.filter(v =>
        (v.title && v.title.toLowerCase().includes(k)) ||
        (v.uploaderName && v.uploaderName.toLowerCase().includes(k))
    )
  }
  return list
})

const form = ref({ title: '', description: '', url: '', poster: '' })
const videoFileList = ref([])
const posterFileList = ref([])

const openUpload = async () => {
  form.value = { title: '', description: '', url: '', poster: '' }
  videoFileList.value = []
  posterFileList.value = []
  const res = await $axios.post('/psych/video/createFolder')
  if (res.data.code === 200) {
    folderUuid.value = res.data.data
    openUploadDialog.value = true
  } else {
    ElMessage.error(res.data.msg)
  }
}

const closeDialog = () => {
  openUploadDialog.value = false
  folderUuid.value = ''
}

const loadList = async () => {
  const res = await $axios.get('/psych/video/list')
  if (res.data.code === 200) {
    videoList.value = res.data.data
  }
}

const handleUploadVideoFile = async (opt) => {
  const fd = new FormData()
  fd.append("file", opt.file)
  fd.append("folderUuid", folderUuid.value)
  const res = await $axios.post('/psych/video/uploadFile', fd, { headers: { "Content-Type": "multipart/form-data" } })
  if (res.data.code === 200) {
    form.value.url = res.data.data
    ElMessage.success("视频文件上传成功")
  } else {
    ElMessage.error(res.data.msg)
  }
}

const handleUploadPoster = async (opt) => {
  const fd = new FormData()
  fd.append("file", opt.file)
  fd.append("folderUuid", folderUuid.value)
  const res = await $axios.post('/psych/video/uploadPoster', fd, { headers: { "Content-Type": "multipart/form-data" } })
  if (res.data.code === 200) {
    form.value.poster = res.data.data
    ElMessage.success("封面上传成功")
  } else {
    ElMessage.error(res.data.msg)
  }
}

const submitAdd = async () => {
  if (!form.value.title) return ElMessage.warning("请填写标题")
  if (!form.value.url) return ElMessage.warning("请上传mp4视频文件")
  const res = await $axios.post('/psych/video/add', form.value)
  if (res.data.code === 200) {
    ElMessage.success("新增视频成功")
    openUploadDialog.value = false
    await loadList()
  } else {
    ElMessage.error(res.data.msg)
  }
}

const delVideo = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该视频？删除后不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  try {
    await $axios.post(`/psych/video/delete?id=${id}`)
    ElMessage.success('已删除')
    await loadList()
  } catch (err) {
    ElMessage.error(err.msg || '删除失败')
  }
}

const goDetail = (vid) => {
  router.push(`/psych/video/detail/${vid}`)
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.page-wrap {
  padding: 24px;
  max-width: 1240px;
  margin: 0 auto;
}
.header-box {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.header-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}
.tip-text {
  color: #666;
  font-size: 13px;
  margin: 4px 0 0;
}

.search-bar {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 4px 0 12px;
}

.video-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-top: 12px;
}
.video-card {
  cursor: pointer;
  transition: transform 0.25s;
  overflow: hidden;
}
.video-card:hover {
  transform: translateY(-4px);
}

.card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background-color: #cccccc;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.no-poster {
  font-size: 13px;
  color: #666;
}

.card-delete-btn {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 2;
  width: 28px;
  height: 28px;
  border-radius: 0 0 0 10px;
  background-color: rgba(0, 0, 0, 0.55) !important;
  border-color: transparent !important;
  color: #fff !important;
}
.card-delete-btn:hover {
  background-color: rgba(0, 0, 0, 0.8) !important;
}

.card-title {
  padding: 10px 12px 0;
  font-weight: 500;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ★ 关键：margin-bottom 改成 0，用 padding 撑出底部间距，消除白条 */
.card-meta {
  margin: 8px 12px 0;
  padding: 6px 0 12px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
  border-top: 1px dashed #eee;
}
.meta-time {
  color: #bbb;
  flex-shrink: 0;
  margin-left: 8px;
}
</style>