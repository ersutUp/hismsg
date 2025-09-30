<template>
  <div class="page-container">
    <div class="page-header">
      <h1>标签推送配置</h1>
      <p class="page-description">配置消息标签对应的推送平台，实现精准推送</p>
    </div>
    
    <!-- 搜索和操作 -->
    <el-card class="search-card mb-3">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <span class="toolbar-title">
            共 {{ tagConfigs.length }} 个标签配置
          </span>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" :icon="Plus" @click="openDialog()">
            新增配置
          </el-button>
          <el-button :icon="Refresh" :loading="loading" @click="fetchData">
            刷新
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 配置列表 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="tagConfigs"
        stripe
        style="width: 100%"
      >
        <el-table-column label="标签名称" width="150">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.tagName }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="推送配置" min-width="300">
          <template #default="{ row }">
            <div class="push-configs">
              <el-tag
                v-for="config in getSelectedConfigs(row.pushConfigIds)"
                :key="config.id"
                :type="getPlatformTagType(config.platform)"
                size="small"
                class="config-tag"
              >
                {{ config.configName }} ({{ getPlatformName(config.platform) }})
              </el-tag>
              <span v-if="!row.pushConfigIds || row.pushConfigIds.length === 0" class="no-configs">
                未配置推送平台
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isEnabled === 1 ? 'success' : 'danger'" size="small">
              {{ row.isEnabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              type="primary"
              size="small"
              @click="openDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              text
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑标签配置' : '新增标签配置'"
      width="600px"
      :before-close="closeDialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="标签名称" prop="tagName">
          <el-input
            v-model="formData.tagName"
            placeholder="请输入标签名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="推送配置" prop="pushConfigIds">
          <el-select
            v-model="formData.pushConfigIds"
            placeholder="请选择推送配置"
            multiple
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="config in userPushConfigs"
              :key="config.id"
              :label="`${config.configName} (${getPlatformName(config.platform)})`"
              :value="config.id"
              :disabled="config.isEnabled !== 1"
            >
              <div class="config-option">
                <span>{{ config.configName }}</span>
                <el-tag
                  :type="getPlatformTagType(config.platform)"
                  size="small"
                  class="ml-2"
                >
                  {{ getPlatformName(config.platform) }}
                </el-tag>
                <el-tag
                  v-if="config.isEnabled !== 1"
                  type="info"
                  size="small"
                  class="ml-1"
                >
                  已禁用
                </el-tag>
              </div>
            </el-option>
          </el-select>
          <div class="form-tip">
            选择该标签对应的具体推送配置，可多选
          </div>
        </el-form-item>
        
        <el-form-item label="状态" prop="isEnabled">
          <el-radio-group v-model="formData.isEnabled">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  getTagPushConfigList,
  getUserPushConfigs,
  saveTagPushConfig,
  deleteTagPushConfig,
  type TagPushConfig,
  type UserPushConfig
} from '@/api/tagPushConfig'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const tagConfigs = ref<TagPushConfig[]>([])
const userPushConfigs = ref<UserPushConfig[]>([])

// 表单数据
const formData = reactive<TagPushConfig>({
  tagName: '',
  pushConfigIds: [],
  isEnabled: 1,
  remark: ''
})

// 表单验证规则
const formRules: FormRules = {
  tagName: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 50, message: '标签名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  pushConfigIds: [
    { required: true, message: '请选择至少一个推送配置', trigger: 'change' }
  ]
}

// 计算属性
const getSelectedConfigs = computed(() => (configIds: number[]) => {
  if (!configIds || configIds.length === 0) return []
  return userPushConfigs.value.filter(config => configIds.includes(config.id))
})

// 获取平台名称
const getPlatformName = (platform: string) => {
  const platformMap: Record<string, string> = {
    bark: 'Bark',
    email: '邮箱',
    wxpusher: 'WxPusher',
    pushme: 'PushMe'
  }
  return platformMap[platform] || platform
}

// 获取平台标签类型
const getPlatformTagType = (platform: string) => {
  const typeMap: Record<string, string> = {
    bark: 'success',
    email: 'primary',
    wxpusher: 'warning',
    pushme: 'info'
  }
  return typeMap[platform] || ''
}

// 获取标签配置列表
const fetchTagConfigs = async () => {
  try {
    const { data } = await getTagPushConfigList()
    tagConfigs.value = data || []
  } catch (error) {
    console.error('获取标签配置失败:', error)
    ElMessage.error('获取标签配置失败')
  }
}

// 获取用户推送配置列表
const fetchUserPushConfigs = async () => {
  try {
    const { data } = await getUserPushConfigs()
    userPushConfigs.value = data || []
  } catch (error) {
    console.error('获取推送配置失败:', error)
    ElMessage.error('获取推送配置失败')
  }
}

// 获取所有数据
const fetchData = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchTagConfigs(),
      fetchUserPushConfigs()
    ])
  } finally {
    loading.value = false
  }
}

// 打开弹窗
const openDialog = (row?: TagPushConfig) => {
  isEdit.value = !!row
  if (row) {
    Object.assign(formData, {
      id: row.id,
      tagName: row.tagName,
      pushConfigIds: row.pushConfigIds || [],
      isEnabled: row.isEnabled,
      remark: row.remark || ''
    })
  } else {
    Object.assign(formData, {
      id: undefined,
      tagName: '',
      pushConfigIds: [],
      isEnabled: 1,
      remark: ''
    })
  }
  dialogVisible.value = true
}

// 关闭弹窗
const closeDialog = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    await saveTagPushConfig(formData)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    
    closeDialog()
    await fetchTagConfigs()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除配置
const handleDelete = async (row: TagPushConfig) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签配置 "${row.tagName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteTagPushConfig(row.id!)
    ElMessage.success('删除成功')
    await fetchTagConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 格式化日期时间
const formatDateTime = (time: string | Date) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.search-card {
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .toolbar-title {
      font-size: 14px;
      color: #666;
    }
  }
}

.push-configs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  
  .config-tag {
    margin: 0;
  }
  
  .no-configs {
    color: #999;
    font-size: 12px;
    font-style: italic;
  }
}

.config-option {
  display: flex;
  align-items: center;
  width: 100%;
}

.form-tip {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  line-height: 1.4;
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
}
</style>