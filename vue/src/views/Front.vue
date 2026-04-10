<template>
  <div>
    <!--头部-->
    <div class="front-header">
      <div class="front-header-left">
        <img src="@/assets/imgs/logo.png" alt="">
        <div class="title">项目前台</div>
      </div>
      <div class="front-header-center">
        <div class="front-header-nav">
          <el-menu :default-active="router.currentRoute.value.path" mode="horizontal" router>
						<el-menu-item index="/front/home">首页</el-menu-item>
						<el-menu-item index="/front/person">个人中心</el-menu-item>
          </el-menu>
        </div>
      </div>
      <div class="front-header-right">
        <div v-if="!data.user.username">
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button @click="router.push('/register')">注册</el-button>
        </div>
        <div v-else>
          <el-dropdown>
            <div class="front-header-dropdown">
              <img :src="data.user.avatar" alt="">
              <div style="margin-left: 10px">
                <span>{{ data.user.name }}</span><el-icon><arrow-down /></el-icon>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click.native="logout">退出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
    <!--主体-->
    <div>
      <router-view ref="child" @updateUser="updateUser" />
    </div>
  </div>

</template>

<script setup>
import {reactive} from "vue";
import router from "@/router";

const data = reactive({
  user: JSON.parse(localStorage.getItem("xm-user") || '{}'),
})

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('xm-user') || '{}')   // 重新获取下用户的最新信息
}

// 退出登录
const logout = () => {
  localStorage.removeItem("xm-user");
  router.push("/login");
}
</script>

<style scoped>
  @import "@/assets/css/front.css";
</style>