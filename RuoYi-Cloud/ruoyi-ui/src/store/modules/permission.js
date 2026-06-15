import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import store from '@/store'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

const NURSE_POST_ID = 2
const NURSE_ROUTE_COMPONENTS = new Set([
  'patient/patient/index',
  'register/register/index',
  'register/in/index',
  'payment/payment/index'
])
const NURSE_HIDDEN_ROUTE_PATH_PREFIXES = ['/emr', 'emr', '/hisexam', 'hisexam']
const NURSE_HIDDEN_ROUTE_NAMES = new Set(['会诊管理', '医院检测'])
const NURSE_HIDDEN_ROUTE_COMPONENTS = new Set([
  'emr/encounter/index',
  'emr/record/index',
  'emr/disease/index',
  'emr/recorddisease/index',
  'hisexam/apply/index',
  'hisexam/technology/index'
])

const permission = {
  state: {
    routes: [],
    addRoutes: [],
    defaultRoutes: [],
    topbarRouters: [],
    sidebarRouters: []
  },
  mutations: {
    SET_ROUTES: (state, routes) => {
      state.addRoutes = routes
      state.routes = constantRoutes.concat(routes)
    },
    SET_DEFAULT_ROUTES: (state, routes) => {
      state.defaultRoutes = constantRoutes.concat(routes)
    },
    SET_TOPBAR_ROUTES: (state, routes) => {
      state.topbarRouters = routes
    },
    SET_SIDEBAR_ROUTERS: (state, routes) => {
      state.sidebarRouters = routes
    },
  },
  actions: {
    // 生成路由
    GenerateRoutes({ commit }) {
      return new Promise(resolve => {
        // 向后端请求路由数据
        getRouters().then(res => {
          const routeData = isNurseUser() ? filterNurseRoutes(res.data) : res.data
          const sdata = JSON.parse(JSON.stringify(routeData))
          const rdata = JSON.parse(JSON.stringify(routeData))
          const sidebarRoutes = filterAsyncRouter(sdata)
          const rewriteRoutes = filterAsyncRouter(rdata, false, true)
          const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
          rewriteRoutes.push({ path: '*', redirect: '/404', hidden: true })
          router.addRoutes(asyncRoutes)
          commit('SET_ROUTES', rewriteRoutes)
          commit('SET_SIDEBAR_ROUTERS', constantRoutes.concat(sidebarRoutes))
          commit('SET_DEFAULT_ROUTES', sidebarRoutes)
          commit('SET_TOPBAR_ROUTES', sidebarRoutes)
          resolve(rewriteRoutes)
        })
      })
    }
  }
}

function isNurseUser() {
  return (store.getters.postIds || []).some(postId => Number(postId) === NURSE_POST_ID)
}

function filterNurseRoutes(routes) {
  return routes.reduce((result, route) => {
    if (isNurseHiddenRoute(route)) {
      return result
    }
    const nextRoute = { ...route }
    if (route.children && route.children.length) {
      nextRoute.children = filterNurseRoutes(route.children)
    }
    const componentAllowed = NURSE_ROUTE_COMPONENTS.has(route.component)
    const hasAllowedChildren = nextRoute.children && nextRoute.children.length > 0
    if (componentAllowed || hasAllowedChildren) {
      result.push(nextRoute)
    }
    return result
  }, [])
}

function isNurseHiddenRoute(route) {
  const path = route.path || ''
  const component = route.component || ''
  const menuName = route.meta?.title || route.menuName || route.name || ''
  return NURSE_HIDDEN_ROUTE_NAMES.has(menuName)
    || NURSE_HIDDEN_ROUTE_PATH_PREFIXES.some(prefix => path === prefix || path.startsWith(prefix + '/'))
    || NURSE_HIDDEN_ROUTE_COMPONENTS.has(component)
    || component.startsWith('emr/')
    || component.startsWith('hisexam/')
}

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export const loadView = (view) => {
  if (process.env.NODE_ENV === 'development') {
    return (resolve) => require([`@/views/${view}`], resolve)
  } else {
    // 使用 import 实现生产环境的路由懒加载
    return () => import(`@/views/${view}`)
  }
}

export default permission
