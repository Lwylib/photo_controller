<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.categoryName" placeholder="请输入相册名称查询" style="width: 240px; margin-right: 5px"></el-input>
      <el-input v-model="data.userName" placeholder="请输入用户姓名查询" v-if="data.user.role === 'ADMIN'" style="width: 240px"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load">查询</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px;">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="相册封面" width="100">
          <template #default="scope">
            <el-image style="width: 40px; height: 40px; border-radius: 5px" v-if="scope.row.categoryImg" :src="scope.row.categoryImg" :preview-src-list="[scope.row.categoryImg]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户姓名" width="150"></el-table-column>
        <el-table-column prop="categoryName" label="相册名称" width="200">
          <template v-slot="scope">
            <div style="cursor: pointer; color: #4486ea" @click="navTo('/manager/pictures?id=' + scope.row.categoryId)">{{ scope.row.categoryName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="申诉内容" prop="content" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="申诉结果" width="200">
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === '待审核'" type="warning">{{ scope.row.status }}</el-tag>
            <el-tag v-if="scope.row.status === '审核通过'" type="success">{{ scope.row.status }}</el-tag>
            <el-tag v-if="scope.row.status === '审核拒绝'" type="danger">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" v-if="data.user.role === 'ADMIN'">
          <template v-slot="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>

    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog title="" v-model="data.formVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="80px"  style="padding: 20px 30px" ref="formRef">
        <el-form-item label="申诉结果" prop="status">
          <el-radio-group v-model="data.form.status">
            <el-radio-button label="审核通过" value="审核通过" />
            <el-radio-button label="审核拒绝" value="审核拒绝" />
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="update">保存</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive } from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
import {onBeforeUnmount, shallowRef} from "vue";

const baseUrl = import.meta.env.VITE_BASE_URL
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  tableData: [],
  total: 0,
  pageNum: 1,  // 当前的页码
  pageSize: 5,  // 每页的个数
  formVisible: false,
  form: {},
  categoryName: null,
  userName: null,
})

const navTo = (url) => {
  location.href = url
}

// 加载表格数据
const load = () => {
  request.get('/appeal/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      categoryName: data.categoryName,
      userName: data.userName
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total
  })
}

// 打开编辑弹窗
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}


// 更新
const update = () => {
  request.put('/appeal/update', data.form).then(res => {
    if (res.code === '200') {
      data.formVisible = false
      ElMessage.success('操作成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const reset = () => {
  data.categoryName = null
  data.userName = null
  load()
}

load()

</script>