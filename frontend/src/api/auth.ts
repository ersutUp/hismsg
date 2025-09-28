import { request } from '@/utils/request'
import type { LoginRequest, LoginResponse, User } from '@/types/auth'

// 用户登录
export const login = (data: LoginRequest) => {
  return request.post<LoginResponse>('/auth/login', data)
}

// 用户登出
export const logout = () => {
  return request.post('/auth/logout')
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return request.get<User>('/user/current')
}