<template>
  <div class="auth-page">
    <div class="auth-ambient">
      <span class="ambient-pulse pulse-a"></span>
      <span class="ambient-pulse pulse-b"></span>
      <span class="scan-line"></span>
    </div>
    <div class="medical-visual-background">
      <svg class="medical-art" viewBox="0 0 640 560" role="img" aria-label="医疗主题插画">
        <defs>
          <linearGradient id="cloudGradient" x1="0" x2="1" y1="0" y2="1">
            <stop offset="0" stop-color="#f8fdff" />
            <stop offset="1" stop-color="#dff4fb" />
          </linearGradient>
          <linearGradient id="mintGradient" x1="0" x2="1" y1="0" y2="1">
            <stop offset="0" stop-color="#b9e1cd" />
            <stop offset="1" stop-color="#24e192" />
          </linearGradient>
          <linearGradient id="blueGradient" x1="0" x2="1" y1="0" y2="1">
            <stop offset="0" stop-color="#cfe8d8" />
            <stop offset="1" stop-color="#8ed6f2" />
          </linearGradient>
          <filter id="softShadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="0" dy="18" stdDeviation="18" flood-color="#8cc9dd" flood-opacity=".22" />
          </filter>
        </defs>

        <g class="wave-lines" fill="none" stroke="#ccecf5" stroke-linecap="round" stroke-width="3">
          <path d="M34 380C148 332 230 430 330 374S522 328 608 390" />
          <path d="M60 418C176 370 238 448 342 406S510 380 586 432" />
          <path d="M80 456C188 422 250 470 360 440S502 420 566 466" />
        </g>

        <g class="cloud cloud-a" fill="url(#cloudGradient)">
          <path d="M83 150c5-29 30-50 60-50 23 0 43 13 53 32 8-4 18-6 28-6 35 0 63 28 63 62H92c-30 0-54-24-54-54s24-54 54-54c2 0 4 0 6 1-7 8-12 18-15 29Z" />
        </g>
        <g class="cloud cloud-b" fill="url(#cloudGradient)">
          <path d="M410 96c6-22 26-38 50-38 19 0 36 10 45 26 7-3 14-4 22-4 30 0 54 24 54 54H396c-24 0-44-20-44-44s20-44 44-44c8 0 15 2 21 5-3 5-5 10-7 15Z" />
        </g>

        <g class="floating icon-card form-card" filter="url(#softShadow)">
          <rect x="82" y="225" width="112" height="130" rx="24" fill="#fffaf4" />
          <rect x="112" y="248" width="52" height="14" rx="7" fill="#acdbe9" />
          <rect x="108" y="282" width="68" height="10" rx="5" fill="#d9edf2" />
          <rect x="108" y="306" width="58" height="10" rx="5" fill="#d9edf2" />
          <path d="M126 334h28" stroke="#82d7bf" stroke-width="12" stroke-linecap="round" />
        </g>

        <g class="floating icon-card med-kit" filter="url(#softShadow)">
          <rect x="438" y="180" width="120" height="96" rx="22" fill="#fff7f1" />
          <path d="M472 180v-22c0-8 7-15 15-15h22c8 0 15 7 15 15v22" fill="none" stroke="#b9e1cd" stroke-width="10" stroke-linecap="round" />
          <path d="M498 206v46M475 229h46" stroke="#f0a5b8" stroke-width="12" stroke-linecap="round" />
        </g>

        <g class="floating stethoscope" fill="none" stroke="#6fbacb" stroke-linecap="round" stroke-width="10">
          <path d="M402 338v48c0 38-28 70-64 70s-64-32-64-70v-28" />
          <path d="M248 338v38c0 24-18 44-40 44s-40-20-40-44v-38" />
          <circle cx="402" cy="332" r="14" fill="#fffaf4" />
        </g>

        <g class="heart-pulse" fill="none" stroke="#ef8fa9" stroke-linecap="round" stroke-linejoin="round" stroke-width="8">
          <path d="M306 140c-22-25-62-9-62 26 0 35 62 70 62 70s62-35 62-70c0-35-40-51-62-26Z" fill="#fff5f7" />
          <path class="pulse-line" d="M252 182h35l12-24 20 48 14-28h39" />
        </g>

        <g class="pill pill-a">
          <rect x="210" y="130" width="86" height="34" rx="17" fill="#fffaf4" transform="rotate(-18 253 147)" />
          <path d="M253 130v34" stroke="#a9e5d2" stroke-width="4" transform="rotate(-18 253 147)" />
        </g>
        <g class="pill pill-b">
          <rect x="484" y="360" width="76" height="30" rx="15" fill="#e6f6fb" transform="rotate(23 522 375)" />
          <path d="M522 360v30" stroke="#efb2c1" stroke-width="4" transform="rotate(23 522 375)" />
        </g>

        <g class="hospital" filter="url(#softShadow)">
          <path d="M146 494h388c18 0 32-14 32-32v-24H114v24c0 18 14 32 32 32Z" fill="#d8eef4" />
          <rect x="174" y="284" width="292" height="210" rx="26" fill="#fffdf8" />
          <rect x="238" y="224" width="164" height="270" rx="28" fill="#f6fbfd" />
          <rect x="274" y="254" width="92" height="68" rx="18" fill="url(#blueGradient)" />
          <path d="M320 268v38M301 287h38" stroke="#fffdf8" stroke-width="12" stroke-linecap="round" />
          <g fill="#b9e8f4">
            <rect x="208" y="326" width="42" height="38" rx="10" />
            <rect x="208" y="386" width="42" height="38" rx="10" />
            <rect x="410" y="326" width="42" height="38" rx="10" />
            <rect x="410" y="386" width="42" height="38" rx="10" />
          </g>
          <rect x="290" y="382" width="80" height="112" rx="18" fill="url(#mintGradient)" />
          <path d="M330 382v112" stroke="#ecfff8" stroke-width="5" />
        </g>

        <g class="plant plant-left">
          <path d="M118 494c0-46 18-86 56-118" fill="none" stroke="#8bc9ae" stroke-width="8" stroke-linecap="round" />
          <path d="M138 444c-28-6-44-24-48-52 30 1 52 18 60 48Z" fill="#b9e1cd" />
          <path d="M157 405c5-27 24-45 56-54-2 33-21 56-52 66Z" fill="#9bd5bc" />
        </g>
        <g class="plant plant-right">
          <path d="M550 494c-4-42-22-74-54-98" fill="none" stroke="#8bc9ae" stroke-width="8" stroke-linecap="round" />
          <path d="M522 434c24-10 42-27 52-52-30-2-55 13-66 42Z" fill="#b9e1cd" />
        </g>

        <g class="tiny-crosses" fill="none" stroke="#b9e1cd" stroke-linecap="round" stroke-width="5">
          <path d="M132 92v24M120 104h24" />
          <path d="M516 150v22M505 161h22" />
          <path d="M76 405v18M67 414h18" />
          <path d="M564 448v18M555 457h18" />
        </g>
      </svg>
    </div>

    <div class="auth-shell">
      <div class="auth-back" @click="goGuide">
        <van-icon name="arrow-left" />
      </div>
      <section class="auth-hero" aria-hidden="true"></section>
      <main class="auth-panel">
        <form class="auth-form" @submit.prevent="handleLogin">
          <div class="form-sparkles">
            <span></span>
            <span></span>
          </div>
          <div class="brand-lockup">
            <h3 class="title">欢迎登录</h3>
            <div class="form-badge">HOSPITAL PORTAL</div>
          </div>

          <div class="form-item">
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
                  <path d="M4 20C4 15.5817 7.58172 12 12 12C16.4183 12 20 15.5817 20 20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input 
                v-model="loginForm.username" 
                type="text" 
                placeholder="请输入账号"
                autocomplete="off"
              />
            </div>
          </div>

          <div class="form-item">
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="11" width="18" height="10" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M7 11V8C7 5.23858 9.23858 3 12 3C14.7614 3 17 5.23858 17 8V11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input 
                v-model="loginForm.password" 
                :type="showPassword ? 'text' : 'password'" 
                placeholder="请输入密码"
                autocomplete="off"
              />
              <span class="password-toggle" @click="showPassword = !showPassword">
                <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M17.94 17.94C16.2306 19.243 14.1491 19.9649 12 20C5 20 1 12 1 12C2.24389 9.6819 3.96914 7.65661 6.06 6.06M14.12 5.12C13.4596 4.79555 12.7358 4.57559 12 4.5C19 4.5 23 12 23 12C22.3931 13.1301 21.5536 14.2047 20.52 15.15M9.88 9.88C9.58604 10.1565 9.36141 10.4996 9.23 10.88C9.07635 11.3247 9.01532 11.7927 9.05 12.26C9.08468 12.7273 9.21442 13.1814 9.43 13.6C9.75389 14.2289 10.2424 14.7531 10.84 15.11C11.2084 15.3281 11.6132 15.4636 12.03 15.51C12.4468 15.5564 12.8682 15.5178 13.26 15.39M9.88 9.88L4 4M9.88 9.88L14.12 5.12M9.88 9.88L13.26 15.39M20.52 15.15L20 20M20.52 15.15L15.39 13.26M4 4L20 20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </div>
          </div>

          <div class="form-item">
            <div class="input-wrapper captcha-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M9 9L11 12L9 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M15 9L13 12L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
              <input 
                v-model="loginForm.code" 
                type="text" 
                placeholder="请输入验证码"
                autocomplete="off"
              />
              <div class="captcha-img" @click="getCode">
                <img v-if="codeUrl" :src="codeUrl" alt="验证码" />
                <span v-else>点击刷新</span>
              </div>
            </div>
          </div>

          <div class="auth-options">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.rememberMe" />
              <span>记住密码</span>
            </label>
          </div>

          <button type="submit" class="auth-submit" :disabled="loading">
            <span v-if="loading">登录中...</span>
            <span v-else>安全登录</span>
          </button>

          <div class="register-link">
            <span>还没有账号？</span>
            <span @click="goRegister">立即注册</span>
          </div>
        </form>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { patientLogin as loginApi, getCodeImg } from '@/api/login'
import { getPatientByUserId } from '@/api/patient'
import { getInfo } from '@/api/user'
import { showToast } from 'vant'

const router = useRouter()

const loginForm = ref({
  username: 'admin',
  password: 'admin123',
  rememberMe: false,
  code: '',
  uuid: ''
})

const showPassword = ref(false)
const loading = ref(false)
const codeUrl = ref('')

const getCode = async () => {
  try {
    const res = await getCodeImg()
    codeUrl.value = 'data:image/gif;base64,' + res.img
    loginForm.value.uuid = res.uuid
  } catch (error) {
    console.error('获取验证码失败', error)
  }
}

const checkAndRedirect = async () => {
  try {
    const res = await getInfo()
    if (res.user && res.user.userId) {
      const patientRes = await getPatientByUserId(res.user.userId)
      const patient = patientRes?.data
      const hasPatient = patient && Object.keys(patient).length > 0
      if (!hasPatient) {
        showToast('请先完善患者信息')
        setTimeout(() => {
          router.push('/patient-complete')
        }, 1000)
        return
      }
      localStorage.setItem('patientId', patient.patientId)
      localStorage.setItem('patientName', patient.name)
    }
    router.push('/')
  } catch (error) {
    console.error('检查患者信息失败', error)
    router.push('/')
  }
}

const handleLogin = async () => {
  if (!loginForm.value.username) {
    showToast('请输入账号')
    return
  }
  if (!loginForm.value.password) {
    showToast('请输入密码')
    return
  }
  if (!loginForm.value.code) {
    showToast('请输入验证码')
    return
  }

  loading.value = true
  try {
    const res = await loginApi(
      loginForm.value.username,
      loginForm.value.password,
      loginForm.value.code,
      loginForm.value.uuid
    )
    localStorage.setItem('token', res.data.access_token)
    localStorage.setItem('username', loginForm.value.username)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    showToast('登录成功')
    await checkAndRedirect()
  } catch (error) {
    console.error('登录失败:', error)
    showToast(error.message || error.msg || '登录失败')
    getCode()
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  router.push('/register-account')
}

const goGuide = () => {
  router.replace('/guide')
}

onMounted(() => {
  getCode()
})
</script>

<style scoped lang="scss">
.auth-page {
  position: relative;
  display: flex;
  min-height: 100vh;
  padding: 14px 16px 18px;
  overflow: hidden;
  color: #1d3440;
  background:
    radial-gradient(circle at 50% 9%, rgba(198, 248, 231, .5) 0 17%, transparent 18%),
    radial-gradient(circle at 12% 82%, rgba(220, 246, 235, .42), transparent 32%),
    linear-gradient(180deg, #006970 0%, #1f8a7d 36%, #eef9f4 36.2%, #f8fcfa 100%);
  align-items: stretch;
  justify-content: center;
}

.auth-ambient {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.ambient-pulse {
  position: absolute;
  border: 1px solid rgba(209, 250, 232, .38);
  border-radius: 50%;
  box-shadow: inset 0 0 40px rgba(255, 255, 255, .08);
  animation: ringBreath 6.5s ease-in-out infinite;
}

.pulse-a {
  top: 42px;
  left: calc(50% - 128px);
  width: 256px;
  height: 256px;
}

.pulse-b {
  top: 88px;
  right: -82px;
  width: 168px;
  height: 168px;
  animation-delay: -2.2s;
}

.scan-line {
  position: absolute;
  top: 0;
  left: -18%;
  width: 42%;
  height: 46%;
  background: linear-gradient(100deg, transparent, rgba(229, 255, 246, .18), transparent);
  transform: skewX(-18deg);
  animation: scanSweep 6.8s ease-in-out infinite;
}

.medical-visual-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  pointer-events: none;
}

.medical-art {
  position: absolute;
  top: 14px;
  left: 50%;
  width: min(102vw, 390px);
  height: 31vh;
  min-height: 206px;
  max-height: 260px;
  opacity: .76;
  filter: drop-shadow(0 22px 38px rgba(4, 40, 50, .18));
  transform: translateX(-50%);
}

.auth-back {
  position: absolute;
  top: 10px;
  left: 4px;
  z-index: 3;

  .van-icon {
    width: 38px;
    height: 38px;
    border: 1px solid rgba(223, 251, 253, .5);
    border-radius: 8px;
    background: rgba(10, 55, 66, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 20px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.92);
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.auth-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-rows: minmax(218px, 31vh) auto;
  width: 100%;
  max-width: 420px;
  min-height: calc(100vh - 24px);
  overflow: visible;
  border: none;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.auth-hero {
  position: relative;
  z-index: 2;
}

.auth-panel {
  position: relative;
  display: flex;
  margin: -10px -2px 0;
  padding: 26px 8px 22px;
  min-height: 0;
  background: rgba(247, 252, 251, .96);
  border: 1px solid rgba(226, 244, 240, .92);
  border-radius: 8px;
  box-shadow: 0 -8px 34px rgba(8, 70, 70, .14);
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(16px);

  &::before {
    position: absolute;
    top: 10px;
    left: 50%;
    width: 54px;
    height: 4px;
    border-radius: 999px;
    content: "";
    background: #8fd8cf;
    transform: translateX(-50%);
  }
}

.auth-form {
  position: relative;
  width: 100%;
  max-width: 404px;
  margin: 0 auto;
  padding: 16px 22px 18px;
  overflow: hidden;
  border: 1px solid rgba(224, 239, 243, .74);
  border-radius: 8px;
  background: rgba(255, 255, 255, .88);
  box-shadow: 0 18px 46px rgba(36, 111, 106, .12);
  backdrop-filter: blur(12px);

  &::before {
    position: absolute;
    inset: 0;
    content: "";
    background:
      linear-gradient(135deg, rgba(128, 218, 204, .15), transparent 34%),
      linear-gradient(315deg, rgba(238, 145, 169, .1), transparent 30%);
    pointer-events: none;
  }
}

.form-sparkles {
  position: absolute;
  inset: 0;
  pointer-events: none;

  span {
    position: absolute;
    width: 12px;
    height: 12px;
    animation: authSparkle 3.8s ease-in-out infinite;
  }

  span::before,
  span::after {
    position: absolute;
    content: "";
    border-radius: 2px;
    background: rgba(43, 157, 145, .22);
  }

  span::before {
    top: 5px;
    left: 0;
    width: 12px;
    height: 2px;
  }

  span::after {
    top: 0;
    left: 5px;
    width: 2px;
    height: 12px;
  }

  span:nth-child(1) {
    top: 20px;
    right: 24px;
  }

  span:nth-child(2) {
    right: 68px;
    bottom: 30px;
    animation-delay: -1.5s;
  }
}

.brand-lockup {
  position: relative;
  z-index: 1;
  margin-bottom: 18px;
  text-align: center;
}

.form-badge {
  display: inline-flex;
  height: 24px;
  margin-top: 8px;
  padding: 0 14px;
  border: 1px solid rgba(187, 223, 219, .9);
  border-radius: 8px;
  color: #1d756e;
  background: rgba(237, 250, 248, .92);
  box-shadow: 0 8px 18px rgba(36, 111, 106, .1);
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 900;
  letter-spacing: .12em;
}

.title {
  margin: 0;
  color: #173c48;
  font-size: 20px;
  font-weight: 900;
  line-height: 1.25;
}

.form-item {
  position: relative;
  z-index: 1;
  margin-bottom: 14px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 50px;
  padding: 0 16px;
  border-radius: 8px;
  background: #f5fbfc;
  box-shadow: inset 0 0 0 1px rgba(195, 220, 226, .92);
  transition: box-shadow .25s ease, background .25s ease, transform .25s ease;

  &:hover {
    background: #ffffff;
  }

  &:focus-within {
    background: #ffffff;
    box-shadow: inset 0 0 0 1px #319c91, 0 0 0 4px rgba(49, 156, 145, .12);
    transform: translateY(-1px);
  }
}

.input-icon {
  width: 18px;
  height: 18px;
  margin-right: 10px;
  color: #247b73;
  flex-shrink: 0;
}

.input-wrapper input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: #183c47;
  font-size: 15px;
  font-weight: 700;

  &::placeholder {
    color: #8da5ad;
    font-weight: 600;
  }
}

.password-toggle {
  width: 20px;
  height: 20px;
  margin-left: 8px;
  color: #7b9aa3;
  cursor: pointer;
  flex-shrink: 0;
}

.captcha-wrapper {
  .captcha-img {
    width: 100px;
    height: 36px;
    margin-left: 10px;
    border: 1px solid rgba(179, 214, 222, .9);
    border-radius: 8px;
    background: #ffffff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      border-radius: 7px;
      object-fit: cover;
    }

    span {
      color: #5d8c96;
      font-size: 12px;
      font-weight: 700;
    }
  }
}

.auth-options {
  position: relative;
  z-index: 1;
  display: flex;
  margin: -2px 0 12px;
  align-items: center;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;

  input[type="checkbox"] {
    width: 16px;
    height: 16px;
    accent-color: #1d8279;
  }

  span {
    color: #617f89;
    font-size: 14px;
    font-weight: 600;
  }
}

.auth-submit {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 50px;
  overflow: hidden;
  border: 0;
  border-radius: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 900;
  background: #197c75;
  box-shadow: 0 12px 24px rgba(25, 124, 117, .26);
  cursor: pointer;
  transition: transform .25s ease, box-shadow .25s ease, opacity .25s ease;

  &:hover:not(:disabled) {
    background: #156b65;
    box-shadow: 0 16px 30px rgba(25, 124, 117, .3);
    transform: translateY(-2px);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.register-link {
  position: relative;
  z-index: 1;
  text-align: center;
  margin-top: 14px;
  font-size: 14px;

  span:first-child {
    color: #718b94;
  }

  span:last-child {
    color: #197c75;
    font-weight: 800;
    cursor: pointer;
    margin-left: 4px;
  }
}

.wave-lines {
  animation: waveMove 9s ease-in-out infinite;
}

.cloud {
  opacity: .88;
  animation: cloudDrift 9s ease-in-out infinite;
}

.cloud-b {
  animation-delay: -3s;
}

.floating,
.pill,
.plant {
  animation: floatSoft 5.8s ease-in-out infinite;
}

.form-card { animation-delay: -.8s; }
.med-kit { animation-delay: -1.4s; }
.stethoscope { animation-delay: -2s; }
.pill-a { animation-delay: -2.8s; }
.pill-b { animation-delay: -1.8s; }
.plant-left { animation-delay: -1.2s; }
.plant-right { animation-delay: -2.4s; }

.heart-pulse {
  animation: heartbeat 3.4s ease-in-out infinite;
  transform-origin: 306px 214px;
}

.pulse-line {
  stroke-dasharray: 190;
  stroke-dashoffset: 190;
  animation: pulseDraw 3.4s ease-in-out infinite;
}

.tiny-crosses {
  animation: twinkle 4.8s ease-in-out infinite;
}

@keyframes waveMove {
  0%, 100% { transform: translateX(-8px) translateY(0); opacity: .48; }
  50% { transform: translateX(12px) translateY(-8px); opacity: .76; }
}

@keyframes cloudDrift {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(18px); }
}

@keyframes floatSoft {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  45% { transform: scale(1.035); }
  60% { transform: scale(.99); }
}

@keyframes pulseDraw {
  0% { stroke-dashoffset: 190; opacity: .2; }
  45%, 75% { stroke-dashoffset: 0; opacity: 1; }
  100% { stroke-dashoffset: -190; opacity: .25; }
}

@keyframes twinkle {
  0%, 100% { opacity: .38; }
  50% { opacity: .88; }
}

@keyframes authSparkle {
  0%, 100% {
    opacity: .28;
    transform: scale(.88);
  }
  50% {
    opacity: .78;
    transform: scale(1.08);
  }
}

@keyframes ringBreath {
  0%, 100% {
    opacity: .36;
    transform: scale(.96);
  }
  50% {
    opacity: .72;
    transform: scale(1.04);
  }
}

@keyframes scanSweep {
  0%, 28% {
    opacity: 0;
    transform: translateX(0) skewX(-18deg);
  }
  42% {
    opacity: .9;
  }
  70%, 100% {
    opacity: 0;
    transform: translateX(320%) skewX(-18deg);
  }
}

@media (max-width: 360px) {
  .auth-page {
    padding: 8px 10px 12px;
  }

  .auth-shell {
    grid-template-rows: minmax(194px, 29vh) auto;
    min-height: calc(100vh - 20px);
  }

  .medical-art {
    top: 10px;
    width: 108vw;
    min-height: 190px;
    max-height: 226px;
  }

  .auth-hero {
    min-height: 100%;
  }

  .auth-form {
    padding: 14px 16px 16px;
  }

  .auth-panel {
    margin-top: -8px;
    padding: 22px 8px 18px;
  }

  .brand-lockup {
    margin-bottom: 12px;
  }

  .captcha-wrapper .captcha-img {
    width: 88px;
  }
}
</style>
