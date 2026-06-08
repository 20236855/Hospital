<template>
  <div class="auth-page register">
    <span class="auth-bg-cross cross-one"></span>
    <span class="auth-bg-cross cross-two"></span>
    <span class="auth-bg-cross cross-three"></span>
    <span class="auth-bg-cross cross-four"></span>

    <div class="auth-shell">
      <AuthMedicalVisual />

      <main class="auth-panel">
        <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="auth-form register-form">
          <div class="form-sparkles">
            <span></span>
            <span></span>
          </div>
          <div class="brand-lockup">
            <span class="brand-mark"><i class="brand-cross"></i></span>
            <p class="slogan">用心守护，智慧就医</p>
            <h3 class="title">注册{{ title }}</h3>
          </div>
      <el-form-item prop="userType" label="身份">
        <el-radio-group v-model="registerForm.userType">
          <el-radio value="patient">就诊患者</el-radio>
          <el-radio value="doctor">医护人员</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="username">
        <el-input 
          v-model="registerForm.username" 
          type="text" 
          size="large" 
          auto-complete="off" 
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password" :rules="registerPwdValidator">
        <el-input
          v-model="registerForm.password"
          type="password"
          size="large" 
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          size="large" 
          auto-complete="off"
          placeholder="确认密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="nickName">
        <el-input 
          v-model="registerForm.nickName" 
          type="text" 
          size="large" 
          auto-complete="off" 
          placeholder="姓名"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="phonenumber">
        <el-input 
          v-model="registerForm.phonenumber" 
          type="text" 
          size="large" 
          auto-complete="off" 
          placeholder="手机号"
        >
          <template #prefix><svg-icon icon-class="phone" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>

      <el-form-item prop="code" v-if="captchaEnabled" class="auth-code-item">
        <el-input
          size="large" 
          v-model="registerForm.code"
          class="auth-code-input"
          auto-complete="off"
          placeholder="验证码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="auth-code register-code">
          <img :src="codeUrl" @click="getCode" class="auth-code-img register-code-img" />
        </div>
      </el-form-item>
      <el-form-item>
        <el-button
          class="auth-submit"
          :loading="loading"
          size="large" 
          type="primary"
          @click.prevent="handleRegister"
        >
          <span v-if="!loading">注册</span>
          <span v-else>注册中...</span>
        </el-button>
      </el-form-item>
          <div class="auth-switch">
            <span>已有账号？</span>
            <router-link :to="'/login'">去登录</router-link>
          </div>
        </el-form>
      </main>
    </div>
    <!--  底部  -->
    <div class="el-register-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from "element-plus"
import { getCodeImg, register } from "@/api/login"
import defaultSettings from '@/settings'
import { usePasswordRule } from "@/utils/passwordRule"
import AuthMedicalVisual from '@/components/AuthMedicalVisual/index.vue'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const router = useRouter()
const { proxy } = getCurrentInstance()
const { registerPwdValidator } = usePasswordRule()

const registerForm = ref({
  username: "",
  password: "",
  confirmPassword: "",
  code: "",
  uuid: "",
  userType: "",
  nickName: "",
  phonenumber: ""
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const validateUserType = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请选择身份"))
  } else {
    callback()
  }
}

const registerRules = {
  userType: [
    { required: true, validator: validateUserType, trigger: "change" }
  ],
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" },
    { min: 2, max: 20, message: "用户账号长度必须介于 2 和 20 之间", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  nickName: [
    { required: true, trigger: "blur", message: "请输入您的姓名" }
  ],
  phonenumber: [
    { required: true, trigger: "blur", message: "请输入您的手机号" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(true)

function handleRegister() {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      if (registerForm.value.username === 'admin') {
        proxy.$modal.msgError("超级管理员账号不开放自助注册")
        return
      }
      loading.value = true
      const submitData = { ...registerForm.value }
      delete submitData.confirmPassword
      register(submitData).then(() => {
        const message = buildRegisterSuccessMessage(registerForm.value)
        ElMessageBox.alert(message, "注册成功", {
          dangerouslyUseHTMLString: true,
          type: "success",
          customClass: "register-success-message-box",
          confirmButtonText: "去登录",
          closeOnClickModal: false,
          closeOnPressEscape: false
        }).then(() => {
          router.push("/login")
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function buildRegisterSuccessMessage(form) {
  const isDoctor = form.userType === 'doctor'
  const username = escapeHtml(form.username)
  const identity = isDoctor ? '医护人员' : '就诊患者'
  const title = isDoctor ? '账号已提交审核' : '账号已创建完成'
  const desc = isDoctor
    ? '管理员审核通过后即可登录系统，请留意审核结果。'
    : '现在可以前往登录，首次登录后请继续完善个人就诊资料。'
  const tag = isDoctor ? '等待审核' : '可立即登录'

  return `
    <div class="register-success-card">
      <div class="success-mark">
        <span></span>
      </div>
      <div class="success-content">
        <div class="success-tag">${tag}</div>
        <h3>${title}</h3>
        <p>${desc}</p>
        <div class="success-summary">
          <div>
            <span>账号</span>
            <strong>${username}</strong>
          </div>
          <div>
            <span>身份</span>
            <strong>${identity}</strong>
          </div>
        </div>
      </div>
    </div>
  `
}

function escapeHtml(value) {
  return String(value || '').replace(/[&<>"']/g, match => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[match]))
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      registerForm.value.uuid = res.uuid
    }
  })
}

getCode()
</script>

<style lang='scss'>
@use "@/assets/styles/auth-medical.scss";

.register-success-message-box {
  width: min(430px, calc(100vw - 32px));
  border-radius: 8px;
  border: 1px solid rgba(33, 150, 136, 0.16);
  overflow: hidden;

  .el-message-box__header {
    padding: 18px 22px 0;
  }

  .el-message-box__title {
    color: #163b46;
    font-size: 18px;
    font-weight: 700;
  }

  .el-message-box__status {
    display: none;
  }

  .el-message-box__content {
    padding: 16px 22px 8px;
  }

  .el-message-box__btns {
    padding: 8px 22px 22px;

    .el-button {
      min-width: 116px;
      border-radius: 6px;
      font-weight: 600;
    }
  }
}

.register-success-card {
  display: flex;
  gap: 16px;
  padding: 18px;
  border-radius: 8px;
  background: linear-gradient(135deg, #f0fbf8, #f5fbff);
  border: 1px solid #d9efea;
}

.success-mark {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #15a884, #168ba1);
  box-shadow: 0 10px 24px rgba(21, 151, 139, 0.26);

  span {
    width: 22px;
    height: 12px;
    border-left: 3px solid #fff;
    border-bottom: 3px solid #fff;
    transform: rotate(-45deg) translate(1px, -1px);
  }
}

.success-content {
  flex: 1;
  min-width: 0;

  h3 {
    margin: 8px 0 8px;
    color: #173b46;
    font-size: 20px;
    line-height: 1.35;
  }

  p {
    margin: 0;
    color: #61727a;
    font-size: 14px;
    line-height: 1.7;
  }
}

.success-tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  color: #0b7c72;
  font-size: 12px;
  font-weight: 700;
  background: #ddf5ef;
}

.success-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 16px;

  div {
    min-width: 0;
    padding: 12px;
    border-radius: 6px;
    background: rgba(255, 255, 255, 0.82);
    border: 1px solid rgba(22, 139, 161, 0.12);
  }

  span {
    display: block;
    color: #7b8a91;
    font-size: 12px;
    margin-bottom: 5px;
  }

  strong {
    display: block;
    overflow: hidden;
    color: #244752;
    font-size: 14px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

@media (max-width: 480px) {
  .register-success-card {
    display: block;
  }

  .success-mark {
    margin-bottom: 14px;
  }

  .success-summary {
    grid-template-columns: 1fr;
  }
}
</style>
