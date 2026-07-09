<template>
  <div class="health-page">
    <header class="health-hero">
      <button class="back-btn" type="button" @click="router.back()">
        <van-icon name="arrow-left" />
      </button>
      <div class="hero-copy">
        <span class="eyebrow">HEALTH INSIGHTS</span>
        <h1>健康资讯</h1>
        <p>精选权威健康内容，帮助你做好日常管理</p>
      </div>
      <div class="hero-card">
        <img :src="featuredArticle.image" :alt="featuredArticle.title" />
        <div>
          <span>{{ featuredArticle.category }}</span>
          <strong>{{ featuredArticle.title }}</strong>
        </div>
      </div>
    </header>

    <main class="health-content">
      <section class="search-panel">
        <div class="search-box">
          <van-icon name="search" />
          <input v-model.trim="keyword" type="search" placeholder="搜索营养、睡眠、慢病管理" />
        </div>
        <button class="fav-filter" type="button" :class="{ active: onlyFavorites }" @click="onlyFavorites = !onlyFavorites">
          <van-icon :name="onlyFavorites ? 'star' : 'star-o'" />
          <span>收藏</span>
        </button>
      </section>

      <section class="category-strip" aria-label="资讯分类">
        <button
          v-for="item in categories"
          :key="item"
          type="button"
          :class="{ active: activeCategory === item }"
          @click="activeCategory = item"
        >
          {{ item }}
        </button>
      </section>

      <section class="article-list">
        <article v-for="article in filteredArticles" :key="article.id" class="article-card" @click="openArticle(article)">
          <div class="article-cover">
            <img :src="article.image" :alt="article.title" />
            <span>{{ article.category }}</span>
          </div>
          <div class="article-body">
            <div class="article-meta">
              <span>{{ article.readTime }}</span>
              <span>{{ article.source }}</span>
            </div>
            <h2>{{ article.title }}</h2>
            <p>{{ article.summary }}</p>
            <div class="article-actions">
              <button type="button" @click.stop="toggleFavorite(article.id)">
                <van-icon :name="isFavorite(article.id) ? 'star' : 'star-o'" />
                <span>{{ isFavorite(article.id) ? '已收藏' : '收藏' }}</span>
              </button>
              <button type="button" @click.stop="openArticle(article, true)">
                <van-icon name="comment-o" />
                <span>{{ commentCount(article.id) }} 条评论</span>
              </button>
            </div>
          </div>
        </article>

        <div v-if="filteredArticles.length === 0" class="empty-news">
          <van-icon name="newspaper-o" />
          <strong>暂无匹配资讯</strong>
          <span>换个关键词或取消收藏筛选试试。</span>
        </div>
      </section>
    </main>

    <van-popup v-model:show="detailVisible" position="bottom" round :style="{ height: '92%' }">
      <article v-if="currentArticle" class="detail-sheet">
        <div class="sheet-handle"></div>
        <div class="detail-cover">
          <img :src="currentArticle.image" :alt="currentArticle.title" />
          <button type="button" class="close-btn" @click="detailVisible = false">
            <van-icon name="cross" />
          </button>
        </div>

        <div class="detail-content">
          <div class="article-meta">
            <span>{{ currentArticle.category }}</span>
            <span>{{ currentArticle.readTime }}</span>
          </div>
          <h2>{{ currentArticle.title }}</h2>
          <p class="detail-summary">{{ currentArticle.summary }}</p>

          <section class="key-points">
            <h3>重点提示</h3>
            <ul>
              <li v-for="point in currentArticle.points" :key="point">{{ point }}</li>
            </ul>
          </section>

          <section class="detail-section">
            <h3>健康建议</h3>
            <p v-for="paragraph in currentArticle.content" :key="paragraph">{{ paragraph }}</p>
          </section>

          <a class="source-link" :href="currentArticle.sourceUrl" target="_blank" rel="noreferrer">
            参考来源：{{ currentArticle.source }}
            <van-icon name="arrow" />
          </a>

          <section class="comments-block" ref="commentsRef">
            <div class="comments-head">
              <h3>评论</h3>
              <span>{{ currentComments.length }} 条</span>
            </div>
            <div class="comment-editor">
              <input v-model.trim="commentText" maxlength="80" placeholder="写下你的想法或问题" />
              <button type="button" @click="submitComment">发送</button>
            </div>
            <div v-if="currentComments.length" class="comment-list">
              <div v-for="comment in currentComments" :key="comment.id" class="comment-item">
                <span class="avatar">{{ comment.author.slice(0, 1) }}</span>
                <div>
                  <strong>{{ comment.author }}</strong>
                  <p>{{ comment.content }}</p>
                  <time>{{ comment.time }}</time>
                </div>
              </div>
            </div>
            <div v-else class="no-comments">还没有评论，欢迎留下第一条想法。</div>
          </section>
        </div>

        <footer class="detail-toolbar">
          <button type="button" :class="{ active: isFavorite(currentArticle.id) }" @click="toggleFavorite(currentArticle.id)">
            <van-icon :name="isFavorite(currentArticle.id) ? 'star' : 'star-o'" />
            <span>{{ isFavorite(currentArticle.id) ? '已收藏' : '收藏' }}</span>
          </button>
          <button type="button" @click="scrollToComments">
            <van-icon name="comment-o" />
            <span>评论</span>
          </button>
        </footer>
      </article>
    </van-popup>

    <van-tabbar v-model="active" route>
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="add-o" to="/register">挂号</van-tabbar-item>
      <van-tabbar-item icon="notes-o" to="/record">病历</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const active = ref(0)
const keyword = ref('')
const activeCategory = ref('全部')
const onlyFavorites = ref(false)
const detailVisible = ref(false)
const currentArticle = ref(null)
const commentText = ref('')
const favorites = ref([])
const comments = ref({})
const commentsRef = ref(null)

const storageKeys = {
  favorites: 'healthNewsFavorites',
  comments: 'healthNewsComments'
}

const articles = [
  {
    id: 'diet-balance',
    category: '营养',
    title: '均衡饮食：把控油盐糖放在每天餐桌上',
    summary: '合理搭配主食、蔬果、蛋白质和健康脂肪，是慢病预防和体重管理的基础。',
    source: 'WHO',
    sourceUrl: 'https://www.who.int/news-room/fact-sheets/detail/healthy-diet',
    readTime: '4 分钟',
    image: 'https://images.unsplash.com/photo-1498837167922-ddd27525d352?auto=format&fit=crop&w=1200&q=80',
    points: ['每天保证足量蔬菜水果', '减少高盐、高糖和反式脂肪摄入', '优先选择蒸煮炖等少油方式'],
    content: [
      '日常饮食不需要追求复杂，关键是稳定和可持续。每餐尽量让一半盘子留给蔬菜，主食选择粗细搭配，蛋白质来源可在鱼、蛋、奶、豆制品和瘦肉之间轮换。',
      '加工食品常常含有较多盐和糖，购买时可以留意营养成分表。已有高血压、糖尿病、高脂血症的人群，应按照医生或营养师建议进一步个体化调整。'
    ]
  },
  {
    id: 'hand-hygiene',
    category: '预防',
    title: '手卫生不是小事：就医前后这些时刻要洗手',
    summary: '规范洗手能减少病原体传播，是门诊、家庭护理和呼吸道传染病预防的重要环节。',
    source: 'CDC',
    sourceUrl: 'https://www.cdc.gov/clean-hands/about/index.html',
    readTime: '3 分钟',
    image: 'https://images.unsplash.com/photo-1584483766114-2cea6facdf57?auto=format&fit=crop&w=1200&q=80',
    points: ['饭前、如厕后、接触公共设施后洗手', '流水和肥皂揉搓至少 20 秒', '不方便洗手时使用含酒精免洗手消毒剂'],
    content: [
      '门诊环境人员流动大，电梯按钮、候诊椅、门把手等位置都可能被频繁接触。洗手时应覆盖手心、手背、指缝、指尖和手腕。',
      '如果皮肤有破损、湿疹或正在护理免疫力较弱的家人，手卫生更要稳定执行。免洗消毒剂不能完全替代流水洗手，双手明显脏污时仍应优先洗手。'
    ]
  },
  {
    id: 'sleep-routine',
    category: '睡眠',
    title: '睡眠管理：让身体恢复有一个固定节律',
    summary: '规律作息、减少睡前刺激和建立放松流程，有助于改善睡眠质量。',
    source: 'CDC',
    sourceUrl: 'https://www.cdc.gov/sleep/about/index.html',
    readTime: '4 分钟',
    image: 'https://images.unsplash.com/photo-1511295742362-92c96b1cf484?auto=format&fit=crop&w=1200&q=80',
    points: ['固定起床时间比临时补觉更重要', '睡前减少咖啡因、酒精和强光刺激', '连续失眠应及时就医评估'],
    content: [
      '睡眠不是简单地“睡够时间”，还包括节律稳定和醒后状态。建议每天尽量在相近时间起床，白天适度接触自然光，晚上逐渐降低环境亮度。',
      '若长期打鼾、夜间憋醒、白天明显困倦，可能与睡眠呼吸问题有关，需要到医院进一步评估。不要长期自行服用助眠药物。'
    ]
  },
  {
    id: 'blood-pressure',
    category: '慢病',
    title: '血压管理：家庭测量要比偶尔一次更有价值',
    summary: '规范家庭血压记录能帮助医生判断趋势，减少“偶然高一次”的误判。',
    source: 'WHO',
    sourceUrl: 'https://www.who.int/news-room/fact-sheets/detail/hypertension',
    readTime: '5 分钟',
    image: 'https://images.unsplash.com/photo-1581595220892-b0739db3ba8c?auto=format&fit=crop&w=1200&q=80',
    points: ['测量前安静休息 5 分钟', '同一时间段连续记录更有参考价值', '不要自行停药或频繁改药'],
    content: [
      '家庭血压测量建议坐位、上臂与心脏同高，袖带大小合适。记录时写清日期、时间、收缩压、舒张压和心率。',
      '血压管理通常需要生活方式和药物共同配合。若出现胸痛、呼吸困难、肢体无力、剧烈头痛等症状，应立即就医。'
    ]
  },
  {
    id: 'activity',
    category: '运动',
    title: '运动处方：从可坚持的 10 分钟开始',
    summary: '规律身体活动能改善心肺功能、情绪状态和代谢水平，关键是循序渐进。',
    source: 'WHO',
    sourceUrl: 'https://www.who.int/news-room/fact-sheets/detail/physical-activity',
    readTime: '4 分钟',
    image: 'https://images.unsplash.com/photo-1554284126-aa88f22d8b74?auto=format&fit=crop&w=1200&q=80',
    points: ['久坐人群先从低强度开始', '有基础疾病先咨询医生', '运动后明显胸闷心悸需停止并就医'],
    content: [
      '运动不必一开始就追求高强度。快走、骑车、游泳、抗阻训练都可以纳入计划，先让身体适应，再逐渐增加频次和时长。',
      '老年人和慢病患者尤其需要关注安全。运动前后做好热身和放松，避免空腹或过饱后立即运动。'
    ]
  }
]

const categories = computed(() => ['全部', ...new Set(articles.map((item) => item.category))])
const featuredArticle = computed(() => articles[0])
const filteredArticles = computed(() => {
  const value = keyword.value.toLowerCase()
  return articles.filter((article) => {
    const matchesCategory = activeCategory.value === '全部' || article.category === activeCategory.value
    const matchesKeyword = !value || `${article.title}${article.summary}${article.category}`.toLowerCase().includes(value)
    const matchesFavorite = !onlyFavorites.value || isFavorite(article.id)
    return matchesCategory && matchesKeyword && matchesFavorite
  })
})
const currentComments = computed(() => currentArticle.value ? (comments.value[currentArticle.value.id] || []) : [])

function isFavorite(id) {
  return favorites.value.includes(id)
}

function toggleFavorite(id) {
  if (isFavorite(id)) {
    favorites.value = favorites.value.filter((item) => item !== id)
    showToast('已取消收藏')
  } else {
    favorites.value = [...favorites.value, id]
    showToast('已收藏')
  }
  localStorage.setItem(storageKeys.favorites, JSON.stringify(favorites.value))
}

function commentCount(id) {
  return (comments.value[id] || []).length
}

function openArticle(article, focusComments = false) {
  currentArticle.value = article
  detailVisible.value = true
  commentText.value = ''
  if (focusComments) {
    nextTick(scrollToComments)
  }
}

function openArticleFromRoute() {
  const articleId = route.query.articleId
  if (!articleId) return
  const target = articles.find((article) => article.id === articleId)
  if (target) {
    openArticle(target)
  }
}

function submitComment() {
  if (!currentArticle.value) return
  if (!commentText.value) {
    showToast('请输入评论内容')
    return
  }
  const username = localStorage.getItem('patientName') || localStorage.getItem('username') || '患者'
  const nextComment = {
    id: `${Date.now()}`,
    author: username,
    content: commentText.value,
    time: new Date().toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
  }
  const articleId = currentArticle.value.id
  comments.value = {
    ...comments.value,
    [articleId]: [nextComment, ...(comments.value[articleId] || [])]
  }
  localStorage.setItem(storageKeys.comments, JSON.stringify(comments.value))
  commentText.value = ''
  showToast('评论已发布')
}

function scrollToComments() {
  nextTick(() => {
    commentsRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

onMounted(() => {
  try {
    favorites.value = JSON.parse(localStorage.getItem(storageKeys.favorites) || '[]')
    comments.value = JSON.parse(localStorage.getItem(storageKeys.comments) || '{}')
  } catch (error) {
    favorites.value = []
    comments.value = {}
  }
  openArticleFromRoute()
})

watch(() => route.query.articleId, () => {
  openArticleFromRoute()
})
</script>

<style scoped lang="scss">
.health-page {
  min-height: 100vh;
  padding-bottom: calc(78px + env(safe-area-inset-bottom));
  background:
    radial-gradient(circle at 16% 0%, rgba(96, 165, 250, .4), transparent 32%),
    linear-gradient(180deg, #e6f0ff 0%, #f4f9ff 44%, #e6f0ff 100%);
  color: #1e3a5f;
}

.health-hero {
  position: relative;
  min-height: 238px;
  padding: 20px 16px 28px;
  overflow: hidden;
  background:
    linear-gradient(120deg, rgba(255, 255, 255, .18) 0%, rgba(255, 255, 255, 0) 28%),
    linear-gradient(155deg, #0f4fbd 0%, #1476d8 34%, #19a7c7 66%, #42c99c 100%);
  box-shadow: inset 0 -1px 0 rgba(255, 255, 255, .24), 0 18px 42px rgba(20, 118, 216, .22);

  &::before {
    position: absolute;
    inset: 0;
    opacity: .45;
    background:
      repeating-linear-gradient(115deg, rgba(255, 255, 255, .18) 0 1px, transparent 1px 18px),
      linear-gradient(90deg, rgba(255, 255, 255, .22), transparent 42%);
    clip-path: polygon(42% 0, 100% 0, 100% 72%, 68% 100%);
    content: "";
  }

  &::after {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 88px;
    content: "";
    background: linear-gradient(180deg, rgba(255, 255, 255, 0), rgba(221, 239, 255, .34));
  }
}

.back-btn {
  position: relative;
  z-index: 2;
  display: grid;
  width: 38px;
  height: 38px;
  border: 1px solid rgba(255, 255, 255, .35);
  border-radius: 8px;
  color: #fff;
  background: rgba(11, 50, 116, .22);
  place-items: center;
  font-size: 19px;
  box-shadow: 0 10px 24px rgba(7, 52, 115, .18);
  backdrop-filter: blur(12px);
}

.hero-copy {
  position: relative;
  z-index: 2;
  margin-top: 16px;
  color: #fff;

  h1 {
    margin: 6px 0 6px;
    font-size: 32px;
    line-height: 1.1;
    font-weight: 900;
    text-shadow: 0 10px 26px rgba(10, 64, 132, .24);
  }

  p {
    max-width: 260px;
    margin: 0;
    color: rgba(244, 252, 255, .9);
    font-size: 14px;
    line-height: 1.45;
    font-weight: 800;
  }
}

.eyebrow {
  color: rgba(235, 255, 248, .86);
  font-size: 10px;
  font-weight: 900;
  letter-spacing: .14em;
}

.hero-card {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 104px 1fr;
  gap: 12px;
  align-items: center;
  margin-top: 20px;
  padding: 10px 12px 10px 10px;
  border: 1px solid rgba(255, 255, 255, .32);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, .28), rgba(255, 255, 255, .12)),
    rgba(8, 70, 145, .2);
  box-shadow: 0 18px 38px rgba(7, 55, 95, .22);
  backdrop-filter: blur(16px);

  img {
    width: 104px;
    height: 74px;
    border-radius: 8px;
    object-fit: cover;
    box-shadow: 0 10px 22px rgba(7, 55, 95, .18);
  }

  span,
  strong {
    display: block;
  }

  span {
    color: rgba(232, 255, 248, .82);
    font-size: 11px;
    font-weight: 800;
  }

  strong {
    margin-top: 5px;
    color: #fff;
    font-size: 16px;
    line-height: 1.3;
    font-weight: 900;
  }
}

.health-content {
  position: relative;
  z-index: 3;
  margin-top: -26px;
  padding: 0 14px;
}

.search-panel {
  display: grid;
  grid-template-columns: 1fr 78px;
  gap: 10px;
  padding: 12px;
  border: 1px solid rgba(213, 235, 228, .9);
  border-radius: 8px;
  background: rgba(255, 255, 255, .9);
  box-shadow: 0 16px 34px rgba(55, 121, 107, .12);
  backdrop-filter: blur(14px);
}

.search-box {
  display: flex;
  min-width: 0;
  height: 42px;
  padding: 0 12px;
  border-radius: 8px;
  background: #f0f5ff;
  color: #3b82f6;
  align-items: center;
  gap: 8px;

  input {
    min-width: 0;
    flex: 1;
    border: 0;
    outline: 0;
    background: transparent;
    color: #1e3a5f;
    font-size: 14px;
    font-weight: 700;
  }
}

.fav-filter {
  border: 0;
  border-radius: 8px;
  color: #475569;
  background: #eff6ff;
  font-size: 12px;
  font-weight: 800;

  &.active {
    color: #fff;
    background: #357ABD;
  }
}

.category-strip {
  display: flex;
  gap: 8px;
  margin: 14px -14px 12px;
  padding: 0 14px;
  overflow-x: auto;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }

  button {
    flex: 0 0 auto;
    height: 34px;
    padding: 0 14px;
    border: 1px solid rgba(191, 224, 216, .8);
    border-radius: 8px;
    color: #475569;
    background: rgba(255, 255, 255, .82);
    font-size: 13px;
    font-weight: 800;

    &.active {
      color: #fff;
      border-color: #357ABD;
      background: #357ABD;
      box-shadow: 0 8px 18px rgba(31, 138, 125, .2);
    }
  }
}

.article-list {
  display: grid;
  gap: 14px;
}

.article-card {
  overflow: hidden;
  border: 1px solid rgba(213, 235, 228, .88);
  border-radius: 8px;
  background: rgba(255, 255, 255, .92);
  box-shadow: 0 18px 38px rgba(55, 121, 107, .1);
}

.article-cover {
  position: relative;
  height: 162px;
  overflow: hidden;
  background: #dbeafe;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  span {
    position: absolute;
    left: 12px;
    bottom: 12px;
    padding: 5px 10px;
    border-radius: 8px;
    color: #fff;
    background: rgba(12, 73, 67, .72);
    font-size: 12px;
    font-weight: 900;
    backdrop-filter: blur(10px);
  }
}

.article-body {
  padding: 14px;

  h2 {
    margin: 8px 0 8px;
    color: #1e3a5f;
    font-size: 18px;
    line-height: 1.35;
    font-weight: 900;
  }

  p {
    margin: 0;
    color: #64748b;
    font-size: 13px;
    line-height: 1.6;
    font-weight: 600;
  }
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 11px;
  font-weight: 800;

  span {
    padding: 3px 7px;
    border-radius: 8px;
    background: #eff6ff;
  }
}

.article-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 14px;

  button {
    height: 38px;
    border: 1px solid rgba(198, 226, 219, .8);
    border-radius: 8px;
    color: #2563eb;
    background: #f8fafc;
    font-size: 13px;
    font-weight: 800;
  }
}

.empty-news {
  min-height: 180px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  color: #64748b;
  text-align: center;

  .van-icon {
    font-size: 34px;
    color: #93c5fd;
  }

  strong {
    color: #1e3a5f;
    font-size: 16px;
  }

  span {
    font-size: 13px;
  }
}

.detail-sheet {
  min-height: 100%;
  padding-bottom: 74px;
  background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
}

.sheet-handle {
  position: absolute;
  top: 8px;
  left: 50%;
  z-index: 4;
  width: 44px;
  height: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, .78);
  transform: translateX(-50%);
}

.detail-cover {
  position: relative;
  height: 224px;
  background: #dbeafe;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &::after {
    position: absolute;
    inset: 0;
    content: "";
    background: linear-gradient(180deg, rgba(0, 0, 0, .18), transparent 44%, rgba(0, 0, 0, .28));
  }
}

.close-btn {
  position: absolute;
  top: 18px;
  right: 16px;
  z-index: 3;
  display: grid;
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 8px;
  color: #fff;
  background: rgba(0, 0, 0, .35);
  place-items: center;
}

.detail-content {
  padding: 18px 16px;

  h2 {
    margin: 10px 0 10px;
    color: #1e3a5f;
    font-size: 23px;
    line-height: 1.28;
    font-weight: 900;
  }
}

.detail-summary {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.65;
  font-weight: 600;
}

.key-points,
.detail-section,
.comments-block {
  margin-top: 16px;
  padding: 16px;
  border: 1px solid rgba(213, 235, 228, .86);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 26px rgba(55, 121, 107, .08);

  h3 {
    margin: 0 0 12px;
    color: #1e3a5f;
    font-size: 16px;
    font-weight: 900;
  }
}

.key-points ul {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.key-points li {
  position: relative;
  padding-left: 18px;
  color: #475569;
  font-size: 14px;
  line-height: 1.5;
  font-weight: 700;

  &::before {
    position: absolute;
    top: 8px;
    left: 0;
    width: 7px;
    height: 7px;
    border-radius: 50%;
    content: "";
    background: #357ABD;
  }
}

.detail-section p {
  margin: 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.75;
  font-weight: 600;

  + p {
    margin-top: 10px;
  }
}

.source-link {
  display: flex;
  height: 42px;
  margin-top: 14px;
  padding: 0 12px;
  border-radius: 8px;
  color: #1d4ed8;
  background: #eff6ff;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 800;
  text-decoration: none;
}

.comments-head {
  display: flex;
  align-items: center;
  justify-content: space-between;

  span {
    color: #64748b;
    font-size: 12px;
    font-weight: 800;
  }
}

.comment-editor {
  display: grid;
  grid-template-columns: 1fr 64px;
  gap: 8px;

  input {
    min-width: 0;
    height: 42px;
    padding: 0 12px;
    border: 1px solid rgba(198, 226, 219, .9);
    border-radius: 8px;
    outline: 0;
    color: #1e3a5f;
    background: #f8fafc;
    font-size: 14px;
    font-weight: 700;
  }

  button {
    border: 0;
    border-radius: 8px;
    color: #fff;
    background: #357ABD;
    font-size: 14px;
    font-weight: 900;
  }
}

.comment-list {
  display: grid;
  gap: 12px;
  margin-top: 14px;
}

.comment-item {
  display: grid;
  grid-template-columns: 36px 1fr;
  gap: 10px;

  strong {
    color: #1e3a5f;
    font-size: 13px;
    font-weight: 900;
  }

  p {
    margin: 4px 0;
    color: #475569;
    font-size: 14px;
    line-height: 1.45;
  }

  time {
    color: #94a3b8;
    font-size: 11px;
    font-weight: 700;
  }
}

.avatar {
  display: grid;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #fff;
  background: linear-gradient(135deg, #357ABD, #7BB3E8);
  place-items: center;
  font-size: 14px;
  font-weight: 900;
}

.no-comments {
  margin-top: 14px;
  padding: 14px;
  border-radius: 8px;
  color: #64748b;
  background: #f0f5ff;
  font-size: 13px;
  text-align: center;
}

.detail-toolbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 10px 14px calc(10px + env(safe-area-inset-bottom));
  border-top: 1px solid rgba(213, 235, 228, .9);
  background: rgba(255, 255, 255, .94);
  backdrop-filter: blur(16px);

  button {
    height: 44px;
    border: 1px solid rgba(198, 226, 219, .88);
    border-radius: 8px;
    color: #2563eb;
    background: #f8fafc;
    font-size: 14px;
    font-weight: 900;

    &.active {
      color: #fff;
      border-color: #357ABD;
      background: #357ABD;
    }
  }
}
</style>
