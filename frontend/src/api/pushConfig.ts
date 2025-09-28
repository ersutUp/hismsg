import { request } from '@/utils/request'
import type { PushConfig } from '@/types/api'

// 获取推送配置列表
export const getPushConfigList = () => {
  return request.get<PushConfig[]>('/user/push-config/list')
}

// 根据平台获取推送配置
export const getPushConfigByPlatform = (platform: string) => {
  return request.get<PushConfig[]>(`/user/push-config/platform/${platform}`)
}

// 根据ID获取推送配置详情
export const getPushConfigDetail = (id: number) => {
  return request.get<PushConfig>(`/user/push-config/${id}`)
}

// 新增推送配置
export const createPushConfig = (data: PushConfig) => {
  return request.post('/user/push-config', data)
}

// 修改推送配置
export const updatePushConfig = (data: PushConfig) => {
  return request.put('/user/push-config', data)
}

// 删除推送配置
export const deletePushConfig = (id: number) => {
  return request.delete(`/user/push-config/${id}`)
}

// 切换推送配置状态
export const togglePushConfig = (id: number, enabled: boolean) => {
  return request.put(`/user/push-config/${id}/toggle?enabled=${enabled}`)
}

// 测试推送配置
export const testPushConfig = (id: number) => {
  return request.post(`/user/push-config/${id}/test`)
}

// 获取支持的推送平台列表
export const getSupportedPlatforms = () => {
  return request.get<string[]>('/user/push-config/platforms')
}