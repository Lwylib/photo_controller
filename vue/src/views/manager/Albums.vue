<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" placeholder="请输入相册名称查询" style="width: 240px; margin-right: 5px"></el-input>
      <el-select v-model="data.sortType" placeholder="排序方式" style="width: 120px; margin-right: 5px">
        <el-option label="默认排序" value="default"></el-option>
        <el-option label="热度排序" value="hot"></el-option>
      </el-select>
      <el-button type="info" plain style="margin-left: 10px" @click="load">查询</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div style="margin: 10px 0">
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in data.tableData" style="margin-bottom: 20px">
          <div style="display: flex; grid-gap: 10px" class="card">
            <img :src="item.img" alt="" style="width: 200px; height: 200px; border-radius: 5px; cursor: pointer" @click="navTo('/manager/pictures?id=' + item.id)">
            <div style="flex: 1">
              <div style="font-size: 18px">{{ item.name }}</div>
              <div style="margin-top: 10px; display: flex; align-items: center; grid-gap: 10px">
                <img :src="item.userAvatar" alt="" style="width: 30px; height: 30px; border-radius: 50%">
                <div style="font-size: 15px">{{ item.userName }}</div>
              </div>
              <div style="margin-top: 10px; color: #666666; text-align: justify; height: 60px; line-height: 20px" class="line3">{{ item.description }}</div>
              <div style="margin-top: 5px; color: #666666">创建时间：{{ item.time }}</div>
              <!-- 第一行：状态标签 -->
              <div style="margin-top: 10px; display: flex; align-items: center; grid-gap: 10px; flex-wrap: wrap;">
                <el-tag v-if="item.roleRadio === '公开'" size="large" type="success">{{ item.roleRadio }}</el-tag>
                <el-tag v-else type="danger" size="large">{{ item.roleRadio }}</el-tag>
                <el-tag v-if="item.statusRadio === '正常'" size="large" type="success">{{ item.statusRadio }}</el-tag>
                <el-tag v-else type="danger" size="large">{{ item.statusRadio }}</el-tag>
              </div>
              
              <!-- 第二行：热度信息和操作按钮 -->
              <div style="margin-top: 10px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap;">
                <!-- 热度信息 -->
                <div style="display: flex; align-items: center; grid-gap: 10px;">
                  <el-tooltip content="浏览量" placement="top">
                    <div style="display: flex; align-items: center; color: #666;">
                      <el-icon><View /></el-icon>
                      <span style="margin-left: 2px">{{ item.viewCount || 0 }}</span>
                    </div>
                  </el-tooltip>
                  <el-tooltip content="收藏数" placement="top">
                    <div style="display: flex; align-items: center; color: #666;">
                      <el-icon><Star /></el-icon>
                      <span style="margin-left: 2px">{{ item.collectCount || 0 }}</span>
                    </div>
                  </el-tooltip>
                  <el-tooltip content="热度值" placement="top">
                    <el-tag type="warning" size="small">{{ item.hotPoint || 0 }}</el-tag>
                  </el-tooltip>
                </div>
                
                <!-- 操作按钮 -->
                <div style="display: flex; align-items: center; grid-gap: 5px;">
                  <!-- 导出按钮 -->
                  <el-button type="primary" size="small" @click="exportAlbum(item)" :loading="data.exportLoading[item.id]">
                    <el-icon><Download /></el-icon>
                    导出
                  </el-button>
                  
                  <!-- 管理员操作按钮 -->
                  <div v-if="data.user.role === 'ADMIN'" style="margin-left: 5px">
                    <el-button v-if="item.statusRadio !== '违规'" size="small" type="danger" @click="markAsViolated(item)">标记违规</el-button>
                    <el-button v-else size="small" type="success" @click="markAsNormal(item)">恢复正常</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

  </div>
</template>

<script setup>
import { reactive } from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Delete, Edit, View, Star, Download} from "@element-plus/icons-vue";
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  tableData: [],
  total: 0,
  pageNum: 1,  // 当前的页码
  pageSize: 6,  // 每页的个数
  name: null,
  sortType: 'default', // 排序方式：default-默认，hot-热度
  exportLoading: {}, // 导出加载状态
})

const navTo = (url) => {
  location.href = url
}

// 加载表格数据
const load = () => {
  const apiUrl = data.sortType === 'hot' ? '/category/selectHotAlbumPage' : '/category/selectAlbumPage'
  request.get(apiUrl, {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      statusRadio: '正常',
      roleRadio: '公开'
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total
  })
}


const reset = () => {
  data.name = null
  load()
}

// 标记相册为违规
const markAsViolated = (item) => {
  ElMessageBox.confirm(`确定要将相册《${item.name}》标记为违规吗？`, '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.put('/category/updateStatus', {
      id: item.id,
      statusRadio: '违规'
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功');
        item.statusRadio = '违规';
      } else {
        ElMessage.error(res.msg);
      }
    })
  }).catch(() => {
    // 用户取消操作
  });
}

// 标记相册为正常
const markAsNormal = (item) => {
  ElMessageBox.confirm(`确定要将相册《${item.name}》恢复正常吗？`, '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    request.put('/category/updateStatus', {
      id: item.id,
      statusRadio: '正常'
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功');
        item.statusRadio = '正常';
      } else {
        ElMessage.error(res.msg);
      }
    })
  }).catch(() => {
    // 用户取消操作
  });
}

// 导出相册
const exportAlbum = (item) => {
  // 设置导出加载状态
  data.exportLoading[item.id] = true;
  
  // 创建隐藏的下载链接
  const link = document.createElement('a');
  link.style.display = 'none';
  
  // 构建完整的下载URL，包含token
  const downloadUrl = request.defaults.baseURL + '/export/album/' + item.id;
  
  // 使用fetch API发送带token的请求
  fetch(downloadUrl, {
    headers: {
      'token': data.user.token
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error('导出失败');
    }
    return response.blob();
  })
  .then(blob => {
    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    link.href = url;
    link.setAttribute('download', `album_${item.id}.zip`);
    document.body.appendChild(link);
    link.click();
    
    // 清理
    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);
    
    ElMessage.success('相册导出成功');
  })
  .catch(error => {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  })
  .finally(() => {
    // 清除加载状态
    data.exportLoading[item.id] = false;
  });
}

load()

</script>