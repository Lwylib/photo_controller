<template>
  <div
    class="cui-root"
    @mousedown="onCanvasMouseDown"
    @wheel.prevent="onWheel"
    @mousemove="onMouseMove"
    @mouseup="onMouseUp"
    @mouseleave="onMouseUp"
    @contextmenu.prevent
  >
    <!-- Top bar -->
    <div class="cui-topbar">
      <div class="cui-topbar-left">
        <svg class="cui-logo" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3"/><circle cx="12" cy="12" r="9" stroke-dasharray="4 4"/>
        </svg>
        <span class="cui-app-name">ComfyFlow</span>
        <el-select
          v-model="currentWorkflowId"
          placeholder="选择工作流"
          size="default"
          class="cui-type-select"
          @change="switchWorkflow"
        >
          <el-option v-for="wt in workflowTypes" :key="wt.id" :label="wt.name" :value="wt.id" />
        </el-select>
      </div>
      <div class="cui-topbar-right">
        <span v-if="isGenerating" class="cui-gen-status">生成中...</span>
        <el-button
          type="primary"
          size="small"
          :loading="isGenerating"
          :disabled="!parsedEditableNodes.length"
          @click="startGeneration"
        >
          {{ isGenerating ? '生成中...' : '生成' }}
        </el-button>
      </div>
    </div>

    <!-- Error / Loading -->
    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      closable
      class="cui-error"
      @close="errorMsg = ''"
    />
    <div v-if="loading" class="cui-loading" v-loading="loading" />

    <!-- Canvas -->
    <div
      v-if="!loading && parsedEditableNodes.length"
      ref="canvasRef"
      class="cui-canvas"
      @mousedown="onCanvasMouseDown"
    >
      <div
        class="cui-canvas-world"
        :style="{ transform: `translate(${offsetX}px, ${offsetY}px) scale(${scale})` }"
      >
        <!-- Grid dots background -->
        <div class="cui-grid" />

        <!-- Connection lines -->
        <svg class="cui-lines">
          <path
            v-for="conn in visibleConnections"
            :key="conn.id"
            :d="conn.d"
            :class="['cui-conn', conn.highlight && 'cui-conn-highlight']"
          />
          <!-- Drag-preview line -->
          <path
            v-if="draggingConn"
            :d="draggingConn.previewPath"
            class="cui-conn cui-conn-drag"
          />
        </svg>

        <!-- Nodes -->
        <div
          v-for="node in parsedEditableNodes"
          :key="node.nodeId"
          :class="['cui-node', { 'cui-node-selected': selectedNodeId === node.nodeId }]"
          :style="nodeStyle(node.nodeId)"
          @mousedown.stop="onNodeMouseDown($event, node.nodeId)"
        >
          <!-- Node header -->
          <div :class="['cui-node-head', `cui-head-${nodeClass(node)}`]">
            <span class="cui-node-title">{{ node.title }}</span>
          </div>

          <!-- Node body: one row per input field -->
          <div class="cui-node-body">
            <div
              v-for="field in node.fields"
              :key="field.key"
              class="cui-node-row"
            >
              <!-- Input port dot -->
              <div
                v-if="field.connection"
                :class="['cui-port', `cui-port-${nodeClass(node)}`]"
                @mousedown.stop="onPortMouseDown($event, node.nodeId, field.key)"
                @mouseup.stop="onPortMouseUp($event, node.nodeId, field.key)"
              />
              <div v-else class="cui-port cui-port-empty" />

              <!-- Field widget -->
              <template v-if="!field.connection">
                <!-- Upload -->
                <template v-if="field.type === 'upload'">
                  <span class="cui-field-key">{{ field.key }}</span>
                  <button
                    class="cui-upload-btn"
                    @click="triggerUpload(node.nodeId)"
                    @mousedown.stop
                  >
                    {{ uploadStates[node.nodeId]?.filename || field.value || '上传图片' }}
                  </button>
                </template>
                <!-- CKPT text input -->
                <template v-else-if="field.type === 'ckpt'">
                  <span class="cui-field-key">{{ field.key }}</span>
                  <input
                    class="cui-input"
                    :value="formValues[node.nodeId]?.[field.key]"
                    placeholder="模型文件名"
                    @mousedown.stop
                    @input="e => setField(node.nodeId, field.key, e.target.value)"
                  />
                </template>
                <!-- Sampler -->
                <template v-else-if="field.type === 'sampler'">
                  <span class="cui-field-key">{{ field.key }}</span>
                  <select
                    class="cui-select"
                    :value="formValues[node.nodeId]?.[field.key]"
                    @change="e => setField(node.nodeId, field.key, e.target.value)"
                    @mousedown.stop
                  >
                    <option v-for="s in samplerOptions" :key="s" :value="s">{{ s }}</option>
                  </select>
                </template>
                <!-- Scheduler -->
                <template v-else-if="field.type === 'scheduler'">
                  <span class="cui-field-key">{{ field.key }}</span>
                  <select
                    class="cui-select"
                    :value="formValues[node.nodeId]?.[field.key]"
                    @change="e => setField(node.nodeId, field.key, e.target.value)"
                    @mousedown.stop
                  >
                    <option v-for="s in schedulerOptions" :key="s" :value="s">{{ s }}</option>
                  </select>
                </template>
                <!-- Textarea (prompt) -->
                <template v-else-if="field.type === 'textarea'">
                  <span class="cui-field-key">{{ field.key }}</span>
                  <textarea
                    class="cui-textarea"
                    :rows="field.rows || 2"
                    :value="formValues[node.nodeId]?.[field.key]"
                    @input="e => setField(node.nodeId, field.key, e.target.value)"
                    @mousedown.stop
                  />
                </template>
                <!-- Number -->
                <template v-else-if="field.type === 'number'">
                  <span class="cui-field-key">{{ field.key }}</span>
                  <input
                    class="cui-input"
                    type="number"
                    :value="formValues[node.nodeId]?.[field.key]"
                    @mousedown.stop
                    @input="e => setField(node.nodeId, field.key, Number(e.target.value))"
                  />
                </template>
                <!-- Boolean -->
                <template v-else-if="field.type === 'boolean'">
                  <span class="cui-field-key">{{ field.key }}</span>
                  <button
                    :class="['cui-toggle', { 'cui-toggle-on': formValues[node.nodeId]?.[field.key] }]"
                    @click="setField(node.nodeId, field.key, !formValues[node.nodeId]?.[field.key])"
                    @mousedown.stop
                  >
                    {{ formValues[node.nodeId]?.[field.key] ? 'ON' : 'OFF' }}
                  </button>
                </template>
                <!-- Text -->
                <template v-else>
                  <span class="cui-field-key">{{ field.key }}</span>
                  <input
                    class="cui-input"
                    :value="formValues[node.nodeId]?.[field.key]"
                    @mousedown.stop
                    @input="e => setField(node.nodeId, field.key, e.target.value)"
                  />
                </template>
              </template>
              <!-- Connected field: show source info -->
              <template v-else>
                <span class="cui-field-key">{{ field.key }}</span>
                <span class="cui-conn-ref">
                  ← {{ connectionSource(field.connection) }}
                </span>
              </template>
            </div>
          </div>

          <!-- Output ports (right side) -->
          <div
            v-for="(oport, opi) in (nodeOutputPorts[node.nodeId] || [])"
            :key="'out-' + oport.index"
            :class="['cui-port-out', `cui-port-${nodeClass(node)}`]"
            :style="{ top: outPortYOffset(opi, (nodeOutputPorts[node.nodeId] || []).length, node.fields.length) + 'px' }"
            @mousedown.stop
          >
            <span class="cui-out-label">{{ oport.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- No workflow state -->
    <div v-if="!loading && !parsedEditableNodes.length && !errorMsg && workflowTypes.length" class="cui-empty">
      <p>选择一个工作流开始编辑</p>
    </div>

    <!-- Result -->
    <div v-if="generationResult" class="cui-result">
      <div v-if="generationResult.files?.length" class="cui-result-grid">
        <div v-for="(f, i) in generationResult.files" :key="i" class="cui-result-item">
          <img :src="imageBlobUrls[f.url] || ''" class="cui-result-img" alt="result" />
          <a :href="imageBlobUrls[f.url] || '#'" :download="f.filename" class="cui-result-link">下载</a>
        </div>
      </div>
      <span v-else class="cui-no-result">未获取到图片</span>
    </div>

    <!-- Hidden upload input -->
    <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElNotification } from 'element-plus'
import { useRouter } from 'vue-router'
import { getWorkflow, getWorkflowTypes, uploadImage, generateImage, loadAuthImageBlob, getModels } from '@/api/ai/genPicture'



const router = useRouter()
const modelDict = ref([])

const samplerOptions = [
  'euler', 'euler_ancestral', 'heun', 'heunpp2', 'dpm_2', 'dpm_2_ancestral',
  'lms', 'dpm_fast', 'dpm_adaptive', 'dpmpp_2s_ancestral', 'dpmpp_sde', 'dpmpp_sde_gpu',
  'dpmpp_2m', 'dpmpp_2m_sde', 'dpmpp_2m_sde_gpu', 'dpmpp_3m_sde', 'dpmpp_3m_sde_gpu',
  'ddpm', 'lcm', 'ipndm', 'ipndm_v', 'deis', 'res_multistep', 'res_multistep_cfg',
  'res_multistep_ancestral', 'res_multistep_ancestral_cfg', 'gradient_estimation'
]
const schedulerOptions = ['normal', 'karras', 'exponential', 'sgm_uniform', 'simple', 'ddim_uniform']

// ── Core state ──
const workflowTypes = ref([])
const currentWorkflowId = ref('')
const loading = ref(false)
const errorMsg = ref('')
const isGenerating = ref(false)
const originalWorkflow = ref(null)
const parsedEditableNodes = ref([])
const formValues = reactive({})
const uploadStates = reactive({})
const nodePositions = reactive({})

// Canvas transform
const offsetX = ref(0)
const offsetY = ref(0)
const scale = ref(1)
const canvasRef = ref(null)

// Selection
const selectedNodeId = ref(null)

// Drag state
const draggingNodeId = ref(null)
const dragStartX = ref(0)
const dragStartY = ref(0)
const draggingConn = ref(null)
const panning = ref(false)
const panStartX = ref(0)
const panStartY = ref(0)

// Upload
const fileInput = ref(null)
const activeUploadNodeId = ref(null)

// Result
const generationResult = ref(null)
const imageBlobUrls = reactive({})

// ── Lifecycle ──
onMounted(() => loadWorkflowTypes())

async function loadWorkflowTypes() {
  try {
    const res = await getWorkflowTypes()
    if (res.code === '200' && res.data?.length) {
      workflowTypes.value = res.data
      currentWorkflowId.value = String(res.data[0].id)
      await switchWorkflow(currentWorkflowId.value)
    } else {
      errorMsg.value = '暂无可用的工作流'
    }
  } catch (e) {
    errorMsg.value = '加载工作流列表失败: ' + e.message
  }
  getModels().then(res => { if (res.code === '200' && res.data) modelDict.value = res.data })
}

async function switchWorkflow(id) {
  if (!id) return
  currentWorkflowId.value = id
  errorMsg.value = ''
  generationResult.value = null
  // reset
  for (const k of Object.keys(uploadStates)) delete uploadStates[k]
  for (const k of Object.keys(nodePositions)) delete nodePositions[k]
  for (const k of Object.keys(formValues)) delete formValues[k]
  offsetX.value = 0; offsetY.value = 0; scale.value = 1
  await loadWorkflow(id)
}

async function loadWorkflow(id) {
  loading.value = true; parsedEditableNodes.value = []; originalWorkflow.value = null
  try {
    const res = await getWorkflow(id)
    if (res.code !== '200') { errorMsg.value = '加载工作流失败'; return }
    originalWorkflow.value = res.data
    const nodes = parseEditableNodes(res.data)
    parsedEditableNodes.value = nodes
    initFormValues(nodes)
    computeInitialLayout(nodes, res.data)
  } catch (e) {
    errorMsg.value = '加载工作流失败: ' + e.message
  } finally {
    loading.value = false
  }
}

// ── Parse workflow ──
function parseEditableNodes(wf) {
  const result = []
  for (const [nodeId, node] of Object.entries(wf)) {
    const title = node._meta?.title || node.class_type || nodeId
    const fields = []
    if (node.inputs) {
      for (const [key, val] of Object.entries(node.inputs)) {
        if (Array.isArray(val)) {
          // Connection — show as port, not editable field
          fields.push({ key, value: null, type: 'connection', connection: val })
        } else {
          const type = detectFieldType(node.class_type, key, val)
          const field = { key, value: val, type, connection: null }
          if (type === 'textarea') {
            field.rows = (title.includes('负面') || title.includes('负向')) ? 2 : 3
          }
          fields.push(field)
        }
      }
    }
    if (fields.length > 0) {
      result.push({ nodeId, title, classType: node.class_type, fields })
    }
  }
  return result
}

function detectFieldType(classType, key, val) {
  if (classType === 'LoadImage' && key === 'image') return 'upload'
  if (key === 'sampler_name') return 'sampler'
  if (key === 'scheduler') return 'scheduler'
  if (key === 'ckpt_name') return 'ckpt'
  if (classType === 'CLIPTextEncode' && key === 'text') return 'textarea'
  if (typeof val === 'number') return 'number'
  if (typeof val === 'boolean') return 'boolean'
  return 'text'
}

function connectionSource(conn) {
  if (conn && conn.length >= 1) return conn[0]
  return '?'
}

// ── Form values init ──
function initFormValues(nodes) {
  for (const node of nodes) {
    const vals = {}
    for (const f of node.fields) {
      if (f.type !== 'connection') vals[f.key] = f.value
    }
    formValues[node.nodeId] = reactive(vals)
    if (node.classType === 'LoadImage') {
      const uploadField = node.fields.find(f => f.type === 'upload')
      uploadStates[node.nodeId] = { previewUrl: '', filename: uploadField?.value || '' }
    }
  }
}

function setField(nodeId, key, value) {
  if (formValues[nodeId]) formValues[nodeId][key] = value
}

// ── Layout ──
function computeInitialLayout(nodes, wf) {
  // Build adjacency (sourceId -> [childIds])
  const children = new Map()
  for (const n of nodes) children.set(n.nodeId, [])
  for (const n of nodes) {
    for (const f of n.fields) {
      if (f.connection && f.connection.length >= 1) {
        const src = String(f.connection[0])
        if (children.has(src)) children.get(src).push(n.nodeId)
      }
    }
  }

  // In-degree
  const inDeg = new Map()
  for (const n of nodes) inDeg.set(n.nodeId, 0)
  for (const n of nodes) {
    for (const child of (children.get(n.nodeId) || [])) {
      inDeg.set(child, (inDeg.get(child) || 0) + 1)
    }
  }

  // Layer BFS
  const layer = new Map()
  const q = []
  for (const [id, deg] of inDeg) {
    if (deg === 0) { layer.set(id, 0); q.push(id) }
  }

  const degRemaining = new Map(inDeg)
  while (q.length) {
    const cur = q.shift()
    const cl = layer.get(cur)
    for (const child of (children.get(cur) || [])) {
      const nl = cl + 1
      if (!layer.has(child) || nl > layer.get(child)) layer.set(child, nl)
      degRemaining.set(child, degRemaining.get(child) - 1)
      if (degRemaining.get(child) === 0) q.push(child)
    }
  }

  // Group by layer
  const layerGroups = new Map()
  for (const [id, l] of layer) {
    if (!layerGroups.has(l)) layerGroups.set(l, [])
    layerGroups.get(l).push(id)
  }

  const H_GAP = 340, V_GAP = 240, START_X = 20, START_Y = 20
  for (const [l, ids] of layerGroups) {
    ids.forEach((id, i) => {
      nodePositions[id] = { x: START_X + (l || 0) * H_GAP, y: START_Y + i * V_GAP }
    })
  }

  // Nodes not reached by BFS get default positions
  const placed = new Set(layer.keys())
  let extraIdx = 0
  for (const n of nodes) {
    if (!placed.has(n.nodeId)) {
      nodePositions[n.nodeId] = { x: 20, y: 20 + extraIdx * 200 }
      extraIdx++
    }
  }
}

function nodeStyle(nodeId) {
  const p = nodePositions[nodeId] || { x: 0, y: 0 }
  return {
    left: p.x + 'px',
    top: p.y + 'px',
  }
}

// ── Node visual class ──
function nodeClass(node) {
  const t = node.classType || ''
  if (t.includes('KSampler')) return 'sampler'
  if (t.includes('CLIPTextEncode')) return 'textenc'
  if (t.includes('CheckpointLoader')) return 'loader'
  if (t.includes('LoadImage')) return 'image'
  if (t.includes('VAEEncode')) return 'vae'
  if (t.includes('VAEDecode')) return 'vae'
  if (t.includes('EmptyLatentImage')) return 'latent'
  if (t.includes('SaveImage') || t.includes('PreviewImage')) return 'output'
  return 'default'
}

// ── Output ports per node ──
// Scan all connections to discover what outputs each node has.
// e.g. if node "9" has inputs.positive = ["8", 0], then node "8" has output index 0 named "positive"
const nodeOutputPorts = computed(() => {
  const map = {}
  for (const node of parsedEditableNodes.value) {
    for (const field of node.fields) {
      if (field.type !== 'connection' || !field.connection || field.connection.length < 2) continue
      const srcId = String(field.connection[0])
      const outIdx = field.connection[1]
      if (!map[srcId]) map[srcId] = []
      if (!map[srcId].some(p => p.index === outIdx)) {
        map[srcId].push({ index: outIdx, name: field.key })
      }
    }
  }
  // Sort by index for consistent ordering
  for (const id of Object.keys(map)) {
    map[id].sort((a, b) => a.index - b.index)
  }
  return map
})

// ── Connections ──
const HEAD_H = 32
const ROW_H = 28
const NODE_W = 280
const PORT_R = 6

function outPortYOffset(portListIdx, totalPorts, fieldCount) {
  const bodyH = fieldCount * ROW_H
  // Evenly distribute ports within the body area
  return HEAD_H + bodyH * (portListIdx + 0.5) / totalPorts
}
function inPortYOffset(fieldIdx) {
  return HEAD_H + fieldIdx * ROW_H + ROW_H / 2
}

const visibleConnections = computed(() => {
  const conns = []
  if (!parsedEditableNodes.value.length) return conns

  // Build a quick lookup: nodeId -> { fields count }
  const fieldCounts = {}
  for (const n of parsedEditableNodes.value) {
    fieldCounts[n.nodeId] = n.fields.length
  }

  for (const node of parsedEditableNodes.value) {
    const tgtPos = nodePositions[node.nodeId]
    if (!tgtPos) continue

    for (const field of node.fields) {
      if (!field.connection || field.connection.length < 2) continue
      const srcId = String(field.connection[0])
      const outIdx = field.connection[1]
      const srcPos = nodePositions[srcId]
      if (!srcPos) continue

      // Source output port position
      const srcPorts = nodeOutputPorts.value[srcId] || []
      const srcPortIdx = srcPorts.findIndex(p => p.index === outIdx)
      const sx = srcPos.x + NODE_W + PORT_R
      const sy = srcPos.y + outPortYOffset(
        srcPortIdx >= 0 ? srcPortIdx : 0,
        Math.max(srcPorts.length, 1),
        fieldCounts[srcId] || 1
      )

      // Target input port position
      const fieldIdx = node.fields.findIndex(f => f.key === field.key)
      const ey = tgtPos.y + inPortYOffset(fieldIdx >= 0 ? fieldIdx : 0)
      const ex = tgtPos.x - PORT_R

      const dx = Math.abs(ex - sx) * 0.5
      const path = `M ${sx} ${sy} C ${sx + dx} ${sy}, ${ex - dx} ${ey}, ${ex} ${ey}`

      conns.push({
        id: `${srcId}->${node.nodeId}:${field.key}`,
        d: path,
        highlight: selectedNodeId.value === srcId || selectedNodeId.value === node.nodeId,
      })
    }
  }
  return conns
})

// ── Canvas interactions ──
function onCanvasMouseDown(e) {
  if (e.target === canvasRef.value || e.target.classList.contains('cui-canvas')) {
    panning.value = true
    panStartX.value = e.clientX - offsetX.value
    panStartY.value = e.clientY - offsetY.value
  }
}

function onMouseMove(e) {
  if (panning.value) {
    offsetX.value = e.clientX - panStartX.value
    offsetY.value = e.clientY - panStartY.value
  }
  if (draggingNodeId.value) {
    const mx = (e.clientX - dragStartX.value) / scale.value
    const my = (e.clientY - dragStartY.value) / scale.value
    if (nodePositions[draggingNodeId.value]) {
      nodePositions[draggingNodeId.value].x = nodePositions[draggingNodeId.value]._startX + mx
      nodePositions[draggingNodeId.value].y = nodePositions[draggingNodeId.value]._startY + my
    }
  }
  if (draggingConn.value) {
    // Update preview path
    const targetX = (e.clientX - offsetX.value - canvasRef.value?.getBoundingClientRect().left) / scale.value || 0
    const targetY = (e.clientY - offsetY.value - canvasRef.value?.getBoundingClientRect().top) / scale.value || 0
    const sx = draggingConn.value.startX
    const sy = draggingConn.value.startY
    const dx = Math.abs(targetX - sx) * 0.5
    draggingConn.value.previewPath = `M ${sx} ${sy} C ${sx + dx} ${sy}, ${targetX - dx} ${targetY}, ${targetX} ${targetY}`
  }
}

function onMouseUp() {
  panning.value = false
  draggingNodeId.value = null
  draggingConn.value = null
}

function onWheel(e) {
  const delta = e.deltaY > 0 ? 0.9 : 1.1
  const newScale = Math.min(2, Math.max(0.15, scale.value * delta))
  // Zoom toward cursor position
  const rect = canvasRef.value?.getBoundingClientRect()
  if (rect) {
    const cx = e.clientX - rect.left
    const cy = e.clientY - rect.top
    const ratio = newScale / scale.value
    offsetX.value = cx - ratio * (cx - offsetX.value)
    offsetY.value = cy - ratio * (cy - offsetY.value)
  }
  scale.value = newScale
}

// ── Node drag ──
function onNodeMouseDown(e, nodeId) {
  selectedNodeId.value = nodeId
  const pos = nodePositions[nodeId]
  if (pos) {
    pos._startX = pos.x
    pos._startY = pos.y
  }
  draggingNodeId.value = nodeId
  dragStartX.value = e.clientX
  dragStartY.value = e.clientY
}

// ── Port interaction (for visual reference only — connections are computed) ──
function onPortMouseDown(_e, nodeId, _key) {
  selectedNodeId.value = nodeId
}
function onPortMouseUp() {}

// ── Upload ──
function triggerUpload(nodeId) {
  activeUploadNodeId.value = nodeId
  fileInput.value?.click()
}

function onFileChange(e) {
  const file = e.target.files[0]
  if (file) handleImageFile(file, activeUploadNodeId.value)
  e.target.value = ''
}

async function handleImageFile(file, nodeId) {
  if (!file || !file.type.startsWith('image/')) {
    errorMsg.value = '请选择图片文件'; return
  }
  const reader = new FileReader()
  reader.onload = e => {
    if (!uploadStates[nodeId]) uploadStates[nodeId] = { previewUrl: '', filename: '' }
    uploadStates[nodeId].previewUrl = e.target.result
  }
  reader.readAsDataURL(file)
  try {
    const res = await uploadImage(file)
    if (res.code !== '200') { errorMsg.value = '图片上传失败'; return }
    const filename = res.data || res.msg
    if (!uploadStates[nodeId]) uploadStates[nodeId] = { previewUrl: '', filename: '' }
    uploadStates[nodeId].filename = filename
    if (formValues[nodeId]) formValues[nodeId].image = filename
  } catch (e2) {
    errorMsg.value = '图片上传失败: ' + e2.message
  }
}

// ── Build modified workflow ──
function buildModifiedWorkflow() {
  const modified = JSON.parse(JSON.stringify(originalWorkflow.value))
  for (const node of parsedEditableNodes.value) {
    const vals = formValues[node.nodeId]
    if (!vals) continue
    for (const field of node.fields) {
      if (field.type === 'connection') continue
      const newVal = vals[field.key]
      if (field.type === 'upload') {
        const uploaded = uploadStates[node.nodeId]?.filename
        if (uploaded) modified[node.nodeId].inputs[field.key] = uploaded
        else if (newVal) modified[node.nodeId].inputs[field.key] = newVal
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
  // Check all upload fields
  for (const node of parsedEditableNodes.value) {
    if (node.fields.some(f => f.type === 'upload')) {
      if (!uploadStates[node.nodeId]?.filename) {
        errorMsg.value = '请先在"' + node.title + '"中上传参考图片'
        return
      }
    }
  }

  isGenerating.value = true; errorMsg.value = ''; generationResult.value = null
  try {
    const wf = buildModifiedWorkflow()
    const res = await generateImage(wf)
    if (res.code !== '200') throw new Error(res.msg || '生成失败')
    generationResult.value = res.data
    if (res.data.files) {
      for (const f of res.data.files) {
        loadAuthImageBlob(f.url).then(blob => { imageBlobUrls[f.url] = blob })
      }
    }
    const cnt = res.data.files?.length || 0
    ElNotification({
      title: '生图成功', message: `已生成 ${cnt} 张图片`, type: 'success', duration: 5000,
      onClick: () => {
        const routes = router.getRoutes()
        const r = routes.find(rr => {
          const c = rr.components?.default
          return (c?.name || c?.__name || '').includes('GenPictureHistory')
        })
        if (r?.name) router.push({ name: r.name })
      },
    })
  } catch (e) {
    errorMsg.value = '生图失败: ' + e.message
    ElNotification({ title: '生图失败', message: e.message, type: 'error', duration: 6000 })
  } finally {
    isGenerating.value = false
  }
}
</script>

<style scoped>
/* ── Root ── */
.cui-root {
  width: 100%;
  height: calc(100vh - 86px); /* minus header + tags */
  background: #0b0b0d;
  color: #c8c8d0;
  font-family: 'JetBrains Mono', 'Cascadia Code', 'Fira Code', monospace;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  user-select: none;
  position: relative;
}

/* ── Top bar ── */
.cui-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 14px;
  background: #131316;
  border-bottom: 1px solid #1e1e24;
  z-index: 10;
  flex-shrink: 0;
}
.cui-topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cui-logo {
  width: 22px;
  height: 22px;
  color: #4a9eff;
}
.cui-app-name {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #e0e0e8;
}
.cui-type-select {
  width: 160px;
  --el-fill-color-blank: #1a1a20;
  --el-border-color: #2a2a34;
  --el-text-color-regular: #c8c8d0;
}
.cui-topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cui-gen-status {
  font-size: 12px;
  color: #4a9eff;
}

/* ── Error / Loading ── */
.cui-error {
  margin: 8px 14px; flex-shrink: 0;
}
.cui-loading {
  flex: 1; display: flex; align-items: center; justify-content: center;
}

/* ── Canvas ── */
.cui-canvas {
  flex: 1;
  position: relative;
  overflow: hidden;
  cursor: grab;
  background:
    radial-gradient(circle, #1a1a24 1px, transparent 1px);
  background-size: 32px 32px;
}
.cui-canvas:active {
  cursor: grabbing;
}
.cui-canvas-world {
  position: absolute;
  top: 0; left: 0;
  width: 0; height: 0;
  transform-origin: 0 0;
}

/* ── Connection lines ── */
.cui-lines {
  position: absolute;
  top: 0; left: 0;
  width: 12000px; height: 12000px;
  pointer-events: none;
  overflow: visible;
}
.cui-conn {
  fill: none;
  stroke: #3a3a48;
  stroke-width: 2;
  stroke-dasharray: 6 3;
  transition: stroke 0.2s;
}
.cui-conn-highlight {
  stroke: #7a9eff;
  stroke-dasharray: none;
}
.cui-conn-drag {
  stroke: #4a9eff;
  stroke-width: 1.5;
  stroke-dasharray: 4 4;
}

/* ── Node card ── */
.cui-node {
  position: absolute;
  width: 280px;
  background: #141418;
  border: 1px solid #24242c;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.5);
  transition: border-color 0.2s, box-shadow 0.2s;
  cursor: default;
  z-index: 1;
}
.cui-node:hover {
  border-color: #3a3a48;
}
.cui-node-selected {
  border-color: #4a9eff !important;
  box-shadow: 0 0 0 3px rgba(74,158,255,0.15), 0 4px 24px rgba(0,0,0,0.5);
  z-index: 2;
}

/* ── Node header ── */
.cui-node-head {
  padding: 6px 12px;
  border-radius: 7px 7px 0 0;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.3px;
  cursor: grab;
  display: flex;
  align-items: center;
  gap: 6px;
}
.cui-node-head:active { cursor: grabbing; }
.cui-node-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #e4e4ec;
}

/* Header color variants */
.cui-head-sampler { background: #c2410c; }
.cui-head-textenc { background: #4338ca; }
.cui-head-loader { background: #0d9488; }
.cui-head-image { background: #0891b2; }
.cui-head-vae { background: #4f46e5; }
.cui-head-latent { background: #52525b; }
.cui-head-output { background: #1c1917; border-bottom: 1px solid #292524; }
.cui-head-default { background: #27272a; }

/* ── Node body ── */
.cui-node-body {
  padding: 4px 0;
}
.cui-node-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 2px 8px;
  min-height: 28px;
}
.cui-node-row:hover {
  background: rgba(255,255,255,0.02);
}

/* ── Ports ── */
.cui-port {
  width: 10px; height: 10px;
  border-radius: 50%;
  border: 1.5px solid #3a3a48;
  background: #1a1a20;
  flex-shrink: 0;
  cursor: crosshair;
  transition: border-color 0.15s, background 0.15s;
}
.cui-port:hover {
  border-color: #6a6a7a;
  background: #2a2a34;
}
.cui-port-empty {
  border-color: transparent;
  cursor: default;
}
.cui-port-sampler { border-color: #c2410c; }
.cui-port-textenc { border-color: #6366f1; }
.cui-port-loader { border-color: #0d9488; }
.cui-port-image { border-color: #0891b2; }
.cui-port-vae { border-color: #6366f1; }
.cui-port-latent { border-color: #71717a; }
.cui-port-output { border-color: #57534e; }
.cui-port-default { border-color: #3f3f46; }

.cui-port-out {
  position: absolute;
  right: -7px;
  width: 12px; height: 12px;
  border-radius: 50%;
  border: 2px solid #52525b;
  background: #1a1a20;
  cursor: crosshair;
  transition: border-color 0.15s, background 0.15s;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  transform: translate(-2px, -6px);
}
.cui-port-out:hover {
  border-color: #8a8a9a;
  background: #2a2a34;
  z-index: 5;
}
.cui-out-label {
  position: absolute;
  right: 18px;
  font-size: 9px;
  color: #606070;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.15s;
}
.cui-port-out:hover .cui-out-label {
  opacity: 1;
}

/* ── Field widgets ── */
.cui-field-key {
  font-size: 11px;
  color: #707080;
  white-space: nowrap;
  min-width: 40px;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cui-input {
  flex: 1;
  background: #1a1a20;
  border: 1px solid #2a2a34;
  border-radius: 3px;
  color: #d0d0d8;
  padding: 2px 6px;
  font-size: 12px;
  font-family: inherit;
  min-width: 0;
}
.cui-input:focus {
  outline: none;
  border-color: #4a9eff;
}
.cui-textarea {
  flex: 1;
  background: #1a1a20;
  border: 1px solid #2a2a34;
  border-radius: 3px;
  color: #d0d0d8;
  padding: 3px 6px;
  font-size: 12px;
  font-family: inherit;
  resize: vertical;
  min-width: 0;
}
.cui-textarea:focus {
  outline: none;
  border-color: #4a9eff;
}
.cui-select {
  flex: 1;
  background: #1a1a20;
  border: 1px solid #2a2a34;
  border-radius: 3px;
  color: #d0d0d8;
  padding: 2px 4px;
  font-size: 11px;
  font-family: inherit;
  min-width: 0;
  cursor: pointer;
}
.cui-select:focus {
  outline: none;
  border-color: #4a9eff;
}
.cui-toggle {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 3px;
  border: 1px solid #3a3a48;
  background: #1a1a20;
  color: #707080;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.cui-toggle-on {
  background: #1e3a5f;
  border-color: #4a9eff;
  color: #4a9eff;
}
.cui-upload-btn {
  flex: 1;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 3px;
  border: 1px dashed #3a3a48;
  background: #1a1a20;
  color: #707080;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: border-color 0.2s, color 0.2s;
}
.cui-upload-btn:hover {
  border-color: #0891b2;
  color: #0891b2;
}
.cui-conn-ref {
  font-size: 11px;
  color: #505058;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── Empty ── */
.cui-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #505058;
  font-size: 14px;
}

/* ── Result ── */
.cui-result {
  position: fixed;
  bottom: 16px;
  right: 16px;
  z-index: 20;
  background: #141418;
  border: 1px solid #2a2a34;
  border-radius: 10px;
  padding: 10px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.6);
  max-width: 360px;
}
.cui-result-grid {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.cui-result-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
.cui-result-img {
  max-width: 160px;
  max-height: 200px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.4);
}
.cui-result-link {
  font-size: 11px;
  color: #4a9eff;
  text-decoration: none;
}
.cui-result-link:hover { text-decoration: underline; }
.cui-no-result {
  font-size: 13px;
  color: #606070;
}
</style>
