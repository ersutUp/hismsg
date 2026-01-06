<template>
  <el-container class="layout-container">
    <!-- 移动端遮罩层 -->
    <div
      v-if="isMobile && mobileMenuVisible"
      class="mobile-overlay"
      @click="closeMobileMenu"
    ></div>

    <!-- 侧边栏 -->
    <el-aside
      :width="isCollapse ? '64px' : '240px'"
      :class="['layout-aside', { 'mobile-hidden': isMobile && !mobileMenuVisible }]"
    >
      <div class="logo">
        <el-icon v-if="isCollapse" class="logo-icon">
          <Message />
        </el-icon>
        <div v-else class="logo-content">
          <el-icon class="logo-icon">
            <Message />
          </el-icon>
          <span class="logo-text">HisMsg</span>
        </div>
      </div>

      <el-menu
        :default-active="currentRoute"
        :collapse="isCollapse"
        router
        class="layout-menu"
        @select="closeMobileMenu"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          <el-icon>
            <component :is="item.meta.icon" />
          </el-icon>
          <template #title>{{ item.meta.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container class="layout-main">
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-button
            text
            size="large"
            @click="toggleCollapse"
          >
            <el-icon>
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </el-button>
          
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleUserMenuCommand">
            <div class="user-info">
              <el-avatar
                :size="32"
                :src="userStore.currentUser?.avatar"
                class="user-avatar"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.currentUser?.nickname || userStore.currentUser?.username }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  Message,
  ChatDotRound,
  Setting,
  Collection,
  User,
  ArrowDown,
  Fold,
  Expand,
  Document,
  Odometer,
  PriceTag
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 检测是否为移动端
const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 侧边栏折叠状态（移动端默认折叠）
const isCollapse = ref(false)

// 移动端侧边栏显示状态
const mobileMenuVisible = ref(false)

// 当前路由
const currentRoute = computed(() => route.path)

// 当前页面标题
const currentPageTitle = computed(() => {
  return route.meta?.title || '未知页面'
})

// 菜单项
const menuItems = computed(() => {
  const items = [
    {
      path: '/dashboard',
      meta: { title: '仪表板', icon: 'Odometer' }
    },
    {
      path: '/messages',
      meta: { title: '消息记录', icon: 'ChatDotRound' }
    },
    {
      path: '/push-docs',
      meta: { title: '推送文档', icon: 'Document' }
    },
    {
      path: '/push-config',
      meta: { title: '推送配置', icon: 'Setting' }
    },
    {
      path: '/tag-push-config',
      meta: { title: '标签配置', icon: 'PriceTag' }
    },
    {
      path: '/scheduled-tasks',
      meta: { title: '定时任务', icon: 'Timer' }
    }
  ]
  
  // 如果是管理员，显示字典管理
  if (userStore.currentUser?.username === 'admin') {
    items.push({
      path: '/dict-management',
      meta: { title: '字典管理', icon: 'Collection' }
    })
  }
  
  return items
})

// 切换侧边栏折叠
const toggleCollapse = () => {
  if (isMobile.value) {
    // 移动端切换菜单显示/隐藏
    mobileMenuVisible.value = !mobileMenuVisible.value
  } else {
    // PC端切换折叠/展开
    isCollapse.value = !isCollapse.value
  }
}

// 关闭移动端菜单
const closeMobileMenu = () => {
  if (isMobile.value) {
    mobileMenuVisible.value = false
  }
}

// 生命周期钩子
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// 处理用户菜单命令
const handleUserMenuCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能暂未实现')
      break
    case 'changePassword':
      router.push('/change-password')
      break
    case 'logout':
      try {
        await userStore.userLogout()
        router.push('/login')
      } catch (error) {
        console.error('退出登录失败:', error)
      }
      break
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: #001529;
  transition: width 0.3s ease;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    background: rgba(0, 0, 0, 0.2);
    
    .logo-content {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .logo-icon {
      font-size: 24px;
      color: #1890ff;
    }
    
    .logo-text {
      font-size: 18px;
      font-weight: 600;
      color: #fff;
    }
  }
}

.layout-menu {
  border: none;
  background: #001529;
  
  :deep(.el-menu-item) {
    color: rgba(255, 255, 255, 0.65);
    
    &:hover {
      color: #fff;
      background-color: rgba(255, 255, 255, 0.08);
    }
    
    &.is-active {
      color: #1890ff;
      background-color: rgba(24, 144, 255, 0.1);
    }
  }
}

.layout-main {
  display: flex;
  flex-direction: column;
}

.layout-header {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .el-breadcrumb {
      font-size: 16px;
      font-weight: 500;
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 6px;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: #f5f5f5;
      }
      
      .user-avatar {
        flex-shrink: 0;
      }
      
      .username {
        font-size: 14px;
        color: #333;
      }
      
      .dropdown-icon {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

.layout-content {
  background: #f5f5f5;
  overflow-y: auto;
}

// 移动端遮罩层
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  transition: opacity 0.3s ease;
}

// 响应式设计
@media (max-width: 768px) {
  .layout-aside {
    position: fixed;
    z-index: 1000;
    height: 100vh;
    transition: transform 0.3s ease;

    &.mobile-hidden {
      transform: translateX(-100%);
    }
  }

  .layout-main {
    width: 100%;
    margin-left: 0 !important;
  }

  .layout-header {
    padding: 0 12px;
    height: 56px;

    .header-left {
      gap: 8px;

      .el-breadcrumb {
        font-size: 14px;
      }
    }

    .username {
      display: none;
    }

    .dropdown-icon {
      display: none;
    }
  }

  .layout-content {
    padding: 8px;
  }
}

// 深色主题适配
.dark {
  .layout-header {
    background: var(--el-bg-color);
    border-bottom-color: var(--el-border-color);
    
    .user-info:hover {
      background-color: var(--el-fill-color-light);
    }
    
    .username {
      color: var(--el-text-color-primary);
    }
  }
  
  .layout-content {
    background: var(--el-bg-color-page);
  }
}
</style>