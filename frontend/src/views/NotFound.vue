<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-message">页面不存在</div>
      <div class="error-description">
        抱歉，您访问的页面不存在或已被删除
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">
          返回首页
        </el-button>
        <el-button @click="goBack">
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.not-found-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.not-found-content {
  text-align: center;
  padding: 40px;
  
  .error-code {
    font-size: 120px;
    font-weight: bold;
    color: #409eff;
    line-height: 1;
    margin-bottom: 20px;
    background: linear-gradient(135deg, #409eff, #79bbff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .error-message {
    font-size: 32px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }
  
  .error-description {
    font-size: 16px;
    color: #666;
    margin-bottom: 40px;
    line-height: 1.5;
  }
  
  .error-actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    
    .el-button {
      padding: 12px 24px;
      font-size: 14px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .not-found-content {
    padding: 20px;
    
    .error-code {
      font-size: 80px;
    }
    
    .error-message {
      font-size: 24px;
    }
    
    .error-description {
      font-size: 14px;
    }
    
    .error-actions {
      flex-direction: column;
      align-items: center;
      
      .el-button {
        width: 200px;
      }
    }
  }
}
</style>