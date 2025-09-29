<template>
  <div class="page-container">
    <div class="page-header">
      <h1>修改密码</h1>
      <p class="page-description">更改您的登录密码</p>
    </div>

    <el-row justify="center">
      <el-col :xs="24" :sm="18" :md="12" :lg="8">
        <el-card class="password-form-card">
          <template #header>
            <div class="form-header">
              <el-icon class="header-icon"><Lock /></el-icon>
              <span>修改登录密码</span>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            class="password-form"
            @submit.prevent="handleSubmit"
          >
            <!-- 当前密码 -->
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input
                v-model="form.currentPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
                clearable
                :prefix-icon="Lock"
                autocomplete="current-password"
              />
            </el-form-item>

            <!-- 新密码 -->
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="form.newPassword"
                type="password"
                placeholder="请输入新密码（6-20位）"
                show-password
                clearable
                :prefix-icon="Key"
                autocomplete="new-password"
              />
              <div class="password-strength" v-if="form.newPassword">
                <div class="strength-label">密码强度：</div>
                <div class="strength-bar">
                  <div 
                    class="strength-fill" 
                    :class="passwordStrength.class"
                    :style="{ width: passwordStrength.width }"
                  ></div>
                </div>
                <span class="strength-text" :class="passwordStrength.class">
                  {{ passwordStrength.text }}
                </span>
              </div>
            </el-form-item>

            <!-- 确认新密码 -->
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
                clearable
                :prefix-icon="Key"
                autocomplete="new-password"
              />
            </el-form-item>

            <!-- 密码规则提示 -->
            <el-alert
              title="密码规则"
              type="info"
              :closable="false"
              class="password-rules"
            >
              <template #default>
                <ul class="rules-list">
                  <li>密码长度为6-20位字符</li>
                  <li>建议包含大小写字母、数字和特殊字符</li>
                  <li>不能与当前密码相同</li>
                  <li>避免使用简单的密码组合</li>
                </ul>
              </template>
            </el-alert>

            <!-- 操作按钮 -->
            <el-form-item class="form-actions">
              <el-button 
                type="primary" 
                :loading="loading"
                @click="handleSubmit"
                class="submit-btn"
              >
                <el-icon><Check /></el-icon>
                {{ loading ? '修改中...' : '确认修改' }}
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
              <el-button @click="handleCancel">
                <el-icon><Close /></el-icon>
                取消
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 安全提示 -->
    <el-row justify="center" class="security-tips">
      <el-col :xs="24" :sm="18" :md="12" :lg="8">
        <el-card>
          <template #header>
            <div class="tips-header">
              <el-icon class="tips-icon"><InfoFilled /></el-icon>
              <span>安全提示</span>
            </div>
          </template>
          
          <div class="tips-content">
            <el-alert
              v-for="tip in securityTips"
              :key="tip.title"
              :title="tip.title"
              :type="tip.type"
              :closable="false"
              class="tip-item"
            >
              <template #default>
                <p>{{ tip.content }}</p>
              </template>
            </el-alert>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { changePassword, logout } from '@/api/auth'
import { useUserStore } from '@/store/user'
import {
  Lock,
  Key,
  Check,
  Refresh,
  Close,
  InfoFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 表单数据
const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码强度计算
const passwordStrength = computed(() => {
  const password = form.newPassword
  if (!password) {
    return { width: '0%', class: '', text: '' }
  }

  let score = 0
  let feedback = []

  // 长度检查
  if (password.length >= 8) {
    score += 20
  } else if (password.length >= 6) {
    score += 10
    feedback.push('建议密码长度至少8位')
  } else {
    feedback.push('密码太短')
  }

  // 包含小写字母
  if (/[a-z]/.test(password)) {
    score += 20
  } else {
    feedback.push('建议包含小写字母')
  }

  // 包含大写字母
  if (/[A-Z]/.test(password)) {
    score += 20
  } else {
    feedback.push('建议包含大写字母')
  }

  // 包含数字
  if (/\d/.test(password)) {
    score += 20
  } else {
    feedback.push('建议包含数字')
  }

  // 包含特殊字符
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    score += 20
  } else {
    feedback.push('建议包含特殊字符')
  }

  // 根据得分确定强度
  if (score < 40) {
    return {
      width: `${score}%`,
      class: 'weak',
      text: '弱'
    }
  } else if (score < 70) {
    return {
      width: `${score}%`,
      class: 'medium',
      text: '中'
    }
  } else {
    return {
      width: `${score}%`,
      class: 'strong',
      text: '强'
    }
  }
})

// 表单验证规则
const rules: FormRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位字符', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === form.currentPassword) {
          callback(new Error('新密码不能与当前密码相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 安全提示
const securityTips = ref([
  {
    title: '定期更换密码',
    type: 'success',
    content: '建议每3-6个月更换一次密码，增强账户安全性。'
  },
  {
    title: '密码保密',
    type: 'warning',
    content: '请不要将密码告诉他人，也不要在不安全的环境中输入密码。'
  },
  {
    title: '注意登录环境',
    type: 'info',
    content: '在公共场所使用时，请注意周围环境，防止密码被偷窥。'
  }
])

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    await formRef.value.validate()

    // 确认操作
    await ElMessageBox.confirm(
      '确定要修改登录密码吗？修改后需要重新登录。',
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true

    // 调用修改密码接口
    await changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword
    })

    ElMessage.success('密码修改成功，请重新登录')


    // 延迟跳转到登录页
    setTimeout(async() => {
      await userStore.userLogout()
      router.push('/login')
    }, 500)

  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '密码修改失败')
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  if (!formRef.value) return
  
  formRef.value.resetFields()
  form.currentPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
}

// 取消操作
const handleCancel = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.password-form-card {
  margin-bottom: 24px;
  
  .form-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    
    .header-icon {
      color: #409eff;
    }
  }
}

.password-form {
  .password-strength {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    
    .strength-label {
      color: #666;
      min-width: 60px;
    }
    
    .strength-bar {
      flex: 1;
      height: 6px;
      background-color: #f0f0f0;
      border-radius: 3px;
      overflow: hidden;
      
      .strength-fill {
        height: 100%;
        transition: width 0.3s ease;
        border-radius: 3px;
        
        &.weak {
          background-color: #f56c6c;
        }
        
        &.medium {
          background-color: #e6a23c;
        }
        
        &.strong {
          background-color: #67c23a;
        }
      }
    }
    
    .strength-text {
      min-width: 20px;
      font-weight: 600;
      
      &.weak {
        color: #f56c6c;
      }
      
      &.medium {
        color: #e6a23c;
      }
      
      &.strong {
        color: #67c23a;
      }
    }
  }
  
  .password-rules {
    margin: 16px 0;
    
    .rules-list {
      margin: 0;
      padding-left: 16px;
      
      li {
        margin: 4px 0;
        font-size: 13px;
        color: #666;
      }
    }
  }
  
  .form-actions {
    margin-top: 24px;
    
    .submit-btn {
      min-width: 120px;
    }
  }
}

.security-tips {
  .tips-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    
    .tips-icon {
      color: #909399;
    }
  }
  
  .tips-content {
    .tip-item {
      margin-bottom: 12px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      p {
        margin: 0;
        line-height: 1.6;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .password-form {
    .password-strength {
      flex-direction: column;
      align-items: stretch;
      gap: 4px;
      
      .strength-label {
        min-width: auto;
      }
    }
    
    .form-actions {
      .el-button {
        width: 100%;
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}
</style>