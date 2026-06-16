import { createRouter, createWebHashHistory } from 'vue-router'
import { getInfo } from '@/api/user'
import { getPatientByUserId } from '@/api/patient'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register-account',
    name: 'RegisterAccount',
    component: () => import('@/views/RegisterAccount.vue'),
    meta: { title: '注册账户' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '挂号', requiresAuth: true }
  },
  {
    path: '/record',
    name: 'Record',
    component: () => import('@/views/Record.vue'),
    meta: { title: '电子病历', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/patient-complete',
    name: 'PatientComplete',
    component: () => import('@/views/PatientComplete.vue'),
    meta: { title: '完善信息', requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/Chat.vue'),
    meta: { title: '智能问诊', requiresAuth: true }
  },
  {
    path: '/doctors',
    name: 'DoctorList',
    component: () => import('@/views/DoctorList.vue'),
    meta: { title: '科室医生', requiresAuth: true }
  },
  {
    path: '/doctor/:id',
    name: 'DoctorDetail',
    component: () => import('@/views/DoctorDetail.vue'),
    meta: { title: '医生详情', requiresAuth: true }
    path: '/my-appointments',
    name: 'MyAppointments',
    component: () => import('@/views/MyAppointments.vue'),
    meta: { title: '我的预约', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to, from) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }

  if (to.path === '/patient-complete' && token && to.query.mode !== 'edit') {
    const savedPatientId = localStorage.getItem('patientId')
    if (savedPatientId) {
      return '/'
    }

    try {
      const res = await getInfo()
      const userId = res?.user?.userId
      if (userId) {
        const patientRes = await getPatientByUserId(userId)
        const patient = patientRes?.data
        if (patient && Object.keys(patient).length > 0) {
          localStorage.setItem('patientId', patient.patientId)
          localStorage.setItem('patientName', patient.name)
          return '/'
        }
      }
    } catch (error) {
      console.error('路由守卫检查患者信息失败', error)
    }
  }

  return true
})

export default router
