import request from '@/utils/request'

export function listConversations() {
  return request({ url: '/ai/chat/conversations', method: 'get' })
}

export function createConversation(model) {
  return request({ url: '/ai/chat/conversations', method: 'post', params: { model } })
}

export function renameConversation(id, title) {
  return request({ url: `/ai/chat/conversations/${id}/title`, method: 'put', params: { title } })
}

export function deleteConversation(id) {
  return request({ url: `/ai/chat/conversations/${id}`, method: 'delete' })
}

export function listMessages(conversationId) {
  return request({ url: `/ai/chat/conversations/${conversationId}/messages`, method: 'get' })
}
