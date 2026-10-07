<template>
  <PsychHeader title="正念训练与科普" />
  <div class="page-bg">
    <div class="page-wrap">
      <el-card shadow="hover" :body-style="{ paddingBottom: '0' }">
        <template #header>
          <div class="header-box">
            <div>
              <h2>正念训练与科普</h2>
              <p class="tip-text">练习正念、了解心理，给情绪一个喘息的空间</p>
            </div>
            <div class="header-actions">
              <el-button @click="$router.push('/psych/index')">返回首页</el-button>
            </div>
          </div>
        </template>

        <el-tabs v-model="activeTab">
          <!-- ============ Tab 1：每日一句 ============ -->
          <el-tab-pane label="每日一句" name="daily">
            <div class="daily-box">
              <div class="daily-date">{{ today }}</div>

              <template v-if="dailyStatus === 'idle'">
                <div class="daily-circle" @click="drawQuote">
                  <div class="daily-circle-title">点击抽取</div>
                  <div class="daily-circle-sub">今日份的正念</div>
                </div>
              </template>

              <template v-else-if="dailyStatus === 'loading'">
                <div class="daily-circle">
                  <div class="daily-circle-title">抽取中…</div>
                </div>
              </template>

              <template v-else>
                <div class="daily-result">「{{ todayQuote }}」</div>
                <div class="daily-hint">明天再来抽取新的一句吧 🌱</div>
              </template>
            </div>
          </el-tab-pane>

          <!-- ============ Tab 2：呼吸练习 ============ -->
          <el-tab-pane label="呼吸练习" name="breath">
            <div v-if="!breathing.active" class="breath-list">
              <el-card
                  v-for="b in breathList"
                  :key="b.key"
                  class="breath-card"
                  shadow="hover"
              >
                <div class="breath-name">{{ b.name }}</div>
                <div class="breath-desc">{{ b.desc }}</div>

                <div class="breath-block">
                  <div class="block-title">主要功效</div>
                  <ul class="block-list">
                    <li v-for="(benefit, idx) in b.benefits" :key="idx">{{ benefit }}</li>
                  </ul>
                </div>

                <div class="breath-block">
                  <div class="block-title">练习步骤</div>
                  <ol class="block-list">
                    <li v-for="(step, idx) in b.steps" :key="idx">{{ step }}</li>
                  </ol>
                </div>

                <div class="breath-block">
                  <div class="block-title">适用场景</div>
                  <div class="block-text">{{ b.scene }}</div>
                </div>

                <div class="breath-block">
                  <div class="block-title">注意事项</div>
                  <div class="block-text">{{ b.notice }}</div>
                </div>

                <div class="breath-pattern">{{ b.patternText }}</div>
                <div class="breath-source">来源：{{ b.source }}</div>

                <div class="breath-card-footer">
                  <el-button type="primary" @click="startBreath(b)">开始练习</el-button>
                </div>
              </el-card>
            </div>

            <div v-else class="breath-playing">
              <div class="breath-circle-wrap">
                <div class="breath-circle" :class="breathing.phase"></div>
                <div class="breath-text">
                  <div class="phase-text">{{ breathing.phaseText }}</div>
                  <div class="phase-count">{{ breathing.countDown }}</div>
                </div>
              </div>

              <div class="breath-progress">
                {{ breathing.phase === 'ready' ? '准备中…' : `第 ${breathing.round} / ${breathing.totalRounds} 轮` }}
              </div>
              <div class="breath-actions">
                <el-button @click="stopBreath">结束练习</el-button>
              </div>
            </div>
          </el-tab-pane>

          <!-- ============ Tab 3：正念练习 ============ -->
          <el-tab-pane label="正念练习" name="mindful">
            <div v-if="!currentMindful" class="mindful-list">
              <el-card
                  v-for="m in mindfulList"
                  :key="m.key"
                  class="mindful-card"
                  shadow="hover"
              >
                <div class="mindful-header">
                  <div class="mindful-name">{{ m.name }}</div>
                  <div class="mindful-duration">{{ m.duration }}</div>
                </div>
                <div class="mindful-desc">{{ m.desc }}</div>

                <div class="mindful-block">
                  <div class="block-title">主要功效</div>
                  <ul class="block-list">
                    <li v-for="(benefit, idx) in m.benefits" :key="idx">{{ benefit }}</li>
                  </ul>
                </div>

                <div class="mindful-block">
                  <div class="block-title">适用场景</div>
                  <div class="block-text">{{ m.scene }}</div>
                </div>

                <div class="mindful-block">
                  <div class="block-title">注意事项</div>
                  <div class="block-text">{{ m.notice }}</div>
                </div>

                <div class="mindful-source">来源：{{ m.source }}</div>

                <div class="mindful-card-footer">
                  <el-button type="primary" @click="startMindful(m)">开始练习</el-button>
                </div>
              </el-card>
            </div>

            <div v-else class="mindful-playing">
              <div class="mindful-title">
                {{ currentMindful.name }} · 第 {{ mindfulStep + 1 }} / {{ currentMindful.steps.length }} 步
              </div>
              <div class="mindful-text">{{ currentMindful.steps[mindfulStep] }}</div>
              <div class="mindful-actions">
                <el-button @click="exitMindful">退出</el-button>
                <el-button
                    v-if="mindfulStep < currentMindful.steps.length - 1"
                    type="primary"
                    @click="mindfulStep++"
                >下一步</el-button>
                <el-button v-else type="success" @click="finishMindful">完成</el-button>
              </div>
            </div>
          </el-tab-pane>

          <!-- ============ Tab 4：科普文章 ============ -->
          <el-tab-pane label="科普文章" name="article">
            <div v-if="isTeacher" class="article-toolbar">
              <el-button type="primary" @click="openArticleDialog">发布科普文章</el-button>
              <el-select
                  v-model="articleFilterType"
                  size="small"
                  style="width: 120px; margin-left: 8px;"
              >
                <el-option label="全部文章" value="all" />
                <el-option label="我发布的" value="mine" />
              </el-select>
            </div>

            <div class="article-search">
              <el-input
                  v-model="articleKeyword"
                  placeholder="搜索标题或摘要"
                  clearable
                  style="max-width: 300px;"
                  @keyup.enter="loadArticleList"
                  @clear="loadArticleList"
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-button type="primary" @click="loadArticleList">搜索</el-button>
            </div>

            <div class="article-list">
              <el-card
                  v-for="a in showArticleList"
                  :key="a.id"
                  class="article-card"
                  shadow="hover"
              >
                <div class="article-header" @click="openArticleDetail(a)">
                  <div class="article-title">{{ a.title }}</div>
                  <div class="article-time">{{ formatDate(a.createTime) }}</div>
                </div>
                <div class="article-summary" @click="openArticleDetail(a)">{{ a.summary }}</div>
                <div class="article-footer">
                  <el-button link type="primary" @click="openArticleDetail(a)">阅读全文</el-button>
                  <el-button
                      v-if="isTeacher && a.userId === currentUser?.id"
                      link
                      type="danger"
                      @click="delArticle(a.id)"
                  >删除</el-button>
                </div>
              </el-card>
              <el-empty v-if="showArticleList.length === 0" description="暂无科普文章" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <el-dialog v-model="articleDetailVisible" :title="currentArticle.title" width="720px">
      <div class="detail-meta">发布时间：{{ formatDate(currentArticle.createTime) }}</div>
      <div class="detail-content" v-html="formatContent(currentArticle.content)"></div>
    </el-dialog>

    <el-dialog v-model="articleDialogVisible" title="发布科普文章" width="680px">
      <el-form :model="articleForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="articleForm.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="articleForm.summary" type="textarea" rows="2" placeholder="一两句话概括" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="articleForm.content" type="textarea" rows="10" placeholder="支持换行，分段用空行" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="articleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitArticle">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import PsychHeader from '@/components/header/PsychHeader.vue'
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import $axios from '@/utils/axios'

const activeTab = ref('daily')

const currentUser = computed(() => {
  const s = localStorage.getItem('user')
  if (!s) return null
  try { return JSON.parse(s) } catch (e) { return null }
})
const isTeacher = computed(() => currentUser.value?.role === 1)

const formatDate = (t) => (t ? String(t).slice(0, 16) : '')

/* ============ 每日一句 ============ */
const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
const todayQuote = ref('')
const dailyStatus = ref('idle')

const loadTodayQuote = async () => {
  try {
    const res = await $axios.get('/dailyQuote/today')
    if (res.data.code === 200) {
      const { drawn, quote } = res.data.data
      if (drawn) {
        todayQuote.value = quote
        dailyStatus.value = 'drawn'
      } else {
        dailyStatus.value = 'idle'
      }
    }
  } catch (e) {
    dailyStatus.value = 'idle'
  }
}

const drawQuote = async () => {
  if (dailyStatus.value === 'loading' || dailyStatus.value === 'drawn') return
  dailyStatus.value = 'loading'
  try {
    const res = await $axios.post('/dailyQuote/draw')
    if (res.data.code === 200) {
      todayQuote.value = res.data.data.quote
      dailyStatus.value = 'drawn'
    } else {
      ElMessage.error(res.data.msg || '抽取失败')
      dailyStatus.value = 'idle'
    }
  } catch (err) {
    ElMessage.error(err.msg || '抽取失败，请重试')
    dailyStatus.value = 'idle'
  }
}

/* ============ 呼吸练习 ============ */
const breathList = [
  {
    key: '478',
    name: '4-7-8 呼吸法',
    desc: '由美国整合医学先驱 Andrew Weil 博士推广，被称为“神经系统天然的镇静剂”。通过延长呼气时间，强烈激活副交感神经，帮助身体从“战斗-逃跑”模式切回“休息-消化”模式。',
    patternText: '吸气 4 秒 → 屏息 7 秒 → 呼气 8 秒',
    benefits: [
      '快速缓解焦虑与紧张情绪',
      '帮助入睡、改善入睡困难',
      '平复心跳、降低血压',
      '打断“越想越焦虑”的思维循环'
    ],
    steps: [
      '找一个安静的位置坐下，背部自然挺直，舌尖轻抵上颚前部。',
      '用鼻子缓缓吸气，默数 4 秒。',
      '屏住呼吸，默数 7 秒。',
      '微微张嘴，用嘴呼气，发出轻轻的“呼”声，默数 8 秒。',
      '重复 3-4 轮，感受身体的放松。'
    ],
    scene: '睡前、考前、面试前、情绪激动时、无法入睡时。',
    notice: '初次练习若感到头晕，可适当缩短屏息时间。孕妇、低血压人群请咨询医生后再练习。',
    source: 'Andrew Weil 博士 / 瑜伽调息法（Pranayama）',
    phases: [['inhale', 4, '吸气'], ['hold', 7, '屏息'], ['exhale', 8, '呼气']],
    rounds: 3
  },
  {
    key: 'box',
    name: '箱式呼吸',
    desc: '一种古老的瑜伽调息技巧，美国海军海豹突击队（Navy SEALs）将其用于高压环境下的快速冷静与专注。四个阶段时长相等，节奏稳定，是训练神经系统的“均衡器”。',
    patternText: '吸气 4 → 屏息 4 → 呼气 4 → 屏息 4',
    benefits: [
      '在高压力下迅速恢复冷静',
      '提升专注力与临场表现',
      '稳定心率、降低皮质醇水平',
      '训练呼吸节奏感，作为日常“定心”练习'
    ],
    steps: [
      '坐直，肩膀放松，用鼻子自然呼吸。',
      '吸气 4 秒，感受腹部缓缓隆起。',
      '屏息 4 秒，保持身体放松。',
      '呼气 4 秒，缓缓吐出。',
      '再屏息 4 秒，然后进入下一轮吸气。',
      '循环 4-5 分钟，节奏尽量均匀。'
    ],
    scene: '重要考试/演讲前、情绪紧张时、需要高度集中注意力时。',
    notice: '屏息阶段不要用力憋气，保持自然。有心脏病、高血压者请先咨询医生。',
    source: '瑜伽调息法 / 美国海军海豹突击队',
    phases: [['inhale', 4, '吸气'], ['hold', 4, '屏息'], ['exhale', 4, '呼气'], ['hold', 4, '屏息']],
    rounds: 4
  },
  {
    key: 'belly',
    name: '腹式呼吸',
    desc: '最基础、最容易上手的放松练习。通过让腹部（而非胸部）主导呼吸，缓慢而深长地激活副交感神经，是几乎所有正念与减压疗法的入门步骤。',
    patternText: '慢吸 5 秒 → 慢呼 5 秒',
    benefits: [
      '入门放松、缓解浅快呼吸',
      '改善紧张引起的胸闷、心慌',
      '提升血氧水平与身体放松感',
      '帮助初学者建立“呼吸觉察”'
    ],
    steps: [
      '仰卧或端坐，一只手放胸口，一只手放腹部。',
      '用鼻子慢慢吸气 5 秒，感受腹部鼓起（胸部尽量不动）。',
      '用嘴缓缓呼气 5 秒，感受腹部回落。',
      '持续 5-10 分钟，逐渐让节奏自然。'
    ],
    scene: '日常放松、睡前、学习工作间隙、正念练习入门。',
    notice: '不要刻意用力鼓肚子，自然舒适即可。如感不适，回到自然呼吸。',
    source: '瑜伽调息法 / 现代减压疗法通用基础',
    phases: [['inhale', 5, '吸气'], ['exhale', 5, '呼气']],
    rounds: 6
  },
  {
    key: 'alt-nostril',
    name: '交替鼻孔呼吸',
    desc: '瑜伽中最经典的调息法之一（Nadi Shodhana）。通过左右鼻孔交替呼吸，平衡左右脑活动，稳定神经系统，是冥想前的经典准备练习。',
    patternText: '左吸 4 秒 → 屏息 4 秒 → 右呼 4 秒 → 右吸 4 秒 → 屏息 4 秒 → 左呼 4 秒（循环）',
    benefits: [
      '平衡情绪、稳定思绪',
      '提升专注力与头脑清晰度',
      '缓解焦虑、改善睡眠质量',
      '作为冥想前的“静心准备”'
    ],
    steps: [
      '坐直，右手拇指按住右鼻孔，用左鼻孔吸气 4 秒。',
      '屏息 4 秒，用右手无名指按住左鼻孔、松开拇指。',
      '用右鼻孔呼气 4 秒。',
      '再用右鼻孔吸气 4 秒，屏息 4 秒。',
      '松开拇指、按住右鼻孔，用左鼻孔呼气 4 秒。',
      '这样为 1 轮，循环 5-10 轮。'
    ],
    scene: '冥想前、需要专注时、思绪杂乱时、日常情绪平衡练习。',
    notice: '感冒鼻塞时不宜练习。屏息阶段保持自然，不要用力。',
    source: '瑜伽调息法 / Nadi Shodhana',
    phases: [
      ['inhale', 4, '左吸'],
      ['hold', 4, '屏息'],
      ['exhale', 4, '右呼'],
      ['inhale', 4, '右吸'],
      ['hold', 4, '屏息'],
      ['exhale', 4, '左呼']
    ],
    rounds: 5
  },
  {
    key: 'sigh',
    name: '生理性叹息',
    desc: '由斯坦福大学医学院 Andrew Huberman 教授推广。两次连续吸气 + 一次长呼气，模拟身体自然的“叹气”反射，是目前已知最快的减压方法之一。',
    patternText: '吸 4 秒 → 再吸 2 秒 → 长呼 6 秒',
    benefits: [
      '数秒内快速平复情绪',
      '降低心率、缓解急性焦虑',
      '改善胸闷、叹气频繁的状况',
      '不需要任何工具，随时可用'
    ],
    steps: [
      '用鼻子吸气 4 秒，再补一小口吸 2 秒（一口气两段）。',
      '再用嘴缓缓长呼气 6 秒，尽量把气吐尽。',
      '感受身体的释放，重复 3-5 次。'
    ],
    scene: '焦虑发作时、与人冲突后、压力骤增时、睡前紧张时。',
    notice: '适合短时应急使用，不建议长时间频繁练习。心脏疾病患者请咨询医生。',
    source: 'Andrew Huberman 教授 · 斯坦福大学医学院',
    phases: [['inhale', 4, '第一次吸'], ['inhale', 2, '补吸一口'], ['exhale', 6, '长呼气']],
    rounds: 5
  },
  {
    key: 'resonant',
    name: '共振呼吸',
    desc: '以每分钟约 6 次的节奏缓慢呼吸（吸气 5 秒、呼气 5 秒），与人体压力感受器的自然频率“共振”，可显著提升心率变异性（HRV），是改善情绪稳定性的长期练习。',
    patternText: '吸 5 秒 → 呼 5 秒',
    benefits: [
      '长期练习可显著提升 HRV',
      '改善情绪稳定性与抗压能力',
      '帮助调节自律神经、改善睡眠',
      '适合每天固定时间练习'
    ],
    steps: [
      '找一个安静的位置，坐直或躺下。',
      '用鼻子吸气 5 秒，用鼻子或嘴呼气 5 秒。',
      '保持节奏均匀，不屏息。',
      '每天练习 10-20 分钟，坚持 4-8 周见效。'
    ],
    scene: '每天固定时间（如早起、睡前）、长期焦虑调理、情绪管理训练。',
    notice: '刚开始可缩短到 4 秒，逐渐拉长。不可憋气，始终以舒适为准。',
    source: 'HeartMath / 心率变异性（HRV）研究',
    phases: [['inhale', 5, '吸气'], ['exhale', 5, '呼气']],
    rounds: 8
  }
]

const breathing = reactive({
  active: false,
  name: '',
  phase: '',
  phaseText: '',
  countDown: 0,
  round: 1,
  totalRounds: 3,
  phases: [],
  phaseIndex: 0,
  timer: null,
  readyTimer: null
})

const startBreath = (b) => {
  breathing.active = true
  breathing.name = b.name
  breathing.phases = b.phases
  breathing.totalRounds = b.rounds
  breathing.round = 1
  breathing.phaseIndex = 0

  breathing.phase = 'ready'
  breathing.phaseText = '准备'
  breathing.countDown = 3

  breathing.readyTimer = setInterval(() => {
    breathing.countDown--
    if (breathing.countDown <= 0) {
      clearInterval(breathing.readyTimer)
      breathing.readyTimer = null
      runPhase()
    }
  }, 1000)
}

const runPhase = () => {
  const [phase, seconds, label] = breathing.phases[breathing.phaseIndex]
  breathing.phase = phase
  breathing.phaseText = label
  breathing.countDown = seconds

  breathing.timer = setInterval(() => {
    breathing.countDown--
    if (breathing.countDown <= 0) {
      clearInterval(breathing.timer)
      nextPhase()
    }
  }, 1000)
}

const nextPhase = () => {
  breathing.phaseIndex++
  if (breathing.phaseIndex >= breathing.phases.length) {
    breathing.phaseIndex = 0
    breathing.round++
    if (breathing.round > breathing.totalRounds) {
      ElMessage.success('完成练习，做得很好 🌿')
      stopBreath()
      return
    }
  }
  runPhase()
}

const stopBreath = () => {
  if (breathing.timer) clearInterval(breathing.timer)
  if (breathing.readyTimer) clearInterval(breathing.readyTimer)
  breathing.timer = null
  breathing.readyTimer = null
  breathing.active = false
  breathing.phase = ''
  breathing.phaseText = ''
  breathing.countDown = 0
}

/* ============ 正念练习 ============ */
const mindfulList = [
  {
    key: 'bodyScan',
    name: '身体扫描',
    duration: '约 5 分钟',
    desc: '正念减压疗法（MBSR）的核心练习之一。通过系统性地将注意力依次停留在身体各部位，训练专注力的同时释放身体积压的紧张，是深度放松与自我觉察的经典练习。',
    benefits: [
      '释放身体慢性紧张与僵硬',
      '改善失眠，帮助身体进入放松状态',
      '提升对身体感觉的敏锐度',
      '缓解焦虑与“头脑停不下来”的状态'
    ],
    scene: '睡前放松、焦虑发作后、长期身体紧绷、正念练习入门。',
    notice: '如中途走神，不必责备自己，温和地把注意力带回身体即可。如遇不适部位，可稍作停留后继续。',
    source: 'Jon Kabat-Zinn 博士 · MBSR（1979）',
    steps: [
      '找个安静的地方坐下或躺下，闭上眼睛，做三次缓慢的深呼吸。',
      '把注意力放在头顶。感受头皮、额头、眼周，让它们放松。',
      '慢慢移到下巴、脖颈、肩膀。让肩膀自然下沉。',
      '注意双臂和双手。感受指尖的温度。',
      '把注意力放在胸口。感受呼吸时胸口的起伏。',
      '再到腹部、后背。让每一处都放松下来。',
      '移到臀部、大腿、膝盖、小腿、脚踝、脚掌。',
      '感受整个身体。深吸一口气，再缓缓呼出。练习结束，慢慢睁开眼睛。'
    ]
  },
  {
    key: 'fiveSenses',
    name: '五感觉察',
    duration: '约 3 分钟',
    desc: '源自 MBSR 8 周课程中的“正念五官训练”。通过调动视、听、嗅、味、触五种感官，把注意力从反刍思维中拉回到此时此地，是快速“落地”的有效技巧。',
    benefits: [
      '快速打断焦虑与反刍思维',
      '从情绪风暴中“着陆”回当下',
      '提升感官敏锐度与生活体验',
      '适合作为任何时刻的“情绪急救”'
    ],
    scene: '焦虑发作时、情绪波动时、注意力涣散时、任何需要“回到当下”的时刻。',
    notice: '不要评判感受的好坏，只是单纯地“注意到”。如果某一感官不明显，换一个即可。',
    source: 'MBSR 8 周课程 · 第六周练习',
    steps: [
      '深呼吸一次，让自己安定下来。',
      '找出你此刻能看到的 5 样东西（颜色、形状、光）。',
      '找出你此刻能听到的 4 种声音（远或近）。',
      '找出你此刻能感觉到的 3 种触感（衣服、椅子、空气）。',
      '找出你此刻能闻到的 2 种气味（或回忆一种喜欢的味道）。',
      '找出你此刻能尝到的 1 种味道（或喝一口水感受）。',
      '感受一下：此刻的你，比刚才更平静一些了吗？'
    ]
  },
  {
    key: 'threeMin',
    name: '三分钟呼吸空间',
    duration: '约 3 分钟',
    desc: '正念认知疗法（MBCT）的经典练习，由 Segal、Williams、Teasdale 三位心理学家开发。结构简单，分三步走，是日常生活中最便携的“情绪急救”工具。',
    benefits: [
      '在压力或情绪波动时快速稳定',
      '把“自动导航”切换回“有意识”',
      '预防负面情绪进一步升级',
      '日常随时可做，不挑场地'
    ],
    scene: '工作学习压力大时、与人冲突后、情绪低落时、任何时候感到“心里堵”。',
    notice: '三分钟只是起步，熟练后可以延长。练习不是压抑情绪，而是先觉察、再选择。',
    source: 'Segal / Williams / Teasdale · MBCT',
    steps: [
      '第一分钟：觉察。问自己——我此刻在想什么？感觉什么？身体有什么感受？',
      '第二分钟：聚焦呼吸。把注意力放在呼吸上，感受一呼一吸。',
      '第三分钟：扩展。把注意力从呼吸扩展到整个身体，带着这份觉察回到当下。'
    ]
  },
  {
    key: 'mindfulEating',
    name: '正念饮食',
    duration: '约 4 分钟',
    desc: 'MBSR 创始人 Jon Kabat-Zinn 最著名的经典练习。用一小块食物（如一颗葡萄干）练习全神贯注，打破“无意识进食”，重新连接身体与食物、与当下的关系。',
    benefits: [
      '训练持续的注意力与觉察力',
      '打破“边吃边刷手机”的自动模式',
      '重新感受食物的味道与身体的饱足感',
      '是理解“什么是正念”的最佳入门'
    ],
    scene: '任何一次进餐、午后茶歇、想练习正念但不知从哪里开始时。',
    notice: '不必刻意选择“健康食物”，重点是觉察。练习时不要着急，慢即是快。',
    source: 'Jon Kabat-Zinn 博士 · MBSR 葡萄干练习',
    steps: [
      '准备一小块食物（葡萄干、巧克力、坚果都可）。',
      '拿在手里，先观察它的颜色、形状、纹理。',
      '放到鼻子下闻一闻，感受气味。',
      '放入口中，先不要咀嚼，感受它的质地。',
      '慢慢咀嚼，注意味道的变化。',
      '咽下后，感受它在食道里的移动。',
      '感受此刻的满足感，练习结束。'
    ]
  },
  {
    key: 'mindfulWalking',
    name: '正念行走',
    duration: '约 6 分钟',
    desc: 'MBSR 的经典正式练习之一。把注意力放在行走时的每一步、脚底与地面的接触、身体重心的移动上，把“走路”这件事变成一次静心练习。适合不喜欢久坐冥想的人。',
    benefits: [
      '把正念融入日常动作，不分场地',
      '缓解久坐带来的焦虑与紧绷',
      '训练身体觉察，改善“自动行走”状态',
      '适合不喜欢静坐冥想的人'
    ],
    scene: '课间、午休、通勤路上、任何一段走廊或小路。',
    notice: '不必走慢，但可以比平时更慢一点。重点是“觉察走路”，不是表演走路。',
    source: 'Jon Kabat-Zinn 博士 · MBSR 正念行走',
    steps: [
      '找一条安静、平坦的路径，站定，做三次深呼吸。',
      '把注意力放在脚底，感受脚与地面的接触。',
      '开始缓慢行走，感受每一步抬起、移动、落下的过程。',
      '走到尽头时，停下、转身，再次感受转身的动作。',
      '不评判，只是观察。如果走神，温和地回到脚底。',
      '持续 5-10 分钟，结束时站定，感受呼吸一次。'
    ]
  },
  {
    key: 'mindfulListening',
    name: '正念聆听',
    duration: '约 4 分钟',
    desc: '源自 MBSR 的“声音冥想”，通过全神贯注地聆听环境中的声音（而不评判、不追溯来源），训练“单纯地听见”的能力，帮助大脑从思维反刍中解放。',
    benefits: [
      '放松身心，缓解焦虑',
      '打断过度思考与反刍',
      '提升专注力与听觉敏感度',
      '无需任何道具，随时可做'
    ],
    scene: '安静环境中、睡前、思绪纷杂时、需要从情绪中抽离时。',
    notice: '不评判声音的好坏，也不去追问来源，只是“听见”。没有声音时，聆听“安静”本身。',
    source: 'Jon Kabat-Zinn 博士 · MBSR 声音冥想',
    steps: [
      '闭上眼睛，做三次缓慢的深呼吸。',
      '把注意力放到耳朵，觉察此刻能听到的所有声音。',
      '听见近处的声音（呼吸、心跳、衣服摩擦）。',
      '听见远处的声音（风声、鸟鸣、远处的人声）。',
      '不追踪声音来源，只是单纯地听见。',
      '当走神时，温和地把注意力带回声音。',
      '练习结束，慢慢睁开眼睛，感受此刻的状态。'
    ]
  },
  {
    key: 'lovingKindness',
    name: '慈心冥想',
    duration: '约 5 分钟',
    desc: '源自佛教传统、被现代心理学广泛采用的练习。通过对“自己—亲友—中性人—困难的人—所有生命”逐步送上祝福，培养对他人的善意与自我接纳。',
    benefits: [
      '提升自我接纳与自我关怀',
      '减少愤怒、敌意与孤独感',
      '改善人际关系与同理心',
      '对抑郁、社交焦虑有辅助作用'
    ],
    scene: '情绪低落时、与他人发生矛盾后、睡前、想提升自我关怀时。',
    notice: '不必强迫自己有“温暖感觉”，只是默念祝福语即可。若情绪涌起，允许它，然后继续。',
    source: '佛教传统 / 现代心理学（Loving-Kindness Meditation）',
    steps: [
      '找一个安静的位置，闭上眼睛，做三次缓慢的深呼吸。',
      '把注意力放在胸口，默念：“愿我平安，愿我健康，愿我自在。”',
      '想象一位亲近的亲友，默念：“愿你平安，愿你健康，愿你自在。”',
      '想象一位中性的人（如快递员、路人），送上同样的祝福。',
      '想象一位与你有矛盾的人，尽力送上同样的祝福。',
      '最后，把祝福扩展到所有生命：“愿众生平安、健康、自在。”',
      '深吸一口气，缓缓呼出，睁开眼睛。'
    ]
  },
  {
    key: 'stop',
    name: 'STOP 三分钟练习',
    duration: '约 3 分钟',
    desc: '正念减压疗法（MBSR）中常用的“微练习”，用 4 个字母 S-T-O-P 概括一个完整的正念停顿流程。极简、随时可用，适合在忙碌中“强制暂停”一次。',
    benefits: [
      '在繁忙中强制暂停一次',
      '打破情绪自动反应链',
      '用极短时间恢复觉察与选择',
      '适合高压力工作/学习节奏'
    ],
    scene: '工作学习压力骤增时、情绪即将爆发时、需要决策前、日常任意时刻。',
    notice: 'STOP 只是“暂停”，不是“解决问题”。做完以后，再决定接下来做什么。',
    source: 'MBSR 微练习 · S.T.O.P.',
    steps: [
      'S — Stop（停下）：无论正在做什么，先停下手上的动作。',
      'T — Take a breath（呼吸）：做一次缓慢、深长的呼吸。',
      'O — Observe（觉察）：觉察此刻的念头、情绪、身体感受，不评判。',
      'P — Proceed（继续）：带着这份觉察，再决定下一步怎么做。'
    ]
  }
]

const currentMindful = ref(null)
const mindfulStep = ref(0)

const startMindful = (m) => {
  currentMindful.value = m
  mindfulStep.value = 0
}
const exitMindful = () => {
  currentMindful.value = null
  mindfulStep.value = 0
}
const finishMindful = () => {
  ElMessage.success('练习完成，给自己一个微笑 🌱')
  exitMindful()
}

/* ============ 科普文章 ============ */
const articleList = ref([])
const articleKeyword = ref('')
const articleFilterType = ref('all')
const articleDetailVisible = ref(false)
const articleDialogVisible = ref(false)
const currentArticle = ref({})
const articleForm = ref({ title: '', summary: '', content: '' })

const showArticleList = computed(() => {
  if (articleFilterType.value === 'mine') {
    const uid = currentUser.value?.id
    return articleList.value.filter(a => a.userId != null && a.userId === uid)
  }
  return articleList.value
})

const loadArticleList = async () => {
  const res = await $axios.get('/science/article/list', {
    params: articleKeyword.value ? { keyword: articleKeyword.value } : {}
  })
  if (res.data.code === 200) {
    articleList.value = res.data.data
  }
}

const openArticleDetail = (a) => {
  currentArticle.value = a
  articleDetailVisible.value = true
}

const formatContent = (content) => {
  if (!content) return ''
  return String(content)
      .split(/\n\s*\n/)
      .map(p => `<p>${p.replace(/\n/g, '<br>')}</p>`)
      .join('')
}

const openArticleDialog = () => {
  articleForm.value = { title: '', summary: '', content: '' }
  articleDialogVisible.value = true
}

const submitArticle = async () => {
  if (!articleForm.value.title) return ElMessage.warning('请填写标题')
  if (!articleForm.value.content) return ElMessage.warning('请填写正文')
  try {
    await $axios.post('/science/article/add', articleForm.value)
    ElMessage.success('发布成功')
    articleDialogVisible.value = false
    await loadArticleList()
  } catch (err) {
    ElMessage.error(err.msg || '发布失败')
  }
}

const delArticle = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这篇文章？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) { return }
  try {
    await $axios.post(`/science/article/delete?id=${id}`)
    ElMessage.success('已删除')
    await loadArticleList()
  } catch (err) {
    ElMessage.error(err.msg || '删除失败')
  }
}

/* ============ 生命周期 ============ */
onMounted(() => {
  loadTodayQuote()
  loadArticleList()
})

onUnmounted(() => {
  if (breathing.timer) clearInterval(breathing.timer)
  if (breathing.readyTimer) clearInterval(breathing.readyTimer)
})
</script>

<style scoped>
.page-wrap {
  padding: 24px;
  max-width: 1080px;
  margin: 0 auto;
}
.header-box {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.tip-text {
  color: #666;
  font-size: 13px;
  margin: 4px 0 0;
  line-height: 1.7;
}

/* ============ 每日一句 ============ */
.daily-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 460px;
  padding: 40px 20px 60px;
}
.daily-date {
  color: #909399;
  font-size: 14px;
  margin-bottom: 32px;
}
.daily-circle {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: linear-gradient(135deg, #a8d8ff 0%, #409eff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  user-select: none;
  box-shadow: 0 12px 32px rgba(64, 158, 255, 0.35);
  transition: transform 0.25s, box-shadow 0.25s;
}
.daily-circle:hover {
  transform: scale(1.05);
  box-shadow: 0 16px 40px rgba(64, 158, 255, 0.5);
}
.daily-circle:active {
  transform: scale(0.97);
}
.daily-circle-title {
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 2px;
}
.daily-circle-sub {
  font-size: 13px;
  margin-top: 8px;
  opacity: 0.9;
}
.daily-result {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  line-height: 1.8;
  max-width: 640px;
  text-align: center;
  padding: 40px 48px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f4ff 100%);
  border-radius: 20px;
  animation: fadeInUp 0.6s ease;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
.daily-hint {
  margin-top: 24px;
  color: #909399;
  font-size: 13px;
}

/* ============ 呼吸练习 ============ */
.breath-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
  margin-top: 12px;
}
.breath-card {
  transition: transform 0.25s;
}
.breath-card:hover {
  transform: translateY(-4px);
}
.breath-name {
  font-weight: 600;
  font-size: 17px;
  margin-bottom: 8px;
}
.breath-desc {
  color: #666;
  font-size: 13px;
  line-height: 1.7;
  margin-bottom: 12px;
}

.breath-block {
  margin-top: 10px;
}
.block-title {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 6px;
}
.block-list {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  font-size: 13px;
  line-height: 1.75;
}
.block-list li {
  margin-bottom: 2px;
}
.block-text {
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
}

.breath-pattern {
  color: #409eff;
  font-size: 13px;
  margin-top: 12px;
  padding: 8px 12px;
  background: #f0f9ff;
  border-radius: 8px;
}
.breath-source {
  color: #b0b3b8;
  font-size: 11px;
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px dashed #f0f0f0;
}
.breath-card-footer {
  margin-top: 14px;
  text-align: right;
}

.breath-playing {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  padding: 40px 20px 60px;
}
.breath-circle-wrap {
  position: relative;
  width: 320px;
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}
.breath-circle {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, #a8d8ff 0%, #409eff 100%);
  transform-origin: center center;
  transition: transform 1s ease-in-out, background 1s;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
}
.breath-circle.inhale {
  transform: scale(1.4);
}
.breath-circle.exhale {
  transform: scale(0.85);
}
.breath-circle.hold {
  transform: scale(1.2);
  background: radial-gradient(circle, #b8e2c8 0%, #67c23a 100%);
}
.breath-circle.ready {
  transform: scale(1);
  background: radial-gradient(circle, #d0e8ff 0%, #79bbff 100%);
}
.breath-text {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  color: #fff;
  text-shadow: 0 1px 4px rgba(0,0,0,0.15);
}
.phase-text {
  font-size: 22px;
  font-weight: 500;
  line-height: 1;
}
.phase-count {
  font-size: 52px;
  font-weight: 700;
  line-height: 1;
  margin-top: 8px;
}
.breath-progress {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
}
.breath-actions {
  margin-top: 8px;
}

/* ============ 正念练习 ============ */
.mindful-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
  margin-top: 12px;
}
.mindful-card {
  transition: transform 0.25s;
}
.mindful-card:hover {
  transform: translateY(-4px);
}
.mindful-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 8px;
}
.mindful-name {
  font-weight: 600;
  font-size: 17px;
}
.mindful-duration {
  color: #909399;
  font-size: 12px;
}
.mindful-desc {
  color: #666;
  font-size: 13px;
  line-height: 1.7;
  margin-bottom: 10px;
}

.mindful-block {
  margin-top: 10px;
}
.mindful-source {
  color: #b0b3b8;
  font-size: 11px;
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px dashed #f0f0f0;
}
.mindful-card-footer {
  margin-top: 14px;
  text-align: right;
}

.mindful-playing {
  padding: 40px 20px;
  text-align: center;
}
.mindful-title {
  color: #409eff;
  font-size: 14px;
  margin-bottom: 20px;
}
.mindful-text {
  font-size: 18px;
  color: #333;
  line-height: 1.8;
  max-width: 640px;
  margin: 0 auto;
  min-height: 120px;
  padding: 24px;
  background: #fafafa;
  border-radius: 12px;
}
.mindful-actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* ============ 科普文章 ============ */
.article-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.article-search {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.article-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 24px;
}
.article-card {
  cursor: default;
}
.article-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  cursor: pointer;
}
.article-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}
.article-title:hover {
  color: #409eff;
}
.article-time {
  color: #909399;
  font-size: 12px;
}
.article-summary {
  color: #666;
  font-size: 13px;
  margin: 8px 0;
  cursor: pointer;
  line-height: 1.6;
}
.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}
.detail-meta {
  color: #909399;
  font-size: 12px;
  margin-bottom: 12px;
}
.detail-content {
  line-height: 1.8;
  color: #333;
  font-size: 14px;
}
.detail-content :deep(p) {
  margin: 0 0 12px;
}
</style>