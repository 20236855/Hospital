<template>
  <div class="register-page">
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
        <h1>患者注册</h1>
      </div>
    </div>

    <div class="content-section slide-up-animation">
      <div class="form-card">
        <div class="card-header">
          <div class="header-icon">👤</div>
          <div class="header-text">
            <h3>创建患者账号</h3>
            <p>填写信息完成注册</p>
          </div>
        </div>

        <form class="register-form" @submit.prevent="handleRegister" id="register-form">
          <div class="form-item">
            <label class="form-label">用户名</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
                  <path d="M4 20C4 15.5817 7.58172 12 12 12C16.4183 12 20 15.5817 20 20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input
                v-model="registerForm.username"
                type="text"
                placeholder="请输入用户名（2-20字符）"
                autocomplete="off"
                required
              />
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">姓名</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                </svg>
              </span>
              <input
                v-model="registerForm.nickName"
                type="text"
                placeholder="请输入真实姓名"
                autocomplete="off"
                required
              />
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">手机号</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22 16.92V21C22 21.2652 21.8946 21.5196 21.7071 21.7071C21.5196 21.8946 21.2652 22 21 22H3C2.73478 22 2.48043 21.8946 2.29289 21.7071C2.10536 21.5196 2 21.2652 2 21V3C2 2.73478 2.10536 2.48043 2.29289 2.29289C2.48043 2.10536 2.73478 2 3 2H21C21.2652 2 21.5196 2.10536 21.7071 2.29289C21.8946 2.48043 22 2.73478 22 3V7.08" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M16 2H8C6.93913 2 6 2.93913 6 4V20C6 21.0609 6.93913 22 8 22H16C17.0609 22 18 21.0609 18 20V4C18 2.93913 17.0609 2 16 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M12 18H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input
                v-model="registerForm.phonenumber"
                type="tel"
                placeholder="请输入手机号"
                autocomplete="off"
                maxlength="11"
                required
              />
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">密码</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="11" width="18" height="10" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M7 11V8C7 5.23858 9.23858 3 12 3C14.7614 3 17 5.23858 17 8V11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input
                v-model="registerForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码（5-20字符）"
                autocomplete="off"
                required
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
            <label class="form-label">确认密码</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="11" width="18" height="10" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M7 11V8C7 5.23858 9.23858 3 12 3C14.7614 3 17 5.23858 17 8V11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input
                v-model="confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="请再次输入密码"
                autocomplete="off"
                required
              />
              <span class="password-toggle" @click="showConfirmPassword = !showConfirmPassword">
                <svg v-if="!showConfirmPassword" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
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
            <label class="form-label">验证码</label>
            <div class="input-wrapper captcha-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M9 9L11 12L9 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M15 9L13 12L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
              <input
                v-model="registerForm.code"
                type="text"
                placeholder="请输入验证码"
                autocomplete="off"
                required
              />
              <div class="captcha-img" @click="getCode">
                <img v-if="codeUrl" :src="codeUrl" alt="验证码" />
                <span v-else>点击刷新</span>
              </div>
            </div>
          </div>

          <button type="button" class="register-btn" :disabled="loading" @click="handleRegister">
            <span v-if="loading">注册中...</span>
            <span v-else>立即注册</span>
          </button>
        </form>

        <div class="login-link">
          已有账号？<span @click="goLogin">立即登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { register, getCodeImg } from '@/api/login'

const router = useRouter()

const registerForm = ref({
  username: '',
  nickName: '',
  phonenumber: '',
  password: '',
  userType: 'patient',
  code: '',
  uuid: ''
})

const codeUrl = ref('')

const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)

const getCode = async () => {
  try {
    const res = await getCodeImg()
    codeUrl.value = 'data:image/gif;base64,' + res.img
    registerForm.value.uuid = res.uuid
  } catch (error) {
    console.error('获取验证码失败', error)
  }
}

const handleRegister = async () => {
  console.log('点击了注册按钮')
  console.log('表单数据:', registerForm.value)
  
  if (!registerForm.value.username) {
    alert('请输入用户名')
    return
  }
  if (registerForm.value.username.length < 2 || registerForm.value.username.length > 20) {
    alert('用户名长度必须在2-20字符之间')
    return
  }
  if (!registerForm.value.nickName) {
    alert('请输入姓名')
    return
  }
  if (!registerForm.value.phonenumber) {
    alert('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(registerForm.value.phonenumber)) {
    alert('请输入正确的手机号（11位数字，以1开头）')
    return
  }
  if (!registerForm.value.password) {
    alert('请输入密码')
    return
  }
  if (registerForm.value.password.length < 5 || registerForm.value.password.length > 20) {
    alert('密码长度必须在5-20字符之间')
    return
  }
  if (registerForm.value.password !== confirmPassword.value) {
    alert('两次输入的密码不一致')
    return
  }
  if (!registerForm.value.code) {
    alert('请输入验证码')
    return
  }

  loading.value = true
  console.log('准备发送注册请求')
  try {
    const result = await register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      nickName: registerForm.value.nickName,
      phonenumber: registerForm.value.phonenumber,
      userType: registerForm.value.userType,
      code: registerForm.value.code,
      uuid: registerForm.value.uuid
    })
    console.log('注册成功，响应:', result)
    alert('注册成功，请登录')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('注册错误:', error)
    alert(error.message || '注册失败，请稍后重试')
    getCode()
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/login')
}

const goLogin = () => {
  router.push('/login')
}

onMounted(() => {
  getCode()
})
</script>

<style scoped lang="scss">
.register-page {
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

.register-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
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
}

.password-toggle {
  width: 20px;
  height: 20px;
  margin-left: 8px;
  color: var(--text-light);
  cursor: pointer;
  flex-shrink: 0;
}

.captcha-wrapper {
  .captcha-img {
    width: 100px;
    height: 36px;
    margin-left: 10px;
    border: 1px solid rgba(180, 224, 235, .82);
    border-radius: 10px;
    background: rgba(255, 255, 255, .72);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      border-radius: 8px;
      object-fit: cover;
    }

    span {
      color: var(--text-regular);
      font-size: 12px;
    }
  }
}

.register-btn {
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

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #8e9fa8;

  span {
    color: var(--primary-color);
    font-weight: 600;
    cursor: pointer;
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
