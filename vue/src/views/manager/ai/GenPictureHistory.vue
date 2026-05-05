<template>
  <div class="gh-container">
    <h1 class="gh-title">生图历史</h1>

    <div v-if="loading" class="gh-loading" v-loading="loading" element-loading-text="加载中..." />

    <el-empty v-else-if="!loading && list.length === 0" description="暂无生图记录" />
    <el-empty v-else-if="!loading && list.length > 0 && filteredList.length === 0" description="无匹配结果" />

    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      closable
      class="gh-error"
      @close="errorMsg = ''"
    />

    <!-- Toolbar -->
    <div v-if="!loading && list.length" class="gh-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索提示词..."
        clearable
        class="gh-search"
        :prefix-icon="Search"
      />
      <el-select v-model="filterType" placeholder="类型" clearable class="gh-filter">
        <el-option label="全部类型" value="" />
        <el-option
          v-for="t in availableTypes"
          :key="t.value"
          :label="t.label"
          :value="t.value"
        />
      </el-select>
      <div class="gh-sort-group">
        <span class="gh-sort-label">排序</span>
        <el-select v-model="sortField" class="gh-sort-select">
          <el-option label="创建时间" value="createTime" />
          <el-option label="图片尺寸" value="dimensions" />
          <el-option label="文件名" value="filename" />
        </el-select>
        <el-button
          :icon="sortAsc ? SortUp : SortDown"
          circle
          size="small"
          @click="sortAsc = !sortAsc"
        />
      </div>
    </div>

    <!-- Grid -->
    <div v-if="!loading && filteredList.length" class="gh-grid">
      <el-card
        v-for="item in filteredList"
        :key="item.id"
        class="gh-card"
        shadow="hover"
        @click="showDetail(item)"
      >
        <img
          :src="getImageUrl(item.url)"
          class="gh-card-img"
          :alt="item.filename"
          loading="lazy"
          @load="onImgLoad($event, item.url)"
        >

        <div class="gh-tags">
          <el-tag size="small" type="primary" effect="light">
            {{ item.workflowType === 'img2img' ? '图生图' : '文生图' }}
          </el-tag>
          <el-tag v-if="dimensionsOf(item)" size="small" type="info" effect="light">
            {{ dimensionsOf(item) }}
          </el-tag>
          <el-tag v-if="modelOf(item)" size="small" type="success" effect="light">
            {{ modelOf(item) }}
          </el-tag>
        </div>

        <p v-if="item.filename" class="gh-filename">{{ item.filename }}</p>
        <p v-if="item.promptText" class="gh-prompt">{{ item.promptText }}</p>

        <div class="gh-footer">
          <span class="gh-time">{{ item.createTime }}</span>
          <div class="gh-actions" @click.stop>
            <el-button size="small" text type="primary" @click="showDetail(item)">详情</el-button>
            <el-button size="small" text type="danger" @click="deleteItem(item)">删除</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="生图详情"
      width="700px"
      destroy-on-close
    >
      <template v-if="detailItem">
        <img
          :src="getImageUrl(detailItem.url)"
          class="gh-detail-img"
          :alt="detailItem.filename"
        >

        <el-descriptions v-if="detailParams.length" :column="3" border class="gh-detail-desc">
          <el-descriptions-item
            v-for="p in detailParams"
            :key="p.key"
            :label="p.key"
            :span="p.key.length > 15 ? 3 : 1"
          >{{ p.value }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detailItem.promptText" class="gh-detail-prompt">
          <div class="gh-detail-label">正向提示词</div>
          <div class="gh-detail-text">{{ detailItem.promptText }}</div>
        </div>
        <div v-if="detailItem.negativeText" class="gh-detail-prompt">
          <div class="gh-detail-label">负向提示词</div>
          <div class="gh-detail-text">{{ detailItem.negativeText || '(空)' }}</div>
        </div>

        <p class="gh-detail-time">生成时间: {{ detailItem.createTime }}</p>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, SortUp, SortDown } from '@element-plus/icons-vue'
import { getHistory, deleteRecord, getImageUrl } from '@/api/ai/genPicture'



const loading = ref(false)
const errorMsg = ref('')
const list = ref([])
const imageDims = reactive({})  // { [url]: { width, height } }
const detailVisible = ref(false)
const detailItem = ref(null)
const detailParams = ref([])

// filters & sort
const keyword = ref('')
const filterType = ref('')
const sortField = ref('createTime')
const sortAsc = ref(false)

const availableTypes = computed(() => {
  const seen = new Set()
  const types = []
  for (const item of list.value) {
    const t = item.workflowType || ''
    if (!seen.has(t)) {
      seen.add(t)
      types.push({ value: t, label: t === 'img2img' ? '图生图' : t === 'txt2img' ? '文生图' : t })
    }
  }
  return types
})

const filteredList = computed(() => {
  let items = [...list.value]

  // filter by workflow type
  if (filterType.value) {
    items = items.filter(i => i.workflowType === filterType.value)
  }

  // filter by keyword in prompt text
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    items = items.filter(i => (i.promptText || '').toLowerCase().includes(kw))
  }

  // sort
  const field = sortField.value
  const asc = sortAsc.value
  items.sort((a, b) => {
    let va, vb
    if (field === 'dimensions') {
      va = dimSize(a); vb = dimSize(b)
    } else if (field === 'filename') {
      va = a.filename || ''; vb = b.filename || ''
      return asc ? va.localeCompare(vb) : vb.localeCompare(va)
    } else {
      // createTime — string comparison works for ISO/standard formats
      va = a.createTime || ''; vb = b.createTime || ''
    }
    if (va < vb) return asc ? -1 : 1
    if (va > vb) return asc ? 1 : -1
    return 0
  })

  return items
})

function dimSize(item) {
  try {
    const p = JSON.parse(item.paramsJson || '{}')
    if (p.width && p.height) return Number(p.width) * Number(p.height)
  } catch (_) {}
  const dims = imageDims[item.url]
  if (dims) return dims.width * dims.height
  return 0
}

onMounted(() => loadHistory())

async function loadHistory() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await getHistory()
    if (res.code !== '200') throw new Error(res.msg)
    list.value = res.data || []
  } catch (e) {
    errorMsg.value = '加载失败: ' + e.message
  } finally {
    loading.value = false
  }
}

function dimensionsOf(item) {
  try {
    const p = JSON.parse(item.paramsJson || '{}')
    if (p.width && p.height) return p.width + 'x' + p.height
  } catch (_) {}
  const dims = imageDims[item.url]
  if (dims) return dims.width + 'x' + dims.height
  return null
}

function onImgLoad(e, url) {
  const img = e.target
  if (img.naturalWidth && img.naturalHeight) {
    imageDims[url] = { width: img.naturalWidth, height: img.naturalHeight }
  }
}

function modelOf(item) {
  try {
    const p = JSON.parse(item.paramsJson || '{}')
    if (p.model) {
      const name = p.model.split('/').pop()
      return name.length > 20 ? name.substring(0, 20) + '...' : name
    }
  } catch (_) {}
  return null
}

function showDetail(item) {
  detailItem.value = item
  const arr = []
  try {
    const p = JSON.parse(item.paramsJson || '{}')
    for (const [k, v] of Object.entries(p)) {
      arr.push({ key: k, value: String(v) })
    }
  } catch (_) {}
  detailParams.value = arr
  detailVisible.value = true
}

async function deleteItem(item) {
  try {
    await ElMessageBox.confirm('确定删除这张图片吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (_) {
    return
  }
  try {
    const res = await deleteRecord(item.id)
    if (res.code === '200') {
      list.value = list.value.filter(i => i.id !== item.id)
      ElMessage.success('已删除')
    } else {
      ElMessage.error('删除失败: ' + (res.msg || ''))
    }
  } catch (e) {
    ElMessage.error('删除失败: ' + e.message)
  }
}
</script>

<style scoped>
.gh-container { max-width: 1200px; margin: 0 auto; padding: 0 4px; }
.gh-title { font-size: 22px; margin: 0 0 16px 0; font-weight: 600; }

.gh-loading { text-align: center; padding: 60px 0; }
.gh-error { margin-bottom: 12px; }

.gh-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.gh-search { flex: 1; min-width: 180px; }
.gh-filter { width: 130px; }
.gh-sort-group {
  display: flex;
  align-items: center;
  gap: 6px;
}
.gh-sort-label { font-size: 13px; color: #909399; white-space: nowrap; }
.gh-sort-select { width: 110px; }

.gh-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.gh-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.gh-card:hover { transform: translateY(-2px); }
.gh-card :deep(.el-card__body) { padding: 0; }

.gh-card-img {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  display: block;
  background: var(--el-border-color-light);
}

.gh-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  padding: 0 14px;
  margin-top: 10px;
}

.gh-filename {
  font-size: 12px;
  color: #606266;
  margin: 6px 14px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gh-prompt {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 8px 14px;
}

.gh-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 14px 14px;
  margin-top: 4px;
}
.gh-time { font-size: 12px; color: #c0c4cc; }
.gh-actions { display: flex; gap: 4px; }

/* detail dialog */
.gh-detail-img { max-width: 100%; border-radius: 8px; margin-bottom: 14px; }
.gh-detail-desc { margin-bottom: 12px; }
.gh-detail-prompt {
  padding: 10px 14px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 10px;
}
.gh-detail-label { font-weight: 600; margin-bottom: 4px; }
.gh-detail-text { color: #606266; }
.gh-detail-time { font-size: 12px; color: #c0c4cc; margin: 12px 0 0; }
</style>
