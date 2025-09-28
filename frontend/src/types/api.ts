// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应类型
export interface PageResponse<T = any> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

// 字典类型
export interface DictType {
  id: number
  dictName: string
  dictType: string
  status: number
  remark?: string
  createTime: string
  updateTime: string
}

export interface DictData {
  id: number
  dictType: string
  dictLabel: string
  dictValue: string
  dictSort: number
  cssClass?: string
  listClass?: string
  isDefault: number
  status: number
  remark?: string
  createTime: string
  updateTime: string
}

// 推送配置类型
export interface PushConfig {
  id?: number
  platform: string
  configName: string
  isEnabled: number
  sortOrder?: number
  remark?: string
  bark?: BarkConfig
  email?: EmailConfig
  wxpusher?: WxPusherConfig
  pushme?: PushMeConfig
}

export interface BarkConfig {
  deviceKey: string
  serverUrl?: string
  sound?: string
  group?: string
  icon?: string
}

export interface EmailConfig {
  toEmail: string
  subjectPrefix?: string
}

export interface WxPusherConfig {
  appToken: string
  uid?: string
  topicId?: string
  summaryLength?: number
  contentType?: number
}

export interface PushMeConfig {
  pushKey: string
  template?: string
}

// 消息记录类型
export interface MessageRecord {
  id: string
  userId: number
  userCode: string
  messageType: string
  title: string
  subtitle?: string
  content: string
  group?: string
  url?: string
  source: string
  level: string
  tags: string[]
  extraData: string
  status: number
  pushedPlatforms: string[]
  pushSuccessCount: number
  pushFailCount: number
  createTime: string
  updateTime: string
}

// 推送记录类型
export interface PushRecord {
  id: number
  messageId: number
  userId: number
  platform: string
  configName: string
  pushStatus: number
  requestData: string
  responseData: string
  errorMessage?: string
  retryCount: number
  pushTime: string
  createTime: string
}