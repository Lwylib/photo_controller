<template>
  <div class="gp-container">
    <!-- Tabs -->
    <el-tabs v-model="currentTab" class="gp-tabs" @tab-change="switchTab">
      <el-tab-pane
        v-for="wt in workflowTypes"
        :key="wt.id"
        :label="wt.name"
        :name="String(wt.id)"
      />
    </el-tabs>

    <!-- Loading -->
    <div v-if="loading" class="gp-loading" v-loading="loading" element-loading-text="加载工作流...">
      <span style="display:inline-block;height:120px"></span>
    </div>

    <!-- Error -->
    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      closable
      class="gp-error"
      @close="errorMsg = ''"
    />

    <!-- Dynamic Editor -->
    <template v-if="!loading && parsedNodes.length">
      <el-card v-for="node in parsedNodes" :key="node.nodeId" class="gp-card">
        <template #header><span class="gp-card-title">{{ node.title }}</span></template>

        <div
          v-for="field in node.fields"
          :key="field.key"
          :class="['gp-form-item', { 'gp-form-full': field.type === 'upload' }]"
        >
          <!-- Upload (LoadImage) -->
          <template v-if="field.type === 'upload'">
            <label class="gp-label">{{ field.key }}</label>
            <div
              class="gp-upload-area"
              :class="{ 'has-image': uploadStates[node.nodeId]?.previewUrl }"
              @click="activeUploadNodeId = node.nodeId; $refs.fileInput.click()"
              @dragover.prevent
              @drop.prevent="e => handleDrop(e, node.nodeId)"
            >
              <template v-if="!uploadStates[node.nodeId]?.previewUrl">
                <el-icon class="gp-upload-icon"><Upload /></el-icon>
                <span class="gp-upload-hint">点击或拖拽上传图片</span>
              </template>
              <template v-else>
                <img :src="uploadStates[node.nodeId].previewUrl" class="gp-upload-preview" alt="preview">
                <span class="gp-upload-clear" @click.stop="clearUpload(node.nodeId)">清除图片</span>
              </template>
            </div>
          </template>

          <!-- CKPT text input -->
          <template v-else-if="field.type === 'ckpt'">
            <label class="gp-label">{{ field.key }}</label>
            <el-input v-model="formValues[node.nodeId][field.key]" placeholder="输入模型文件名，如 xx.safetensors" />
          </template>

          <!-- Sampler select -->
          <template v-else-if="field.type === 'sampler'">
            <label class="gp-label">{{ field.key }}</label>
            <el-select v-model="formValues[node.nodeId][field.key]" class="gp-input-full">
              <el-option v-for="s in samplerOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </template>

          <!-- Scheduler select -->
          <template v-else-if="field.type === 'scheduler'">
            <label class="gp-label">{{ field.key }}</label>
            <el-select v-model="formValues[node.nodeId][field.key]" class="gp-input-full">
              <el-option v-for="s in schedulerOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </template>

          <!-- Textarea -->
          <template v-else-if="field.type === 'textarea'">
            <label class="gp-label">{{ field.key }}</label>
            <el-input v-model="formValues[node.nodeId][field.key]" type="textarea" :rows="field.rows || 2" />
          </template>

          <!-- Number -->
          <template v-else-if="field.type === 'number'">
            <label class="gp-label">{{ field.key }}</label>
            <el-input-number v-model="formValues[node.nodeId][field.key]" :controls="false" class="gp-input-full" />
          </template>

          <!-- Boolean -->
          <template v-else-if="field.type === 'boolean'">
            <label class="gp-label">{{ field.key }}</label>
            <el-switch v-model="formValues[node.nodeId][field.key]" />
          </template>

          <!-- Default text -->
          <template v-else>
            <label class="gp-label">{{ field.key }}</label>
            <el-input v-model="formValues[node.nodeId][field.key]" />
          </template>
        </div>
      </el-card>
    </template>

    <!-- Generate Button -->
    <el-button
      v-if="!loading && parsedNodes.length"
      type="primary"
      class="gp-generate-btn"
      :loading="isGenerating"
      :disabled="isGenerating"
      size="large"
      @click="startGeneration"
    >
      {{ isGenerating ? '生成中，请耐心等待...' : '生成图片' }}
    </el-button>

    <!-- Progress -->
    <el-card v-if="isGenerating" class="gp-card">
      <template #header><span class="gp-card-title">生成进度</span></template>
      <div class="gp-loading-spinner">
        <el-icon class="gp-spin-icon"><Loading /></el-icon>
        <p class="gp-progress-text">等待 ComfyUI 生成中...</p>
      </div>
    </el-card>

    <!-- Result -->
    <el-card v-if="generationResult" class="gp-card">
      <template #header><span class="gp-card-title">生成结果</span></template>
      <div v-if="generationResult.files && generationResult.files.length" class="gp-results">
        <div v-for="(f, i) in generationResult.files" :key="i" class="gp-result-item">
          <img :src="getImageUrl(f.url)" class="gp-result-img" alt="result">
          <a :href="getImageUrl(f.url)" :download="f.filename" class="gp-result-dl">下载原图</a>
        </div>
      </div>
      <span v-else class="gp-text-secondary">未获取到图片</span>
    </el-card>

    <!-- Hidden file input -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      style="display:none"
      @change="onFileChange"
    >
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { Upload, Loading } from '@element-plus/icons-vue'
import { getWorkflow, getWorkflowTypes, uploadImage, generateImage, getImageUrl, getModels } from '@/api/ai/genPicture'

const router = useRouter()
const modelDict = ref([])

// ── well-known option lists ──
const samplerOptions = [
  'euler', 'euler_ancestral', 'heun', 'heunpp2', 'dpm_2', 'dpm_2_ancestral',
  'lms', 'dpm_fast', 'dpm_adaptive', 'dpmpp_2s_ancestral', 'dpmpp_sde', 'dpmpp_sde_gpu',
  'dpmpp_2m', 'dpmpp_2m_sde', 'dpmpp_2m_sde_gpu', 'dpmpp_3m_sde', 'dpmpp_3m_sde_gpu',
  'ddpm', 'lcm', 'ipndm', 'ipndm_v', 'deis', 'res_multistep', 'res_multistep_cfg',
  'res_multistep_ancestral', 'res_multistep_ancestral_cfg', 'gradient_estimation'
]
const schedulerOptions = ['normal', 'karras', 'exponential', 'sgm_uniform', 'simple', 'ddim_uniform']

// ── reactive state ──
const currentTab = ref('')
const workflowTypes = ref([])
const loading = ref(false)
const errorMsg = ref('')
const isGenerating = ref(false)
const originalWorkflow = ref(null)
const parsedNodes = ref([])
const formValues = reactive({})

// upload per node
const uploadStates = reactive({})
const activeUploadNodeId = ref(null)
const fileInput = ref(null)

// generation
const generationResult = ref(null)

// ── lifecycle ──
loadWorkflowTypes()

async function loadWorkflowTypes() {
  try {
    const res = await getWorkflowTypes()
    if (res.code === '200' && res.data?.length) {
      workflowTypes.value = res.data
      currentTab.value = String(res.data[0].id)
      await switchTab(currentTab.value)
    } else {
      errorMsg.value = '暂无可用的工作流'
    }
  } catch (e) {
    errorMsg.value = '加载工作流列表失败: ' + e.message
  }
  getModels().then(res => { if (res.code === '200' && res.data) modelDict.value = res.data })
}

// ── methods ──

async function switchTab(tabId) {
  const wt = workflowTypes.value.find(w => String(w.id) === tabId)
  if (!wt) return
  currentTab.value = tabId
  // clear upload states
  for (const key of Object.keys(uploadStates)) delete uploadStates[key]
  errorMsg.value = ''
  generationResult.value = null
  await loadWorkflow(wt.id)
}

async function loadWorkflow(type) {
  loading.value = true
  parsedNodes.value = []
  originalWorkflow.value = null
  try {
    const res = await getWorkflow(type)
    if (res.code !== '200') {
      errorMsg.value = '加载工作流失败: ' + (res.msg || '未知错误')
      return
    }
    originalWorkflow.value = res.data
    const nodes = parseWorkflow(res.data)
    parsedNodes.value = nodes
    initFormValues(nodes, res.data)
  } catch (e) {
    errorMsg.value = '加载工作流失败: ' + e.message
  } finally {
    loading.value = false
  }
}

// ── Parse: extract editable fields from every node ──
function parseWorkflow(workflow) {
  const result = []

  for (const [nodeId, node] of Object.entries(workflow)) {
    const title = node._meta?.title || node.class_type || nodeId
    const fields = []

    if (node.inputs) {
      for (const [key, val] of Object.entries(node.inputs)) {
        // skip connections (arrays)
        if (Array.isArray(val)) continue

        const type = detectFieldType(node.class_type, key, val)
        const field = { key, value: val, type }
        // extra hint for textarea row count
        if (type === 'textarea') {
          field.rows = title.includes('负面') || title.includes('负向') ? 2 : 3
        }
        fields.push(field)
      }
    }

    // only include nodes that have at least one editable field
    if (fields.length > 0) {
      result.push({ nodeId, title, classType: node.class_type, fields })
    }
  }

  return result
}

function detectFieldType(classType, key, val) {
  // upload: LoadImage node with 'image' field
  if (classType === 'LoadImage' && key === 'image') return 'upload'

  // known select lists
  if (key === 'sampler_name') return 'sampler'
  if (key === 'scheduler') return 'scheduler'
  if (key === 'ckpt_name') return 'ckpt'

  // prompt text: CLIPTextEncode node with 'text' field
  if (classType === 'CLIPTextEncode' && key === 'text') return 'textarea'

  // value-type based
  if (typeof val === 'number') return 'number'
  if (typeof val === 'boolean') return 'boolean'

  return 'text'
}

// ── Init formValues from parsed nodes ──
function initFormValues(nodes, workflow) {
  // clear old keys
  for (const key of Object.keys(formValues)) delete formValues[key]

  for (const node of nodes) {
    const vals = {}
    for (const field of node.fields) {
      vals[field.key] = field.value
    }
    formValues[node.nodeId] = reactive(vals)

    // init upload state for LoadImage nodes
    if (node.classType === 'LoadImage') {
      uploadStates[node.nodeId] = { previewUrl: '', filename: fieldValue(node, 'image') || '' }
    }
  }
}

function fieldValue(node, key) {
  return node.fields.find(f => f.key === key)?.value
}

// ── Upload ──

function onFileChange(e) {
  const file = e.target.files[0]
  if (file) handleImageFile(file, activeUploadNodeId.value)
  e.target.value = ''
}

function handleDrop(e, nodeId) {
  const file = e.dataTransfer.files[0]
  if (file) handleImageFile(file, nodeId)
}

function clearUpload(nodeId) {
  if (uploadStates[nodeId]) {
    uploadStates[nodeId].previewUrl = ''
    uploadStates[nodeId].filename = ''
  }
  if (formValues[nodeId]) formValues[nodeId].image = ''
}

async function handleImageFile(file, nodeId) {
  if (!file || !file.type.startsWith('image/')) {
    errorMsg.value = '请选择图片文件'
    return
  }

  // local preview
  const reader = new FileReader()
  reader.onload = e => {
    if (!uploadStates[nodeId]) uploadStates[nodeId] = { previewUrl: '', filename: '' }
    uploadStates[nodeId].previewUrl = e.target.result
  }
  reader.readAsDataURL(file)

  // upload to server
  try {
    const res = await uploadImage(file)
    if (res.code !== '200') {
      errorMsg.value = '图片上传失败: ' + (res.msg || '')
      return
    }
    const filename = res.data || res.msg
    if (!uploadStates[nodeId]) uploadStates[nodeId] = { previewUrl: '', filename: '' }
    uploadStates[nodeId].filename = filename
    if (formValues[nodeId]) formValues[nodeId].image = filename
    errorMsg.value = ''
  } catch (e) {
    errorMsg.value = '图片上传失败: ' + e.message
  }
}

// ── Build modified workflow ──

function buildModifiedWorkflow() {
  const modified = JSON.parse(JSON.stringify(originalWorkflow.value))

  for (const node of parsedNodes.value) {
    const vals = formValues[node.nodeId]
    if (!vals) continue
    for (const field of node.fields) {
      const newVal = vals[field.key]
      // For upload, prefer the actual uploaded filename
      if (field.type === 'upload') {
        const uploaded = uploadStates[node.nodeId]?.filename
        if (uploaded) {
          modified[node.nodeId].inputs[field.key] = uploaded
        } else if (newVal) {
          modified[node.nodeId].inputs[field.key] = newVal
        }
      } else if (field.type === 'number') {
        modified[node.nodeId].inputs[field.key] = Number(newVal)
      } else {
        modified[node.nodeId].inputs[field.key] = newVal
      }
    }
  }

  return modified
}

// ── Generate ──

async function startGeneration() {
  if (isGenerating.value) return

  // check all upload fields have a value
  for (const node of parsedNodes.value) {
    const hasUpload = node.fields.some(f => f.type === 'upload')
    if (hasUpload) {
      const filename = uploadStates[node.nodeId]?.filename
      if (!filename) {
        errorMsg.value = '请先在"' + node.title + '"中上传参考图片'
        return
      }
    }
  }

  isGenerating.value = true
  errorMsg.value = ''
  generationResult.value = null

  try {
    const workflow = buildModifiedWorkflow()
    const res = await generateImage(workflow)

    if (res.code !== '200') throw new Error(res.msg || '生成失败')

    generationResult.value = res.data

    const fileCount = res.data.files ? res.data.files.length : 0
    ElNotification({
      title: '生图成功',
      message: `已生成 ${fileCount} 张图片，点击查看历史记录`,
      type: 'success',
      duration: 5000,
      onClick: () => {
        const routes = router.getRoutes()
        const historyRoute = routes.find(r => {
          const comp = r.components?.default
          const compName = comp?.name || comp?.__name
          return compName && String(compName).includes('GenPictureHistory')
        })
        if (historyRoute?.name) {
          router.push({ name: historyRoute.name })
        }
      }
    })
  } catch (e) {
    errorMsg.value = '生图失败: ' + e.message
    ElNotification({
      title: '生图失败',
      message: e.message,
      type: 'error',
      duration: 6000
    })
  } finally {
    isGenerating.value = false
  }
}
</script>

<style scoped>
.gp-container { max-width: 860px; margin: 0 auto; padding: 0 4px; }

.gp-tabs {
  --el-tabs-header-height: 44px;
  margin-bottom: 12px;
}

.gp-loading { text-align: center; padding: 60px 0; }

.gp-error { margin-bottom: 12px; }

.gp-card { margin-bottom: 12px; }
.gp-card-title { font-weight: 600; font-size: 15px; }

.gp-form-item { margin-bottom: 10px; }
.gp-form-full { grid-column: 1 / -1; }
.gp-label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
  font-weight: 500;
}
.gp-input-full { width: 100%; }

.gp-generate-btn {
  width: 100%;
  margin-bottom: 12px;
  height: 42px;
  font-size: 15px;
}

.gp-loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
}
.gp-spin-icon {
  font-size: 40px;
  color: #409eff;
  animation: gp-spin 1.5s linear infinite;
}
@keyframes gp-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.gp-progress-text {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
  margin-bottom: 0;
}

.gp-results {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.gp-result-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}
.gp-result-img {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.gp-result-dl {
  font-size: 13px;
  color: #409eff;
  text-decoration: none;
}
.gp-result-dl:hover { text-decoration: underline; }

.gp-text-secondary { color: #909399; font-size: 13px; }

/* ── upload ── */
.gp-upload-area {
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.gp-upload-area:hover,
.gp-upload-area.has-image { border-color: #409eff; border-style: solid; }
.gp-upload-icon { font-size: 32px; color: #c0c4cc; }
.gp-upload-hint { font-size: 13px; color: #909399; }
.gp-upload-preview { max-width: 200px; max-height: 150px; border-radius: 4px; }
.gp-upload-clear { font-size: 12px; color: #f56c6c; cursor: pointer; }

/* ── responsive ── */
@media (max-width: 600px) {
  .gp-container { padding: 0; }
}
</style>
