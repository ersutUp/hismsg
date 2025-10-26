<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑任务' : '创建任务'"
    width="800px"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="任务名称" prop="taskName">
            <el-input v-model="form.taskName" placeholder="请输入任务名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="调度类型" prop="scheduleType">
            <el-select v-model="form.scheduleType" placeholder="选择调度类型" @change="onScheduleTypeChange">
              <el-option label="一次性提醒" value="once" />
              <el-option label="每日提醒" value="daily" />
              <el-option label="每周提醒" value="weekly" />
              <el-option label="每月提醒" value="monthly" />
              <el-option label="自定义Cron" value="custom" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="任务描述">
        <el-input 
          v-model="form.description" 
          type="textarea" 
          :rows="2" 
          placeholder="请输入任务描述（可选）" 
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="通知标题" prop="messageTitle">
            <el-input v-model="form.messageTitle" placeholder="请输入通知标题" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="跳转链接">
            <el-input v-model="form.messageUrl" placeholder="点击通知后跳转的链接（可选）" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="通知内容" prop="messageContent">
        <el-input 
          v-model="form.messageContent" 
          type="textarea" 
          :rows="3" 
          placeholder="请输入通知内容" 
        />
      </el-form-item>

      <el-form-item label="消息标签">
        <el-input
          v-model="form.tags"
          placeholder="请输入标签，多个标签用逗号分隔，如：重要,工作,紧急"
        />
        <div class="form-help">
          <small>多个标签请用逗号（,）分隔</small>
        </div>
      </el-form-item>

      <!-- 调度配置区域 -->
      <el-card shadow="never" class="schedule-config">
        <template #header>
          <span>调度配置</span>
        </template>
        
        <!-- 一次性任务 -->
        <div v-if="form.scheduleType === 'once'">
          <el-form-item label="执行时间" prop="onceTime">
            <el-date-picker
              v-model="form.onceTime"
              type="datetime"
              placeholder="选择执行时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </div>

        <!-- 每日任务 -->
        <div v-if="form.scheduleType === 'daily'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="执行时间" prop="dailyTime">
                <el-time-picker
                  v-model="form.dailyTime"
                  placeholder="选择时间"
                  format="HH:mm"
                  value-format="HH:mm"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="工作日限制">
                <el-checkbox-group v-model="form.weekdays">
                  <el-checkbox label="1">周一</el-checkbox>
                  <el-checkbox label="2">周二</el-checkbox>
                  <el-checkbox label="3">周三</el-checkbox>
                  <el-checkbox label="4">周四</el-checkbox>
                  <el-checkbox label="5">周五</el-checkbox>
                  <el-checkbox label="6">周六</el-checkbox>
                  <el-checkbox label="7">周日</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 每周任务 -->
        <div v-if="form.scheduleType === 'weekly'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="执行时间" prop="weeklyTime">
                <el-time-picker
                  v-model="form.weeklyTime"
                  placeholder="选择时间"
                  format="HH:mm"
                  value-format="HH:mm"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="星期几" prop="dayOfWeek">
                <el-select v-model="form.dayOfWeek" placeholder="选择星期几">
                  <el-option label="周一" value="1" />
                  <el-option label="周二" value="2" />
                  <el-option label="周三" value="3" />
                  <el-option label="周四" value="4" />
                  <el-option label="周五" value="5" />
                  <el-option label="周六" value="6" />
                  <el-option label="周日" value="7" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 每月任务 -->
        <div v-if="form.scheduleType === 'monthly'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="执行时间" prop="monthlyTime">
                <el-time-picker
                  v-model="form.monthlyTime"
                  placeholder="选择时间"
                  format="HH:mm"
                  value-format="HH:mm"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="每月几号" prop="dayOfMonth">
                <el-select v-model="form.dayOfMonth" placeholder="选择日期">
                  <el-option
                    v-for="day in 31"
                    :key="day"
                    :label="`${day}号`"
                    :value="day"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 自定义Cron -->
        <div v-if="form.scheduleType === 'custom'">
          <el-form-item label="Cron表达式" prop="customCron">
            <el-input 
              v-model="form.customCron" 
              placeholder="请输入Cron表达式，如：0 30 9 * * ?" 
              @blur="previewCron"
            />
            <div class="cron-help">
              <small>格式：秒 分 时 日 月 周 [年]，例如：0 30 9 * * ? 表示每天9:30执行</small>
            </div>
          </el-form-item>
          <div v-if="cronPreview" class="cron-preview">
            <el-alert 
              :title="`表达式解析: ${cronPreview.description}`" 
              :type="cronPreview.isValid ? 'success' : 'error'" 
              show-icon 
              :closable="false"
            />
          </div>
        </div>

        <!-- 时间范围 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                placeholder="选择开始日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                placeholder="选择结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大执行次数">
              <el-input-number 
                v-model="form.maxExecutions" 
                :min="-1" 
                :max="9999" 
                placeholder="不限制请输入-1"
              />
              <div class="form-help">
                <small>-1表示无限制，0表示不执行，大于0表示最大执行次数</small>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时区">
              <el-select v-model="form.timezone" placeholder="选择时区">
                <el-option label="北京时间 (GMT+8)" value="Asia/Shanghai" />
                <el-option label="东京时间 (GMT+9)" value="Asia/Tokyo" />
                <el-option label="纽约时间 (GMT-5)" value="America/New_York" />
                <el-option label="伦敦时间 (GMT+0)" value="Europe/London" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/request'

const props = defineProps({
  modelValue: Boolean,
  task: Object
})

const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const formRef = ref()
const submitLoading = ref(false)
const cronPreview = ref(null)

const form = reactive({
  taskName: '',
  description: '',
  messageTitle: '',
  messageContent: '',
  messageUrl: '',
  tags: '',
  scheduleType: 'once',
  timezone: 'Asia/Shanghai',
  startDate: '',
  endDate: '',
  maxExecutions: -1,
  // 一次性任务
  onceTime: '',
  // 每日任务
  dailyTime: '09:00',
  weekdays: [],
  // 每周任务
  weeklyTime: '09:00',
  dayOfWeek: '1',
  // 每月任务
  monthlyTime: '09:00',
  dayOfMonth: 1,
  // 自定义
  customCron: ''
})

// 表单验证规则
const rules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  messageTitle: [
    { required: true, message: '请输入通知标题', trigger: 'blur' }
  ],
  messageContent: [
    { required: true, message: '请输入通知内容', trigger: 'blur' }
  ],
  scheduleType: [
    { required: true, message: '请选择调度类型', trigger: 'change' }
  ],
  onceTime: [
    { required: true, message: '请选择执行时间', trigger: 'change' }
  ],
  dailyTime: [
    { required: true, message: '请选择执行时间', trigger: 'change' }
  ],
  weeklyTime: [
    { required: true, message: '请选择执行时间', trigger: 'change' }
  ],
  dayOfWeek: [
    { required: true, message: '请选择星期几', trigger: 'change' }
  ],
  monthlyTime: [
    { required: true, message: '请选择执行时间', trigger: 'change' }
  ],
  dayOfMonth: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  customCron: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' }
  ]
}

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.task?.id)

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    taskName: '',
    description: '',
    messageTitle: '',
    messageContent: '',
    messageUrl: '',
    tags: '',
    scheduleType: 'once',
    timezone: 'Asia/Shanghai',
    startDate: '',
    endDate: '',
    maxExecutions: -1,
    onceTime: '',
    dailyTime: '09:00',
    weekdays: [],
    weeklyTime: '09:00',
    dayOfWeek: '1',
    monthlyTime: '09:00',
    dayOfMonth: 1,
    customCron: ''
  })
  cronPreview.value = null
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}


// 监听任务数据变化
watch(() => props.task, (task) => {
  if (task) {
    // 编辑模式，填充表单数据
    Object.assign(form, {
      taskName: task.taskName || '',
      description: task.description || '',
      messageTitle: task.messageTitle || '',
      messageContent: task.messageContent || '',
      messageUrl: task.messageUrl || '',
      tags: Array.isArray(task.tags) ? task.tags.join(',') : (task.tags || ''),
      scheduleType: task.scheduleType || 'once',
      timezone: task.timezone || 'Asia/Shanghai',
      startDate: task.startDate || '',
      endDate: task.endDate || '',
      maxExecutions: task.maxExecutions || -1
    })

    // 根据调度类型解析配置
    parseCronExpression(task.cronExpression, task.scheduleType)
  } else {
    // 创建模式，重置表单
    resetForm()
  }
}, { immediate: true })

// 调度类型变化
const onScheduleTypeChange = () => {
  cronPreview.value = null
  // 清空相关字段的验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 预览Cron表达式
const previewCron = async () => {
  if (!form.customCron.trim()) return
  
  try {
    const response = await api.post('/scheduled-tasks/cron/preview', {
      cronExpression: form.customCron
    })
    cronPreview.value = response.data
  } catch (error) {
    console.error('预览Cron表达式失败:', error)
  }
}

// 生成Cron表达式
const generateCronExpression = () => {
  switch (form.scheduleType) {
    case 'once':
      if (!form.onceTime) return ''
      const onceDate = new Date(form.onceTime)
      return `0 ${onceDate.getMinutes()} ${onceDate.getHours()} ${onceDate.getDate()} ${onceDate.getMonth() + 1} ? ${onceDate.getFullYear()}`
    
    case 'daily':
      if (!form.dailyTime) return ''
      const [dailyHour, dailyMinute] = form.dailyTime.split(':')
      if (form.weekdays.length > 0) {
        const weekdaysStr = form.weekdays.join(',')
        return `0 ${dailyMinute} ${dailyHour} ? * ${weekdaysStr}`
      } else {
        return `0 ${dailyMinute} ${dailyHour} * * ?`
      }
    
    case 'weekly':
      if (!form.weeklyTime || !form.dayOfWeek) return ''
      const [weeklyHour, weeklyMinute] = form.weeklyTime.split(':')
      return `0 ${weeklyMinute} ${weeklyHour} ? * ${form.dayOfWeek}`
    
    case 'monthly':
      if (!form.monthlyTime || !form.dayOfMonth) return ''
      const [monthlyHour, monthlyMinute] = form.monthlyTime.split(':')
      return `0 ${monthlyMinute} ${monthlyHour} ${form.dayOfMonth} * ?`
    
    case 'custom':
      return form.customCron || ''
    
    default:
      return ''
  }
}

// 解析Cron表达式（编辑时使用）
const parseCronExpression = (cronExpression, scheduleType) => {
  if (!cronExpression) return
  
  const parts = cronExpression.split(' ')
  if (parts.length < 6) return
  
  const [second, minute, hour, day, month, dayOfWeek, year] = parts
  
  switch (scheduleType) {
    case 'once':
      if (year && day !== '*' && month !== '*') {
        form.onceTime = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')} ${hour.padStart(2, '0')}:${minute.padStart(2, '0')}:00`
      }
      break
    
    case 'daily':
      form.dailyTime = `${hour.padStart(2, '0')}:${minute.padStart(2, '0')}`
      if (dayOfWeek !== '*' && dayOfWeek !== '?') {
        form.weekdays = dayOfWeek.split(',')
      }
      break
    
    case 'weekly':
      form.weeklyTime = `${hour.padStart(2, '0')}:${minute.padStart(2, '0')}`
      if (dayOfWeek !== '*' && dayOfWeek !== '?') {
        form.dayOfWeek = dayOfWeek
      }
      break
    
    case 'monthly':
      form.monthlyTime = `${hour.padStart(2, '0')}:${minute.padStart(2, '0')}`
      if (day !== '*' && day !== '?') {
        form.dayOfMonth = parseInt(day)
      }
      break
    
    case 'custom':
      form.customCron = cronExpression
      previewCron()
      break
  }
}


// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    const cronExpression = generateCronExpression()
    if (!cronExpression) {
      ElMessage.error('生成Cron表达式失败，请检查调度配置')
      return
    }

    // 只发送后端需要的字段
    const submitData = {
      taskName: form.taskName,
      description: form.description,
      messageTitle: form.messageTitle,
      messageContent: form.messageContent,
      messageUrl: form.messageUrl,
      tags: form.tags ? form.tags.split(',').map(tag => tag.trim()).filter(tag => tag) : [],
      scheduleType: form.scheduleType,
      cronExpression,
      timezone: form.timezone,
      startDate: form.startDate || null,  // 空字符串转为null
      endDate: form.endDate || null,      // 空字符串转为null
      maxExecutions: form.maxExecutions
    }
    
    if (isEdit.value) {
      await api.put(`/scheduled-tasks/${props.task.id}`, submitData)
      ElMessage.success('更新任务成功')
    } else {
      await api.post('/scheduled-tasks', submitData)
      ElMessage.success('创建任务成功')
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    console.error('提交失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error(isEdit.value ? '更新任务失败' : '创建任务失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  resetForm()
}
</script>

<style scoped>
.schedule-config {
  margin: 20px 0;
}

.cron-help {
  margin-top: 5px;
  color: #909399;
}

.cron-preview {
  margin-top: 10px;
}

.form-help {
  margin-top: 5px;
}

.form-help small {
  color: #909399;
}
</style>