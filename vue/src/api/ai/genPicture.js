import request from '@/utils/request'

export function getWorkflow(id) {
  return request({ url: '/ai/gen-picture/workflow', method: 'get', params: { id } })
}

export function getWorkflowTypes() {
  return request({ url: '/ai/gen-picture/workflow-types', method: 'get' })
}

export function getModels() {
  return request({ url: '/ai/gen-picture/models', method: 'get' })
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/ai/gen-picture/upload-image',
    method: 'post',
    data: formData
  })
}

export function generateImage(workflow) {
  return request({ url: '/ai/gen-picture/generate', method: 'post', data: workflow, timeout: 2010000 })
}

export function getHistory() {
  return request({ url: '/ai/gen-picture/history', method: 'get' })
}

export function deleteRecord(id) {
  return request({ url: '/ai/gen-picture/' + id, method: 'delete' })
}

// Get auth image URL — appends token as query param so browser can stream directly
export function getImageUrl(url) {
  const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  const baseURL = import.meta.env.VITE_BASE_URL
  const fullUrl = (url.startsWith('http') ? url : baseURL + url)
  const sep = fullUrl.includes('?') ? '&' : '?'
  return fullUrl + sep + 'token=' + encodeURIComponent(user.token || '')
}

// Fetch image blob with auth token (for download or small images only)
export async function loadAuthImageBlob(url) {
  const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  const baseURL = import.meta.env.VITE_BASE_URL
  const fullUrl = (url.startsWith('http') ? url : baseURL + url)
  const res = await fetch(fullUrl, {
    headers: { token: user.token || '' }
  })
  const blob = await res.blob()
  return URL.createObjectURL(blob)
}
