import { request } from '@/utils/request'
import type { PageResponse, MessageRecord, PushRecord } from '@/types/api'

// 分页查询消息记录
export const getMessageList = (params: {
  page?: number
  size?: number
  messageType?: string
  startTime?: string
  endTime?: string
}) => {
  return request.get<PageResponse<MessageRecord>>('/message/record/list', { params })
}

// 根据ID查询消息记录详情
export const getMessageDetail = (id: string) => {
  return request.get<MessageRecord>(`/message/record/${id}`)
}

// 查询消息推送记录
export const getMessagePushRecords = (messageId: number) => {
  return request.get<PushRecord[]>(`/message/record/${messageId}/push-records`)
}

// 获取消息统计数据
export const getMessageStatistics = (days: number = 7) => {
  return request.get<any>(`/message/record/statistics?days=${days}`)
}