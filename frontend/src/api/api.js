// 将接口封装

import https from "@/utils/axiosUtil";
import qs from 'qs'


const post = (url, data) => {
  return https({
    url: url,
    method: 'post',
    data: qs.stringify(data)
  })
}

const get = (url) => {
  return https({
    method: 'get',
    url
  })
}

// 登录 | 登录
export function login(data) {
  return post('login', data);
}
// 登录 | 自动登录
export function autoLogin(data) {
  return post('auto-login', data);
}

// 登录 | 登出
export function logout() {
  return get('logout');
}

// 注册 | 获取验证码
export function getCaptcha(data) {
  return post('get-captcha', data);
}

// 注册
export function register(data) {
  return post('register', data);
}

// 主页 | 获取示例模板的ID
export function getExamplePattern() {
  return get('example-pattern');
}

// 模板库 | 获取公共模板列表
export function getPatternList(data) {
  return post('pattern-list', data);
}

// 模板库 | 获取模板的类型（deprecated）
export function getPatternType() {
  return get('pattern-type');
}

// 模板库 & 我的模板 & 模板设计 | 获取模板所属的领域
export function getPatternField() {
  return get('pattern-field');
}

// 应用 | 获取模板内容
export function getPattern(data) {
  return post('pattern', data);
}

// 应用 | 获取应用说明
export function getMunual() {
  return get('manual');
}

// 应用 | 发送提示获取返回文本
export function sendPrompt(data) {
  return post('prompt-conv', data);
}

// 应用 | 设置最佳文本
export function setIdealContent(data) {
  return post('prompt-ideal', data);
}

// 我的模板 | 获取我的模板列表
export function getMyPattern(data) {
  return post('pattern-mine', data);
}

// 全局 | 获取个人资料
export function getPersonalProfile() {
  return post('personal-profile');
}

// 应用 & 设计 | 获取模板配置
export function getPatternConfig(data) {
  return post('pattern-config', data);
}

// 我的模板 | 获取我的模板信息
export function getMyPatternInfo(data) {
  return post('get-mypattern-info', data);
}

// 模板库 | 将模板添加到我的库
export function addPatternToMine(data) {
  return post('add-pattern-mine', data);
}

// 设计 | 修改模板配置
export function modifyPatternConfig(data) {
  return post('modify-pattern-config', data);
}

// 我的模板 | 删除模板
export function deletePattern(data) {
  return post('delete-pattern', data);
}

// 实验室 | 聊天
export function simpleChat(data) {
  return post('chat-simple', data);
}

//  设计 |修改模板信息
export function modifyPatternInfo(data) {
  return post('modify-pattern-info', data);
}