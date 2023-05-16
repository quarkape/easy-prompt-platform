<template>
  <div class="check-container">
    <div class="check-content">{{ content }}</div>
    <div class="check-btn"><el-button type="primary" @click="toLogin()">进入</el-button></div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      checked: false,
      content: ''
    }
  },
  mounted() {},
  created() {
    this.check()
  },
  methods: {
    check() {
      const agent = navigator.userAgent.toUpperCase();
      // const deviceTypes = ['android', 'iphone', 'window', 'ipad'];
      if (agent.indexOf('WINDOWS') < 0 && agent.indexOf('IPAD') < 0 && agent.indexOf) {
        this.checked = false;
        this.content = '提示：在PC端访问已获得更好的使用体验，或者打开手机浏览器的PC模式';
      } else {
        this.$router.replace("/login");
      }
    },
    toLogin() {
      // 用户依然选择进入登录页面
      this.$router.replace({ path: '/login' });
    }
  }
}
</script>

<style lang="less" scoped>
.check-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 1rem;
  box-sizing: border-box;
  .check-content {
    text-align: center;
    font-weight: 1.5rem;
    margin-bottom: 1rem;
    line-height: 1.5rem;
    font-weight: bold;
  }
}
</style>