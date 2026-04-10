<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" placeholder="请输入相册名称查询" style="width: 240px; margin-right: 5px"></el-input>
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
              <div style="margin-top: 10px; display: flex; align-items: center; grid-gap: 10px">
                <el-tag v-if="item.roleRadio === '公开'" size="large" type="success">{{ item.roleRadio }}</el-tag>
                <el-tag v-else type="danger" size="large">{{ item.roleRadio }}</el-tag>
                <el-tag v-if="item.statusRadio === '正常'" size="large" type="success">{{ item.statusRadio }}</el-tag>
                <el-tag v-else type="danger" size="large">{{ item.statusRadio }}</el-tag>
                <!-- 管理员操作按钮 -->
                <div v-if="data.user.role === 'ADMIN'" style="margin-left: auto">
                  <el-button v-if="item.statusRadio !== '违规'" size="small" type="danger" @click="markAsViolated(item)">标记违规</el-button>
                  <el-button v-else size="small" type="success" @click="markAsNormal(item)">恢复正常</el-button>
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
import {Delete, Edit} from "@element-plus/icons-vue";
import router from "@/router/index.js";
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  tableData: [],
  total: 0,
  pageNum: 1,  // 当前的页码
  pageSize: 6,  // 每页的个数
  name: null,
})

const navTo = (url) => {
  location.href = url
}

// 加载表格数据
const load = () => {
  request.get('/category/selectAlbumPage', {
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

load()

</script>