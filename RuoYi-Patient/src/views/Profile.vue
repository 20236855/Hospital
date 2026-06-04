<template>
  <div class="profile-container">
    <div class="user-header">
      <div class="avatar">
        <van-icon name="user-circle-o" size="60" />
      </div>
      <div class="user-info">
        <h3>患者姓名</h3>
        <p>ID: 12345678</p>
      </div>
    </div>

    <van-cell-group inset>
      <van-cell title="个人信息" is-link @click="goToInfo" />
      <van-cell title="我的预约" is-link @click="goToMyRegister" />
      <van-cell title="就诊记录" is-link @click="goToRecord" />
      <van-cell title="帮助中心" is-link />
    </van-cell-group>

    <div style="margin: 16px;">
      <van-button round block type="danger" plain @click="logout">
        退出登录
      </van-button>
    </div>
  </div>

  <van-tabbar v-model="active">
    <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
    <van-tabbar-item icon="todo-list-o" to="/register">挂号</van-tabbar-item>
    <van-tabbar-item icon="notes-o" to="/record">病历</van-tabbar-item>
    <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { logout } from '@/api/login'

const router = useRouter()
const active = ref(3)

const goToInfo = () => {
  console.log('个人信息')
}

const goToMyRegister = () => {
  console.log('我的预约')
}

const goToRecord = () => {
  router.push('/record')
}

const doLogout = async () => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要退出登录吗？',
    })
    await logout()
    localStorage.removeItem('token')
    showToast('退出成功')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}
</script>

<style scoped lang="scss">
.profile-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 50px;

  .user-header {
    background: linear-gradient(135deg, #1989fa, #07c160);
    padding: 40px 20px 30px;
    display: flex;
    align-items: center;
    gap: 15px;

    .avatar {
      color: white;
    }

    .user-info {
      color: white;

      h3 {
        font-size: 20px;
        margin-bottom: 5px;
      }

      p {
        font-size: 14px;
        opacity: 0.9;
      }
    }
  }
}
</style>
