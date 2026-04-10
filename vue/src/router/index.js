import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login'},
    {
      path: '/manager',
      name: 'Manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/manager/home',
      children: [
        { path: 'person', meta: { name: '个人资料' }, component: () => import('@/views/manager/Person.vue')},
        { path: 'password', meta: { name: '修改密码' }, component: () => import('@/views/manager/Password.vue')},
        { path: 'home', meta: { name: '系统首页' }, component: () => import('@/views/manager/Home.vue')},
        { path: 'admin', meta: { name: '管理员信息' }, component: () => import('@/views/manager/Admin.vue')},
        { path: 'notice', meta: { name: '系统公告' }, component: () => import('@/views/manager/Notice.vue')},
        { path: 'user', meta: { name: '用户信息' }, component: () => import('@/views/manager/User.vue')},
        // 新路由
        { path: 'collect', meta: { name: '收藏信息' }, component: () => import('@/views/manager/Collect.vue')},
        { path: 'picture', meta: { name: '照片信息' }, component: () => import('@/views/manager/Picture.vue')},
        { path: 'pictures', meta: { name: '照片信息' }, component: () => import('@/views/manager/Pictures.vue')},
        { path: 'appeal', meta: { name: '申诉信息' }, component: () => import('@/views/manager/Appeal.vue')},
        { path: 'category', meta: { name: '相册信息' }, component: () => import('@/views/manager/Category.vue')},
        { path: 'albums', meta: { name: '相册广场' }, component: () => import('@/views/manager/Albums.vue')},
      ]
    },
    {
      path: '/front',
      name: 'Front',
      component: () => import('@/views/Front.vue'),
      redirect: '/front/home',
      children: [
        { path: 'home', component: () => import('@/views/front/Home.vue')},
        { path: 'person', component: () => import('@/views/front/Person.vue')},
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue')},
    { path: '/register', component: () => import('@/views/Register.vue')},
    { path: '/404', component: () => import('@/views/404.vue')},
    { path: '/:pathMatch(.*)', redirect: '/404', hidden: true }
  ]
})

export default router
