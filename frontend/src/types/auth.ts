// 用户相关类型
export interface User {
  id: number
  username: string
  nickname?: string
  email?: string
  avatar?: string
  userKey?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  nickname?: string
  email?: string
  avatar?: string
  userKey?: string
  expiresIn: number
}