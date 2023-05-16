<template>
  <div class="bg-container" v-loading="loading">
    <div class="func-items" @click="replacePath($event)">
      <a class="func-item example">
        <div class="item-icon"><i class="fa fa-plug"></i></div>
        <div class="item-name">示例</div>
      </a>
      <a class="func-item repository">
        <div class="item-icon"><i class="fa fa-inbox"></i></div>
        <div class="item-name">公共模板库</div>
      </a>
      <a class="func-item mine">
        <div class="item-icon"><i class="fa fa-folder-open"></i></div>
        <div class="item-name">我的模板库</div>
      </a>
      <a class="func-item design">
        <div class="item-icon"><i class="fa fa-magic"></i></div>
        <div class="item-name">模板设计</div>
      </a>
      <a class="func-item chat">
        <div class="item-icon"><i class="fa fa-comments-o"></i></div>
        <div class="item-name">聊天室</div>
      </a>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false
    }
  },
  mounted() {
  },
  methods: {
    async replacePath(e) {
      let target = e.target;
      // 防止点击func-items空白区域会执行else跳转的问题
      if (target.parentNode.className === "func-items") {
        if (target.className.includes("example")) {
          this.$router.push({ path: '/patterns/process', query: { example: 1 } })
        }
        else if (target.className.includes("chat")) {
          this.$router.replace({ path: '/func/chat'})
        } else {
          this.$router.replace({ path: `/patterns/${target.classList[0]==='func-item'?target.classList[1]:target.classList[0]}` })
        }
      }
    }
  }
}
</script>

<style lang="less" scoped>
.bg-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  .func-items {
    display: flex;
    width: 100%;
    height: 100%;
    justify-content: center;
    gap: 2rem;
    align-items: center;
    .func-item {
      width: 10rem;
      height: 16rem;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 1rem;
      background-color: #FFFFFF;
      border-radius: 0.25rem;
      transition: all ease .4s;
      cursor: pointer;
      &:hover {
        box-shadow: 0 0 1rem #CCCCCC;
        transform: translateY(-0.5rem);
      }
      .item-icon {
        font-size: 2rem;
        pointer-events: none;
      }
      .item-name {
        pointer-events: none;
      }
    }
    .repository {
      color: #409EFF;
    }
    .mine {
      color: #67C23A;
    }
    .design {
      color: #E6A23C;
    }
    .chat {
      color: #F56C6C;
    }
  }
}
</style>