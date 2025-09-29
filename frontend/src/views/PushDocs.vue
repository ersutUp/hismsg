<template>
  <div class="page-container">
    <div class="page-header">
      <h1>推送接口文档</h1>
      <p class="page-description">消息推送API使用说明，兼容Bark格式</p>
    </div>

    <!-- 快速开始 -->
    <el-card class="doc-section">
      <template #header>
        <div class="section-header">
          <el-icon><Star /></el-icon>
          <span>快速开始</span>
        </div>
      </template>
      
      <div class="quick-start">
        <div class="user-key-display">
          <el-alert
            title="您的推送密钥"
            type="info"
            :closable="false"
          >
            <template #default>
              <div class="key-content">
                <span class="key-label">UserKey:</span>
                <el-input
                  v-model="userKey"
                  readonly
                  class="key-input"
                >
                  <template #append>
                    <el-button @click="copyUserKey" :icon="CopyDocument">
                      复制
                    </el-button>
                  </template>
                </el-input>
              </div>
              <p class="key-desc">请妥善保管您的推送密钥，不要泄露给他人</p>
            </template>
          </el-alert>
        </div>
        
        <div class="quick-example">
          <h4>快速测试</h4>
          <p>复制以下命令到终端执行，即可发送一条测试消息：</p>
          <div class="code-block">
            <el-input
              v-model="quickTestCommand"
              type="textarea"
              :rows="2"
              readonly
              class="code-input"
            />
            <el-button 
              class="copy-btn" 
              size="small" 
              @click="copyText(quickTestCommand)"
              :icon="CopyDocument"
            >
              复制
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- GET 请求接口 -->
    <el-card class="doc-section">
      <template #header>
        <div class="section-header">
          <el-icon><Link /></el-icon>
          <span>GET 请求接口</span>
        </div>
      </template>
      
      <div class="api-docs">
        <h4>支持的URL格式</h4>
        <div class="url-formats">
          <div class="url-item" v-for="format in getUrlFormats" :key="format.path">
            <div class="url-path">
              <el-tag type="success">GET</el-tag>
              <code>{{ format.path }}</code>
            </div>
            <p class="url-desc">{{ format.description }}</p>
          </div>
        </div>
        
        <h4>查询参数（可选）</h4>
        <el-table :data="queryParams" class="params-table">
          <el-table-column prop="name" label="参数名" width="120" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="required" label="必填" width="80">
            <template #default="{ row }">
              <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                {{ row.required ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" />
          <el-table-column prop="example" label="示例值" width="150" />
        </el-table>
        
        <h4>cURL 示例</h4>
        <div class="curl-examples">
          <div v-for="example in getCurlExamples" :key="example.title" class="curl-example">
            <h5>{{ example.title }}</h5>
            <div class="code-block">
              <el-input
                v-model="example.command"
                type="textarea"
                :rows="example.rows"
                readonly
                class="code-input"
              />
              <el-button 
                class="copy-btn" 
                size="small" 
                @click="copyText(example.command)"
                :icon="CopyDocument"
              >
                复制
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- POST 请求接口 -->
    <el-card class="doc-section">
      <template #header>
        <div class="section-header">
          <el-icon><Document /></el-icon>
          <span>POST 请求接口</span>
        </div>
      </template>
      
      <div class="api-docs">
        <h4>Bark兼容格式</h4>
        <div class="url-item">
          <div class="url-path">
            <el-tag type="warning">POST</el-tag>
            <code>{{ baseUrl }}/api/message/push/{userKey}</code>
          </div>
          <p class="url-desc">支持更多参数的POST请求，请求体为JSON格式</p>
        </div>
        
        <h4>通用推送接口</h4>
        <div class="url-item">
          <div class="url-path">
            <el-tag type="warning">POST</el-tag>
            <code>{{ baseUrl }}/api/message/push/send</code>
          </div>
          <p class="url-desc">完整功能的通用推送接口，userKey在请求体中传递</p>
        </div>
        
        <h4>请求体参数</h4>
        <el-table :data="postParams" class="params-table">
          <el-table-column prop="name" label="参数名" width="120" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="required" label="必填" width="80">
            <template #default="{ row }">
              <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                {{ row.required ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" />
          <el-table-column prop="example" label="示例值" width="150" />
        </el-table>
        
        <h4>cURL 示例</h4>
        <div class="curl-examples">
          <div v-for="example in postCurlExamples" :key="example.title" class="curl-example">
            <h5>{{ example.title }}</h5>
            <div class="code-block">
              <el-input
                v-model="example.command"
                type="textarea"
                :rows="example.rows"
                readonly
                class="code-input"
              />
              <el-button 
                class="copy-btn" 
                size="small" 
                @click="copyText(example.command)"
                :icon="CopyDocument"
              >
                复制
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 响应格式 -->
    <el-card class="doc-section">
      <template #header>
        <div class="section-header">
          <el-icon><DataLine /></el-icon>
          <span>响应格式</span>
        </div>
      </template>
      
      <div class="response-docs">
        <h4>成功响应</h4>
        <div class="code-block">
          <el-input
            v-model="successResponse"
            type="textarea"
            :rows="8"
            readonly
            class="code-input"
          />
          <el-button 
            class="copy-btn" 
            size="small" 
            @click="copyText(successResponse)"
            :icon="CopyDocument"
          >
            复制
          </el-button>
        </div>
        
        <h4>错误响应</h4>
        <div class="code-block">
          <el-input
            v-model="errorResponse"
            type="textarea"
            :rows="6"
            readonly
            class="code-input"
          />
          <el-button 
            class="copy-btn" 
            size="small" 
            @click="copyText(errorResponse)"
            :icon="CopyDocument"
          >
            复制
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 消息级别说明 -->
    <el-card class="doc-section">
      <template #header>
        <div class="section-header">
          <el-icon><Bell /></el-icon>
          <span>消息级别说明</span>
        </div>
      </template>
      
      <div class="level-docs">
        <el-table :data="messageLevels" class="params-table">
          <el-table-column prop="bark" label="Bark级别" width="120" />
          <el-table-column prop="system" label="系统级别" width="120" />
          <el-table-column prop="description" label="说明" />
          <el-table-column prop="behavior" label="推送行为" />
        </el-table>
      </div>
    </el-card>

    <!-- 注意事项 -->
    <el-card class="doc-section">
      <template #header>
        <div class="section-header">
          <el-icon><WarningFilled /></el-icon>
          <span>注意事项</span>
        </div>
      </template>
      
      <div class="notes">
        <el-alert
          v-for="note in importantNotes"
          :key="note.title"
          :title="note.title"
          :type="note.type"
          :closable="false"
          class="note-item"
        >
          <template #default>
            <p>{{ note.content }}</p>
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  Star,
  Link,
  Document,
  DataLine,
  Bell,
  WarningFilled,
  CopyDocument
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 基础URL
const baseUrl = window.location.origin

// 用户密钥
const userKey = computed(() => userStore.currentUser?.userKey || 'YOUR_USER_KEY')

// 快速测试命令
const quickTestCommand = computed(() => 
  `curl "${baseUrl}/api/message/push/${userKey.value}/快速测试/这是一条测试消息"`
)

// GET请求URL格式
const getUrlFormats = computed(() => [
  {
    path: `${baseUrl}/api/message/push/{userKey}`,
    description: '最简单的形式，只发送一个空消息'
  },
  {
    path: `${baseUrl}/api/message/push/{userKey}/{title}`,
    description: '发送带标题的消息'
  },
  {
    path: `${baseUrl}/api/message/push/{userKey}/{title}/{content}`,
    description: '发送带标题和内容的消息'
  },
  {
    path: `${baseUrl}/api/message/push/{userKey}/{title}/{subtitle}/{content}`,
    description: '发送带标题、副标题和内容的完整消息'
  }
])

// 查询参数
const queryParams = ref([
  { name: 'title', type: 'string', required: false, description: '消息标题（优先级高于URL路径中的标题）', example: '重要通知' },
  { name: 'body', type: 'string', required: false, description: '消息内容（优先级高于URL路径中的内容）', example: '系统升级完成' },
  { name: 'url', type: 'string', required: false, description: '点击消息跳转的URL', example: 'https://example.com' },
  { name: 'level', type: 'string', required: false, description: '消息级别', example: 'active' },
  { name: 'group', type: 'string', required: false, description: '消息分组', example: 'work' },
  { name: 'icon', type: 'string', required: false, description: '消息图标URL', example: 'https://example.com/icon.png' },
  { name: 'sound', type: 'string', required: false, description: '推送声音', example: 'bell' },
  { name: 'category', type: 'string', required: false, description: '消息类型', example: 'notification' }
])

// GET请求cURL示例
const getCurlExamples = computed(() => [
  {
    title: '发送简单消息',
    rows: 2,
    command: `curl "${baseUrl}/api/message/push/${userKey.value}/Hello/World"`
  },
  {
    title: '发送带查询参数的消息',
    rows: 3,
    command: `curl "${baseUrl}/api/message/push/${userKey.value}?title=系统通知&body=服务器维护完成&level=active&url=https://example.com"`
  },
  {
    title: '发送完整格式消息',
    rows: 2,
    command: `curl "${baseUrl}/api/message/push/${userKey.value}/系统通知/维护通知/服务器维护已完成，系统恢复正常"`
  }
])

// POST请求参数
const postParams = ref([
  { name: 'userKey', type: 'string', required: true, description: '用户推送密钥（仅通用接口需要）', example: userKey.value },
  { name: 'title', type: 'string', required: false, description: '消息标题', example: '重要通知' },
  { name: 'body', type: 'string', required: false, description: '消息内容', example: '系统升级完成' },
  { name: 'content', type: 'string', required: false, description: '消息内容（与body同义）', example: '系统升级完成' },
  { name: 'subtitle', type: 'string', required: false, description: '消息副标题', example: '维护通知' },
  { name: 'url', type: 'string', required: false, description: '点击消息跳转的URL', example: 'https://example.com' },
  { name: 'level', type: 'string', required: false, description: '消息级别', example: 'active' },
  { name: 'category', type: 'string', required: false, description: '消息类型', example: 'notification' },
  { name: 'group', type: 'string', required: false, description: '消息分组', example: 'work' },
  { name: 'tags', type: 'array', required: false, description: '消息标签数组', example: '["urgent", "system"]' }
])

// POST请求cURL示例
const postCurlExamples = computed(() => [
  {
    title: 'Bark兼容格式 - 简单消息',
    rows: 6,
    command: `curl -X POST "${baseUrl}/api/message/push/${userKey.value}" \\
  -H "Content-Type: application/json" \\
  -d '{
    "title": "系统通知",
    "body": "服务器维护完成"
  }'`
  },
  {
    title: 'Bark兼容格式 - 完整消息',
    rows: 10,
    command: `curl -X POST "${baseUrl}/api/message/push/${userKey.value}" \\
  -H "Content-Type: application/json" \\
  -d '{
    "title": "系统通知",
    "body": "服务器维护完成，所有服务已恢复正常",
    "url": "https://example.com/status",
    "level": "active",
    "category": "system"
  }'`
  },
  {
    title: '通用推送接口',
    rows: 12,
    command: `curl -X POST "${baseUrl}/api/message/push/send" \\
  -H "Content-Type: application/json" \\
  -d '{
    "userKey": "${userKey.value}",
    "title": "重要通知",
    "content": "这是一条重要的系统通知",
    "subtitle": "系统维护",
    "level": "high",
    "tags": ["system", "maintenance"]
  }'`
  }
])

// 响应格式
const successResponse = ref(`{
  "code": 200,
  "message": "推送成功",
  "data": {
    "timestamp": 1640995200,
    "messageId": 123456789
  }
}`)

const errorResponse = ref(`{
  "code": 400,
  "message": "用户密钥不能为空",
  "data": null
}`)

// 消息级别
const messageLevels = ref([
  { bark: 'passive', system: 'low', description: '被动推送，不会主动提醒', behavior: '静默推送到通知中心' },
  { bark: 'active', system: 'normal', description: '主动推送，正常提醒', behavior: '显示通知和声音提醒' },
  { bark: 'timeSensitive', system: 'high', description: '时效性推送，重要提醒', behavior: '突破免打扰模式推送' },
  { bark: 'critical', system: 'critical', description: '紧急推送，最高优先级', behavior: '强制推送，忽略所有设置' }
])

// 注意事项
const importantNotes = ref([
  {
    title: '安全性',
    type: 'warning',
    content: '请妥善保管您的用户密钥，不要在公共场所或不安全的网络环境中使用。建议定期更换密钥。'
  },
  {
    title: 'URL编码',
    type: 'info',
    content: 'GET请求中的中文字符和特殊符号需要进行URL编码。建议使用POST请求发送包含特殊字符的消息。'
  },
  {
    title: '推送限制',
    type: 'warning',
    content: '为防止滥用，系统对推送频率有一定限制。频繁推送可能会被暂时限制服务。'
  },
  {
    title: 'Bark兼容性',
    type: 'success',
    content: '本接口完全兼容Bark客户端的推送格式，您可以直接在Bark客户端中使用我们的服务器地址。'
  }
])

// 复制文本到剪贴板
const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

// 复制用户密钥
const copyUserKey = async () => {
  if (!userStore.currentUser?.userKey) {
    ElMessage.warning('用户密钥为空')
    return
  }
  
  try {
    await navigator.clipboard.writeText(userStore.currentUser.userKey)
    ElMessage.success('用户密钥已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

onMounted(() => {
  // 页面加载完成
})
</script>

<style lang="scss" scoped>
.doc-section {
  margin-bottom: 24px;
  
  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
  }
}

.quick-start {
  .user-key-display {
    margin-bottom: 24px;
    
    .key-content {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      
      .key-label {
        font-weight: 600;
        min-width: 80px;
      }
      
      .key-input {
        flex: 1;
        max-width: 500px;
      }
    }
    
    .key-desc {
      margin: 0;
      font-size: 12px;
      color: #909399;
    }
  }
  
  .quick-example {
    h4 {
      margin: 0 0 12px 0;
      color: #333;
    }
    
    p {
      margin: 0 0 12px 0;
      color: #666;
    }
  }
}

.api-docs {
  h4 {
    margin: 0 0 16px 0;
    color: #333;
    font-size: 16px;
  }
  
  .url-formats {
    margin-bottom: 24px;
  }
  
  .url-item {
    margin-bottom: 16px;
    padding: 16px;
    background-color: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #409eff;
    
    .url-path {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      
      code {
        background-color: #f1f2f3;
        padding: 4px 8px;
        border-radius: 4px;
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
        font-size: 14px;
        color: #e74c3c;
      }
    }
    
    .url-desc {
      margin: 0;
      color: #666;
      font-size: 14px;
    }
  }
  
  .params-table {
    margin: 16px 0 24px 0;
  }
  
  .curl-examples {
    .curl-example {
      margin-bottom: 24px;
      
      h5 {
        margin: 0 0 12px 0;
        color: #333;
        font-size: 14px;
        font-weight: 600;
      }
    }
  }
}

.code-block {
  position: relative;
  margin-bottom: 16px;
  
  .code-input {
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    
    :deep(.el-textarea__inner) {
      background-color: #f6f8fa;
      border: 1px solid #e1e4e8;
      font-family: inherit;
      font-size: 13px;
      line-height: 1.4;
    }
  }
  
  .copy-btn {
    position: absolute;
    top: 8px;
    right: 8px;
    z-index: 10;
  }
}

.response-docs {
  h4 {
    margin: 0 0 16px 0;
    color: #333;
    font-size: 16px;
  }
}

.level-docs {
  .params-table {
    margin: 0;
  }
}

.notes {
  .note-item {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    p {
      margin: 0;
      line-height: 1.6;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .quick-start {
    .user-key-display {
      .key-content {
        flex-direction: column;
        align-items: stretch;
        gap: 8px;
        
        .key-label {
          min-width: auto;
        }
        
        .key-input {
          max-width: none;
        }
      }
    }
  }
  
  .url-item {
    .url-path {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
      
      code {
        word-break: break-all;
      }
    }
  }
  
  .code-block {
    .copy-btn {
      position: static;
      margin-top: 8px;
      width: 100%;
    }
  }
}
</style>