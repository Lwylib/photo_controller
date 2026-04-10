<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" placeholder="请输入关键字查询" style="width: 240px"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load">查询</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px" v-if="data.user.role === 'USER'">
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px;">
      <el-table :data="data.tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="照片图片">
          <template #default="scope">
            <el-image style="width: 40px; height: 40px; border-radius: 5px" v-if="scope.row.img" :src="scope.row.img" :preview-src-list="[scope.row.img]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="照片名称"></el-table-column>
        <el-table-column prop="description" label="照片描述" show-overflow-tooltip></el-table-column>
        <el-table-column prop="userName" label="用户姓名"></el-table-column>
        <el-table-column prop="categoryName" label="相册名称"></el-table-column>
        <el-table-column prop="time" label="上传时间"></el-table-column>
        <el-table-column prop="roleRadio" label="照片权限"></el-table-column>
        <el-table-column prop="statusRadio" label="照片状态">
          <template v-slot="scope">
            <el-tag type="warning" v-if="scope.row.statusRadio === '待审核'">{{ scope.row.statusRadio }}</el-tag>
            <el-tag type="success" v-if="scope.row.statusRadio === '审核通过'">{{ scope.row.statusRadio }}</el-tag>
            <el-tag type="danger" v-if="scope.row.statusRadio === '审核拒绝'">{{ scope.row.statusRadio }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>

    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog title="照片信息" v-model="data.formVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form :rules="data.rules" :model="data.form" label-width="80px"  style="padding: 20px 30px" ref="formRef">
        <el-form-item label="照片图片" prop="img" v-if="data.user.role === 'USER'">
          <el-upload
            :action="baseUrl + '/files/upload'"
            :headers="{ token: data.user.token }"
            list-type="picture"
            :on-success="handleImgSuccess"
          >
            <el-button type="primary">上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="照片名称" prop="name" v-if="data.user.role === 'USER'">
          <el-input v-model="data.form.name" placeholder="照片名称"></el-input>
        </el-form-item>
        <el-form-item label="照片描述" prop="description" v-if="data.user.role === 'USER'">
          <el-input type="textarea" :rows="3" v-model="data.form.description" placeholder="照片描述"></el-input>
        </el-form-item>
        <el-form-item label="选择相册" prop="categoryId" v-if="data.user.role === 'USER'">
          <el-select style="width: 100%" v-model="data.form.categoryId">
            <el-option v-for="item in data.categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="照片权限" prop="roleRadio" v-if="data.user.role === 'USER'">
          <el-radio-group v-model="data.form.roleRadio">
            <el-radio-button label="公开" value="公开" />
            <el-radio-button label="私有" value="私有" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="照片状态" prop="statusRadio" v-if="data.user.role === 'ADMIN'">
          <el-radio-group v-model="data.form.statusRadio">
            <el-radio-button label="待审核" value="待审核" />
            <el-radio-button label="审核通过" value="审核通过" />
            <el-radio-button label="审核拒绝" value="审核拒绝" />
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

  </div>
</template>

<script setup>
import { reactive, ref } from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
const baseUrl = import.meta.env.VITE_BASE_URL
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  tableData: [],
  total: 0,
  pageNum: 1,  // 当前的页码
  pageSize: 5,  // 每页的个数
  formVisible: false,
  form: {},
  ids: [],
  name: null,
  categoryList: [],
  rules: {
    img: [
      { required: true, message: '请上传图片', trigger: 'blur' },
    ],
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' },
    ],
    categoryId: [
      { required: true, message: '请选择相册', trigger: 'blur' },
    ],
  }
})

const formRef = ref()

const handleImgSuccess = (res) => {
  data.form.img = res.data
}

const loadCategory = () => {
  request.get('/category/selectAll', {
    params: {
      userId: data.user.id
    }
  }).then(res => {
    if (res.code === '200') {
      data.categoryList = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadCategory()
// 加载表格数据
const load = () => {
  let userId = data.user.role !== 'ADMIN' ? data.user.id : null
  request.get('/picture/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      userId: userId
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total
  })
}

// 打开新增弹窗
const handleAdd = () => {
  data.form = {}
  data.form.userId = data.user.id
  data.form.statusRadio = '待审核'
  data.form.roleRadio = '公开'
  data.formVisible = true
}

// 打开编辑弹窗
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增
const add = () => {
  request.post('/picture/add', data.form).then(res => {
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
  request.put('/picture/update', data.form).then(res => {
    if (res.code === '200') {
      data.formVisible = false
      ElMessage.success('操作成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 删除
const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/picture/delete/' + id).then(res => {
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

// 批量删除
const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/picture/delete/batch', {data: data.ids}).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()  // 刷新表格数据
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => console.log(err))
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
  load()
}

load()

</script>