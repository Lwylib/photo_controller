<template>
  <div style="width: 40%; margin: 0 auto; padding-top: 20px">
    <div class="card">
      <el-form ref="formRef" :model="data.user" label-width="80px" style="padding: 20px 30px">
        <div style="text-align: center; margin: 15px">
          <el-upload
              class="avatar-uploader"
              :action="baseUrl + '/files/upload'"
              :on-success="handleFileUpload"
              :show-file-list="false"
          >
            <img v-if="data.user.avatar" alt="" :src="data.user.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </div>
        <el-form-item prop="username" label="用户名" >
          <el-input v-model="data.user.username" disabled />
        </el-form-item>
        <el-form-item prop="name" label="姓名">
          <el-input v-model="data.user.name" />
        </el-form-item>
        <el-form-item prop="phone" label="电话">
          <el-input v-model="data.user.phone" />
        </el-form-item>
        <el-form-item prop="email" label="邮箱">
          <el-input v-model="data.user.email" />
        </el-form-item>
        <div style="text-align: center">
          <el-button size="large" style="width: 150px" type="primary" @click="update">保存</el-button>
        </div>
      </el-form>
    </div>

  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user:  JSON.parse(localStorage.getItem('xm-user') || '{}')
})

const emit = defineEmits(["updateUser"])
const update = () => {
  if (data.user.role === 'ADMIN') {
    request.put('/admin/update', data.user).then(res => {
      if (res.code === '200') {
        ElMessage.success('更新成功')
        localStorage.setItem('xm-user', JSON.stringify(data.user))
        emit('updateUser')
      } else {
        ElMessage.success(res.msg)
      }
    })
  }
}

const handleFileUpload = (res) => {
  data.user.avatar = res.data
}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>