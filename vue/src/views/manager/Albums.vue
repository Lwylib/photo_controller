<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" placeholder="请输入相册名称查询" style="width: 240px; margin-right: 5px"></el-input>
      <el-select v-model="data.sortType" placeholder="排序方式" style="width: 120px; margin-right: 5px">
        <el-option label="默认排序" value="default"></el-option>
        <el-option label="热度排序" value="hot"></el-option>
      </el-select>
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
              
              <!-- 评论功能 -->
              <div style="margin-top: 15px; border-top: 1px solid #eee; padding-top: 10px;">
                <!-- 评论输入框 -->
                <div style="display: flex; align-items: flex-start; grid-gap: 10px; margin-bottom: 10px;">
                  <el-avatar :size="32" :src="data.user.avatar" style="flex-shrink: 0;"></el-avatar>
                  <div style="flex: 1;">
                    <el-input
                      v-model="data.commentContent[item.id]"
                      type="textarea"
                      :rows="2"
                      placeholder="写下你的评论..."
                      maxlength="200"
                      show-word-limit
                    ></el-input>
                    <div style="margin-top: 5px; display: flex; justify-content: flex-end;">
                      <el-button size="small" @click="submitComment(item)">发表评论</el-button>
                    </div>
                  </div>
                </div>
                
                <!-- 评论列表 -->
                <div v-if="data.comments[item.id] && data.comments[item.id].length > 0" class="comment-list">
                  <div v-for="comment in data.comments[item.id]" :key="comment.id" class="comment-item" style="margin-bottom: 15px; padding: 10px; background: #f8f9fa; border-radius: 8px;">
                    <!-- 顶级评论 -->
                    <div style="display: flex; align-items: flex-start; grid-gap: 10px;">
                      <el-avatar :size="28" :src="comment.userAvatar"></el-avatar>
                      <div style="flex: 1;">
                        <div style="font-weight: bold; font-size: 14px;">{{ comment.userName }}</div>
                        <div style="margin-top: 5px; font-size: 14px;">{{ comment.content }}</div>
                        <div style="margin-top: 5px; display: flex; align-items: center; grid-gap: 15px; font-size: 12px; color: #999;">
                          <span>{{ formatTime(comment.createTime) }}</span>
                          <span style="cursor: pointer;" @click="toggleReply(comment, item)">回复</span>
                          <div style="display: flex; align-items: center; grid-gap: 5px;">
                            <el-icon :color="comment.liked ? '#f56c6c' : '#999'" style="cursor: pointer;" @click="toggleLike(comment)">
                              <Star />
                            </el-icon>
                            <span>{{ comment.likeCount || 0 }}</span>
                          </div>
                        </div>
                        
                        <!-- 回复输入框 -->
                        <div v-if="data.replyingCommentId === comment.id" style="margin-top: 10px;">
                          <el-input
                            v-model="data.replyContent[comment.id]"
                            type="textarea"
                            :rows="2"
                            placeholder="回复评论..."
                            maxlength="200"
                            show-word-limit
                          ></el-input>
                          <div style="margin-top: 5px; display: flex; justify-content: flex-end; grid-gap: 5px;">
                            <el-button size="small" @click="cancelReply(comment)">取消</el-button>
                            <el-button size="small" type="primary" @click="submitReply(comment, item)">回复</el-button>
                          </div>
                        </div>
                        
                        <!-- 回复列表 -->
                        <div v-if="comment.replies && comment.replies.length > 0" style="margin-top: 10px;">
                          <div v-for="reply in comment.replies" :key="reply.id" style="margin-top: 10px; padding: 8px; background: #fff; border-radius: 6px; border-left: 3px solid #e0e0e0;">
                            <div style="display: flex; align-items: flex-start; grid-gap: 8px;">
                              <el-avatar :size="24" :src="reply.userAvatar"></el-avatar>
                              <div style="flex: 1;">
                                <div style="font-weight: bold; font-size: 13px;">{{ reply.userName }}</div>
                                <div style="margin-top: 3px; font-size: 13px;">{{ reply.content }}</div>
                                <div style="margin-top: 3px; display: flex; align-items: center; grid-gap: 10px; font-size: 11px; color: #999;">
                                  <span>{{ formatTime(reply.createTime) }}</span>
                                  <div style="display: flex; align-items: center; grid-gap: 3px;">
                                    <el-icon :color="reply.liked ? '#f56c6c' : '#999'" style="cursor: pointer;" @click="toggleLike(reply)">
                                      <Star />
                                    </el-icon>
                                    <span>{{ reply.likeCount || 0 }}</span>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- 加载更多评论 -->
                <div v-if="data.comments[item.id] && data.comments[item.id].length > 0" style="text-align: center; margin-top: 10px;">
                  <el-button type="text" size="small" @click="loadMoreComments(item)">
                    加载更多评论
                  </el-button>
                </div>
              </div>
              <!-- 第一行：状态标签 -->
              <div style="margin-top: 10px; display: flex; align-items: center; grid-gap: 10px; flex-wrap: wrap;">
                <el-tag v-if="item.roleRadio === '公开'" size="large" type="success">{{ item.roleRadio }}</el-tag>
                <el-tag v-else type="danger" size="large">{{ item.roleRadio }}</el-tag>
                <el-tag v-if="item.statusRadio === '正常'" size="large" type="success">{{ item.statusRadio }}</el-tag>
                <el-tag v-else type="danger" size="large">{{ item.statusRadio }}</el-tag>
              </div>
              
              <!-- 第二行：热度信息和操作按钮 -->
              <div style="margin-top: 10px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap;">
                <!-- 热度信息 -->
                <div style="display: flex; align-items: center; grid-gap: 10px;">
                  <el-tooltip content="浏览量" placement="top">
                    <div style="display: flex; align-items: center; color: #666;">
                      <el-icon><View /></el-icon>
                      <span style="margin-left: 2px">{{ item.viewCount || 0 }}</span>
                    </div>
                  </el-tooltip>
                  <el-tooltip content="收藏数" placement="top">
                    <div style="display: flex; align-items: center; color: #666;">
                      <el-icon><Star /></el-icon>
                      <span style="margin-left: 2px">{{ item.collectCount || 0 }}</span>
                    </div>
                  </el-tooltip>
                  <el-tooltip content="热度值" placement="top">
                    <el-tag type="warning" size="small">{{ item.hotPoint || 0 }}</el-tag>
                  </el-tooltip>
                </div>
                
                <!-- 操作按钮 -->
                <div style="display: flex; align-items: center; grid-gap: 5px;">
                  <!-- 导出按钮 -->
                  <el-button type="primary" size="small" @click="exportAlbum(item)" :loading="data.exportLoading[item.id]">
                    <el-icon><Download /></el-icon>
                    导出
                  </el-button>
                  
                  <!-- 管理员操作按钮 -->
                  <div v-if="data.user.role === 'ADMIN'" style="margin-left: 5px">
                    <el-button v-if="item.statusRadio !== '违规'" size="small" type="danger" @click="markAsViolated(item)">标记违规</el-button>
                    <el-button v-else size="small" type="success" @click="markAsNormal(item)">恢复正常</el-button>
                  </div>
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
import { reactive, watch } from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Delete, Edit, View, Star, Download, ChatDotRound} from "@element-plus/icons-vue";
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  tableData: [],
  total: 0,
  pageNum: 1,  // 当前的页码
  pageSize: 6,  // 每页的个数
  name: null,
  sortType: 'default', // 排序方式：default-默认，hot-热度
  exportLoading: {}, // 导出加载状态
  
  // 评论相关数据
  comments: {}, // 存储每个相册的评论列表 {相册ID: 评论数组}
  commentContent: {}, // 存储每个相册的评论内容 {相册ID: 评论内容}
  replyContent: {}, // 存储每个评论的回复内容 {评论ID: 回复内容}
  replyingCommentId: null, // 当前正在回复的评论ID
  commentLoading: {}, // 评论加载状态
})

const navTo = (url) => {
  location.href = url
}

// 加载表格数据
const load = () => {
  const apiUrl = data.sortType === 'hot' ? '/category/selectHotAlbumPage' : '/category/selectAlbumPage'
  request.get(apiUrl, {
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

// 导出相册
const exportAlbum = (item) => {
  // 设置导出加载状态
  data.exportLoading[item.id] = true;
  
  // 创建隐藏的下载链接
  const link = document.createElement('a');
  link.style.display = 'none';
  
  // 构建完整的下载URL，包含token
  const downloadUrl = request.defaults.baseURL + '/export/album/' + item.id;
  
  // 使用fetch API发送带token的请求
  fetch(downloadUrl, {
    headers: {
      'token': data.user.token
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error('导出失败');
    }
    return response.blob();
  })
  .then(blob => {
    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    link.href = url;
    link.setAttribute('download', `album_${item.id}.zip`);
    document.body.appendChild(link);
    link.click();
    
    // 清理
    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);
    
    ElMessage.success('相册导出成功');
  })
  .catch(error => {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  })
  .finally(() => {
    // 清除加载状态
    data.exportLoading[item.id] = false;
  });
}

// 发表评论
const submitComment = (item) => {
  const content = data.commentContent[item.id];
  if (!content || content.trim() === '') {
    ElMessage.warning('请输入评论内容');
    return;
  }
  
  data.commentLoading[item.id] = true;
  
  request.post('/comment/add', {
    categoryId: item.id,
    content: content.trim(),
    parentId: 0 // 顶级评论
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('评论发表成功');
      data.commentContent[item.id] = '';
      // 重新加载评论
      loadComments(item.id);
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(error => {
    console.error('发表评论失败:', error);
    ElMessage.error('发表评论失败');
  }).finally(() => {
    data.commentLoading[item.id] = false;
  });
}

// 回复评论
const submitReply = (comment, item) => {
  const content = data.replyContent[comment.id];
  if (!content || content.trim() === '') {
    ElMessage.warning('请输入回复内容');
    return;
  }
  
  request.post('/comment/add', {
    categoryId: item.id,
    content: content.trim(),
    parentId: comment.id // 回复评论
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('回复成功');
      data.replyContent[comment.id] = '';
      data.replyingCommentId = null;
      // 重新加载评论
      loadComments(item.id);
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(error => {
    console.error('回复失败:', error);
    ElMessage.error('回复失败');
  });
}

// 取消回复
const cancelReply = (comment) => {
  data.replyContent[comment.id] = '';
  data.replyingCommentId = null;
}

// 显示/隐藏回复框
const toggleReply = (comment, item) => {
  if (data.replyingCommentId === comment.id) {
    data.replyingCommentId = null;
  } else {
    data.replyingCommentId = comment.id;
    data.replyContent[comment.id] = '';
  }
}

// 点赞/取消点赞
const toggleLike = async (comment) => {
  try {
    // 检查当前点赞状态
    const isLikedRes = await request.get(`/comment/isLiked/${comment.id}`);
    const isLiked = isLikedRes.data;
    
    if (isLiked) {
      // 取消点赞
      const res = await request.post(`/comment/unlike/${comment.id}`);
      if (res.code === '200') {
        comment.liked = false;
        comment.likeCount = (comment.likeCount || 1) - 1;
        ElMessage.success('取消点赞成功');
      }
    } else {
      // 点赞
      const res = await request.post(`/comment/like/${comment.id}`);
      if (res.code === '200') {
        comment.liked = true;
        comment.likeCount = (comment.likeCount || 0) + 1;
        ElMessage.success('点赞成功');
      }
    }
  } catch (error) {
    console.error('点赞操作失败:', error);
    ElMessage.error('操作失败');
  }
}

// 加载评论
const loadComments = (categoryId) => {
  request.get(`/comment/selectByCategoryId/${categoryId}`).then(res => {
    if (res.code === '200') {
      // 为每个评论添加点赞状态
      const commentsWithLikeStatus = res.data.map(comment => ({
        ...comment,
        liked: false // 初始状态，后续会通过API检查
      }));
      
      data.comments[categoryId] = commentsWithLikeStatus;
      
      // 异步检查每个评论的点赞状态
      commentsWithLikeStatus.forEach(async (comment) => {
        try {
          const isLikedRes = await request.get(`/comment/isLiked/${comment.id}`);
          comment.liked = isLikedRes.data;
        } catch (error) {
          console.error('检查点赞状态失败:', error);
        }
      });
    }
  }).catch(error => {
    console.error('加载评论失败:', error);
  });
}

// 加载更多评论
const loadMoreComments = (item) => {
  // 这里可以实现分页加载更多评论
  ElMessage.info('加载更多评论功能待实现');
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  
  try {
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now - date;
    
    // 转换为分钟
    const minutes = Math.floor(diff / 60000);
    
    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}小时前`;
    
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}天前`;
    
    // 超过一周显示具体日期
    return date.toLocaleDateString();
  } catch (error) {
    return timeStr;
  }
}

// 初始化时加载评论
const initComments = () => {
  // 可以在加载相册数据后调用
}

load()

// 监听相册数据变化，加载评论
watch(() => data.tableData, (newVal) => {
  if (newVal && newVal.length > 0) {
    newVal.forEach(item => {
      // 为每个相册初始化评论数据
      if (!data.comments[item.id]) {
        data.comments[item.id] = [];
        data.commentContent[item.id] = '';
      }
      // 加载评论
      loadComments(item.id);
    });
  }
}, { immediate: true });

</script>