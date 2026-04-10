<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" placeholder="请输入相册名称查询" style="width: 240px; margin-right: 5px"></el-input>
      <el-input v-model="data.userName" placeholder="请输入用户姓名查询" v-if="data.user.role === 'ADMIN'" style="width: 240px"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load">查询</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px" v-if="data.user.role === 'USER'">
      <el-button type="primary" plain @click="handleAdd">创建相册</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px;" v-if="data.user.role === 'ADMIN'">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="相册封面">
          <template #default="scope">
            <el-image style="width: 40px; height: 40px; border-radius: 5px" v-if="scope.row.img" :src="scope.row.img" :preview-src-list="[scope.row.img]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="相册名称"></el-table-column>
        <el-table-column prop="userName" label="用户姓名"></el-table-column>
        <el-table-column prop="description" label="相册描述" show-overflow-tooltip></el-table-column>
        <el-table-column prop="time" label="创建时间"></el-table-column>
        <el-table-column prop="roleRadio" label="相册权限"></el-table-column>
        <el-table-column prop="statusRadio" label="状态">
          <template v-slot="scope">
            <el-tag v-if="scope.row.statusRadio === '正常'" type="success">{{ scope.row.statusRadio }}</el-tag>
            <el-tag v-if="scope.row.statusRadio === '违规'" type="danger">{{ scope.row.statusRadio }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div v-else style="margin: 10px 0">
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in data.tableData">
          <div style="display: flex; grid-gap: 10px" class="card">
            <img :src="item.img" alt="" style="width: 200px; height: 200px; border-radius: 5px; cursor: pointer" @click="navTo('/manager/pictures?id=' + item.id)">
            <div style="flex: 1">
              <div style="font-size: 18px">{{ item.name }}</div>
              <div style="margin-top: 10px; color: #666666; text-align: justify; height: 100px; line-height: 20px" class="line5">{{ item.description }}</div>
              <div style="margin-top: 5px; color: #666666">创建时间：{{ item.time }}</div>
              <div style="margin-top: 10px; display: flex; align-items: center; grid-gap: 10px">
                <el-tag v-if="item.roleRadio === '公开'" size="large" type="success">{{ item.roleRadio }}</el-tag>
                <el-tag v-else type="danger" size="large">{{ item.roleRadio }}</el-tag>
                <el-tag v-if="item.statusRadio === '正常'" size="large" type="success">{{ item.statusRadio }}</el-tag>
                <el-tag v-else type="danger" size="large">{{ item.statusRadio }}</el-tag>
                <el-icon size="30" style="cursor: pointer; color: #4486ea" @click="handleEdit(item)"><Edit /></el-icon>
                <el-icon size="26" style="cursor: pointer; color: red" @click="del(item.id)"><Delete /></el-icon>
                <el-icon size="26" style="cursor: pointer; color: #30cbc5" @click="handleSubmit(item)" v-if="item.statusRadio === '违规'"><Position /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog title="相册信息" v-model="data.formVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form :rules="data.rules" :model="data.form" label-width="80px"  style="padding: 20px 30px" ref="formRef">
        <el-form-item label="相册封面" prop="img" v-if="data.user.role === 'USER'">
          <el-upload
            :action="baseUrl + '/files/upload'"
            :headers="{ token: data.user.token }"
            list-type="picture"
            :on-success="handleImgSuccess"
          >
            <el-button type="primary">上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="相册名称" prop="name" v-if="data.user.role === 'USER'">
          <el-input v-model="data.form.name" placeholder="相册名称"></el-input>
        </el-form-item>
        <el-form-item label="相册描述" prop="description" v-if="data.user.role === 'USER'">
          <el-input type="textarea" :rows="4" v-model="data.form.description" placeholder="相册描述"></el-input>
        </el-form-item>
        <el-form-item label="相册权限" prop="roleRadio" v-if="data.user.role === 'USER'">
          <el-radio-group v-model="data.form.roleRadio">
            <el-radio-button label="公开" value="公开" />
            <el-radio-button label="私有" value="私有" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="相册状态" prop="statusRadio" v-if="data.user.role === 'ADMIN'">
          <el-radio-group v-model="data.form.statusRadio">
            <el-radio-button label="正常" value="正常" />
            <el-radio-button label="违规" value="违规" />
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog title="申诉信息" v-model="data.appealVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="80px"  style="padding: 20px 30px" ref="formRef">
        <el-form-item label="申诉内容" prop="description">
          <el-input type="textarea" :rows="4" v-model="data.form.content" placeholder="申诉内容"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.appealVisible = false">取消</el-button>
          <el-button type="primary" @click="appeal">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
import router from "@/router/index.js";
const baseUrl = import.meta.env.VITE_BASE_URL
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  tableData: [],
  total: 0,
  pageNum: 1,  // 当前的页码
  pageSize: 6,  // 每页的个数
  formVisible: false,
  appealVisible: false,
  form: {},
  ids: [],
  name: null,
  userName: null,
  userList: [],
  rules: {
    img: [
      { required: true, message: '请上传图片', trigger: 'blur' },
    ],
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' },
    ],
    description: [
      { required: true, message: '请输入描述', trigger: 'blur' },
    ],
  }
})

const formRef = ref()

const handleImgSuccess = (res) => {
  data.form.img = res.data
}

const navTo = (url) => {
  location.href = url
}

// 加载表格数据
const load = () => {
  request.get('/category/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      userName: data.userName
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total
  })
}

// 打开新增弹窗
const handleAdd = () => {
  data.form = {}
  data.form.status = '正常'
  data.form.userId = data.user.id
  data.form.roleRadio = '公开'
  data.formVisible = true
}

// 打开编辑弹窗
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}
// 打开申诉弹框
const handleSubmit = (row) => {
  data.form = {}
  data.form.userId = row.userId
  data.form.categoryId = row.id
  data.form.status = '待审核'
  data.appealVisible = true
}

// 新增
const add = () => {
  request.post('/category/add', data.form).then(res => {
    if (res.code === '200') {
      data.formVisible = false
      ElMessage.success('操作成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 更新
const update = () => {
  request.put('/category/update', data.form).then(res => {
    if (res.code === '200') {
      data.formVisible = false
      ElMessage.success('操作成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const appeal = () => {
  request.post('/appeal/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('提交成功，等待管理员审核')
      data.appealVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 删除
const del = (id) => {
  ElMessageBox.confirm('删除相册，里面的图片也会被删除，数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/category/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}


const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add()
    }
  })
}

const reset = () => {
  data.name = null
  data.userName = null
  load()
}

load()

</script>