<template>
  <div class="page-container">
    <div class="page-header">
      <h1>推送配置</h1>
      <p class="page-description">管理消息推送平台的配置信息</p>
    </div>
    
    <!-- 配置列表 -->
    <el-card>
      <div class="table-toolbar">
        <div class="toolbar-left">
          <span class="toolbar-title">
            共 {{ configList.length }} 个推送配置
          </span>
        </div>
        <div class="toolbar-right">
          <el-button
            type="primary"
            :icon="Plus"
            @click="showAddDialog"
          >
            新增配置
          </el-button>
          <el-button
            :icon="Refresh"
            :loading="loading"
            @click="fetchConfigList"
          >
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="configList"
        stripe
      >
        <el-table-column label="配置名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="config-name">
              <el-icon class="platform-icon">
                <component :is="getPlatformIcon(row.platform)" />
              </el-icon>
              {{ row.configName }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="推送平台" width="100">
          <template #default="{ row }">
            <el-tag :type="getPlatformTagType(row.platform)">
              {{ getPlatformText(row.platform) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="配置信息" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="config-info">
              {{ getConfigSummary(row) }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.isEnabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="排序" width="80">
          <template #default="{ row }">
            {{ row.sortOrder || 0 }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              type="primary"
              size="small"
              :loading="testingId === row.id"
              @click="testConfig(row)"
            >
              测试
            </el-button>
            <el-button
              text
              type="primary"
              size="small"
              @click="editConfig(row)"
            >
              编辑
            </el-button>
            <el-button
              text
              type="danger"
              size="small"
              @click="deleteConfig(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="configList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无推送配置">
          <el-button type="primary" @click="showAddDialog">
            立即创建
          </el-button>
        </el-empty>
      </div>
    </el-card>
    
    <!-- 新增/编辑配置弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="closeDialog"
    >
      <el-form
        ref="configFormRef"
        :model="configForm"
        :rules="configRules"
        label-width="100px"
      >
        <el-form-item label="推送平台" prop="platform">
          <el-select
            v-model="configForm.platform"
            placeholder="请选择推送平台"
            :disabled="isEdit"
            @change="handlePlatformChange"
          >
            <el-option
              v-for="platform in supportedPlatforms"
              :key="platform"
              :label="getPlatformText(platform)"
              :value="platform"
            >
              <div class="platform-option">
                <el-icon><component :is="getPlatformIcon(platform)" /></el-icon>
                <span>{{ getPlatformText(platform) }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="配置名称" prop="configName">
          <el-input
            v-model="configForm.configName"
            placeholder="请输入配置名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <!-- Bark配置 -->
        <template v-if="configForm.platform === 'bark'">
          <el-form-item label="设备Key" prop="bark.deviceKey">
            <el-input
              v-model="configForm.bark.deviceKey"
              placeholder="请输入Bark设备Key"
              show-password
            />
          </el-form-item>
          <el-form-item label="服务器地址">
            <el-input
              v-model="configForm.bark.serverUrl"
              placeholder="默认使用官方服务器"
            />
          </el-form-item>
          <el-form-item label="默认声音">
            <el-input
              v-model="configForm.bark.sound"
              placeholder="推送声音名称"
            />
          </el-form-item>
          <el-form-item label="分组">
            <el-input
              v-model="configForm.bark.group"
              placeholder="消息分组"
            />
          </el-form-item>
          <el-form-item label="图标">
            <el-input
              v-model="configForm.bark.icon"
              placeholder="图标URL"
            />
          </el-form-item>
        </template>
        
        <!-- 邮箱配置 -->
        <template v-if="configForm.platform === 'email'">
          <el-form-item label="收件邮箱" prop="email.toEmail">
            <el-input
              v-model="configForm.email.toEmail"
              placeholder="请输入收件邮箱地址"
            />
          </el-form-item>
          <el-form-item label="主题前缀">
            <el-input
              v-model="configForm.email.subjectPrefix"
              placeholder="邮件主题前缀"
            />
          </el-form-item>
        </template>
        
        <!-- WxPusher配置 -->
        <template v-if="configForm.platform === 'wxpusher'">
          <el-form-item label="应用Token" prop="wxpusher.appToken">
            <el-input
              v-model="configForm.wxpusher.appToken"
              placeholder="请输入WxPusher应用Token"
              show-password
            />
          </el-form-item>
          <el-form-item label="用户UID">
            <el-input
              v-model="configForm.wxpusher.uid"
              placeholder="用户UID（与主题ID二选一）"
            />
          </el-form-item>
          <el-form-item label="主题ID">
            <el-input
              v-model="configForm.wxpusher.topicId"
              placeholder="主题ID（与用户UID二选一）"
            />
          </el-form-item>
          <el-form-item label="摘要长度">
            <el-input-number
              v-model="configForm.wxpusher.summaryLength"
              :min="10"
              :max="100"
              placeholder="消息摘要长度"
            />
          </el-form-item>
          <el-form-item label="内容类型">
            <el-select v-model="configForm.wxpusher.contentType">
              <el-option label="纯文本" :value="1" />
              <el-option label="HTML" :value="2" />
              <el-option label="Markdown" :value="3" />
            </el-select>
          </el-form-item>
        </template>
        
        <!-- PushMe配置 -->
        <template v-if="configForm.platform === 'pushme'">
          <el-form-item label="推送Key" prop="pushme.pushKey">
            <el-input
              v-model="configForm.pushme.pushKey"
              placeholder="请输入PushMe推送Key"
              show-password
            />
          </el-form-item>
          <el-form-item label="推送模板">
            <el-input
              v-model="configForm.pushme.template"
              placeholder="推送模板（可选）"
            />
          </el-form-item>
        </template>
        
        <el-form-item label="排序">
          <el-input-number
            v-model="configForm.sortOrder"
            :min="0"
            :max="999"
            placeholder="数值越小越靠前"
          />
        </el-form-item>
        
        <el-form-item label="启用状态">
          <el-switch
            v-model="configForm.isEnabled"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="configForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="submitConfig"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getPushConfigList,
  createPushConfig,
  updatePushConfig,
  deletePushConfig,
  togglePushConfig,
  testPushConfig,
  getSupportedPlatforms
} from '@/api/pushConfig'
import type { PushConfig } from '@/types/api'
import {
  Plus,
  Refresh,
  Message,
  Postcard,
  ChatDotSquare,
  Notification
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const configList = ref<PushConfig[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const testingId = ref<number | null>(null)
const supportedPlatforms = ref<string[]>([])

// 表单相关
const configFormRef = ref<FormInstance>()
const configForm = reactive<PushConfig>({
  platform: '',
  configName: '',
  isEnabled: 1,
  sortOrder: 0,
  remark: '',
  bark: {
    deviceKey: '',
    serverUrl: '',
    sound: '',
    group: '',
    icon: ''
  },
  email: {
    toEmail: '',
    subjectPrefix: ''
  },
  wxpusher: {
    appToken: '',
    uid: '',
    topicId: '',
    summaryLength: 50,
    contentType: 1
  },
  pushme: {
    pushKey: '',
    template: ''
  }
})

// 表单验证规则
const configRules: FormRules = {
  platform: [{ required: true, message: '请选择推送平台', trigger: 'change' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  'bark.deviceKey': [{ required: true, message: '请输入Bark设备Key', trigger: 'blur' }],
  'email.toEmail': [
    { required: true, message: '请输入收件邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  'wxpusher.appToken': [{ required: true, message: '请输入WxPusher应用Token', trigger: 'blur' }],
  'pushme.pushKey': [{ required: true, message: '请输入PushMe推送Key', trigger: 'blur' }]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑配置' : '新增配置')

// 获取配置列表
const fetchConfigList = async () => {
  loading.value = true
  try {
    const { data } = await getPushConfigList()
    configList.value = data
  } catch (error) {
    console.error('获取推送配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取支持的平台列表
const fetchSupportedPlatforms = async () => {
  try {
    const { data } = await getSupportedPlatforms()
    supportedPlatforms.value = data
  } catch (error) {
    console.error('获取支持的平台失败:', error)
    supportedPlatforms.value = ['bark', 'email', 'wxpusher', 'pushme']
  }
}

// 显示新增弹窗
const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑配置
const editConfig = (config: PushConfig) => {
  isEdit.value = true
  Object.assign(configForm, config)
  dialogVisible.value = true
}

// 删除配置
const deleteConfig = (config: PushConfig) => {
  ElMessageBox.confirm(
    `确定要删除配置"${config.configName}"吗？`,
    '确认删除',
    {
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deletePushConfig(config.id!)
      ElMessage.success('删除成功')
      fetchConfigList()
    } catch (error) {
      console.error('删除配置失败:', error)
    }
  })
}

// 状态切换
const handleStatusChange = async (config: PushConfig) => {
  try {
    await togglePushConfig(config.id!, config.isEnabled === 1)
    ElMessage.success(`已${config.isEnabled === 1 ? '启用' : '禁用'}配置`)
  } catch (error) {
    console.error('切换状态失败:', error)
    // 恢复原状态
    config.isEnabled = config.isEnabled === 1 ? 0 : 1
  }
}

// 测试配置
const testConfig = async (config: PushConfig) => {
  testingId.value = config.id!
  try {
    await testPushConfig(config.id!)
    ElMessage.success('测试消息已发送，请检查您的设备')
  } catch (error) {
    console.error('测试配置失败:', error)
  } finally {
    testingId.value = null
  }
}

// 平台改变
const handlePlatformChange = () => {
  // 重置平台特定的配置
  configForm.bark = { deviceKey: '', serverUrl: '', sound: '', group: '', icon: '' }
  configForm.email = { toEmail: '', subjectPrefix: '' }
  configForm.wxpusher = { appToken: '', uid: '', topicId: '', summaryLength: 50, contentType: 1 }
  configForm.pushme = { pushKey: '', template: '' }
}

// 提交配置
const submitConfig = async () => {
  if (!configFormRef.value) return

  try {
    await configFormRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      await updatePushConfig(configForm)
      ElMessage.success('更新成功')
    } else {
      await createPushConfig(configForm)
      ElMessage.success('创建成功')
    }

    closeDialog()
    fetchConfigList()
  } catch (error) {
    console.error('保存配置失败:', error)
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
const closeDialog = () => {
  dialogVisible.value = false
  nextTick(() => {
    resetForm()
  })
}

// 重置表单
const resetForm = () => {
  if (configFormRef.value) {
    configFormRef.value.resetFields()
  }
  Object.assign(configForm, {
    id: undefined,
    platform: '',
    configName: '',
    isEnabled: 1,
    sortOrder: 0,
    remark: '',
    bark: { deviceKey: '', serverUrl: '', sound: '', group: '', icon: '' },
    email: { toEmail: '', subjectPrefix: '' },
    wxpusher: { appToken: '', uid: '', topicId: '', summaryLength: 50, contentType: 1 },
    pushme: { pushKey: '', template: '' }
  })
}

// 获取平台图标
const getPlatformIcon = (platform: string) => {
  const iconMap: Record<string, any> = {
    bark: Message,
    email: Postcard,
    wxpusher: ChatDotSquare,
    pushme: Notification
  }
  return iconMap[platform] || Message
}

// 获取平台标签类型
const getPlatformTagType = (platform: string) => {
  const typeMap: Record<string, string> = {
    bark: 'primary',
    email: 'success',
    wxpusher: 'warning',
    pushme: 'info'
  }
  return typeMap[platform] || ''
}

// 获取平台文本
const getPlatformText = (platform: string) => {
  const textMap: Record<string, string> = {
    bark: 'Bark',
    email: '邮箱',
    wxpusher: 'WxPusher',
    pushme: 'PushMe'
  }
  return textMap[platform] || platform
}

// 获取配置摘要
const getConfigSummary = (config: PushConfig) => {
  switch (config.platform) {
    case 'bark':
      return config.bark?.deviceKey ? `设备Key: ${config.bark.deviceKey.substring(0, 8)}****` : '未配置'
    case 'email':
      return config.email?.toEmail || '未配置'
    case 'wxpusher':
      return config.wxpusher?.appToken ? `Token: ${config.wxpusher.appToken.substring(0, 8)}****` : '未配置'
    case 'pushme':
      return config.pushme?.pushKey ? `Key: ${config.pushme.pushKey.substring(0, 8)}****` : '未配置'
    default:
      return '未配置'
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchConfigList()
  fetchSupportedPlatforms()
})
</script>

<style lang="scss" scoped>
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

.config-name {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .platform-icon {
    color: var(--el-color-primary);
  }
}

.config-info {
  font-size: 13px;
  color: #666;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.platform-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 响应式设计
@media (max-width: 768px) {
  .table-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .el-dialog {
    width: 95% !important;
    margin: 0 auto;
  }
  
  .el-table {
    font-size: 12px;
    
    .el-table-column {
      &:nth-child(n+4) {
        display: none;
      }
    }
  }
}
</style>