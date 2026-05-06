<template>
  <div class="manager-container">
    <div class="manager-header">
      <div class="manager-header-left">
        <img src="@/assets/imgs/logo.png" alt="">
        <div class="title">照片管理系统</div>
      </div>
      <div class="manager-header-center">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item to="/manager/home">首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ router.currentRoute.value.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="manager-header-right">
        <el-dropdown style="cursor: pointer;">
          <div style="padding-right: 20px; display: flex; align-items: center;">
            <img v-if="data.user.avatar" :src="data.user?.avatar" alt="" style="width: 40px; height: 40px; display: block; border-radius: 50%">
            <img v-else src="@/assets/imgs/avatar.png" alt="" style="width: 40px; height: 40px; display: block; border-radius: 50%">
            <span style="margin-left: 5px">{{ data.user?.name }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click.native="router.push('/manager/person')">个人资料</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/manager/password')">修改密码</el-dropdown-item>
              <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div style="display: flex">
      <div class="manager-main-left">
        <el-menu
            :default-active="router.currentRoute.value.path"
            :default-openeds="['1', '2', '3']"
            router
        >
          <el-menu-item index="/manager/home">
            <el-icon><home-filled /></el-icon><span>系统首页</span>
          </el-menu-item>
          <el-menu-item index="/manager/albums">
            <el-icon><Picture /></el-icon><span>相册广场</span>
          </el-menu-item>
          <el-sub-menu index="1">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span>个人空间</span>
            </template>
            <el-menu-item index="/manager/notice" v-if="data.user.role === 'ADMIN'">系统公告</el-menu-item>
            <!-- 新菜单 -->
            <el-menu-item index="/manager/category" v-if="data.user.role === 'ADMIN'">相册信息</el-menu-item>
            <el-menu-item index="/manager/category" v-else>我的相册</el-menu-item>
            <el-menu-item index="/manager/picture" v-if="data.user.role === 'ADMIN'">照片信息</el-menu-item>
            <el-menu-item index="/manager/picture" v-else>我的照片</el-menu-item>
            <el-menu-item index="/manager/appeal">申诉信息</el-menu-item>
            <el-menu-item index="/manager/collect" v-if="data.user.role === 'ADMIN'">收藏信息</el-menu-item>
            <el-menu-item index="/manager/collect" v-else>我的收藏</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="2" v-if="data.user.role === 'ADMIN'">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>用户信息</span>
            </template>
            <el-menu-item index="/manager/admin">管理员信息</el-menu-item>
            <el-menu-item index="/manager/user">用户信息</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="3">
            <template #title>
              <el-icon><Cpu /></el-icon>
              <span>AI创作</span>
            </template>
            <el-menu-item index="/manager/ai-chat">AI对话</el-menu-item>
            <el-menu-item index="/manager/ai-gen-picture">AI生图</el-menu-item>
            <el-menu-item index="/manager/ai-gen-picture-history">生图历史</el-menu-item>
            <el-menu-item index="/manager/ai-gen-picture-comfyui">ComfyUI节点编辑</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/manager/air-condition">
            <el-icon><Sunny /></el-icon><span>便携小空调</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="manager-main-right">
        <router-view @updateUser="updateUser" />
      </div>
    </div>

  </div>
</template>

<script setup>
import {reactive} from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";

const data = reactive({
  user:  JSON.parse(localStorage.getItem('xm-user') || '{}')
})

const logout = () => {
  localStorage.removeItem('xm-user')
  router.push('/login')
}

if (!data.user.id) {
  logout()
  ElMessage.error('请登录!')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('xm-user') || '{}')   // 重新获取下用户的最新信息
}
</script>

<style scoped>
@import '@/assets/css/manager.css';
</style>
