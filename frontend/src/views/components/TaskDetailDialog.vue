<template>
  <el-dialog
    v-model="visible"
    title="任务详情"
    width="700px"
    :before-close="handleClose"
  >
    <div v-loading="loading">
      <el-descriptions v-if="task" :column="2" border>
        <el-descriptions-item label="任务名称" :span="2">
          {{ task.taskName }}
        </el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2" v-if="task.description">
          {{ task.description }}
        </el-descriptions-item>
        <el-descriptions-item label="调度类型">
          <el-tag :type="getScheduleTypeColor(task.scheduleType)" size="small">
            {{ getScheduleTypeName(task.scheduleType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getStatusColor(task.status)" size="small">
            {{ getStatusName(task.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="通知标题" :span="2">
          {{ task.messageTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="通知内容" :span="2">
          <div class="content-box">{{ task.messageContent }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="跳转链接" :span="2" v-if="task.messageUrl">
          <el-link :href="task.messageUrl" type="primary" target="_blank">
            {{ task.messageUrl }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="消息标签" :span="2" v-if="task.tags && task.tags.length > 0">
          <el-tag 
            v-for="tag in task.tags" 
            :key="tag" 
            size="small" 
            style="margin-right: 8px"
          >
            {{ tag }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Cron表达式" :span="2">
          <code>{{ task.cronExpression }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="时区">
          {{ task.timezone || 'Asia/Shanghai' }}
        </el-descriptions-item>
        <el-descriptions-item label="已执行次数">
          {{ task.executedCount || 0 }}
          <span v-if="task.maxExecutions > 0" class="max-executions">
            / {{ task.maxExecutions }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="开始日期" v-if="task.startDate">
          {{ task.startDate }}
        </el-descriptions-item>
        <el-descriptions-item label="结束日期" v-if="task.endDate">
          {{ task.endDate }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(task.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDateTime(task.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 执行统计 -->
      <el-card v-if="stats" shadow="never" class="stats-card">
        <template #header>
          <span>执行统计</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ stats.total || 0 }}</div>
              <div class="stat-label">总执行次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number success">{{ stats.successCount || 0 }}</div>
              <div class="stat-label">成功次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number danger">{{ stats.failedCount || 0 }}</div>
              <div class="stat-label">失败次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number warning">{{ stats.skippedCount || 0 }}</div>
              <div class="stat-label">跳过次数</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="viewExecutionLogs">查看执行记录</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import api from '@/utils/request'

const props = defineProps({
  modelValue: Boolean,
  taskId: [Number, String]
})

const emit = defineEmits(['update:modelValue'])

// 响应式数据
const loading = ref(false)
const task = ref(null)
const stats = ref(null)

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 监听任务ID变化
watch(() => props.taskId, (taskId) => {
  if (taskId && visible.value) {
    loadTaskDetail()
    loadTaskStats()
  }
}, { immediate: true })

watch(visible, (newVal) => {
  if (newVal && props.taskId) {
    loadTaskDetail()
    loadTaskStats()
  }
})

// 加载任务详情
const loadTaskDetail = async () => {
  if (!props.taskId) return
  
  try {
    loading.value = true
    const response = await api.get(`/scheduled-tasks/${props.taskId}`)
    task.value = response.data
  } catch (error) {
    console.error('加载任务详情失败:', error)
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

// 查看执行记录
const viewExecutionLogs = () => {
  // 这里可以打开执行记录对话框或跳转到执行记录页面
  console.log('查看执行记录:', props.taskId)
}

// 辅助函数
const getScheduleTypeName = (type) => {
  const typeMap = {
    once: '一次性',
    daily: '每日',
    weekly: '每周', 
    monthly: '每月',
    custom: '自定义'
  }
  return typeMap[type] || type
}

const getScheduleTypeColor = (type) => {
  const colorMap = {
    once: 'warning',
    daily: 'success',
    weekly: 'primary',
    monthly: 'info',
    custom: 'danger'
  }
  return colorMap[type] || ''
}

const getStatusName = (status) => {
  const statusMap = {
    enabled: '启用中',
    disabled: '已停用',
    completed: '已完成',
    deleted: '已删除'
  }
  return statusMap[status] || status
}

const getStatusColor = (status) => {
  const colorMap = {
    enabled: 'success',
    disabled: 'info',
    completed: 'warning',
    deleted: 'danger'
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
  task.value = null
  stats.value = null
}
</script>

<style scoped>
.content-box {
  max-height: 100px;
  overflow-y: auto;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
}

.max-executions {
  color: #909399;
  font-size: 12px;
}

.stats-card {
  margin-top: 20px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
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

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

code {
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}
</style>