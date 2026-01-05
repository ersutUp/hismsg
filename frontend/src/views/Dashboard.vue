<template>
  <div class="page-container">
    <div class="page-header">
      <h1>仪表板</h1>
      <p class="page-description">查看系统概览和消息统计</p>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon success">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
            <div class="stat-label">总消息数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon primary">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.typeStats?.notification || 0 }}</div>
            <div class="stat-label">通知消息</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon warning">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.typeStats?.alert || 0 }}</div>
            <div class="stat-label">告警消息</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon info">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ pushConfigCount }}</div>
            <div class="stat-label">推送配置</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 主要内容 -->
    <el-row :gutter="20" class="main-content">
      <el-col :xs="24" :lg="16">
        <!-- 最近消息 -->
        <el-card class="recent-messages">
          <template #header>
            <div class="card-header">
              <span>最近消息</span>
              <el-link type="primary" @click="$router.push('/messages')">
                查看更多
              </el-link>
            </div>
          </template>
          
          <div v-if="loading" class="text-center p-4">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span class="ml-2">加载中...</span>
          </div>
          
          <div v-else-if="recentMessages.length === 0" class="empty-state">
            <el-empty description="暂无消息记录" />
          </div>
          
          <div v-else class="message-list">
            <div
              v-for="message in recentMessages"
              :key="message.id"
              class="message-item"
              @click="goToMessageDetail(message.id)"
            >
              <div class="message-left">
                <el-tag
                  :type="getMessageTypeTagType(message.messageType)"
                  size="small"
                  class="message-type"
                >
                  {{ getMessageTypeText(message.messageType) }}
                </el-tag>
                <div class="message-info">
                  <div class="message-title">{{ message.title }}</div>
                  <div class="message-content">{{ message.content }}</div>
                </div>
              </div>
              <div class="message-right">
                <div class="message-time">
                  {{ formatTime(message.createTime) }}
                </div>
                <el-tag
                  v-if="message.pushSuccessCount > 0"
                  type="success"
                  size="small"
                >
                  已推送
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <!-- 快速操作 -->
        <el-card class="quick-actions mb-4">
          <template #header>
            <span>快速操作</span>
          </template>
          
          <div class="action-buttons">
            <el-button
              type="primary"
              :icon="Setting"
              @click="$router.push('/push-config')"
            >
              配置推送
            </el-button>
            <el-button
              type="success"
              :icon="View"
              @click="$router.push('/messages')"
            >
              查看消息
            </el-button>
            <el-button
              v-if="userStore.currentUser?.username === 'admin'"
              type="warning"
              :icon="Collection"
              @click="$router.push('/dict-management')"
            >
              字典管理
            </el-button>
          </div>
        </el-card>
        
        <!-- 系统信息 -->
        <el-card class="system-info">
          <template #header>
            <span>系统信息</span>
          </template>
          
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">当前用户：</span>
              <span class="info-value">{{ userStore.currentUser?.username }}</span>
            </div>
            <div class="info-item" v-if="userStore.currentUser?.userKey">
              <span class="info-label">用户密钥：</span>
              <span class="info-value user-key" @click="copyUserKey">
                {{ userStore.currentUser.userKey }}
                <el-icon class="copy-icon"><CopyDocument /></el-icon>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">系统版本：</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后登录：</span>
              <span class="info-value">{{ formatTime(new Date()) }}</span>
            </div>
            <div class="info-item">
              <el-button 
                type="primary" 
                size="small" 
                text 
                @click="$router.push('/change-password')"
                class="change-password-btn"
              >
                <el-icon><Lock /></el-icon>
                修改密码
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getMessageStatistics, getMessageList } from '@/api/message'
import { getPushConfigList } from '@/api/pushConfig'
import { resetUserKey } from '@/api/auth'
import type { MessageRecord } from '@/types/api'
import {
  ChatDotRound,
  Bell,
  Warning,
  Tools,
  Loading,
  Setting,
  View,
  Collection,
  CopyDocument,
  Lock,
  Refresh
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const resetKeyLoading = ref(false)
const statistics = ref<any>({})
const recentMessages = ref<MessageRecord[]>([])
const pushConfigCount = ref(0)

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const { data } = await getMessageStatistics(7)
    statistics.value = data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取最近消息
const fetchRecentMessages = async () => {
  loading.value = true
  try {
    const { data } = await getMessageList({ page: 1, size: 5 })
    recentMessages.value = data.records
  } catch (error) {
    console.error('获取最近消息失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取推送配置数量
const fetchPushConfigCount = async () => {
  try {
    const { data } = await getPushConfigList()
    pushConfigCount.value = data.length
  } catch (error) {
    console.error('获取推送配置数量失败:', error)
  }
}

// 跳转到消息详情
const goToMessageDetail = (id: string) => {
  debugger
  router.push(`/messages?id=${id}`)
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
  const typeMap: Record<string, string> = {
    notification: '通知',
    alert: '告警',
    system: '系统',
    custom: '自定义'
  }
  return typeMap[type] || '未知'
}

// 复制用户密钥
const copyUserKey = async () => {
  if (!userStore.currentUser?.userKey) return
  
  try {
    await navigator.clipboard.writeText(userStore.currentUser.userKey)
    ElMessage.success('用户密钥已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

// 重置用户密钥
const handleResetUserKey = async () => {
  try {
    await ElMessageBox.confirm(
      '重置用户密钥后，之前使用的密钥将失效，需要重新配置推送服务。确定要重置吗？',
      '确认重置用户密钥',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    resetKeyLoading.value = true

    // 调用重置密钥接口
    const { data: newUserKey } = await resetUserKey()

    // 更新用户store中的密钥
    if (userStore.currentUser) {
      userStore.updateUser({ userKey: newUserKey })
    }

    ElMessage.success('用户密钥重置成功')

  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '重置用户密钥失败')
    }
  } finally {
    resetKeyLoading.value = false
  }
}

// 格式化时间
const formatTime = (time: string | Date) => {
  return dayjs(time).format('MM-DD HH:mm')
}

// 页面加载时获取数据
onMounted(() => {
  fetchStatistics()
  fetchRecentMessages()
  fetchPushConfigCount()
})
</script>

<style lang="scss" scoped>
.stats-cards {
  margin-bottom: 20px;
  
  .stat-card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    gap: 16px;
    transition: transform 0.2s, box-shadow 0.2s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: #fff;
      
      &.primary { background: linear-gradient(135deg, #409eff, #79bbff); }
      &.success { background: linear-gradient(135deg, #67c23a, #95d475); }
      &.warning { background: linear-gradient(135deg, #e6a23c, #ebb563); }
      &.info { background: linear-gradient(135deg, #909399, #b1b3b8); }
    }
    
    .stat-content {
      flex: 1;
      
      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #333;
        line-height: 1;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #666;
      }
    }
  }
}

.main-content {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .recent-messages {
    min-height: 400px;
    
    .message-list {
      .message-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: background-color 0.2s;
        
        &:hover {
          background-color: #f8f9fa;
          border-radius: 8px;
          padding-left: 8px;
          padding-right: 8px;
        }
        
        &:last-child {
          border-bottom: none;
        }
        
        .message-left {
          display: flex;
          align-items: flex-start;
          gap: 12px;
          flex: 1;
          
          .message-type {
            margin-top: 2px;
          }
          
          .message-info {
            flex: 1;
            min-width: 0;
            
            .message-title {
              font-size: 16px;
              font-weight: 500;
              color: #333;
              margin-bottom: 4px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            
            .message-content {
              font-size: 14px;
              color: #666;
              line-height: 1.4;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;
            }
          }
        }
        
        .message-right {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 4px;
          
          .message-time {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }
    
    .empty-state {
      padding: 40px 0;
    }
  }
  
  .quick-actions {
    .action-buttons {
      display: flex;
      flex-direction: column;
      gap: 12px;
      
      .el-button {
        justify-content: flex-start;
      }
    }
  }
  
  .system-info {
    .info-list {
      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;
        
        .info-label {
          font-size: 14px;
          color: #666;
        }
        
        .info-value {
          font-size: 14px;
          color: #333;
          font-weight: 500;
          
          &.user-key {
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 4px 8px;
            border-radius: 4px;
            background-color: #f5f7fa;
            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
            font-size: 12px;
            transition: all 0.2s;
            
            &:hover {
              background-color: #ecf5ff;
              color: #409eff;
            }
            
            .copy-icon {
              font-size: 14px;
              opacity: 0.6;
              transition: opacity 0.2s;
            }
            
            &:hover .copy-icon {
              opacity: 1;
            }
          }
        }
        
        .user-key-container {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 8px;
          
          .reset-key-btn {
            align-self: stretch;
            justify-content: center;
          }
        }
        
        .change-password-btn {
          width: 100%;
          justify-content: center;
          margin-top: 8px;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .stats-cards {
    .el-col {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .stat-card {
      padding: 16px;

      .stat-icon {
        width: 48px;
        height: 48px;
        font-size: 20px;
      }

      .stat-value {
        font-size: 24px;
      }
    }
  }

  .main-content {
    .el-col {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .message-item {
    .message-left {
      gap: 8px;

      .message-info {
        .message-title {
          font-size: 14px;
        }

        .message-content {
          font-size: 12px;
        }
      }
    }
  }
}
</style>