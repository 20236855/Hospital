<template>
  <div class="detail-page">
    <div class="back-btn" @click="goBack">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="m15 18-6-6 6-6" />
      </svg>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <template v-else-if="doctor">
        <!-- 头部区域：大头像+基本信息 -->
        <div class="detail-hero">
          <div class="hero-bg">
            <svg viewBox="0 0 400 300" aria-hidden="true">
              <defs>
                <linearGradient id="heroBgGrad" x1="0" x2="1" y1="0" y2="1">
                  <stop offset="0" stop-color="#cfe8d8" />
                  <stop offset="1" stop-color="#b9e1cd" />
                </linearGradient>
              </defs>
              <circle cx="350" cy="60" r="80" fill="url(#heroBgGrad)" opacity="0.4" />
              <circle cx="320" cy="200" r="50" fill="url(#heroBgGrad)" opacity="0.3" />
              <circle cx="50" cy="180" r="40" fill="url(#heroBgGrad)" opacity="0.3" />
              <path d="M0 260 Q 100 230, 200 250 T 400 240 L 400 300 L 0 300 Z" fill="#cfe8d8" opacity="0.5" />
            </svg>
          </div>

          <div class="hero-content">
            <div class="big-avatar-wrap">
              <img :src="getAvatar(doctor)" @error="onAvatarError" class="big-avatar" />
              <div class="avatar-ring" aria-hidden="true"></div>
            </div>
            <div class="hero-info">
              <h1 class="doctor-name">{{ doctor.doctorName }}</h1>
              <div class="level-row">
                <span class="level-tag" v-if="doctor.levelName">{{ doctor.levelName }}</span>
                <span class="status-tag" :class="{ online: doctor.status === '0' }">
                  {{ doctor.status === '0' ? '在职' : '停诊' }}
                </span>
              </div>
              <div class="dept-row" v-if="doctor.deptId">
                <svg viewBox="0 0 24 24" class="info-icon" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 21h18M5 21V7l8-4v18M19 21V11l-6-4" />
                </svg>
                <span>{{ doctor.deptId }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 基本信息区 -->
        <div class="info-section">
          <div class="info-card">
            <div class="info-row" v-if="doctor.gender">
              <div class="info-icon-wrap">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="8" r="5" />
                  <path d="M20 21a8 8 0 0 0-16 0" />
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">性别</span>
                <span class="info-value">{{ doctor.gender }}</span>
              </div>
            </div>

            <div class="info-row" v-if="doctor.phone">
              <div class="info-icon-wrap">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92Z" />
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">联系电话</span>
                <span class="info-value">{{ doctor.phone }}</span>
              </div>
            </div>

            <div class="info-row" v-if="doctor.doctorNo">
              <div class="info-icon-wrap">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z" />
                  <path d="M14 2v6h6" />
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">工号</span>
                <span class="info-value">{{ doctor.doctorNo }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 擅长领域 -->
        <div class="info-section" v-if="doctor.specialty">
          <div class="section-title">
            <div class="title-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12a9 9 0 1 1-6.219-8.56" />
              </svg>
            </div>
            <span>擅长领域</span>
          </div>
          <div class="content-card">
            <p>{{ doctor.specialty }}</p>
          </div>
        </div>

        <!-- 医生简介 -->
        <div class="info-section" v-if="doctor.introduction">
          <div class="section-title">
            <div class="title-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2Z" />
                <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7Z" />
              </svg>
            </div>
            <span>医生简介</span>
          </div>
          <div class="content-card">
            <p>{{ doctor.introduction }}</p>
          </div>
        </div>

        <!-- 装饰性底部 -->
        <div class="bottom-deco" aria-hidden="true">
          <svg viewBox="0 0 300 80">
            <path d="M0 60 Q 75 20, 150 50 T 300 40 L 300 80 L 0 80 Z" fill="#b9e1cd" opacity="0.3" />
            <path d="M0 70 Q 75 40, 150 60 T 300 55 L 300 80 L 0 80 Z" fill="#cfe8d8" opacity="0.5" />
          </svg>
        </div>
      </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDoctor } from '@/api/doctor'
import defAva from '@/assets/images/title.png'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const doctor = ref(null)

function getAvatar(d) {
  return d?.avatar || defAva
}

function onAvatarError(e) {
  e.target.src = defAva
}

function fetchDoctorDetail() {
  const doctorId = route.params.id
  if (!doctorId) {
    goBack()
    return
  }
  loading.value = true
  getDoctor(doctorId).then(response => {
    doctor.value = response.data
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

onMounted(() => {
  fetchDoctorDetail()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f5faf7;
  position: relative;
  padding-bottom: 40px;
}

.back-btn {
  position: fixed;
  top: 16px;
  left: 16px;
  z-index: 100;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 10px rgba(26, 77, 69, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgb(26, 77, 69);
  cursor: pointer;
}

.back-btn svg {
  width: 22px;
  height: 22px;
}

/* 头部英雄区 */
.detail-hero {
  position: relative;
  background: linear-gradient(135deg, rgb(185, 225, 205) 0%, rgb(223, 246, 239) 100%);
  padding: 60px 20px 48px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-bg svg {
  width: 100%;
  height: 100%;
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
}

.big-avatar-wrap {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.big-avatar {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  object-fit: cover;
  border: 6px solid #fff;
  box-shadow: 0 8px 32px rgba(26, 77, 69, 0.2);
  background: #fff;
  position: relative;
  z-index: 2;
}

.avatar-ring {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  border: 2px dashed rgba(255, 255, 255, 0.7);
  animation: rotateRing 20s linear infinite;
}

@keyframes rotateRing {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.doctor-name {
  font-size: 26px;
  font-weight: 700;
  color: rgb(26, 77, 69);
  margin: 0 0 12px;
}

.level-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
}

.level-tag {
  font-size: 13px;
  color: rgb(230, 162, 60);
  background: #fdf6ec;
  padding: 6px 14px;
  border-radius: 12px;
  font-weight: 500;
}

.status-tag {
  font-size: 13px;
  color: #fff;
  background: rgba(144, 147, 153, 0.9);
  padding: 6px 14px;
  border-radius: 12px;
  font-weight: 500;
}

.status-tag.online {
  background: rgba(103, 194, 58, 0.95);
}

.dept-row {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: rgba(26, 77, 69, 0.75);
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
}

.info-icon {
  width: 16px;
  height: 16px;
}

/* 信息区 */
.info-section {
  padding: 0 20px;
  margin-top: 20px;
}

.info-card {
  background: #fff;
  border-radius: 16px;
  padding: 8px 16px;
  box-shadow: 0 2px 12px rgba(26, 77, 69, 0.08);
}

.info-row {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid rgba(185, 225, 205, 0.3);
}

.info-row:last-child {
  border-bottom: none;
}

.info-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(185, 225, 205) 0%, rgb(223, 246, 239) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
  color: rgb(26, 77, 69);
}

.info-icon-wrap svg {
  width: 20px;
  height: 20px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.info-label {
  font-size: 12px;
  color: rgba(26, 77, 69, 0.5);
}

.info-value {
  font-size: 15px;
  color: rgb(26, 77, 69);
  font-weight: 500;
}

/* 章节标题 */
.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: rgb(26, 77, 69);
}

.title-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgb(185, 225, 205) 0%, rgb(223, 246, 239) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgb(26, 77, 69);
}

.title-icon svg {
  width: 18px;
  height: 18px;
}

.content-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(26, 77, 69, 0.08);
}

.content-card p {
  margin: 0;
  font-size: 14px;
  color: rgba(26, 77, 69, 0.8);
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 底部装饰 */
.bottom-deco {
  margin-top: 40px;
  height: 80px;
  overflow: hidden;
}

.bottom-deco svg {
  width: 100%;
  height: 100%;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  color: rgba(26, 77, 69, 0.6);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(185, 225, 205, 0.3);
  border-top-color: rgb(185, 225, 205);
  border-radius: 50%;
  margin-bottom: 12px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
