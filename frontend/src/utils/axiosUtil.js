import axios from 'axios'
import { getToken, removeToken } from '@/utils/tokenUtil.js'
import { ElMessage } from 'element-plus'
import router from '@/router'
import npg from 'nprogress'

import 'nprogress/nprogress.css'


npg.configure({ showSpinner: false })

const myMsg = (msg) => {
  return ElMessage({
    message: msg,
    type: 'warning'
  })
}

const https = axios.create({
  // baseURL: 'http://localhost:8002/api/',
  // 设置超时时间3分钟
  timeout: 180000
})
/**
 * 请求拦截
 */
const excludes = ['login', 'register', 'get-captcha'];
 https.interceptors.request.use((config) => {
  if (!excludes.includes(config.url)) {
    // 判断是否存在token
    if (!getToken('bearer')) {
      router.replace({ path: '/login' })
      return Promise.reject('error');
    }
    // 获取并设置token
    // 直接将bearer传入后端，由后端进行校验
    config.headers['bearer'] = getToken('bearer');
  }
  npg.start();
  return config;
}, (error) => {
  npg.done();
  return Promise.reject(error);
})

/**
 * 响应拦截
 */
 https.interceptors.response.use((response) => {
  npg.done();
  if (response.data.code != 0) {
    // 得到了正常的响应
    myMsg(response.data.message);
    return Promise.reject(response);
  }
  return response.data.data;
}, (error) => {
  // 没有得到正常的响应
  npg.done();
  if (error && error.response) {
    switch (error.response.status) {
      case 402:
        removeToken("bearer");
        router.replace({ path: '/login' });
        break;
      case 404:
        myMsg("找不到当前页面请求");
        break;
      case 500:
        myMsg("服务器出错，请联系管理员");
        break;
      case 601:
        myMsg("权限不足");
        break;
      case 602:
        myMsg("请重新登录");
        removeToken("bearer");
        setTimeout(() => {
          router.replace({ path: '/login' });
        })
        break;
      case 700:
        myMsg("不在可用时间范围内");
        router.replace({ path: '/login' });
        break;
      default:
        myMsg('ops!');
        break;
    }
  }
  return Promise.reject(error);
})


export default https