<template>
  <el-dialog
    v-model="visible"
    title="执行记录"
    width="900px"
    :before-close="handleClose"
  >
    <div v-loading="loading">
      <!-- 统计信息 -->
      <div class="stats-row" v-if="stats">
        <div class="stat-card">
          <div class="stat-number">{{ stats.total || 0 }}</div>
          <div class="stat-label">总执行次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-number success">{{ stats.successCount || 0 }}</div>
          <div class="stat-label">成功次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-number danger">{{ stats.failedCount || 0 }}</div>
          <div class="stat-label">失败次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-number warning">{{ stats.skippedCount || 0 }}</div>
          <div class="stat-label">跳过次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-number info">
            {{ stats.total > 0 ? Math.round((stats.successCount || 0) / stats.total * 100) : 0 }}%
          </div>
          <div class="stat-label">成功率</div>
        </div>
      </div>

      <!-- 执行记录列表 -->
      <el-table :data="executionList" row-key="id">
        <el-table-column prop="executionTime" label="执行时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.executionTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executionDuration" label="执行耗时" width="100">
          <template #default="{ row }">
            <span v-if="row.executionDuration">{{ row.executionDuration }}ms</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="messageId" label="消息ID" width="120">
          <template #default="{ row }">
            <span v-if="row.messageId">{{ row.messageId }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="nodeId" label="执行节点" width="120">
          <template #default="{ row }">
            <code v-if="row.nodeId" class="node-id">{{ row.nodeId.substring(0, 8) }}...</code>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="200">
          <template #default="{ row }">
            <div v-if="row.errorMessage" class="error-message">
              {{ row.errorMessage }}
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="记录时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadExecutionLogs"
          @current-change="loadExecutionLogs"
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="loadExecutionLogs">刷新</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import api from '@/utils/request'

const props = defineProps({
  modelValue: Boolean,
  taskId: [Number, String]
})

const emit = defineEmits(['update:modelValue'])

// 响应式数据
const loading = ref(false)
const executionList = ref([])
const total = ref(0)
const stats = ref(null)

const queryForm = reactive({
  page: 1,
  size: 20
})

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 监听变化
watch(() => props.taskId, (taskId) => {
  if (taskId && visible.value) {
    loadExecutionLogs()
    loadTaskStats()
  }
}, { immediate: true })

watch(visible, (newVal) => {
  if (newVal && props.taskId) {
    queryForm.page = 1
    loadExecutionLogs()
    loadTaskStats()
  }
})

// 加载执行记录
const loadExecutionLogs = async () => {
  if (!props.taskId) return
  
  try {
    loading.value = true
    const response = await api.get(`/scheduled-tasks/${props.taskId}/executions`, {
      params: queryForm
    })
    executionList.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    console.error('加载执行记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载执行统计
const loadTaskStats = async () => {
  if (!props.taskId) return
  
  try {
    const response = await api.get(`/scheduled-tasks/${props.taskId}/stats`)
    stats.value = response.data
  } catch (error) {
    console.error('加载执行统计失败:', error)
  }
}

// 辅助函数
const getStatusName = (status) => {
  const statusMap = {
    success: '成功',
    failed: '失败', 
    skipped: '跳过'
  }
  return statusMap[status] || status
}

const getStatusColor = (status) => {
  const colorMap = {
    success: 'success',
    failed: 'danger',
    skipped: 'warning'
  }
  return colorMap[status] || ''
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  executionList.value = []
  stats.value = null
}
</script>

<style scoped>
.stats-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-number.success {
  color: #67c23a;
}

.stat-number.danger {
  color: #f56c6c;
}

.stat-number.warning {
  color: #e6a23c;
}

.stat-number.info {
  color: #409eff;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.node-id {
  background-color: #f5f7fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.error-message:hover {
  overflow: visible;
  white-space: normal;
  word-wrap: break-word;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>