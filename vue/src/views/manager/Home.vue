<template>
  <div>
    <div class="card">尊敬的 <span style="font-weight: bold">{{ data.user?.name }}</span> 您好！欢迎使用本系统，祝您今天有个好心情！</div>
    <div style="margin-top: 10px; display: flex; grid-gap: 10px" v-if="data.user.role === 'USER'">
      <div class="card" style="width: 50%">
        <div style="font-size: 20px; font-weight: bold; padding: 15px 20px">系统公告</div>
        <el-timeline style="max-width: 650px">
          <el-timeline-item color="#0bbd87" :key="notice.id" v-for="notice in data.noticeList" placement="top" :timestamp="notice.time">
            <div style="font-weight: bold; font-size: 15px">{{ notice.title }}</div>
            <div style="margin-top: 10px; line-height: 20px">{{ notice.content }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
    <div v-if="data.user.role === 'ADMIN'" class="card" style="margin-top: 10px; height: 600px" id="bar"></div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import * as echarts from "echarts";
import {ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  noticeList: [],
})
const loadNotice = () => {
  request.get('/notice/selectAll').then(res => {
    data.noticeList = res.data
  })
}
loadNotice()

const loadBar = () => {
  // 只在用户是管理员时才调用bar接口
  if (data.user.role === 'ADMIN') {
    request.get('/picture/bar').then(res => {
      if (res.code === '200') {
        let chartDom = document.getElementById('bar')
        let myChart = echarts.init(chartDom)
        barOptions.xAxis.data = res.data.xList
        barOptions.series[0].data = res.data.yList
        myChart.setOption(barOptions)
      } else {
        ElMessage.error(res.msg)
      }
    })
  }
}
loadBar()

// 柱状图
let barOptions = {
  title: {
    text: '相册热度Top10柱状图', // 主标题
    subtext: '统计维度：相册名称', // 副标题
    left: 'center'
  },
  grid : {   // ---------------------------增加这个属性，bottom就是距离底部的距离
    bottom : '15%',
    top: "20%"
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  xAxis: {
    type: 'category',
    data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'], // 示例数据：统计的维度（横坐标）
    name: '相册名称',
    axisLabel: {
      show: true, // 是否显示刻度标签，默认显示
      interval: 0, // 坐标轴刻度标签的显示间隔，在类目轴中有效；默认会采用标签不重叠的策略间隔显示标签；可以设置成0强制显示所有标签；如果设置为1，表示『隔一个标签显示一个标签』，如果值为2，表示隔两个标签显示一个标签，以此类推。
      rotate: -60, // 刻度标签旋转的角度，在类目轴的类目标签显示不下的时候可以通过旋转防止标签之间重叠；旋转的角度从-90度到90度
      inside: false, // 刻度标签是否朝内，默认朝外
      margin: 6, // 刻度标签与轴线之间的距离
    },
  },
  yAxis: {
    type: 'value',
    name: '热度',
  },
  tooltip: {
    trigger: 'item',
  },
  series: [
    {
      data: [120, 200, 150, 80, 70, 110, 130], // 示例数据：横坐标维度对应的值（纵坐标）
      type: 'bar',
      itemStyle: {
        normal: {
          color: function () {
            return "#" + Math.floor(Math.random() * (256 * 256 * 256 - 1)).toString(16);
          }
        },
      },
    }
  ]
};
</script>