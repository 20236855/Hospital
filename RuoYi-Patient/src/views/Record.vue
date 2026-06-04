<template>
  <div class="record-container">
    <van-nav-bar title="电子病历" left-arrow @click-left="goBack" />

    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list v-model:loading="loading" :finished="finished" @load="onLoad">
          <div v-if="recordList.length === 0 && !loading" class="empty">
            <van-empty description="暂无病历记录" />
          </div>
          <div v-else>
            <van-card
              v-for="item in recordList"
              :key="item.recordId"
              :title="item.diagnosis"
              :desc="item.createTime"
              is-link
              @click="viewDetail(item)"
            >
              <template #tags>
                <van-tag plain type="primary" size="small">{{ item.visitType }}</van-tag>
              </template>
            </van-card>
          </div>
        </van-list>
      </van-pull-refresh>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMedicalRecordList } from '@/api/emr'

const router = useRouter()
const active = ref(2)
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const recordList = ref([])
const pageNum = ref(1)

const onLoad = async () => {
  try {
    const res = await getMedicalRecordList({
      pageNum: pageNum.value,
      pageSize: 10
    })
    if (pageNum.value === 1) {
      recordList.value = res.rows
    } else {
      recordList.value = [...recordList.value, ...res.rows]
    }
    loading.value = false
    if (recordList.value.length >= res.total) {
      finished.value = true
    } else {
      pageNum.value++
    }
  } catch (error) {
    loading.value = false
    console.error(error)
  }
}

const onRefresh = async () => {
  pageNum.value = 1
  finished.value = false
  recordList.value = []
  await onLoad()
  refreshing.value = false
}

const viewDetail = (item) => {
  console.log('查看详情', item)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  onLoad()
})
</script>

<style scoped lang="scss">
.record-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 50px;

  .content {
    padding: 10px;
  }

  .empty {
    padding-top: 100px;
  }
}
</style>
