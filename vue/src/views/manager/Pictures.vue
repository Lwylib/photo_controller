<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" placeholder="请输入照片名称查询" style="width: 240px"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load">查询</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px; display: flex; justify-content: center; grid-gap: 50px; padding: 20px" v-if="data.categoryData.name">
      <div style="width: 250px">
        <img :src="data.categoryData.img" alt="" style="width: 100%; height: 250px; border-radius: 5px">
        <div style="font-size: 18px" class="line1">{{ data.categoryData.name }}</div>
        <div style="margin-top: 10px; display: flex; align-items: center; grid-gap: 10px">
          <img :src="data.categoryData.userAvatar" alt="" style="width: 30px; height: 30px; border-radius: 50%">
          <div style="font-size: 15px">{{ data.categoryData.userName }}</div>
          <div><el-tag type="success">{{ data.categoryData.roleRadio }}</el-tag></div>
        </div>
        <div style="margin-top: 15px; color: #666666; text-align: justify">{{ data.categoryData.description }}</div>
        <div style="margin-top: 10px; color: #666666">创建时间：{{ data.categoryData.time }}</div>
        <div style="margin-top: 20px; text-align: center">
          <el-button type="primary" style="padding: 20px 30px" v-if="data.user.role === 'USER'" @click="collect(data.categoryData.id)">收藏相册</el-button>
        </div>
      </div>
      <div style="flex: 1">
        <el-row :gutter="10" v-if="data.tableData.length">
          <el-col :span="6" v-for="item in data.tableData" style="margin-bottom: 20px">
            <el-image style="width: 100%; height: 250px; border-radius: 5px; display: block" v-if="item.img"
                      :src="item.img" :preview-src-list="[item.img]" preview-teleported></el-image>
            <div style="font-size: 16px; margin-top: 5px" class="line1">{{ item.name }}</div>
          </el-col>
        </el-row>
        <div v-else style="text-align: center; font-size: 20px; color: #666666">空空如也……该相册目前还没有上传照片哦~</div>
      </div>
    </div>
    <div v-else></div>
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
import {Delete, Edit} from "@element-plus/icons-vue";
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  tableData: [],
  total: 0,
  pageNum: 1,  // 当前的页码
  pageSize: 10,  // 每页的个数
  form: {},
  name: null,
  categoryData: {},
  categoryId: router.currentRoute.value.query.id
})

const loadCategory = () => {
  request.get('/category/selectById/' + data.categoryId).then(res => {
    if (res.code === '200') {
      data.categoryData = res.data
      load()
      // 增加相册浏览量
      increaseViewCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 增加相册浏览量
const increaseViewCount = () => {
  request.put('/category/increaseViewCount/' + data.categoryId).then(res => {
    if (res.code === '200') {
      console.log('浏览量增加成功')
    }
  })
}
loadCategory()

// 加载表格数据
const load = () => {
  request.get('/picture/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      categoryId: data.categoryId,
      statusRadio: '审核通过',
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

const collect = (categoryId) => {
  request.post('/collect/add', {
    userId: data.user.id,
    categoryId: categoryId
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('收藏成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}


</script>
<style>
.el-col-5 {
  width: 20%;
  max-width: 20%;
}
</style>