import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'
import { login, logout, getCurrentUser } from '@/api/auth'
import type { LoginRequest, LoginResponse, User } from '@/types/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(Cookies.get('token') || '')
  const currentUser = ref<User | null>(null)
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 登录
  const userLogin = async (loginForm: LoginRequest): Promise<void> => {
    loading.value = true
    try {
      const response = await login(loginForm)
      const loginData: LoginResponse = response.data

      // 保存token
      token.value = loginData.token
      Cookies.set('token', loginData.token, { expires: 7 }) // 7天过期

      // 保存用户信息
      currentUser.value = {
        id: loginData.userId,
        username: loginData.username,
        nickname: loginData.nickname,
        email: loginData.email,
        avatar: loginData.avatar,
        userKey: loginData.userKey
      }

      ElMessage.success('登录成功')
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 登出
  const userLogout = async (): Promise<void> => {
    try {
      await logout()
    } catch (error) {
      console.warn('登出接口调用失败:', error)
    } finally {
      // 清除本地状态
      token.value = ''
      currentUser.value = null
      Cookies.remove('token')
      ElMessage.success('已登出')
    }
  }

  // 初始化用户信息（从token恢复）
  const initializeUser = async (): Promise<void> => {
    if (!token.value) return

    try {
      const response = await getCurrentUser()
      currentUser.value = response.data
    } catch (error) {
      console.warn('获取用户信息失败:', error)
      // Token可能已过期，清除本地状态
      token.value = ''
      currentUser.value = null
      Cookies.remove('token')
    }
  }

  // 更新用户信息
  const updateUser = (userData: Partial<User>): void => {
    if (currentUser.value) {
      currentUser.value = { ...currentUser.value, ...userData }
    }
  }

  return {
    // 状态
    token,
    currentUser,
    loading,
    // 计算属性
    isLoggedIn,
    // 方法
    userLogin,
    userLogout,
    initializeUser,
    updateUser
  }
})