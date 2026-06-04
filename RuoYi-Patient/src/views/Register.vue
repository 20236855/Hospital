<template>
  <div class="register-container">
    <van-nav-bar title="预约挂号" left-arrow @click-left="goBack" />

    <div class="content">
      <van-cell-group inset>
        <van-field
          v-model="doctorId"
          name="doctor"
          label="选择医生"
          placeholder="请选择医生"
          readonly
          is-link
          @click="showDoctorSelect = true"
        />
        <van-field
          v-model="scheduleId"
          name="schedule"
          label="选择时间"
          placeholder="请选择时间"
          readonly
          is-link
          @click="showScheduleSelect = true"
        />
      </van-cell-group>

      <div style="margin: 16px;">
        <van-button round block type="primary" @click="submitRegister" :loading="loading">
          确认挂号
        </van-button>
      </div>
    </div>

    <van-popup v-model:show="showDoctorSelect" position="bottom" round>
      <div class="select-header">选择医生</div>
      <van-picker
        :columns="doctorList"
        show-toolbar
        @confirm="onDoctorConfirm"
        @cancel="showDoctorSelect = false"
      />
    </van-popup>

    <van-popup v-model:show="showScheduleSelect" position="bottom" round>
      <div class="select-header">选择时间</div>
      <van-picker
        :columns="scheduleList"
        show-toolbar
        @confirm="onScheduleConfirm"
        @cancel="showScheduleSelect = false"
      />
    </van-popup>
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
import { showToast, showConfirmDialog } from 'vant'
import { getDoctorList, getScheduleList, addRegister } from '@/api/register'

const router = useRouter()
const active = ref(1)
const loading = ref(false)
const showDoctorSelect = ref(false)
const showScheduleSelect = ref(false)

const doctorId = ref('')
const doctorName = ref('')
const doctorList = ref([])

const scheduleId = ref('')
const scheduleTime = ref('')
const scheduleList = ref([])

const loadDoctorList = async () => {
  try {
    const res = await getDoctorList({ pageNum: 1, pageSize: 100 })
    doctorList.value = res.rows.map(item => ({
      text: item.doctorName,
      value: item.doctorId
    }))
  } catch (error) {
    console.error(error)
  }
}

const loadScheduleList = async () => {
  try {
    const res = await getScheduleList({ pageNum: 1, pageSize: 100 })
    scheduleList.value = res.rows.map(item => ({
      text: item.scheduleDate + ' ' + item.timeSlot,
      value: item.scheduleId
    }))
  } catch (error) {
    console.error(error)
  }
}

const onDoctorConfirm = ({ selectedValues, selectedOptions }) => {
  doctorId.value = selectedValues[0]
  doctorName.value = selectedOptions[0].text
  showDoctorSelect.value = false
}

const onScheduleConfirm = ({ selectedValues, selectedOptions }) => {
  scheduleId.value = selectedValues[0]
  scheduleTime.value = selectedOptions[0].text
  showScheduleSelect.value = false
}

const submitRegister = async () => {
  if (!doctorId.value) {
    showToast('请选择医生')
    return
  }
  if (!scheduleId.value) {
    showToast('请选择时间')
    return
  }

  try {
    await showConfirmDialog({
      title: '确认挂号',
      message: `确认预约 ${doctorName.value} 医生，${scheduleTime.value} 吗？`,
    })

    loading.value = true
    await addRegister({
      doctorId: doctorId.value,
      scheduleId: scheduleId.value
    })
    showToast('挂号成功')
    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadDoctorList()
  loadScheduleList()
})
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 50px;

  .content {
    padding-top: 10px;
  }

  .select-header {
    padding: 16px;
    text-align: center;
    font-weight: bold;
    font-size: 16px;
  }
}
</style>
