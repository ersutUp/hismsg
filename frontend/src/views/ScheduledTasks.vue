<template>
  <div class="scheduled-tasks-container">
    <div class="page-header">
      <div class="header-left">
        <h2>定时任务</h2>
        <p class="page-description">管理您的定时提醒任务</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          创建任务
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-number">{{ dashboard.totalTasks }}</div>
        <div class="stat-label">总任务数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ dashboard.enabledTasks }}</div>
        <div class="stat-label">启用中</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ dashboard.disabledTasks }}</div>
        <div class="stat-label">已停用</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ dashboard.completedTasks }}</div>
        <div class="stat-label">已完成</div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="queryForm.keyword"
            placeholder="搜索任务名称或描述"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="queryForm.status" placeholder="状态筛选" clearable>
            <el-option label="全部状态" value="" />
            <el-option label="启用中" value="enabled" />
            <el-option label="已停用" value="disabled" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="queryForm.scheduleType" placeholder="类型筛选" clearable>
            <el-option label="全部类型" value="" />
            <el-option label="一次性" value="once" />
            <el-option label="每日" value="daily" />
            <el-option label="每周" value="weekly" />
            <el-option label="每月" value="monthly" />
          </el-select>
        </el-col>
        <el-col :span="10">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button 
            :disabled="selectedTasks.length === 0"
            @click="batchEnable"
          >
            批量启用
          </el-button>
          <el-button 
            :disabled="selectedTasks.length === 0"
            @click="batchDisable"
          >
            批量停用
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 任务列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="tableLoading"
        :data="taskList"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="taskName" label="任务名称" min-width="150">
          <template #default="{ row }">
            <div class="task-name">
              <span class="name">{{ row.taskName }}</span>
              <div class="description" v-if="row.description">{{ row.description }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="messageTitle" label="通知标题" min-width="120" />
        <el-table-column prop="scheduleType" label="调度类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getScheduleTypeColor(row.scheduleType)" size="small">
              {{ getScheduleTypeName(row.scheduleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cronExpression" label="调度规则" min-width="120">
          <template #default="{ row }">
            <div class="cron-desc">{{ getCronDescription(row.cronExpression) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="120">
          <template #default="{ row }">
            <el-tag 
              v-for="tag in (row.tags || []).slice(0, 2)" 
              :key="tag" 
              size="small" 
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
            <span v-if="row.tags && row.tags.length > 2" class="more-tags">
              +{{ row.tags.length - 2 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="executedCount" label="执行次数" width="100">
          <template #default="{ row }">
            <span>{{ row.executedCount || 0 }}</span>
            <span v-if="row.maxExecutions > 0" class="max-executions">
              /{{ row.maxExecutions }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewTask(row)">
              详情
            </el-button>
            <el-button link type="primary" size="small" @click="editTask(row)">
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="executeNow(row)">
              立即执行
            </el-button>
            <el-dropdown @command="(command) => handleCommand(command, row)">
              <el-button link type="primary" size="small">
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    :command="{ action: 'toggle', row }"
                    :disabled="row.status === 'completed'"
                  >
                    {{ row.status === 'enabled' ? '停用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'logs', row }">
                    执行记录
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'delete', row }" divided>
                    <span style="color: #f56c6c">删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 创建/编辑任务对话框 -->
    <TaskFormDialog
      v-model="showTaskDialog"
      :task="currentTask"
      @success="handleTaskSuccess"
    />

    <!-- 任务详情对话框 -->
    <TaskDetailDialog
      v-model="showDetailDialog"
      :task-id="currentTaskId"
    />

    <!-- 执行记录对话框 -->
    <ExecutionLogDialog
      v-model="showLogDialog"
      :task-id="currentTaskId"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, ArrowDown } from '@element-plus/icons-vue'
import api from '@/utils/request'
import TaskFormDialog from './components/TaskFormDialog.vue'
import TaskDetailDialog from './components/TaskDetailDialog.vue'
import ExecutionLogDialog from './components/ExecutionLogDialog.vue'

// 响应式数据
const tableLoading = ref(false)
const taskList = ref([])
const total = ref(0)
const selectedTasks = ref([])
const showTaskDialog = ref(false)
const showDetailDialog = ref(false)
const showLogDialog = ref(false)
const currentTask = ref(null)
const currentTaskId = ref(null)

const dashboard = reactive({
  totalTasks: 0,
  enabledTasks: 0,
  disabledTasks: 0,
  completedTasks: 0
})

const queryForm = reactive({
  page: 1,
  size: 20,
  keyword: '',
  status: '',
  scheduleType: ''
})

// 页面加载
onMounted(() => {
  loadDashboard()
  loadTaskList()
})

// 加载统计数据
const loadDashboard = async () => {
  try {
    const response = await api.get('/scheduled-tasks/dashboard')
    Object.assign(dashboard, response.data)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载任务列表
const loadTaskList = async () => {
  try {
    tableLoading.value = true
    const response = await api.get('/scheduled-tasks/list', { params: queryForm })
    taskList.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    ElMessage.error('加载任务列表失败')
    console.error(error)
  } finally {
    tableLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryForm.page = 1
  loadTaskList()
}

// 重置
const handleReset = () => {
  queryForm.page = 1
  queryForm.keyword = ''
  queryForm.status = ''
  queryForm.scheduleType = ''
  loadTaskList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
}

// 显示创建对话框
const showCreateDialog = () => {
  currentTask.value = null
  showTaskDialog.value = true
}

// 编辑任务
const editTask = (task) => {
  currentTask.value = { ...task }
  showTaskDialog.value = true
}

// 查看任务详情
const viewTask = (task) => {
  currentTaskId.value = task.id
  showDetailDialog.value = true
}

// 立即执行
const executeNow = async (task) => {
  try {
    await ElMessageBox.confirm(`确认立即执行任务 "${task.taskName}" 吗？`, '确认执行', {
      type: 'warning'
    })
    
    await api.post(`/scheduled-tasks/${task.id}/execute`)
    ElMessage.success('任务执行请求已发送')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('执行任务失败')
      console.error(error)
    }
  }
}

// 处理命令
const handleCommand = ({ action, row }) => {
  switch (action) {
    case 'toggle':
      toggleTaskStatus(row)
      break
    case 'logs':
      viewExecutionLogs(row)
      break
    case 'delete':
      deleteTask(row)
      break
  }
}

// 切换任务状态
const toggleTaskStatus = async (task) => {
  const newStatus = task.status === 'enabled' ? 'disabled' : 'enabled'
  const action = newStatus === 'enabled' ? '启用' : '停用'
  
  try {
    await ElMessageBox.confirm(`确认${action}任务 "${task.taskName}" 吗？`, `确认${action}`, {
      type: 'warning'
    })
    
    await api.put(`/scheduled-tasks/${task.id}/status?status=${newStatus}`)
    ElMessage.success(`${action}成功`)
    loadTaskList()
    loadDashboard()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
      console.error(error)
    }
  }
}

// 查看执行记录
const viewExecutionLogs = (task) => {
  currentTaskId.value = task.id
  showLogDialog.value = true
}

// 删除任务
const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(
      `确认删除任务 "${task.taskName}" 吗？此操作不可撤销。`, 
      '确认删除', 
      { type: 'error' }
    )
    
    await api.delete(`/scheduled-tasks/${task.id}`)
    ElMessage.success('删除成功')
    loadTaskList()
    loadDashboard()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 批量启用
const batchEnable = async () => {
  if (selectedTasks.value.length === 0) return
  
  try {
    const taskIds = selectedTasks.value.map(task => task.id)
    await api.put('/scheduled-tasks/batch/status', null, {
      params: { taskIds: taskIds.join(','), status: 'enabled' }
    })
    ElMessage.success('批量启用成功')
    loadTaskList()
    loadDashboard()
  } catch (error) {
    ElMessage.error('批量启用失败')
    console.error(error)
  }
}

// 批量停用
const batchDisable = async () => {
  if (selectedTasks.value.length === 0) return
  
  try {
    const taskIds = selectedTasks.value.map(task => task.id)
    await api.put('/scheduled-tasks/batch/status', null, {
      params: { taskIds: taskIds.join(','), status: 'disabled' }
    })
    ElMessage.success('批量停用成功')
    loadTaskList()
    loadDashboard()
  } catch (error) {
    ElMessage.error('批量停用失败')
    console.error(error)
  }
}

// 任务操作成功回调
const handleTaskSuccess = () => {
  loadTaskList()
  loadDashboard()
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

const getCronDescription = (cronExpression) => {
  // 这里可以调用后端API获取Cron表达式的描述
  // 简化处理，直接显示表达式
  return cronExpression
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.scheduled-tasks-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0 0 5px 0;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-card {
  margin-bottom: 20px;
}

.task-name .name {
  font-weight: 500;
  color: #303133;
}

.task-name .description {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.cron-desc {
  font-size: 12px;
  color: #606266;
}

.tag-item {
  margin-right: 4px;
}

.more-tags {
  font-size: 12px;
  color: #909399;
}

.max-executions {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .scheduled-tasks-container {
    padding: 8px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-right {
    width: 100%;
  }

  .header-right .el-button {
    width: 100%;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .stat-card {
    padding: 12px;
  }

  .stat-number {
    font-size: 20px;
  }

  .stat-label {
    font-size: 12px;
  }

  .filter-card :deep(.el-row) {
    display: flex;
    flex-direction: column;
  }

  .filter-card :deep(.el-col) {
    max-width: 100%;
    margin-bottom: 12px;
  }

  .filter-card :deep(.el-input),
  .filter-card :deep(.el-select) {
    width: 100%;
  }

  .filter-card :deep(.el-button) {
    width: 100%;
    margin-left: 0;
    margin-bottom: 8px;
  }

  .pagination-container {
    text-align: center;
  }

  .pagination-container :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
  }

  .pagination-container :deep(.el-pagination__sizes),
  .pagination-container :deep(.el-pagination__jump) {
    margin-top: 8px;
  }
}
</style>