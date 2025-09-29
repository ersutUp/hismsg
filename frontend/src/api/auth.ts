import { request } from '@/utils/request'
import type { LoginRequest, LoginResponse, User } from '@/types/auth'

// 修改密码请求接口
export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

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

// 修改密码
export const changePassword = (data: ChangePasswordRequest) => {
  return request.post('/user/change-password', data)
}

// 重置用户密钥
export const resetUserKey = () => {
  return request.post<string>('/user/reset-user-key')
}