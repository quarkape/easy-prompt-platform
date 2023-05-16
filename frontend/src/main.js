import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import locale from 'element-plus/es/locale/lang/zh-cn'
import qs from 'qs'
import 'font-awesome/css/font-awesome.min.css'
import { pdate, getUUID } from '@/utils/toolsUtil.js'

import router from '@/router'



/**
 * 检查token是否过期
 */
router.beforeEach((to, from, next) => {
  // let bearer = localStorage.getItem('bearer');
  // if (bearer) {
  //   if (JSON.parse(window.atob(bearer)).exp > new Date().getTime()) {
  //     if (to.path === '/login') {
  //       router.replace({ path: '/home' }); 
  //     }
  //     if (to.path === '/patterns/process' && !to.query.patternid) {
  //       router.replace({ path: '/home' });
  //     }
  //     return next();
  //   }
  // } else {
  //   return router.replace({ path: '/login' });
  // }
  next();
})


const app = createApp(App)

app.config.globalProperties.$qs = qs
app.config.globalProperties.$filters = {
  pdate: pdate,
  getUUID: getUUID
}

app.use(router).use(ElementPlus, {locale}).mount('#app')