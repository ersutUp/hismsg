import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置NProgress
NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表板', icon: 'Dashboard' }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/Messages.vue'),
        meta: { title: '消息记录', icon: 'ChatDotRound' }
      },
      {
        path: 'push-config',
        name: 'PushConfig',
        component: () => import('@/views/PushConfig.vue'),
        meta: { title: '推送配置', icon: 'Setting' }
      },
      {
        path: 'push-docs',
        name: 'PushDocs',
        component: () => import('@/views/PushDocs.vue'),
        meta: { title: '接口文档', icon: 'Document' }
      },
      {
        path: 'change-password',
        name: 'ChangePassword',
        component: () => import('@/views/ChangePassword.vue'),
        meta: { title: '修改密码', icon: 'Lock' }
      },
      {
        path: 'dict-management',
        name: 'DictManagement',
        component: () => import('@/views/DictManagement.vue'),
        meta: { title: '字典管理', icon: 'Collection', adminOnly: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn
  const requiresAuth = to.meta?.requiresAuth !== false

  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - HisMsg`
  } else {
    document.title = 'HisMsg - 消息通知系统'
  }

  // 如果需要认证但用户未登录，跳转到登录页
  if (requiresAuth && !isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 如果已登录且访问登录页，跳转到首页
  if (isLoggedIn && to.name === 'Login') {
    next({ name: 'Dashboard' })
    return
  }

  // 检查管理员权限
  if (to.meta?.adminOnly && userStore.currentUser?.username !== 'admin') {
    ElMessage.error('权限不足')
    next({ name: 'Dashboard' })
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router