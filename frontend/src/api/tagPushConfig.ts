import { request } from '@/utils/request'

// 标签推送配置类型
export interface TagPushConfig {
  id?: number
  userId?: number
  tagName: string
  pushConfigIds: number[]
  isEnabled: number
  remark?: string
  createTime?: string
  updateTime?: string
}

// 用户推送配置类型
export interface UserPushConfig {
  id: number
  userId: number
  platform: string
  configName: string
  configData: string
  isEnabled: number
  sortOrder?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

// 获取标签推送配置列表
export const getTagPushConfigList = () => {
  return request.get<TagPushConfig[]>('/tag-push-config/list')
}

// 获取标签名称列表
export const getTagNames = () => {
  return request.get<string[]>('/tag-push-config/tag-names')
}

// 获取用户推送配置列表（用于标签配置选择）
export const getUserPushConfigs = () => {
  return request.get<UserPushConfig[]>('/tag-push-config/user-push-configs')
}

// 保存标签推送配置
export const saveTagPushConfig = (data: TagPushConfig) => {
  return request.post('/tag-push-config/save', data)
}

// 删除标签推送配置
export const deleteTagPushConfig = (id: number) => {
  return request.delete(`/tag-push-config/${id}`)
}