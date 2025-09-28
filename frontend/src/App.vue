<template>
  <router-view />
</template>

<script setup lang="ts">
import { provide } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 初始化用户信息
userStore.initializeUser()

// 提供全局的用户状态
provide('userStore', userStore)
</script>

<style>
#app {
  height: 100vh;
  width: 100vw;
}
</style>