<template>
  <div class="chat-container">
    <el-card class="box-card">
      <div class="config">
        <div class="func-tools">
          <el-button type="primary" @click="exportFile()" plain><i class="fa fa-download"></i>&nbsp;导出</el-button>
          <el-button type="warning" @click="showNotificationDialog = true" plain>说明</el-button>
        </div>
        <div class="config-items">
          <div class="config-item">
            <div class="config-title">temperature:</div>
            <!-- <el-slider :min="0" :max="2" :step="0.1" v-model="temperature" class="slider" :format-tooltip="setTempMsg"></el-slider> -->
            <el-slider :min="0" :max="2" :step="0.1" v-model="temperature" class="slider"></el-slider>
          </div>
          <div class="config-item">
            <div class="config-title">presence_penalty:</div>
            <el-slider :min="-2" :max="2" :step="0.1" v-model="ppval" class="slider"></el-slider>
          </div>
          <div class="config-item">
            <div class="config-title">frequency_penalty:</div>
            <el-slider :min="-2" :max="2" :step="0.1" v-model="pfval" class="slider"></el-slider>
          </div>
        </div>        
      </div>
      <div class="chat-items scrollbar scrollbar-dark">
        <div class="chat-item" v-for="(item, index) in chats" :key="index">
          <div class="item-avatar">
            <img :src="item.type===0?userAvatar:aiAvatar" alt="用户头像" />
          </div>
          <div class="item-content"><pre>{{ item.content }}</pre></div>
          <div class="item-tool" @click="copyContent(item.content)">复制</div>
        </div>
        <div class="chat-placeholder"></div>
      </div>
      <!-- 
        
       -->
      <div class="prompt-box">
        <div class="show-status" :style="{color:(sendingStatus?'#F56C6C':'#E6A23C')}"><i :class="[sendingStatus?'fa fa-hourglass-2  fa-spin':'fa fa-rocket']"></i>&nbsp;{{ sendingStatus===0?'空闲...':'任务正在进行...' }}</div>
        <div class="statistic"><i class="fa fa-cloud-upload"></i>&nbsp;{{ promptTokens }}&nbsp;&nbsp;|&nbsp;&nbsp;<i class="fa fa-cloud-download"></i>&nbsp;{{ completionTokens }}&nbsp;&nbsp;|&nbsp;&nbsp;<i class="fa fa-clock-o"></i>&nbsp;{{ timeCost/1000 }}s | <span @click="clearChats()" class="refresh-btn" title="清屏（仍将保留上下文能力）。想要重新开始请刷新页面。"><i class="fa fa-refresh"></i></span></div>
        <el-input resize="none" type="textarea" maxlength="2000" :autosize="{ minRows: 2, maxRows: 10 }" v-model="prompt" placeholder="输入内容以开始"></el-input>
        <el-button @click="sendPrompt()" class="send-btn" type="primary" :disabled="sendingStatus===1"><i :class="[sendingStatus?'fa fa-spinner fa-pulse':'fa fa-send']"></i></el-button>
      </div>
    </el-card>

    <!-- 说明弹窗 -->
    <el-dialog class="notification" v-model="showNotificationDialog" title="操作说明" width="30%">
      <p>1、一般情况下，不建议调整右上角三个参数。</p>
      <p>2、如果发送文本后得到提示“服务器错误，请联系管理员”可能是因为将要返回的文本太多，无法继续返回内容导致。请重试或者使用网页版已获得继续下文的能力。</p>
    </el-dialog>
  </div>
</template>

<script>
import { simpleChat } from '@/api/api.js'
// 使用下面的插件可以实现代码的高亮显示
// import hljs from 'highlight.js'
// import 'highlight.js/styles/idea.css'
export default {
  data() {
    return {
      userAvatar: require('../../assets/imgs/avatar.jpg'),
      aiAvatar: require('../../assets/imgs/ai.png'),
      prompt: '',
      chatToken: null,
      temperature: 0,
      ppval: 0,
      pfval: 0,
      sendingStatus: 0,
      promptTokens: 0,
      completionTokens: 0,
      timeCost: 0,
      chats: [
        {
          type: 1,
          content: '基于gpt-3.5-turbo开发，从下方输入框输入任何文字以开始。'
        }
      ],
      showNotificationDialog: false
    }
  },
  mounted() {
  },
  methods: {
    async sendPrompt() {
      if (this.sendingStatus) {
        return this.$message({
          type: 'warning',
          message: '请等待当前任务处理完成'
        })
      }
      // 限制输入字数2000
      if (this.prompt.length > 2000) {
        return this.$message({
          type: 'warning',
          message: '内容限制2000'
        })
      }
      if (!this.prompt.trim()) {
        return this.$message({
          type: 'warning',
          message: '请输入提示内容'
        })
      }
      this.sendingStatus = 1;
      let chata = {
        type: 0,
        content: this.prompt
      }
      this.chats.push(chata);
      let chatb = {
        type: 1,
        content: '任务正在处理中......'
      }
      this.chats.push(chatb);
      this.$nextTick(() => {
        document.getElementsByClassName("chat-placeholder")[0].scrollIntoView(false);
      })
      let timea = Date.now();
      let curPrompt = this.prompt;
      // 让输入框失焦
      document.getElementsByClassName("el-textarea__inner")[0].blur();
      let params = {
        prompt: curPrompt,
        temperature: this.temperature,
        ppval: this.ppval,
        pfval: this.pfval,
        token: this.chatToken
      }
      // this.prompt = '';
      await simpleChat(params).then(res => {
        if (res.token) {
          this.chatToken = res.token
        }
        this.chats.slice(-1)[0].content = res.content;
        this.$nextTick(() => {
          document.getElementsByClassName("chat-placeholder")[0].scrollIntoView(false);
        })
        this.timeCost += (Date.now() - timea);
        this.promptTokens += res.promptTokens;
        this.completionTokens += res.completionTokens;
        // 如果任务执行成功，则清除输入框内的内容
        this.prompt = '';
      }).catch(() => {
        this.chats = this.chats.slice(0, -2);
      }).finally(() => {
        this.sendingStatus = 0;
        // 任务完成后，再让输入框聚焦
        document.getElementsByClassName("el-textarea__inner")[0].focus();
      })
    },
    copyContent(content) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(content).then(() => {
          this.$message({
            type: 'success',
            message: '操作成功'
          })
        });
      } else {
        let textbox = document.createElement("textarea")
        textbox.style.cssText = 'position:fixed;top:0;display:none';
        // 通过js设置class样式的几种方式
        // 1. node.style
        //  1.1 node.style.propertyName
        //  1.2 node.style.cssText
        //  1.3 node.style.setProperty('position', 'fixed', 'important')
        // 2. node.classList.add()
        // 3. node.ClassName()
        // 4. node.setAttribute
        //  4.1 node.setAttribute("style", "position:fixed")
        //  4.2 node.setAttribute("position", "fixed")
        textbox.value = content;
        document.body.appendChild(textbox);
        textbox.select();
        document.execCommand('copy', true);
        document.body.removeChild(textbox);
        this.$message({
          type: 'success',
          message: '操作成功'
        })
      }
    },
    clearChats() {
      if (this.sendingStatus) {
        this.$message({
          type: 'warning',
          message: '请等待当前任务处理完成'
        })
      }
      this.chats = this.chats.slice(0, 1);
      this.chatToken = null;
    },
    exportFile() {
      if (this.sendingStatus) {
        return this.$message({
          type: 'warning',
          message: '请等待当前任务完成'
        })
      }
      if (this.chats.length === 1) {
        return this.$message({
          type: 'warning',
          message: '暂无记录可以导出'
        })
      }
      this.$notify({
        type: 'info',
        title: '提示',
        message: '导出中，请稍等...'
      })
      let content = '';
      content += `【EASY_PROMPT互动记录导出】\n\n`;
      for (let i=1;i<this.chats.length;i++) {
        content += `${i & 1 ? '发送' : '回复'}:\n${this.chats[i].content}\n\n`;
      }
      const el = document.createElement("a");
      el.setAttribute('download', 'EASY_PROMPT互动记录导出.doc');
      const blob = new Blob([content])
      el.setAttribute('href', URL.createObjectURL(blob));
      el.style.display = 'none';
      document.body.appendChild(el);
      el.click();
      document.body.removeChild(el);
    }
  }
}
</script>

<style lang="less" scoped>
:deep(.el-card__body) {
  padding-bottom: none;
}
:deep(textarea::placeholder) {
  color: #666;
}
:deep(textarea::-webkit-scrollbar) {
  width: 8px;
  height: 10px;
}
:deep(textarea::-webkit-scrollbar-track) {
  background: transparent;
}
:deep(textarea::-webkit-scrollbar-thumb) {
  background-color: #999;
  border-radius: 4px;
}
:deep(textarea::-webkit-scrollbar-thumb:hover) {
  background-color: #666;
}
:deep(textarea::-webkit-scrollbar-corner) {
  background-color: #333;
}
:deep(.el-card__body) {
  height: calc(100% - 2rem);
  overflow: hidden;
  position: relative;
  padding: 1rem!important;
}
:deep(.slider) {
  width: 60px !important;
}
:deep(.el-slider__bar) {
  height: 0.25rem;
}
:deep(.el-slider__button) {
  width: 12px;
  height: 12px;
}
:deep(.el-slider__runway) {
  height: 0.25rem;
}
pre {
  white-space: pre-wrap!important;
}
.chat-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  .box-card {
    height: 100%;
    width: 100%;
    position: relative;
    box-sizing: border-box;
    overflow-y: hidden;
    .config {
      margin-bottom: 1rem;
      text-align: right;
      height: 3rem;
      box-sizing: border-box;
      display: flex;
      gap: 0.6rem;
      position: relative;
      text-align: right;
      justify-content: space-between;
      // background-color: #f6f6f6;
      // float: right;
      .func-tools {
        gap: 0.5rem;
        display: flex;
        height: 3rem;
        align-items: center;
        padding-left: 1rem;
      }
      .config-items {
        height: 3rem;
        display: flex;
        align-items: center;
        .config-item {
          display: flex;
          align-items: center;
          line-height: 1rem;
          .config-title {
            display: inline-block;
            position: relative;
          }
        }
        .slider {
          width: 100px;
          margin: 0 20px;
        }
      }
    }
    .chat-items {
      overflow-y: auto;
      height: calc(100% - 4rem);
      .chat-item {
        padding: 1.5rem 2rem;
        box-sizing: border-box;
        transition: background-color ease .2s;
        cursor: pointer;
        width: 100%;
        min-height: 4rem;
        position: relative;
        display: flex;
        border-bottom: 1px solid #EBEEF5;
        .item-avatar {
          width: 2rem;
          border-radius: 0.25rem;
          overflow: hidden;
          margin-right: 1rem;
          img {
            width: 2rem;
            height: 2rem;
          }
        }
        .item-content {
          flex: 1;
          text-align: justify;
          white-space: normal;
          word-break: break-all;
          line-height: 1.5rem;
        }
        .item-tool {
          position: absolute;
          right: 0;
          top: 0;
          font-size: 12px;
          padding: 0.25rem 0.5rem;
          background-color: #0081FF;
          visibility: hidden;
          color: #FFFFFF;
        }
        &:hover {
          background-color: #f6f6f6;
        }
        &:hover .item-tool {
          visibility: visible;
        }
      }
      .chat-placeholder {
        width: 100%;
        height: 7rem;
        background-color: #FFFFFF;
      }
    }
    .prompt-box {
      position: absolute;
      width: 100%;
      box-sizing: border-box;
      padding: 0 2rem 1rem 2rem;
      bottom: 0;
      left: 0;
      background-color: #FFFFFF;
      box-shadow: -0.125rem -1.5rem  2rem #FFFFFF;
      .show-status {
        position: absolute;
        left: 2rem;
        top: -2rem;
        height: 2rem;
        line-height: 2rem;
      }
      .statistic {
        position: absolute;
        right: 2rem;
        height: 2rem;
        line-height: 2rem;
        top: -2rem;
        z-index: 2;
        color: #999;
        .refresh-btn {
          padding: 0 1rem;
          cursor: pointer;
          font-size: 1rem;
          &:hover {
            color: #333333;
          }
        }
      }
      & :deep(textarea) {
        width: 100%;
        line-height: 1.5rem;
        box-sizing: border-box;
        padding: 0.5rem 3.5rem 0.5rem 1rem;
        text-align: justify;
      }
      &
      .send-btn {
        position: absolute;
        right: 3rem;
        bottom: 2rem;
        width: 3rem;
        height: 2rem;
      }
    }
  }
  .notification {
    font-size: 16px;
    color: red;
  }
}
</style>