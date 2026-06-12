<template>
  <div class="complete-page">
    <div class="auth-bg-cross cross-one"></div>
    <div class="auth-bg-cross cross-two"></div>
    <div class="auth-bg-cross cross-three"></div>
    <div class="auth-bg-cross cross-four"></div>

    <div class="header-section">
      <div class="header-back" @click="goBack">
        <van-icon name="arrow-left" />
      </div>
      <div class="header-title">
        <span class="title-icon float-animation">
          <span class="title-cross"></span>
        </span>
        <h1>{{ pageTitle }}</h1>
      </div>
    </div>

    <div class="content-section slide-up-animation">
      <div class="form-card">
        <div class="card-header">
          <div class="header-icon">🏥</div>
          <div class="header-text">
            <h3>患者身份信息</h3>
            <p>{{ pageDesc }}</p>
          </div>
        </div>

        <form class="complete-form" @submit.prevent="handleComplete">
          <div class="section-title">基本信息</div>
          
          <div class="form-item">
            <label class="form-label">姓名 <span class="required">*</span></label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                </svg>
              </span>
              <input
                v-model="patientForm.name"
                type="text"
                placeholder="请输入真实姓名"
                autocomplete="off"
                required
              />
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">性别 <span class="required">*</span></label>
            <div class="radio-group">
              <div 
                :class="['radio-item', { active: patientForm.gender === '男' }]"
                @click="patientForm.gender = '男'"
              >
                <div class="radio-circle">
                  <div class="radio-dot" v-if="patientForm.gender === '男'"></div>
                </div>
                <span>男</span>
              </div>
              <div 
                :class="['radio-item', { active: patientForm.gender === '女' }]"
                @click="patientForm.gender = '女'"
              >
                <div class="radio-circle">
                  <div class="radio-dot" v-if="patientForm.gender === '女'"></div>
                </div>
                <span>女</span>
              </div>
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">出生日期 <span class="required">*</span></label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M16 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M8 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M3 10H21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input
                v-model="patientForm.birthday"
                type="date"
                placeholder="请选择出生日期"
                required
              />
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">手机号 <span class="required">*</span></label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22 16.92V21C22 21.2652 21.8946 21.5196 21.7071 21.7071C21.5196 21.8946 21.2652 22 21 22H3C2.73478 22 2.48043 21.8946 2.29289 21.7071C2.10536 21.5196 2 21.2652 2 21V3C2 2.73478 2.10536 2.48043 2.29289 2.29289C2.48043 2.10536 2.73478 2 3 2H21C21.2652 2 21.5196 2.10536 21.7071 2.29289C21.8946 2.48043 22 2.73478 22 3V7.08" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M16 2H8C6.93913 2 6 2.93913 6 4V20C6 21.0609 6.93913 22 8 22H16C17.0609 22 18 21.0609 18 20V4C18 2.93913 17.0609 2 16 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M12 18H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input
                v-model="patientForm.phone"
                type="tel"
                placeholder="请输入手机号"
                autocomplete="off"
                maxlength="11"
                required
              />
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">身份证号 <span class="required">*</span></label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="2" y="4" width="20" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M6 8H14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M6 12H18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <circle cx="17" cy="16" r="1" fill="currentColor"/>
                </svg>
              </span>
              <input
                v-model="patientForm.idCard"
                type="text"
                placeholder="请输入身份证号"
                autocomplete="off"
                maxlength="18"
                required
              />
            </div>
          </div>

          <div class="section-title">医疗信息</div>

          <div class="form-item">
            <label class="form-label">血型</label>
            <div class="select-wrapper">
              <select v-model="patientForm.bloodType">
                <option value="">请选择血型</option>
                <option value="A">A型</option>
                <option value="B">B型</option>
                <option value="AB">AB型</option>
                <option value="O">O型</option>
                <option value="RH+">Rh阳性</option>
                <option value="RH-">Rh阴性</option>
              </select>
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">婚姻状态</label>
            <div class="radio-group">
              <div 
                :class="['radio-item', { active: patientForm.maritalStatus === '未婚' }]"
                @click="patientForm.maritalStatus = '未婚'"
              >
                <div class="radio-circle">
                  <div class="radio-dot" v-if="patientForm.maritalStatus === '未婚'"></div>
                </div>
                <span>未婚</span>
              </div>
              <div 
                :class="['radio-item', { active: patientForm.maritalStatus === '已婚' }]"
                @click="patientForm.maritalStatus = '已婚'"
              >
                <div class="radio-circle">
                  <div class="radio-dot" v-if="patientForm.maritalStatus === '已婚'"></div>
                </div>
                <span>已婚</span>
              </div>
              <div 
                :class="['radio-item', { active: patientForm.maritalStatus === '离异' }]"
                @click="patientForm.maritalStatus = '离异'"
              >
                <div class="radio-circle">
                  <div class="radio-dot" v-if="patientForm.maritalStatus === '离异'"></div>
                </div>
                <span>离异</span>
              </div>
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">家庭住址</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M21 10V20C21 20.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 20V10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M9 22V12H15V22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M3 10L12 3L21 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
              <input
                v-model="patientForm.address"
                type="text"
                placeholder="请输入详细住址"
                autocomplete="off"
              />
            </div>
          </div>

          <div class="section-title">紧急联系</div>

          <div class="form-item">
            <label class="form-label">紧急联系人</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                </svg>
              </span>
              <input
                v-model="patientForm.emergencyContact"
                type="text"
                placeholder="请输入紧急联系人姓名"
                autocomplete="off"
              />
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">紧急联系电话</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22 16.92V21C22 21.2652 21.8946 21.5196 21.7071 21.7071C21.5196 21.8946 21.2652 22 21 22H3C2.73478 22 2.48043 21.8946 2.29289 21.7071C2.10536 21.5196 2 21.2652 2 21V3C2 2.73478 2.10536 2.48043 2.29289 2.29289C2.48043 2.10536 2.73478 2 3 2H21C21.2652 2 21.5196 2.10536 21.7071 2.29289C21.8946 2.48043 22 2.73478 22 3V7.08" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M16 2H8C6.93913 2 6 2.93913 6 4V20C6 21.0609 6.93913 22 8 22H16C17.0609 22 18 21.0609 18 20V4C18 2.93913 17.0609 2 16 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M12 18H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input
                v-model="patientForm.emergencyPhone"
                type="tel"
                placeholder="请输入紧急联系电话"
                autocomplete="off"
                maxlength="11"
              />
            </div>
          </div>

          <div class="section-title">健康史</div>

          <div class="form-item">
            <label class="form-label">过敏史</label>
            <div class="textarea-wrapper">
              <textarea
                v-model="patientForm.allergyHistory"
                placeholder="请输入药物或食物过敏史，如无请填无"
                rows="3"
              ></textarea>
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">既往史</label>
            <div class="textarea-wrapper">
              <textarea
                v-model="patientForm.pastHistory"
                placeholder="请输入既往病史、手术史等，如无请填无"
                rows="3"
              ></textarea>
            </div>
          </div>

          <button type="submit" class="complete-btn" :disabled="loading">
            <span v-if="loading">保存中...</span>
            <span v-else>保存信息</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getPatientByUserId, completePatient } from '@/api/patient'
import { getInfo } from '@/api/user'

const router = useRouter()
const route = useRoute()

const patientForm = ref({
  name: '',
  gender: '',
  birthday: '',
  phone: '',
  idCard: '',
  bloodType: '',
  maritalStatus: '',
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  allergyHistory: '',
  pastHistory: '',
  status: '1'
})

const loading = ref(false)
const isEditMode = computed(() => route.query.mode === 'edit')
const pageTitle = computed(() => isEditMode.value ? '个人信息' : '完善患者信息')
const pageDesc = computed(() => isEditMode.value ? '查看和编辑您的医疗信息' : '请完整填写您的医疗信息')
const userId = ref(null)

const loadUserInfo = async () => {
  try {
    const res = await getInfo()
    if (res.user) {
      userId.value = res.user.userId
      patientForm.value.name = res.user.nickName || ''
      patientForm.value.phone = res.user.phonenumber || ''
      await loadPatientInfo()
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

const loadPatientInfo = async () => {
  if (!userId.value) return
  try {
    const res = await getPatientByUserId(userId.value)
    if (res.data) {
      const patient = res.data
      Object.assign(patientForm.value, patient)
      if (patient.birthday) {
        patientForm.value.birthday = patient.birthday.split(' ')[0]
      }
    }
  } catch (error) {
    console.error('获取患者信息失败', error)
  }
}

const handleComplete = async () => {
  if (!patientForm.value.name) {
    showToast('请输入姓名')
    return
  }
  if (!patientForm.value.gender) {
    showToast('请选择性别')
    return
  }
  if (!patientForm.value.birthday) {
    showToast('请选择出生日期')
    return
  }
  if (!patientForm.value.phone) {
    showToast('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(patientForm.value.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  if (!patientForm.value.idCard) {
    showToast('请输入身份证号')
    return
  }
  if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(patientForm.value.idCard)) {
    showToast('请输入正确的身份证号')
    return
  }

  loading.value = true
  try {
    const result = await completePatient(patientForm.value)
    if (result?.data?.patientId) {
      localStorage.setItem('patientId', result.data.patientId)
    }
    localStorage.setItem('patientName', patientForm.value.name)
    showToast('信息保存成功')
    setTimeout(() => {
      router.push('/profile')
    }, 1500)
  } catch (error) {
    showToast(error.msg || '保存失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.complete-page {
  min-height: 100vh;
  background: var(--bg-gradient);
  padding-bottom: 20px;
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
  padding: 16px 20px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-back {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.96);
    background: rgba(255, 255, 255, 0.8);
  }

  .van-icon {
    color: #5f7580;
    font-size: 20px;
  }
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;

  h1 {
    font-size: 24px;
    font-weight: 700;
    color: #4f7380;
    margin: 0;
  }
}

.title-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(104, 199, 169, 0.2);
}

.title-cross {
  position: relative;
  width: 24px;
  height: 24px;

  &::before,
  &::after {
    content: "";
    position: absolute;
    background: linear-gradient(135deg, #68c7a9, #89dbc1);
    border-radius: 6px;
  }

  &::before {
    top: 50%;
    left: 0;
    width: 100%;
    height: 5px;
    transform: translateY(-50%);
  }

  &::after {
    top: 0;
    left: 50%;
    width: 5px;
    height: 100%;
    transform: translateX(-50%);
  }
}

.content-section {
  padding: 0 16px;
}

.form-card {
  background: rgba(255, 255, 255, 0.74);
  border-radius: 24px;
  padding: 24px;
  border: 1px solid rgba(194, 228, 236, 0.72);
  box-shadow: var(--card-shadow);
  backdrop-filter: blur(8px);
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(213, 237, 243, 0.6);
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e6f6fb, #fffaf4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.header-text {
  flex: 1;

  h3 {
    font-size: 18px;
    font-weight: 700;
    color: #4f7380;
    margin: 0 0 4px;
  }

  p {
    font-size: 14px;
    color: #8e9fa8;
    margin: 0;
  }
}

.complete-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #4f7380;
  padding-top: 8px;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(133, 219, 194, 0.3);
  margin-top: 8px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #5f7580;
  margin-left: 4px;

  .required {
    color: #e74c3c;
  }
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 48px;
  padding: 0 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 0 0 1px rgba(181, 221, 231, 0.9);
  transition: box-shadow 0.25s ease, background 0.25s ease, transform 0.25s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.94);
    box-shadow: inset 0 0 0 1px rgba(142, 214, 242, 0.9);
  }

  &:focus-within {
    background: #fff;
    box-shadow: inset 0 0 0 1px rgba(120, 216, 189, 0.96), 0 0 0 4px rgba(133, 219, 194, 0.18);
  }
}

.input-icon {
  width: 18px;
  height: 18px;
  margin-right: 10px;
  color: var(--secondary-color);
  flex-shrink: 0;
}

.input-wrapper input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 500;

  &::placeholder {
    color: var(--text-light);
  }

  &[type="date"] {
    color: var(--text-primary);
  }
}

.select-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 48px;
  padding: 0 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 0 0 1px rgba(181, 221, 231, 0.9);

  select {
    flex: 1;
    min-width: 0;
    border: none;
    outline: none;
    background: transparent;
    color: var(--text-primary);
    font-size: 15px;
    font-weight: 500;
    cursor: pointer;

    option {
      background: #fff;
      color: var(--text-primary);
    }
  }
}

.textarea-wrapper {
  position: relative;
  display: flex;
  min-height: 48px;
  padding: 12px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 0 0 1px rgba(181, 221, 231, 0.9);

  textarea {
    flex: 1;
    min-width: 0;
    border: none;
    outline: none;
    background: transparent;
    color: var(--text-primary);
    font-size: 15px;
    font-weight: 500;
    resize: none;
    line-height: 1.5;

    &::placeholder {
      color: var(--text-light);
    }
  }
}

.radio-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  border: 2px solid rgba(181, 221, 231, 0.9);
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: rgba(120, 216, 189, 0.7);
  }

  &.active {
    background: rgba(133, 219, 194, 0.15);
    border-color: rgba(120, 216, 189, 0.96);
  }

  span {
    font-size: 15px;
    font-weight: 500;
    color: var(--text-primary);
  }
}

.radio-circle {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid rgba(181, 221, 231, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;

  .radio-item.active & {
    border-color: rgba(120, 216, 189, 0.96);
  }
}

.radio-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #68c7a9, #89dbc1);
}

.complete-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 16px;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #68c7a9, #89dbc1);
  box-shadow: 0 8px 24px rgba(104, 199, 169, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;

  &:active:not(:disabled) {
    transform: scale(0.98);
    box-shadow: 0 4px 12px rgba(104, 199, 169, 0.3);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
    transform: none;
  }
}

@keyframes authCrossBreath {
  0%, 100% { opacity: 0.08; transform: scale(1); }
  50% { opacity: 0.18; transform: scale(1.08); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.float-animation {
  animation: float 3s ease-in-out infinite;
}

.slide-up-animation {
  animation: slideUp 0.5s ease-out;
}
</style>
