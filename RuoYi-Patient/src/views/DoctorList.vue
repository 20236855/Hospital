<template>
  <div class="doctor-page">
    <div class="page-hero">
      <div class="hero-content">
        <h1 class="hero-title">科室医生</h1>
        <p class="hero-subtitle">专业医疗团队，为您的健康保驾护航</p>
      </div>
      <svg class="hero-bg" viewBox="0 0 400 200" aria-hidden="true">
        <defs>
          <linearGradient id="heroGradient" x1="0" x2="1" y1="0" y2="1">
            <stop offset="0" stop-color="#cfe8d8" />
            <stop offset="1" stop-color="#b9e1cd" />
          </linearGradient>
        </defs>
        <circle cx="350" cy="40" r="60" fill="url(#heroGradient)" opacity="0.5" />
        <circle cx="320" cy="120" r="40" fill="url(#heroGradient)" opacity="0.3" />
        <path d="M0 160 Q 100 130, 200 150 T 400 140 L 400 200 L 0 200 Z" fill="#cfe8d8" opacity="0.4" />
      </svg>
    </div>

    <div class="search-bar">
      <div class="search-input-wrap">
        <svg viewBox="0 0 24 24" class="search-icon" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="m21 21-4.3-4.3" />
        </svg>
        <input
          v-model="searchName"
          type="text"
          placeholder="搜索医生姓名..."
          class="search-input"
          @input="handleSearch"
        />
      </div>
    </div>

    <div class="doctor-list">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <div
        v-else
        v-for="doctor in doctorList"
        :key="doctor.doctorId"
        class="doctor-card"
        @click="goToDetail(doctor.doctorId)"
      >
        <div class="card-avatar-wrap">
          <img :src="getAvatar(doctor)" @error="onAvatarError" class="card-avatar" />
          <span class="status-badge" :class="{ online: doctor.status === '0' }">
            {{ doctor.status === '0' ? '在职' : '停诊' }}
          </span>
        </div>
        <div class="card-body">
          <div class="card-header">
            <h3 class="doctor-name">{{ doctor.doctorName }}</h3>
            <span class="doctor-level">{{ doctor.levelName }}</span>
          </div>
          <div class="doctor-dept-row">
            <svg viewBox="0 0 24 24" class="row-icon" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 21h18M5 21V7l8-4v18M19 21V11l-6-4" />
              <path d="M9 9v0M9 12v0M9 15v0M9 18v0" />
            </svg>
            <span>{{ doctor.deptId }}</span>
          </div>
          <div class="doctor-specialty" v-if="doctor.specialty">
            <span class="label">擅长</span>
            <span class="content">{{ doctor.specialty }}</span>
          </div>
          <div class="doctor-intro" v-if="doctor.introduction">
            <span class="label">简介</span>
            <span class="content">{{ doctor.introduction }}</span>
          </div>
        </div>
        <div class="card-action">
          <span>查看详情</span>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="m9 18 6-6-6-6" />
          </svg>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && doctorList.length === 0">
        <svg viewBox="0 0 120 120" class="empty-icon" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="60" cy="45" r="25" />
          <path d="M20 100c8-20 28-30 40-30s32 10 40 30" />
        </svg>
        <p>暂无医生信息</p>
      </div>
    </div>

    <div class="load-more" v-if="total > doctorList.length">
      <button @click="loadMore" :disabled="loadingMore" class="load-btn">
        <span v-if="!loadingMore">加载更多</span>
        <span v-else>加载中...</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listDoctor } from '@/api/doctor'
import defAva from '@/assets/images/guahao.png'

const router = useRouter()
const loading = ref(false)
const loadingMore = ref(false)
const total = ref(0)
const doctorList = ref([])
const searchName = ref('')
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  doctorName: undefined
})

function getAvatar(doctor) {
  return doctor?.avatar || defAva
}

function onAvatarError(e) {
  e.target.src = defAva
}

function fetchDoctorList() {
  loading.value = true
  listDoctor(queryParams.value).then(response => {
    doctorList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleSearch() {
  queryParams.value.pageNum = 1
  queryParams.value.doctorName = searchName.value || undefined
  fetchDoctorList()
}

function loadMore() {
  if (loadingMore.value) return
  queryParams.value.pageNum += 1
  loadingMore.value = true
  listDoctor(queryParams.value).then(response => {
    doctorList.value = [...doctorList.value, ...(response.rows || [])]
    total.value = response.total || doctorList.value.length
    loadingMore.value = false
  }).catch(() => {
    loadingMore.value = false
  })
}

function goToDetail(doctorId) {
  router.push(`/doctor/${doctorId}`)
}

onMounted(() => {
  fetchDoctorList()
})
</script>

<style scoped>
.doctor-page {
  min-height: 100vh;
  background: #f5faf7;
  padding-bottom: 80px;
}

.page-hero {
  position: relative;
  background: linear-gradient(135deg, rgb(185, 225, 205) 0%, rgb(223, 246, 239) 100%);
  padding: 48px 20px 32px;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: rgb(26, 77, 69);
  margin: 0 0 8px;
}

.hero-subtitle {
  font-size: 14px;
  color: rgba(26, 77, 69, 0.7);
  margin: 0;
}

.hero-bg {
  position: absolute;
  top: 0;
  right: 0;
  width: 300px;
  height: 200px;
}

.search-bar {
  padding: 16px 20px;
  background: transparent;
  position: relative;
  margin-top: -20px;
  z-index: 3;
}

.search-input-wrap {
  position: relative;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(26, 77, 69, 0.12);
  display: flex;
  align-items: center;
  padding: 0 18px;
}

.search-icon {
  width: 20px;
  height: 20px;
  color: rgb(185, 225, 205);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  height: 48px;
  font-size: 14px;
  padding-left: 10px;
  background: transparent;
  color: rgb(26, 77, 69);
}

.search-input::placeholder {
  color: #a8c0b8;
}

.doctor-list {
  padding: 0 20px;
}

.doctor-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(26, 77, 69, 0.08);
  transition: all 0.25s ease;
}

.doctor-card:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(26, 77, 69, 0.12);
}

.card-avatar-wrap {
  position: relative;
  width: 100%;
  height: 180px;
  background: linear-gradient(135deg, rgb(185, 225, 205) 0%, rgb(223, 246, 239) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-avatar {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #fff;
  box-shadow: 0 4px 16px rgba(26, 77, 69, 0.15);
  background: #fff;
}

.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  background: rgba(144, 147, 153, 0.9);
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.online {
  background: rgba(103, 194, 58, 0.95);
}

.card-body {
  padding: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.doctor-name {
  font-size: 18px;
  font-weight: 700;
  color: rgb(26, 77, 69);
  margin: 0;
}

.doctor-level {
  font-size: 12px;
  color: rgb(230, 162, 60);
  background: #fdf6ec;
  padding: 4px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.doctor-dept-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(26, 77, 69, 0.75);
  margin-bottom: 8px;
}

.row-icon {
  width: 16px;
  height: 16px;
  color: rgb(185, 225, 205);
}

.doctor-specialty,
.doctor-intro {
  display: flex;
  align-items: flex-start;
  font-size: 13px;
  color: rgba(26, 77, 69, 0.7);
  line-height: 1.6;
  margin-top: 6px;
}

.doctor-specialty .label,
.doctor-intro .label {
  color: rgba(26, 77, 69, 0.5);
  flex-shrink: 0;
  margin-right: 6px;
}

.doctor-specialty .content,
.doctor-intro .content {
  flex: 1;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.card-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 16px;
  background: linear-gradient(to right, rgb(223, 246, 239) 0%, rgb(185, 225, 205) 100%);
  color: rgb(26, 77, 69);
  font-size: 14px;
  font-weight: 500;
}

.card-action svg {
  width: 16px;
  height: 16px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  color: rgba(26, 77, 69, 0.4);
}

.empty-icon {
  width: 120px;
  height: 120px;
  color: rgb(185, 225, 205);
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

.load-more {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.load-btn {
  padding: 10px 40px;
  border: none;
  border-radius: 20px;
  background: #fff;
  color: rgb(26, 77, 69);
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 12px rgba(26, 77, 69, 0.1);
  cursor: pointer;
}

.load-btn:disabled {
  opacity: 0.6;
}
</style>
