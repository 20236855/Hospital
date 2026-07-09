<template>
  <div class="profile-page">
    <div class="auth-bg-cross cross-one"></div>
    <div class="auth-bg-cross cross-two"></div>
    <div class="auth-bg-cross cross-three"></div>
    <div class="auth-bg-cross cross-four"></div>

    <div class="header-section">
      <div class="header-back" @click="goBack">
        <van-icon name="arrow-left" />
      </div>
      <div class="header-title">
        <h1>个人中心</h1>
      </div>
    </div>

    <div class="content-section">
      <div class="profile-card">
        <div class="profile-header">
          <div class="user-avatar">
            <span class="avatar-text">{{ (userName || '患').charAt(0) }}</span>
          </div>
          <div class="user-info">
            <div class="patient-status">患者档案</div>
            <h2 class="user-name">{{ userName || '患者' }}</h2>
            <p class="user-id">档案号：{{ patientProfile?.patientNo || patientProfile?.patientId || userId || '-' }}</p>
          </div>
          <div class="edit-btn" @click="goToInfo">
            <van-icon name="edit" />
          </div>
        </div>

        <div class="profile-detail-grid">
          <div v-for="item in patientMetaRows" :key="item.label" class="profile-detail-item">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>

        <div class="stats-section">
          <div class="stat-item">
            <span class="stat-value">{{ stats.appointments }}</span>
            <span class="stat-label">预约</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.records }}</span>
            <span class="stat-label">病历</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ profileCompletion }}%</span>
            <span class="stat-label">档案完整度</span>
          </div>
        </div>
      </div>

      <div class="health-card">
        <div class="card-title-row">
          <div>
            <strong>健康档案</strong>
            <span>就诊前请保持资料准确</span>
          </div>
          <button type="button" @click="goToInfo">完善</button>
        </div>
        <div class="health-info-list">
          <div class="health-info-item">
            <span>过敏史</span>
            <strong>{{ patientProfile?.allergyHistory || '未填写' }}</strong>
          </div>
          <div class="health-info-item">
            <span>既往史</span>
            <strong>{{ patientProfile?.pastHistory || '未填写' }}</strong>
          </div>
          <div class="health-info-item">
            <span>紧急联系人</span>
            <strong>{{ emergencyContactText }}</strong>
          </div>
        </div>
      </div>

      <div class="menu-card">
        <div class="menu-section">
          <div class="menu-title">我的服务</div>
          <div class="menu-item" @click="goToInfo">
            <div class="menu-icon-wrapper icon-info">
              <van-icon name="user-o" />
            </div>
            <div class="menu-content">
              <span class="menu-name">个人信息</span>
              <span class="menu-desc">查看和编辑个人资料</span>
            </div>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
          <div class="menu-item" @click="goToMyRegister">
            <div class="menu-icon-wrapper icon-appointment">
              <van-icon name="calendar-o" />
            </div>
            <div class="menu-content">
              <span class="menu-name">我的预约</span>
              <span class="menu-desc">查看预约记录</span>
            </div>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
          <div class="menu-item" @click="goToRecord">
            <div class="menu-icon-wrapper icon-record">
              <van-icon name="notes-o" />
            </div>
            <div class="menu-content">
              <span class="menu-name">就诊记录</span>
              <span class="menu-desc">查看历史病历</span>
            </div>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
        </div>
      </div>

      <div class="menu-card">
        <div class="menu-section">
          <div class="menu-title">其他</div>
          <div class="menu-item" @click="showToast('帮助中心功能开发中')">
            <div class="menu-icon-wrapper icon-help">
              <van-icon name="question-o" />
            </div>
            <div class="menu-content">
              <span class="menu-name">帮助中心</span>
              <span class="menu-desc">常见问题解答</span>
            </div>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
          <div class="menu-item" @click="showToast('关于我们功能开发中')">
            <div class="menu-icon-wrapper icon-about">
              <van-icon name="info-o" />
            </div>
            <div class="menu-content">
              <span class="menu-name">关于我们</span>
              <span class="menu-desc">了解我们的服务</span>
            </div>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
          <div class="menu-item" @click="showToast('设置功能开发中')">
            <div class="menu-icon-wrapper icon-settings">
              <van-icon name="setting-o" />
            </div>
            <div class="menu-content">
              <span class="menu-name">设置</span>
              <span class="menu-desc">应用设置</span>
            </div>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
        </div>
      </div>

      <div class="logout-section">
        <van-button round block class="logout-btn" @click="doLogout">
          <van-icon name="log-out" />
          退出登录
        </van-button>
      </div>
    </div>

    <van-tabbar v-model="active" class="custom-tabbar">
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="todo-list-o" to="/register">挂号</van-tabbar-item>
      <van-tabbar-item icon="notes-o" to="/record">病历</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast, showDialog } from 'vant'
import { logout } from '@/api/login'
import { getPatientByUserId } from '@/api/patient'
import { getRegisterList } from '@/api/register'
import { getMedicalRecordList } from '@/api/emr'
import { getInfo } from '@/api/user'

const router = useRouter()
const active = ref(3)

const userName = ref('')
const userId = ref('')
const patientProfile = ref(null)

const stats = ref({
  appointments: 0,
  records: 0
})

const emptyText = '待完善'

const formatGender = (gender) => {
  if (gender === '0' || gender === 0 || gender === '男') return '男'
  if (gender === '1' || gender === 1 || gender === '女') return '女'
  return emptyText
}

const maskPhone = (phone) => {
  if (!phone) return emptyText
  const value = String(phone)
  return value.length >= 7 ? `${value.slice(0, 3)}****${value.slice(-4)}` : value
}

const patientMetaRows = computed(() => [
  { label: '性别', value: formatGender(patientProfile.value?.gender) },
  { label: '年龄', value: patientProfile.value?.age ? `${patientProfile.value.age}岁` : emptyText },
  { label: '手机号', value: maskPhone(patientProfile.value?.phone) },
  { label: '血型', value: patientProfile.value?.bloodType || emptyText }
])

const profileCompletion = computed(() => {
  const fields = ['name', 'gender', 'birthday', 'phone', 'idCard', 'bloodType', 'emergencyContact', 'emergencyPhone']
  const completed = fields.filter(field => patientProfile.value?.[field]).length
  return Math.round((completed / fields.length) * 100)
})

const emergencyContactText = computed(() => {
  const contact = patientProfile.value?.emergencyContact
  const phone = patientProfile.value?.emergencyPhone
  if (!contact && !phone) return '未填写'
  return [contact, maskPhone(phone)].filter(Boolean).join(' / ')
})

const loadUserData = async () => {
  try {
    const savedUsername = localStorage.getItem('username')
    const patientName = localStorage.getItem('patientName')
    userName.value = patientName || savedUsername || '患者'

    const userRes = await getInfo()
    const currentUserId = userRes?.user?.userId
    if (currentUserId) {
      userId.value = currentUserId
      const patientRes = await getPatientByUserId(currentUserId)
      const patient = patientRes?.data
      if (patient) {
        patientProfile.value = patient
        localStorage.setItem('patientId', patient.patientId)
        localStorage.setItem('patientName', patient.name)
        userName.value = patient.name
      }
    }
    
    const registerRes = await getRegisterList({ pageNum: 1, pageSize: 100 })
    if (registerRes.total) {
      stats.value.appointments = registerRes.total
    }
    
    const recordRes = await getMedicalRecordList({ pageNum: 1, pageSize: 100 })
    if (recordRes.total) {
      stats.value.records = recordRes.total
    }
  } catch (error) {
    console.error('加载用户数据失败', error)
    // 使用本地存储的默认值
    const savedUsername = localStorage.getItem('username')
    const patientName = localStorage.getItem('patientName')
    userName.value = patientName || savedUsername || '患者'
    const savedUserId = localStorage.getItem('userId')
    if (savedUserId) {
      userId.value = savedUserId
    }
    patientProfile.value = null
  }
}

const goToInfo = () => {
  router.push({ path: '/patient-complete', query: { mode: 'edit' } })
}

const goToMyRegister = () => {
  router.push('/my-appointments')
}

const goToRecord = () => {
  router.push('/record')
}

const goBack = () => {
  router.back()
}

const doLogout = async () => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要退出登录吗？',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    try {
      await logout()
    } catch (error) {
      console.error('退出登录接口调用失败，但仍将执行退出操作', error)
    }
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('patientId')
    localStorage.removeItem('patientName')
    await showDialog({
      title: '退出成功',
      message: '您已安全退出登录',
      confirmButtonText: '确定',
      theme: 'success'
    })
    router.replace('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadUserData()
})
</script>

<style scoped lang="scss">
.profile-page {
  min-height: 100vh;
  background: var(--bg-gradient);
  padding-top: 60px;
  padding-bottom: 60px;
  position: relative;
  overflow-x: hidden;
}

.auth-bg-cross {
  position: absolute;
  width: 40px;
  height: 40px;
  opacity: 0.12;
  animation: authCrossBreath 8s ease-in-out infinite;

  &::before,
  &::after {
    content: "";
    position: absolute;
    background: #68c7a9;
    border-radius: 8px;
  }

  &::before {
    top: 50%;
    left: 0;
    width: 100%;
    height: 6px;
    transform: translateY(-50%);
  }

  &::after {
    top: 0;
    left: 50%;
    width: 6px;
    height: 100%;
    transform: translateX(-50%);
  }
}

.cross-one {
  top: 12%;
  left: 8%;
  animation-delay: 0s;
}

.cross-two {
  top: 25%;
  right: 10%;
  animation-delay: -2s;
}

.cross-three {
  bottom: 30%;
  left: 12%;
  animation-delay: -4s;
}

.cross-four {
  bottom: 15%;
  right: 6%;
  animation-delay: -6s;
}

.header-section {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 8px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: #1250af;
  color: #fff;
}

.header-back {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.96);
    background: rgba(255, 255, 255, 0.32);
  }

  .van-icon {
    color: #fff;
    font-size: 18px;
  }
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;

  h1 {
    font-size: 17px;
    font-weight: 700;
    color: #fff;
    margin: 0;
  }
}

.content-section {
  padding: 0 10px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card {
  background: rgba(255, 255, 255, 0.82);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(194, 228, 236, 0.72);
  box-shadow: var(--card-shadow);
  backdrop-filter: blur(8px);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.user-avatar {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: linear-gradient(135deg, #68c7a9, #89dbc1);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(104, 199, 169, 0.3);
  flex-shrink: 0;
}

.avatar-text {
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 22px;
  font-weight: 700;
  color: #4f7380;
  margin: 0 0 4px;
}

.patient-status {
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  margin-bottom: 6px;
  border: 1px solid rgba(104, 199, 169, 0.32);
  background: rgba(104, 199, 169, 0.1);
  color: #4a9f87;
  font-size: 12px;
  font-weight: 700;
}

.user-id {
  font-size: 14px;
  color: #8e9fa8;
  margin: 0;
}

.edit-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(104, 199, 169, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.96);
    background: rgba(104, 199, 169, 0.15);
  }

  .van-icon {
    color: #68c7a9;
    font-size: 18px;
  }
}

.profile-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

.profile-detail-item {
  min-height: 58px;
  padding: 12px;
  border: 1px solid rgba(213, 237, 243, 0.82);
  background: rgba(255, 255, 255, 0.62);

  span {
    display: block;
    margin-bottom: 5px;
    color: #8e9fa8;
    font-size: 12px;
  }

  strong {
    color: #4f7380;
    font-size: 15px;
    font-weight: 700;
  }
}

.stats-section {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding-top: 20px;
  border-top: 1px solid rgba(213, 237, 243, 0.6);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #68c7a9;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #8e9fa8;
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: rgba(213, 237, 243, 0.8);
}

.menu-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 16px;
  border: 1px solid #e4eaf0;
  box-shadow: 0 6px 18px rgba(31, 52, 64, 0.06);
  backdrop-filter: blur(8px);
}

.health-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  border: 1px solid #e4eaf0;
  box-shadow: 0 6px 18px rgba(31, 52, 64, 0.06);
  backdrop-filter: blur(8px);
}

.card-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;

  strong {
    display: block;
    color: #4f7380;
    font-size: 17px;
    margin-bottom: 4px;
  }

  span {
    color: #8e9fa8;
    font-size: 13px;
  }

  button {
    border: none;
    padding: 6px 12px;
    background: rgba(104, 199, 169, 0.12);
    color: #4a9f87;
    font-weight: 700;
    cursor: pointer;
  }
}

.health-info-list {
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(213, 237, 243, 0.78);
  background: rgba(255, 255, 255, 0.52);
}

.health-info-item {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 13px 14px;
  border-bottom: 1px solid rgba(213, 237, 243, 0.56);

  &:last-child {
    border-bottom: none;
  }

  span {
    flex-shrink: 0;
    color: #8e9fa8;
    font-size: 13px;
  }

  strong {
    color: #4f7380;
    font-size: 13px;
    font-weight: 600;
    text-align: right;
    line-height: 1.45;
  }
}

.menu-section {
  display: flex;
  flex-direction: column;
}

.menu-title {
  font-size: 13px;
  font-weight: 700;
  color: #8e9fa8;
  margin-bottom: 12px;
  padding-left: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(104, 199, 169, 0.06);
  }

  &:active {
    transform: scale(0.98);
  }
}

.menu-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .van-icon {
    font-size: 20px;
  }
}

.icon-info {
  background: linear-gradient(135deg, rgba(104, 199, 169, 0.15), rgba(104, 199, 169, 0.08));
  .van-icon {
    color: #68c7a9;
  }
}

.icon-appointment {
  background: linear-gradient(135deg, rgba(142, 214, 242, 0.15), rgba(142, 214, 242, 0.08));
  .van-icon {
    color: #8ed6f2;
  }
}

.icon-record {
  background: linear-gradient(135deg, rgba(139, 214, 226, 0.15), rgba(139, 214, 226, 0.08));
  .van-icon {
    color: #8bd6e2;
  }
}

.icon-help {
  background: linear-gradient(135deg, rgba(139, 201, 173, 0.15), rgba(139, 201, 173, 0.08));
  .van-icon {
    color: #8bc9ad;
  }
}

.icon-about {
  background: linear-gradient(135deg, rgba(171, 209, 226, 0.15), rgba(171, 209, 226, 0.08));
  .van-icon {
    color: #abd1e2;
  }
}

.icon-settings {
  background: linear-gradient(135deg, rgba(165, 184, 192, 0.15), rgba(165, 184, 192, 0.08));
  .van-icon {
    color: #a5b8c0;
  }
}

.menu-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.menu-name {
  font-size: 15px;
  font-weight: 600;
  color: #4f7380;
}

.menu-desc {
  font-size: 12px;
  color: #a5b8c0;
}

.menu-arrow {
  color: #a5b8c0;
  font-size: 16px;
  flex-shrink: 0;
}

.logout-section {
  padding-top: 8px;
}

.logout-btn {
  height: 52px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 700;
  background: rgba(255, 107, 107, 0.1);
  border: 2px solid rgba(255, 107, 107, 0.3);
  color: #ff6b6b;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    background: rgba(255, 107, 107, 0.15);
  }

  .van-icon {
    font-size: 18px;
  }
}

.custom-tabbar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(194, 228, 236, 0.72);
}

@keyframes authCrossBreath {
  0%, 100% { opacity: 0.08; transform: scale(1); }
  50% { opacity: 0.18; transform: scale(1.08); }
}
</style>
