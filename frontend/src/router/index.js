import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: '/',
    redirect: '/check'
  },
  {
    path: '/check',
    component: () => import('@/components/login/checkDevice.vue')
  },
  {
    path: '/login',
    component: () => import('@/components/login/login.vue')
  },
  {
    path: '/home',
    component: () => import('@/components/home.vue'),
    redirect: '/patterns/index',
    children: [
      {
        path: '/patterns/index',
        component: () => import('@/components/patterns/index.vue'),
        meta: { title: '主页' }
      },
      {
        path: '/patterns/repository',
        component: () => import('@/components/patterns/repository.vue'),
        meta: { title: '公共模板库' }
      },
      {
        path: '/patterns/process',
        component: () => import('@/components/patterns/process.vue'),
        meta: { title: '提示工程' }
      },
      {
        path: '/patterns/mine',
        component: () => import('@/components/patterns/mine.vue'),
        meta: { title: '我的模板' }
      },
      {
        path: '/patterns/design',
        component: () => import('@/components/patterns/design.vue'),
        meta: { title: '设计模板' }
      },
      {
        path: '/func/chat',
        component: () => import('@/components/func/chat.vue'),
        meta: { title: 'AI交互' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router