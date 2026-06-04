<template>
  <div class="home-container">
    <div class="header">
      <h2>您好，欢迎使用</h2>
      <p>若依患者端</p>
    </div>

    <van-grid :column-num="3" :border="false">
      <van-grid-item icon="todo-list-o" text="预约挂号" @click="goTo('/register')" />
      <van-grid-item icon="notes-o" text="电子病历" @click="goTo('/record')" />
      <van-grid-item icon="user-o" text="个人中心" @click="goTo('/profile')" />
    </van-grid>

    <div class="notice-card">
      <div class="notice-title">
        <van-icon name="info-o" />
        就诊提醒
      </div>
      <van-cell-group inset>
        <van-cell title="暂无预约记录" :border="false" />
      </van-cell-group>
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

const router = useRouter()
const active = ref(0)

const goTo = (path) => {
  router.push(path)
}
</script>

<style scoped lang="scss">
.home-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 50px;

  .header {
    background: linear-gradient(135deg, #1989fa, #07c160);
    color: white;
    padding: 30px 20px 40px;
    border-radius: 0 0 20px 20px;

    h2 {
      font-size: 22px;
      margin-bottom: 5px;
    }

    p {
      font-size: 14px;
      opacity: 0.9;
    }
  }

  .notice-card {
    margin: 20px;
    background: white;
    border-radius: 12px;
    padding: 15px;

    .notice-title {
      font-size: 16px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      gap: 5px;
    }
  }
}
</style>
