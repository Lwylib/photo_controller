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

    <!-- 评论功能 -->
    <div style="margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;">
      <div style="font-size: 18px; font-weight: bold; margin-bottom: 15px;">评论</div>

      <!-- 评论输入框 -->
      <div style="display: flex; align-items: flex-start; grid-gap: 10px; margin-bottom: 20px;">
        <el-avatar :size="40" :src="data.user.avatar" style="flex-shrink: 0;"></el-avatar>
        <div style="flex: 1;">
          <el-input
              v-model="data.commentContent"
              type="textarea"
              :rows="3"
              placeholder="分享你对这个相册的看法..."
              maxlength="200"
              show-word-limit
          ></el-input>
          <div style="margin-top: 10px; display: flex; justify-content: flex-end;">
            <el-button type="primary" @click="submitComment" :loading="data.commentLoading">发表评论</el-button>
          </div>
        </div>
      </div>

      <!-- 评论列表 -->
      <div v-if="data.comments.length > 0" class="comment-list">
        <div v-for="comment in data.comments" :key="comment.id" class="comment-item" style="margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 8px;">
          <!-- 顶级评论 -->
          <div style="display: flex; align-items: flex-start; grid-gap: 12px;">
            <el-avatar :size="36" :src="comment.userAvatar"></el-avatar>
            <div style="flex: 1;">
              <div style="font-weight: bold; font-size: 16px;">{{ comment.userName }}</div>
              <div style="margin-top: 8px; font-size: 15px; line-height: 1.5;">{{ comment.content }}</div>
              <div style="margin-top: 10px; display: flex; align-items: center; grid-gap: 20px; font-size: 13px; color: #999;">
                <span>{{ formatTime(comment.createTime) }}</span>
                <span style="cursor: pointer;" @click="toggleReply(comment)">回复</span>
                <div style="display: flex; align-items: center; grid-gap: 5px;">
                  <el-icon :color="comment.liked ? '#f56c6c' : '#999'" style="cursor: pointer;" @click="toggleLike(comment)">
                    <Star />
                  </el-icon>
                  <span>{{ comment.likeCount || 0 }}</span>
                </div>
              </div>

              <!-- 回复输入框 -->
              <div v-if="data.replyingCommentId === comment.id" style="margin-top: 15px;">
                <el-input
                    v-model="data.replyContent"
                    type="textarea"
                    :rows="2"
                    placeholder="回复评论..."
                    maxlength="200"
                    show-word-limit
                ></el-input>
                <div style="margin-top: 10px; display: flex; justify-content: flex-end; grid-gap: 10px;">
                  <el-button size="small" @click="cancelReply">取消</el-button>
                  <el-button size="small" type="primary" @click="submitReply(comment)">回复</el-button>
                </div>
              </div>

              <!-- 回复列表 -->
              <div v-if="comment.replies && comment.replies.length > 0" style="margin-top: 15px;">
                <div v-for="reply in comment.replies" :key="reply.id" style="margin-top: 12px; padding: 10px; background: #fff; border-radius: 6px; border-left: 3px solid #e0e0e0;">
                  <div style="display: flex; align-items: flex-start; grid-gap: 10px;">
                    <el-avatar :size="28" :src="reply.userAvatar"></el-avatar>
                    <div style="flex: 1;">
                      <div style="font-weight: bold; font-size: 14px;">{{ reply.userName }}</div>
                      <div style="margin-top: 5px; font-size: 14px; line-height: 1.4;">{{ reply.content }}</div>
                      <div style="margin-top: 8px; display: flex; align-items: center; grid-gap: 15px; font-size: 12px; color: #999;">
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

      <!-- 空状态 -->
      <div v-else style="text-align: center; padding: 40px 0; color: #999;">
        <el-icon size="48"><ChatDotRound /></el-icon>
        <div style="margin-top: 10px;">暂无评论，快来发表第一条评论吧~</div>
      </div>

      <!-- 评论分页 -->
      <div v-if="data.commentTotal > 0" style="margin-top: 20px; text-align: center;">
        <el-pagination
            @current-change="loadComments"
            background
            layout="total, prev, pager, next"
            :page-size="data.commentPageSize"
            v-model:current-page="data.commentPageNum"
            :total="data.commentTotal"
            small
        />
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive } from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Delete, Edit, Star, ChatDotRound} from "@element-plus/icons-vue";
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  tableData: [],
  total: 0,
  pageNum: 1,  // 当前的页码
  pageSize: 10,  // 每页的个数
  form: {},
  name: null,
  categoryData: {},
  categoryId: router.currentRoute.value.query.id,
  
  // 评论相关数据
  comments: [], // 评论列表
  commentContent: '', // 评论内容
  replyContent: '', // 回复内容
  replyingCommentId: null, // 当前正在回复的评论ID
  commentLoading: false, // 评论加载状态
  
  // 评论分页数据
  commentPageNum: 1, // 评论当前页码
  commentPageSize: 5, // 评论每页数量
  commentTotal: 0, // 评论总数
})

// 显示/隐藏回复框
const toggleReply = (comment) => {
  if (data.replyingCommentId === comment.id) {
    data.replyingCommentId = null;
  } else {
    data.replyingCommentId = comment.id;
    data.replyContent = '';
  }
}

// 取消回复
const cancelReply = () => {
  data.replyingCommentId = null;
  data.replyContent = '';
}

// 回复评论
const submitReply = (comment) => {
  if (!data.replyContent || data.replyContent.trim() === '') {
    ElMessage.warning('请输入回复内容');
    return;
  }

  request.post('/comment/add', {
    categoryId: data.categoryId,
    content: data.replyContent.trim(),
    parentId: comment.id // 回复评论
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('回复成功');
      data.replyContent = '';
      data.replyingCommentId = null;
      loadComments();
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(error => {
    console.error('回复失败:', error);
    ElMessage.error('回复失败');
  });
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

// 增加相册浏览量
const increaseViewCount = () => {
  request.put('/category/increaseViewCount/' + data.categoryId).then(res => {
    if (res.code === '200') {
      console.log('浏览量增加成功')
    }
  })
}


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

// 发表评论
const submitComment = () => {
  if (!data.commentContent || data.commentContent.trim() === '') {
    ElMessage.warning('请输入评论内容');
    return;
  }

  data.commentLoading = true;

  request.post('/comment/add', {
    categoryId: data.categoryId,
    content: data.commentContent.trim(),
    parentId: 0 // 顶级评论
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('评论发表成功');
      data.commentContent = '';
      loadComments();
    } else {
      ElMessage.error(res.msg);
    }
  }).finally(() => {
    data.commentLoading = false;
  });
}

// 加载评论
const loadComments = () => {
  request.get(`/comment/selectByCategoryId/${data.categoryId}`, {
    params: {
      pageNum: data.commentPageNum,
      pageSize: data.commentPageSize
    }
  }).then(res => {
    if (res.code === '200') {
      const pageInfo = res.data;
      // 从分页对象中获取评论列表
      if (pageInfo && Array.isArray(pageInfo.list)) {
        // 为每个评论添加点赞状态
        const commentsWithLikeStatus = pageInfo.list.map(comment => ({
          ...comment,
          liked: false // 初始状态，后续会通过API检查
        }));
        
        data.comments = commentsWithLikeStatus;
        data.commentTotal = pageInfo.total || 0; // 设置评论总数
        
        // 异步检查每个评论的点赞状态
        commentsWithLikeStatus.forEach(async (comment) => {
          try {
            const isLikedRes = await request.get(`/comment/isLiked/${comment.id}`);
            comment.liked = isLikedRes.data;
          } catch (error) {
            console.error('检查点赞状态失败:', error);
          }
        });
      } else {
        console.error('评论数据格式错误:', res.data);
        data.comments = [];
        data.commentTotal = 0;
      }
    } else {
      console.error('加载评论失败:', res.msg);
      data.comments = [];
      data.commentTotal = 0;
    }
  }).catch(error => {
    console.error('加载评论失败:', error);
    data.comments = [];
    data.commentTotal = 0;
  });
}

// 在加载相册信息后调用加载评论
const loadCategory = () => {
  request.get('/category/selectById/' + data.categoryId).then(res => {
    if (res.code === '200') {
      data.categoryData = res.data
      load()
      increaseViewCount()
      loadComments() // 添加这行代码
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadCategory()

</script>
<style>
.el-col-5 {
  width: 20%;
  max-width: 20%;
}
</style>