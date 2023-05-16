<template>
  <div class="container">
    <!-- 登录 -->
    <div class="box-card">
      <el-card class="login-card" v-loading="loadingItem.loading" :element-loading-text="loadingItem.text">
        <div class="title">EASY PROMPT</div>
        <el-form :model="form" label-width="0">
          <el-form-item label="">
            <el-input maxlength="100" size="large" placeholder="用户ID或绑定邮箱" v-model="form.username" />
          </el-form-item>
          <el-form-item label="">
            <el-input show-password size="large" type="password" placeholder="密码" v-model="form.password" />
          </el-form-item>
          <el-button @click="login()" size="large" type="primary" class="login-btn">登录</el-button>
          <div class="register-btn"><el-button type="primary" link @click="toregister()">没有账号?立即注册</el-button></div>
        </el-form>
      </el-card>
      <el-card class="register-card" v-loading="loadingItem.loading" :element-loading-text="loadingItem.text">
        <div class="title">EASY PROMPT</div>
        <el-form :model="registerForm" label-width="0">
          <el-form-item label="">
            <el-input size="large" placeholder="昵称" v-model="registerForm.username" maxlength="6"></el-input>
          </el-form-item>
          <el-form-item label="">
            <el-input size="large" placeholder="登录密码" type="password" show-password maxlength="30" v-model="registerForm.password"></el-input>
          </el-form-item>
          <el-form-item label="">
            <el-input size="large" placeholder="绑定邮箱" maxlength="100" v-model="registerForm.email"></el-input>
          </el-form-item>
          <el-form-item label="">
            <el-input size="large" placeholder="验证码" v-model="registerForm.captcha" maxlength="6">
              <template #append>
                <el-button type="primary" :disabled="!canGetCaptcha" @click="getCaptcha()">{{ registerBtnText }}</el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-button size="large" type="primary" @click="register" class="register-go">开始注册</el-button>
          <div class="login-btn"><el-button type="primary" link @click="tologin()">已有帐号?前往登录</el-button></div>
        </el-form>
      </el-card>
    </div>
    <div class="footer">{{ dynamicTitle }}<span class="title-pla"></span></div>
  </div>
</template>

<script>
import { login, getCaptcha, register, autoLogin } from '@/api/api.js'
import { setToken, removeToken, getToken } from '@/utils/tokenUtil';
export default {
  data() {
    return {
      loadingItem: {
        loading: false,
        text: ""
      },
      form: {
        username: '',
        password: ''
      },
      canGetCaptcha: true,
      registerForm: {
        username: '',
        password: '',
        email: '',
        captcha: ''
      },
      dynamicTitle: '',
      registerToken: "",
      hasCaptcha: false,
      registerBtnText: '获取验证码',
      emailReg: /^[a-zA-Z0-9*._-]+@[a-zA-Z0-9*._-]+(\.[a-zA-Z0-9_-]+)+[a-zA-Z]+$/
    }
  },
  mounted() {
    this.autoLogin();
    this.titleCode();
    this.showWelcomeInfo();
  },
  methods: {
    /**
     * 自动登录
     */
    async autoLogin() {
      this.loadingItem.loading = true;
      this.loadingItem.text = '登陆中...';
      const bearer = getToken("bearer");
      if (!bearer) {
        this.loadingItem.loading = false;
        this.loadingItem.text = '';
        return;
      }
      await autoLogin({bearer: bearer}).then(() => {
        this.$router.replace('/home');
      }).finally(() => {
        this.loadingItem.loading = false;
        this.loadingItem.text = '';
      })
    },
    /**
     * 手动登录
     */
    async login() {
      if (Object.entries(this.form).length === 0) {
        return this.$message({
          type: 'warning',
          message: '请输入用户名和密码'
        })
      }
      for (const i in this.form) {
        if (!this.form[i] || !this.form[i].trim()) {
          return this.$message({
            type: 'warning',
            message: '请输入用户名/邮箱或密码'
          })
        }
      }
      // 初步校验用户名格式
      const useridReg = /\d{6}/;
      if (!this.emailReg.test(this.form.username)) {
        if (!useridReg.test(this.form.username)) {
          return this.$message({
            type: 'warning',
            message: '用户名格式有误'
          })
        }
      }
      let loginToken = Object.assign({}, this.form);
      loginToken.timestamp = Date.now() + "";
      this.loadingItem.loading = true;
      this.loadingItem.text = "登陆中...";
      await login({loginToken: btoa(JSON.stringify(loginToken))}).then(res => {
        setToken("bearer", res.bearer);
        // 根据用户角色，与主页通信，动态设置主页的菜单显示
        this.$router.push('/home');
      }).catch(() => {
        // 如果失败了，清空本地bearer
        removeToken("bearer");
      }).finally(() => {
        this.loadingItem.loading = false;
      })
    },
    // 打字机效果
    titleCode() {
      const title = " Design and experience various of good prompts!";
      let num = -1;
      let deleteFunc = () => {
        this.dynamicTitle = this.dynamicTitle.slice(0, -1);
        if (this.dynamicTitle === "") {
          knockFunc();
          return;
        }
        setTimeout(deleteFunc, 50);
      }
      let knockFunc = () => {
        if (this.dynamicTitle === title) {
          num = -1;
          setTimeout(deleteFunc, 2000);
          return;
        }
        this.dynamicTitle += title[++num];
        setTimeout(knockFunc, Math.floor(Math.random()*2000+1000)/10);
        // setTimeout(knockFunc, 1000);
      }
      knockFunc();      
    },
    toregister() {
      let loginDialog = document.querySelector(".login-card");
      loginDialog.style.display = "none";
      let registerDialog = document.querySelector(".register-card");
      registerDialog.style.display = "block";
    },
    tologin() {
      let loginDialog = document.querySelector(".login-card");
      loginDialog.style.display = "block";
      let registerDialog = document.querySelector(".register-card");
      registerDialog.style.display = "none";
    },
    // 获取验证码
    async getCaptcha() {
      this.canGetCaptcha = false;
      let params = Object.assign({}, this.registerForm);
      delete params.captcha;
      // 检查是否漏填
      if (Object.entries(params).length === 0) {
        return this.$message({
          type: 'warning',
          message: '内容不能为空'
        })
      }
      for (const i in params) {
        if (!params[i] || !params[i].trim()) {
          return this.$message({
            type: 'warning',
            message: '信息填写不完整'
          })
        }
      }
      // 检查用户名
      const nameReg = / |<|>|'|"/;
      if (nameReg.test(params.username) || params.username.includes("\\")) {
        return this.$message({
          type: 'warning',
          message: '昵称中包含了非法字符'
        })
      }
      if (!this.emailReg.test(params.email)) {
        return this.$message({
          type: 'warning',
          message: '邮箱格式不正确'
        })
      }
      params.timestampss = Date.now() + "";
      this.loadingItem.loading = true;
      this.loadingItem.text = "获取验证码...";
      await getCaptcha(params).then((res) => {
        this.registerToken = res;
        this.$notify({
          title: '提示',
          type: 'success',
          message: '验证码已发送至邮箱, 3分钟内有效。'
        })
        this.hasCaptcha = true;
        let timecost = 60;
        let timeCountInterval = setInterval(() => {
          if (timecost === 0) {
            this.canGetCaptcha = true;
            this.registerBtnText = "获取验证码";
            clearInterval(timeCountInterval);
            return;
          }
          this.registerBtnText = `${timecost--} s后再试`;
        }, 1000)
      }).finally(() => {
        this.loadingItem.loading = false;
        this.loadingItem.text = "";
      })
    },
    // 开始注册
    async register() {
      if (!this.hasCaptcha || !this.registerToken) {
        return this.$message({
          type: 'warning',
          message: '请先获取验证码'
        })
      }
      if (!this.registerForm.captcha || !this.registerForm.captcha.trim() || this.registerForm.captcha.length !== 6) {
        return this.$message({
          type: 'warning',
          message: '验证码有误'
        })
      }
      this.loadingItem.loading = true;
      this.loadingItem.text = "注册中...";
      await register({registerToken: this.registerToken, captcha: this.registerForm.captcha}).then(() => {
        this.$notify({
          title: '提示',
          type: 'success',
          message: '注册成功，请使用邮箱登录。登陆后可以在首页右上角 我的 板块查看你的id，并可在下次登陆时直接使用id登录',
          duraion: 6000
        })
        // 清空相关数据
        for (const i in this.form) {
          this.form[i] = '';
        }
        this.hasCaptcha = false;
        this.registerToken = "";
        // 切换到登录页面
        this.tologin();
      }).finally(() => {
        this.loadingItem.loading = false;
        this.loadingItem.text = "";
      })
    },
    showWelcomeInfo() {
      console.log(`%c welcome to %c easy-prompt %c, a platform to design, use and share good prompts based on L2M category.`, "color: #999", "color: #0081ff", "color: #999");
      console.log(`%c find more interesting projects on %c https://github.com/quarkape`, "color: #999", "color:#0081ff;font-style:italic;");
    }
  }
}
</script>

<style lang="less" scoped>
.container {
  height: 100%;
  width: 100%;
  position: absolute;
  background-image: linear-gradient(to top, #330867 0%, #30cfd0 100%);
  background-repeat: no-repeat;
  background-size: auto;
  .box-card {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    .login-card {
      // display: none;
      width: 450px;
      .title {
        font-size: 20px;
        margin: 10px 0 20px 0;
        text-align: center;
        font-weight: bold;
        height: 20px;
      }
      .login-btn {
        width: 100%;
      }
      .register-btn {
        text-align: right;
        margin-top: 0.75rem;
        font-size: 14px;
      }
    }
    .register-card {
      display: none;
      width: 450px;
      .title {
        font-size: 20px;
        margin: 10px 0 20px 0;
        text-align: center;
        font-weight: bold;
        height: 20px;
      }
      .register-go {
        width: 100%;
        margin-top: 0.5rem;
      }
      .login-btn {
        text-align: right;
        margin-top: 0.75rem;
        font-size: 14px;
      }
    }
    
  }
  
  .footer {
    position: fixed;
    left: 2rem;
    bottom: 2rem;
    z-index: 9;
    font-size: 14px;
    color: #FFFFFF;
    .title-pla {
      padding-left: 2px;
      animation: changeTitleCursorColor ease infinite 1.5s;
      background-color: #FFFFFF;
    }
    .qk {
      color: #FFFFFF;
    }
  }
}

@keyframes changeTitleCursorColor {
  from {
    opacity: 1;
  } to {
    opacity: 0;
  }
}
</style>