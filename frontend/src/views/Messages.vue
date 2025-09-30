<template>
  <div class="page-container">
    <div class="page-header">
      <h1>消息记录</h1>
      <p class="page-description">查看和管理所有消息记录</p>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="search-card mb-3">
      <el-form
        ref="searchFormRef"
        :model="searchForm"
        inline
        @submit.prevent="handleSearch"
      >
        <el-form-item label="消息类型">
          <el-select
            v-model="searchForm.messageType"
            placeholder="请选择消息类型"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="item in messageTypeOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="datetimerange"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 360px"
          />
        </el-form-item>
        
        <el-form-item label="消息标签">
          <el-select
            v-model="searchForm.tags"
            placeholder="请选择标签"
            clearable
            multiple
            style="width: 200px"
          >
            <el-option
              v-for="tag in tagOptions"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            查询
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 消息列表 -->
    <el-card>
      <div class="table-toolbar">
        <div class="toolbar-left">
          <span class="toolbar-title">
            共 {{ pagination.total }} 条消息记录
          </span>
        </div>
        <div class="toolbar-right">
          <el-button
            :icon="Refresh"
            :loading="loading"
            @click="fetchMessageList"
          >
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="messageList"
        stripe
        @row-click="handleRowClick"
        style="cursor: pointer"
      >
        <el-table-column label="消息类型" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getMessageTypeTagType(row.messageType)"
              size="small"
            >
              {{ getMessageTypeText(row.messageType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="消息标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="message-title">
              <div>{{ row.title }}</div>
              <div v-if="row.subtitle" class="message-subtitle">{{ row.subtitle }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="消息内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="message-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="消息级别" width="80">
          <template #default="{ row }">
            <el-tag
              :type="getLevelTagType(row.level)"
              size="small"
            >
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="消息标签" width="150">
          <template #default="{ row }">
            <div class="message-tags">
              <el-tag
                v-for="tag in row.tags || []"
                :key="tag"
                size="small"
                type="info"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
              <span v-if="!row.tags || row.tags.length === 0" class="no-tags">无标签</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="推送状态" width="100">
          <template #default="{ row }">
            <div class="push-status">
              <el-tag
                v-if="row.pushSuccessCount > 0"
                type="success"
                size="small"
              >
                成功 {{ row.pushSuccessCount }}
              </el-tag>
              <el-tag
                v-if="row.pushFailCount > 0"
                type="danger"
                size="small"
                class="mt-1"
              >
                失败 {{ row.pushFailCount }}
              </el-tag>
              <span
                v-if="row.pushSuccessCount === 0 && row.pushFailCount === 0"
                class="text-muted"
              >
                未推送
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="来源" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.source }}
          </template>
        </el-table-column>
        
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              type="primary"
              size="small"
              @click.stop="viewMessageDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 消息详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="消息详情"
      width="800px"
      :before-close="closeDetailDialog"
    >
      <div v-if="selectedMessage" class="message-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="消息ID">
            {{ selectedMessage.id }}
          </el-descriptions-item>
          <el-descriptions-item label="消息类型">
            <el-tag :type="getMessageTypeTagType(selectedMessage.messageType)">
              {{ getMessageTypeText(selectedMessage.messageType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="消息级别">
            <el-tag :type="getLevelTagType(selectedMessage.level)">
              {{ getLevelText(selectedMessage.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="消息标题" :span="2">
            {{ selectedMessage.title }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedMessage.subtitle" label="副标题" :span="2">
            {{ selectedMessage.subtitle }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedMessage.group" label="消息分组">
            <el-tag type="info" size="small">{{ selectedMessage.group }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数据来源">
            {{ selectedMessage.source }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(selectedMessage.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="消息内容" :span="2">
            <div class="message-content-detail">
              {{ selectedMessage.content }}
            </div>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedMessage.url" label="相关链接" :span="2">
            <el-link :href="selectedMessage.url" target="_blank" type="primary">
              {{ selectedMessage.url }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedMessage.tags?.length" label="标签" :span="2">
            <el-tag
              v-for="tag in selectedMessage.tags"
              :key="tag"
              class="mr-1"
              size="small"
            >
              {{ tag }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 推送记录 -->
        <div v-if="pushRecords.length > 0" class="push-records mt-4">
          <h4>推送记录</h4>
          <el-table :data="pushRecords" size="small" class="mt-2">
            <el-table-column label="推送平台" width="100">
              <template #default="{ row }">
                {{ getPlatformText(row.platform) }}
              </template>
            </el-table-column>
            <el-table-column label="配置名称" width="120" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.configName }}
              </template>
            </el-table-column>
            <el-table-column label="推送状态" width="100">
              <template #default="{ row }">
                <el-tag
                  :type="row.pushStatus === 1 ? 'success' : row.pushStatus === 2 ? 'warning' : 'danger'"
                  size="small"
                >
                  {{ getPushStatusText(row.pushStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="重试次数" width="80">
              <template #default="{ row }">
                {{ row.retryCount }}
              </template>
            </el-table-column>
            <el-table-column label="推送时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.pushTime) }}
              </template>
            </el-table-column>
            <el-table-column label="错误信息" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.errorMessage || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="closeDetailDialog">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getMessageList, getMessageDetail, getMessagePushRecords } from '@/api/message'
import { getDictDataByType } from '@/api/dict'
import { getTagNames } from '@/api/tagPushConfig'
import type { MessageRecord, PushRecord, DictData } from '@/types/api'
import { Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { isEmpty } from 'element-plus/es/utils/types.mjs'

const route = useRoute()

// 响应式数据
const loading = ref(false)
const messageList = ref<MessageRecord[]>([])
const selectedMessage = ref<MessageRecord | null>(null)
const pushRecords = ref<PushRecord[]>([])
const detailDialogVisible = ref(false)

// 字典数据
const messageTypeOptions = ref<DictData[]>([])
const messageLevelOptions = ref<DictData[]>([])
const tagOptions = ref<string[]>([])

// 搜索表单
const searchForm = reactive({
  messageType: '',
  timeRange: [] as string[],
  tags: [] as string[]
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 获取字典数据
const fetchDictData = async () => {
  try {
    const [messageTypeResponse, messageLevelResponse] = await Promise.all([
      getDictDataByType('message_type'),
      getDictDataByType('message_level')
    ])
    messageTypeOptions.value = messageTypeResponse.data
    messageLevelOptions.value = messageLevelResponse.data
  } catch (error) {
    console.error('获取字典数据失败:', error)
  }
}

// 获取标签选项
const fetchTagOptions = async () => {
  try {
    const { data } = await getTagNames()
    tagOptions.value = data || []
  } catch (error) {
    console.error('获取标签选项失败:', error)
  }
}

// 获取消息列表
const fetchMessageList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    
    if (searchForm.messageType) {
      params.messageType = searchForm.messageType
    }
    
    if (searchForm.timeRange?.length === 2) {
      params.startTime = searchForm.timeRange[0]
      params.endTime = searchForm.timeRange[1]
    }
    
    if (searchForm.tags?.length > 0) {
      params.tags = searchForm.tags.join(',')
    }
    
    const { data } = await getMessageList(params)
    messageList.value = data.records
    pagination.total = Number(data.total)
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchMessageList()
}

// 重置搜索
const handleReset = () => {
  searchForm.messageType = ''
  searchForm.timeRange = []
  searchForm.tags = []
  pagination.page = 1
  fetchMessageList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  fetchMessageList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchMessageList()
}

// 行点击事件
const handleRowClick = (row: MessageRecord) => {
  viewMessageDetail(row)
}

// 查看消息详情
const viewMessageDetail = async (message: MessageRecord) => {
  try {
    const [detailResponse, pushResponse] = await Promise.all([
      getMessageDetail(message.id),
      getMessagePushRecords(message.id)
    ])
    
    selectedMessage.value = detailResponse.data
    pushRecords.value = pushResponse.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取消息详情失败:', error)
    ElMessage.error('获取消息详情失败')
  }
}

// 关闭详情弹窗
const closeDetailDialog = () => {
  detailDialogVisible.value = false
  selectedMessage.value = null
  pushRecords.value = []
}

// 获取消息类型标签类型
const getMessageTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    notification: '',
    alert: 'warning',
    system: 'info',
    custom: 'success'
  }
  return typeMap[type] || ''
}

// 获取消息类型文本
const getMessageTypeText = (type: string) => {
  const dictItem = messageTypeOptions.value.find(item => item.dictValue === type)
  return dictItem?.dictLabel || type || '未知'
}

// 获取级别标签类型
const getLevelTagType = (level: string) => {
  const levelMap: Record<string, string> = {
    low: 'info',
    normal: '',
    high: 'warning',
    critical: 'danger'
  }
  return levelMap[level] || ''
}

// 获取级别文本
const getLevelText = (level: string) => {
  const dictItem = messageLevelOptions.value.find(item => item.dictValue === level)
  return dictItem?.dictLabel || level || '未知'
}

// 获取平台文本
const getPlatformText = (platform: string) => {
  const platformMap: Record<string, string> = {
    bark: 'Bark',
    email: '邮箱',
    wxpusher: 'WxPusher',
    pushme: 'PushMe'
  }
  return platformMap[platform] || platform
}

// 获取推送状态文本
const getPushStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '失败',
    1: '成功',
    2: '推送中'
  }
  return statusMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (time: string | Date) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 页面加载时获取数据
onMounted(() => {
  // 获取字典数据
  fetchDictData()
  
  // 获取标签选项
  fetchTagOptions()
  
  // 如果URL中有消息ID，直接显示详情
  const messageId = route.query.id
  if (messageId) {
    const id = String(messageId);
    if (!isEmpty(id)) {
      getMessageDetail(id).then(response => {
        viewMessageDetail(response.data)
      }).catch(error => {
        console.error('获取消息详情失败:', error)
      })
    }
  }
  
  fetchMessageList()
})
</script>

<style lang="scss" scoped>
.search-card {
  .el-form {
    margin-bottom: 0;
  }
}

.table-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .toolbar-title {
    font-size: 14px;
    color: #666;
  }
}

.message-title {
  font-weight: 500;
  color: #333;
  
  .message-subtitle {
    font-size: 12px;
    color: #999;
    font-weight: normal;
    margin-top: 2px;
  }
}

.message-content {
  color: #666;
  line-height: 1.4;
}

.message-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  
  .tag-item {
    margin: 0;
  }
  
  .no-tags {
    color: #999;
    font-size: 12px;
    font-style: italic;
  }
}

.push-status {
  .el-tag + .el-tag {
    margin-top: 4px;
  }
}

.text-muted {
  color: #999;
  font-size: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: center;
}

.message-detail {
  .message-content-detail {
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 200px;
    overflow-y: auto;
    padding: 8px;
    background-color: #f8f9fa;
    border-radius: 4px;
  }
  
  .push-records {
    h4 {
      margin-bottom: 12px;
      font-size: 16px;
      color: #333;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .search-card {
    .el-form {
      .el-form-item {
        display: block;
        margin-right: 0;
        margin-bottom: 12px;
        
        .el-select,
        .el-date-picker {
          width: 100%;
        }
      }
    }
  }
  
  .table-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .el-dialog {
    width: 95% !important;
    margin: 0 auto;
  }
}
</style>