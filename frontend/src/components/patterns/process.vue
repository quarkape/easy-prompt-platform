<template>
  <div class="con" v-if="pattern">
    <!-- 上方步骤条 -->
    <!-- <div class="step-con">
      <el-steps v-if="showSlider" :active="step" finish-status="success" simple class="steps-bg scrollbar scrollbar-light">
        <el-step :title="'步骤 '+index" v-for="index in steps.length" :key="index"></el-step>
      </el-steps>
    </div> -->
    <!-- 下方编辑区 -->
    <div class="content-con" v-loading="loading" element-loading-text="加载中">
      <!-- 左边对话框 -->
      <div class="box-chat">
        <!-- 上方交流区 -->
        <div class="chat-items scrollbar scrollbar-dark" v-if="hasGotPattern">
          <div class="chat-item show-tool" v-for="(item, index) in steps[step].chats" :key="index">
            <div class="item-avatar">
              <div><img :src="item.type===0?userAvatar:aiAvatar"></div>
              <div class="ideal-star" v-if="steps[step].needed && item.type===1" :key="idealStar" @click="setIdealContent(index)" :style="{color:(item.ideal?'#409EFF':'#D9ECFF'),visibility:(item.ideal?'visible':'')}" :title="(item.ideal?'已':'')+'标记为最佳生成文本'">
                <svg t="1679550310881" fill="currentColor" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5752" width="1.5em" height="1.5em"><path d="M720.398507 959.573333c73.045333 31.317333 136.96-15.317333 129.706666-94.293333l-20.650666-226.218667 174.634666-199.722666c38.144-43.648 19.2-102.229333-37.418666-115.114667l-258.474667-58.794667-135.68-228.010666c-29.738667-49.877333-91.306667-49.92-121.045333 0L315.74784 265.429333 57.273173 324.224C0.953173 337.066667-18.33216 395.648 19.854507 439.338667l174.634666 199.722666-24.021333 264.405334c-5.248 57.770667 44.544 94.037333 97.877333 71.125333l243.626667-104.533333 208.426667 89.472z" p-id="5753"></path></svg>
              </div>
            </div>
            <div class="item-content">{{ item.content }}</div>
            <div class="item-tool" @click="copyContent(item.content)">
              <div class="tool-copy" size="mini" title="复制文本">复制</div>
            </div>
          </div>
          <div class="chat-placeholder"></div>
        </div>
        <!-- 下方输入区 -->
        <div class="input-box">
          <div class="cur-status" :key="changeStatusKey" :style="{ color: statusList[status].color }"><i :class="statusList[status].icon"></i>&nbsp;{{ statusList[status].name }}</div>
          <div class="calcs">
            <div>{{status===0&&steps[step].chats.length>1?(processStatus===1?('本次处理成功&nbsp;|&nbsp;耗时'+lastTime/1000+'s'):'处理失败'):''}}</div>
            <div class="calc-prompt-tokens"><i class="fa fa-upload"></i> {{ promptTokens }}</div>
            <div class="calc-completion-tokens"><i class="fa fa-download"></i> {{ completionTokens }}</div>
            <div class="calc-time"><i class="fa fa-clock-o"></i>&nbsp;{{ timeCost / 1000 }}s</div>
          </div>
          <el-input type="textarea" class="main-input" :autosize="{ minRows: 1, maxRows: 8}" placeholder="模板会自动填充内容或者需要您输入内容" v-model="inputContent" disabled></el-input>
        </div>
        <!-- 发送按钮 -->
        <div class="statistic">
          <div class="input-btns">
            <button class="resend-btn" title="清空对话框"><i class="fa fa-trash-o" @click="clearChatHistory"></i></button>
            <button class="resend-btn" title="清空输入框"><i class="fa fa-refresh" @click="resetInputContent"></i></button>
            <button class="send-btn" title="发送" @click="sendPrompt()" :disabled="disableInputContent" size="large"><i :class="[status?'fa fa-spinner fa-pulse':'fa fa-send']"></i></button>
          </div>
        </div>
      </div>
      <!-- 右边信息栏 -->
      <div class="detail">
        <!-- 配置区域 -->
        <div class="config">
          <div class="config-item">
            <div class="config-title">t值:</div>
            <!-- <el-slider :min="0" :max="2" :step="0.1" v-model="temperature" class="slider" :format-tooltip="setTempMsg"></el-slider> -->
            <el-slider :min="0" :max="2" :step="0.1" v-model="temperature" class="slider"></el-slider>
          </div>
          <div class="config-item">
            <div class="config-title">pp值:</div>
            <el-slider :min="-2" :max="2" :step="0.1" v-model="ppval" class="slider"></el-slider>
          </div>
          <div class="config-item">
            <div class="config-title">pf值:</div>
            <el-slider :min="-2" :max="2" :step="0.1" v-model="pfval" class="slider"></el-slider>
          </div>
          <div class="config-item">
            <el-tooltip :content="easyModeExplanation" placement="top" effect="light">
              <div class="config-title">简易&nbsp;</div>
            </el-tooltip>
            <el-switch v-model="easyMode"></el-switch>
          </div>
          <div class="reset-config" @click="resetConfig" title="恢复初始值">
            <i class="el-icon-refresh-right"></i>
          </div>
        </div>
        <!-- 下一步/上一步 -->
        <div class="btn-area">
          <div class="descrip double-btn" v-if="showBtnArea">
            <el-button class="info-btn" type="success" size="large" @click="changeStep(1)" :disabled="steps.length===(step+1)"><i class="fa fa-hand-o-right"></i>&nbsp;下一步</el-button>
            <el-button class="info-btn" type="warning" plain size="large" @click="changeStep(0)" :disabled="step===0"><i class="fa fa-hand-o-left"></i>&nbsp;上一步</el-button>
            <el-button class="info-btn" type="primary" plain size="large" @click="exportFile()" title="导出为PDF"><i class="fa fa-file-pdf-o"></i>&nbsp;导出</el-button>
          </div>
        </div>
        <!-- 输入内容检查 -->
        <div class="description scrollbar scrollbar-dark">
          <!-- 最后一步提示 -->
          <div class="descrip" v-if="hasGotPattern">
            <div class="desc-title" title="当前进度">
              <i class="fa fa-dashboard"></i>&nbsp;<span>当前进度</span>
            </div>
            <div class="mark-ideal">{{ step===(steps.length-1)?'你已进入最后一步':('【'+steps[step].name+'】&nbsp;'+(step+1)+'&nbsp;/&nbsp;'+steps.length) }}</div>
          </div>
          <!-- 标记最佳文本 -->
          <div class="descrip" :key="updateMarkIdealContent" v-if="showSlot && steps[step].needed">
            <div class="desc-title" title="必须完成的内容">
              <i class="fa fa-thumb-tack"></i>&nbsp;<span>标记最佳生成文本</span>
            </div>
            <div class="mark-ideal" :style="{color:(hasSetIdealContent?'#67C23A':'#F56C6C')}">{{steps[step].needed ? (hasSetIdealContent ? '【已完成】' : '【未完成】') : ''}}</div>
          </div>
          <!-- 用户输入区域 -->
          <div class="descrip" :key="changeIdealContentTitle" v-if="showInputCheck">
            <div class="desc-title" title="在此处输入原始文本">
              <i class="fa fa-edit"></i>&nbsp;<span>提示组件</span>
            </div>
            <div class="desc-content">
              <el-collapse v-model="checkInputActive">
                <div v-for="(item, index) in steps[step].prompts" :key="index">
                  <el-collapse-item :key="changeCbStatus" :class="[item.category===1?'ori-warning':'']" :title="inputBoxTitleConfig[item.category]+'&nbsp;/&nbsp;'+(item.category===1?(item.status===1?'[已输入]&nbsp;/&nbsp;':'[待输入]&nbsp;/&nbsp;'):'')+item.name" :name="step+''+index">
                    <el-input class="checkinput" type="textarea" :autosize="{ minRows: 4}" resize="none" min="3" :placeholder="item.desc" v-model="item.content" @input="updateCheckboxContent(item.index, item.content)" @blur="changeCheckBoxStatus(index, item.content)"></el-input>
                  </el-collapse-item>
                </div>
              </el-collapse>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getExamplePattern, getMyPatternInfo, getPattern, sendPrompt, setIdealContent } from '@/api/api.js'
export default {
  data() {
    return {
      loading: false,
      idealStar: false,
      patternid: null,
      temperature: 1,
      ppval: 0,
      pfval: 0,
      opToken: null, // 此次编辑的token
      inputContent: '', // 主要输入框的内容
      easyMode: false, // 简单模式,将默认最近的一次返回内容为理想内容
      userAvatar: require('@/assets/imgs/avatar.jpg'), // 用户头像
      inputBoxTitleConfig: ['模板', '输入', '引用'],
      aiAvatar: require('@/assets/imgs/ai.png'), // ai头像
      easyModeExplanation: "开启简易模式后，将默认最后一次生成的文本内容为最佳文本内容", // 简易模式解释
      status: 0, // 0表示无操作，1表示正在发送，2表示正在接收
      // chats: [], // 存储当前的所有对话记录 [[{},{}], [{},{}]]
      step: 0, // 当前是第几步
      hasGotPattern: false, // 用于控制是否显示chats列表，防止未加载之前渲染数据报错
      showSlider: false, // 用于控制是否显示顶部步骤区，如果slot只有一个的话就不显示
      showStep: false, // 用于更新 第几不 标题
      showStepDesc: false, // 用于更新 每一步的描述 板块
      showSlot: false, // 用于更新 是否标记理想文本 板块
      showInputCheck: false, // 是否显示输入检查内容板块
      showBtnArea: false, // 显示按钮区是否渲染
      hasMarkedIdealContent: false, // 标记用户是否设置了理想答案
      hasSetIdealContent: false, // 是否设置了最佳生成文本内容
      checkInputActive: ["00"], // 存储检查输入的折叠面板的活跃内容
      promptTokens: 0, // 上传字符统计
      completionTokens: 0, //下行字符统计
      timeCost: 0, // 耗时统计
      changeCbStatus: false, // 为输入检查中的状态文本绑定key，使之可以更新数据
      changeIdealContentTitle: false, // 为输入状态标题绑定key，使之可以更新文本内容
      changeStatusKey: false, // 更新status,使之能够渲染
      updateMarkIdealContent: false, // 更新右侧用户是否完成了标记的内容
      disableInputContent: false, // 禁止用户在获取内容返回之前重复点击发送按钮
      statusList: [
        {
          name: '空闲',
          icon: 'fa fa-flash',
          color: '#67C23A'
        }, {
          name: '模型正在处理...',
          icon: 'fa fa-hourglass-2  fa-spin',
          color: '#F56C6C'
        }
      ],
      pattern: null,
      prompts: null,
      processStatus: 0, // 记录上一次请求的结果状态, 0表示空闲
      lastTime: 0 // 上一次请求消耗的时间
    }
  },
  // 页面渲染完成之后调用
  mounted() {},
  // create: 页面渲染之前即调用，适用于提前拉取一些数据
  created() {
    this.getPatternId();
  },
  methods: {
    /**
     * 获取模板id
     */
    getPatternId() {
      if (this.$route.query.example) {
        this.getPattern(true);
      } else {
        this.patternid = this.$route.query.patternid;
        this.getPattern();
      }
    },
    // 
    /**
     * 初次加载数据和处理数据
     */
    async getPattern(examplee=false) {
      // this.getPatternId();
      this.loading = !this.loading;
      let goalFunc = null;
      if (examplee) {
        goalFunc = getExamplePattern;
      } else {
        goalFunc = getMyPatternInfo;
      }
      await goalFunc({patternid: this.patternid}).then(res => {
        if (!res) {
          return this.$message({
            type: 'warning',
            message: '没有获取到相关数据'
          })
        }
        this.pattern = res;
        // 默认获取配置文件的时候不会下发opToken
        // this.opToken = res.opToken;
        this.pattern.config = JSON.parse(this.pattern.config);
        this.steps = this.pattern.config.steps;
        // this.inputContent = this.pattern.config.steps[this.step].content
        this.configDefaultParams();
        this.showBtnArea = !this.showBtnArea;
        if (this.pattern.config.steps.length > 1) {
          this.showSlider = !this.showSlider;
        }
        this.showStep = !this.showStep;
        this.showStepDesc = !this.showStepDesc;
        this.showSlot = !this.showSlot;
        this.hasGotPattern = !this.hasGotPattern;
        this.showInputCheck = !this.showInputCheck;
      }).finally(() => {
        this.loading = !this.loading;
      })
    },
    /**
     * 复制
     */
    copyContent(s) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(s).then(() => {
          this.$message({
            type: 'success',
            message: '复制成功'
          });
        })
      } else {
        const ta = document.createElement("textarea");
        ta.style.position = 'fixed';
        ta.style.display = 'none';
        ta.style.top = '10px';
        ta.value = s;
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy', true);
        document.body.removeChild(ta);
        this.$message({
          type: 'success',
          message: '复制成功'
        });
      }
    },
    /**
     * 导出当前记录
     */
    exportFile() {
      if (this.processStatus === 1) {
        return this.$message({
          type: 'warning',
          message: '请等待当前任务完成'
        })
      }
      this.$notify({
        type: 'info',
        title: '提示',
        message: '导出中，请稍等...'
      })
      let content = '';
      content += `【模板名称: ${this.pattern.name}】\n\n`;
      for (const {name, chats} of this.steps) {
        content += `------\n[步骤名: ${name}]\n\n`;
        if (chats && chats.length > 1) {
          for (let i=1;i<chats.length;i++) {
            content += `${i & 1 ? '发送' : '回复'}:\n${chats[i].content}\n\n`;
          }
        }
        content += '\n';
      }
      const el = document.createElement("a");
      el.setAttribute('download', `${this.pattern.name}_导出.doc`);
      const blob = new Blob([content])
      el.setAttribute('href', URL.createObjectURL(blob));
      el.style.display = 'none';
      document.body.appendChild(el);
      el.click();
      document.body.removeChild(el);
    },
    /**
     * 恢复默认配置
     */
    resetConfig() {
      this.temperature = 1;
      this.easyMode = false;
    },
    /**
     * 设置temperature的tooltip文字
     * @param {number} val 修改的值
     */
    setTempMsg(val) {
      if (val === 1) {
        return `${val} | 平衡模式~`;
      } else if (val > 1) {
        return `${val} | 更有创造力~`;
      } else {
        return `${val} | 更精确~`;
      }
    },
    /**
     * 切换步骤的公共方法
     */
    configDefaultParams() {
      let step = this.step;
      // 默认展开第一个输入框（如果有的话）
      this.checkInputActive = [`${step}1`];
      // this.steps[step].prompts.forEach((val, index) => {
      //   if (val.category === 1) {
      //     this.checkInputActive = [`${step}${index}`]
      //   }
      // })
      for (const i in this.steps[step].prompts) {
        if (this.steps[step].prompts[i].category === 1) {
          this.checkInputActive = [`${step}${i}`];
          break;
        }
      }
      // 拼接主要输入框的内容
      this.inputContent = "【提示内容预览】\n"
      for (const {content, name} of this.steps[step].prompts) {
        this.inputContent += content?content:`[待插入:${name}]`;
      }
      // 判断并新建聊天数组
      if (!this.steps[step].chats) {
        let chats = [{
          type: 2, // 0-send, 1-received, 2-introuction
          content: `正位于第${step+1}步。这一步的标题是${this.steps[step].name}，描述是${this.steps[step].desc}。`
        }]
        this.steps[step].chats = chats;
      }
      // 设置是否完成了最佳生成内容的标记并设置样式
      for (const {ideal} of this.steps[step].chats) {
        if (ideal) {
          this.hasSetIdealContent = true;
          break;
        }
      }
      // 重新渲染是否完成了输入的标记组件
      this.updateMarkIdealContent = !this.updateMarkIdealContent;
    },
    /**
     * 切换上一步/下一步
     * @param {number} op 0表示上一步，1表示下一步
     */
    async changeStep(op) {
      let step = this.step;
      if (this.status === 1) {
        // 没完成任务的话，不允许进入下一步
        return this.$message({
          type: 'error',
          message: '请等待当前任务完成'
        })
      }
      if (op) {
        // 检查是否标记了最佳生成文本
        let idealContentId;
        // 如果被依赖，才说明需要检查是否要标记最佳生成文本
        if (this.steps[step].needed) {
          // 如果手动标记了
          if (this.hasSetIdealContent) {
            for (const {ideal, id} of this.steps[step].chats) {
              if(ideal) {
                idealContentId = id;
                break;
              }
            }
          } else {
            // 如果没有手动标记，判断是否开启了简易模式
            if (this.easyMode) {
              // 判断是否开始了聊天，注意最开始生成了一个初始的对话，所以length<=1
              if (this.steps[step].chats.length <= 1) {
                // 如果当前这一步还没开始对话，那肯定也就没有设置最佳文本内容了
                return this.$message({
                  type: 'warning',
                  message: '请先发送提示以获得生成文本内容'
                })
              }
              // 完成了至少一轮对话
              this.steps[step].chats.slice(-1)[0].ideal = 1;
              idealContentId = this.steps[step].chats.slice(-1)[0].id;
              // 如果开启了简易模式，也需要将这个字段设置为true
              // this.hasSetIdealContent = true;
            } else {
              return this.$message({
                type: 'warning',
                message: '请选择最佳生成文本内容或者打开简易模式'
              });
            }
          }
          // 只有当前的步骤是需要设置最佳生成文本的时候才需要发送请求保存数据
          this.loading = !this.loading;
          await setIdealContent({optoken: this.opToken, stepid: this.steps[step].id, idealid: idealContentId}).then(() => {
            this.$message({
              type: 'success',
              message: '当前数据已保存'
            });
          }).catch(() => {
            return this.$message({
              type: 'error',
              message: '当前数据保存失败,请重试'
            });
          }).finally(() => {
            this.loading = !this.loading;
          })
        }
        // 进入下一步
        this.step++;
        step++;

        // 如果下一步需要引用前面的文本，则填充到输入框当中
        // 注意可能有多个需要填充的地方，不要直接跳出循环语句
        // 使用标签语法，快速退出循环
        for (const itema of this.steps[step].prompts) {
          if (itema.category === 2) {
            for (const {id, chats} of this.steps) {
              if (itema.relyid === id) {
                for (const {ideal, content} of chats) {
                  if (ideal) {
                    itema.content = content;
                  }
                }
              }
            }
          }
        }

        // 完成之后，再把这个值设置为false，防止进入下一步之后默认下一步选择了最佳生成文本
        this.hasSetIdealContent = false;
      } else {
        this.step--;
        if (this.steps[step].chats) {
          // 点击上一步主要输入框填充最后发送的内容
          this.inputContent = this.steps[step].chats.slice(-2).content;
        }
      }

      // 刷新最佳文本提示页面
      this.changeIdealContentTitle = !this.changeIdealContentTitle;
      this.configDefaultParams();

      // 根据检查输入框的输入状态，改变相应的文本颜色
      this.$nextTick(() => {
        const checkInputs = document.getElementsByClassName("el-collapse-item__header");
        this.steps[this.step].prompts.forEach(({category, status}, index) => {
          if (category === status == 1) {
            checkInputs[index].style.color = '#67C23A';
          }
        })
      })      
      // 每次将内容添加至数组之后都滚动到最底部
      document.getElementsByClassName("chat-placeholder")[0].scrollIntoView(false);
    },
    /**
     * 发送内容
     */
    async sendPrompt() {
      let step = this.step;
      // 检查需要用户输入的内容是否全部输入了
      for (const {category, status} of this.steps[step].prompts) {
        if (category === 1 && (status === 0 || !status)) {
          return this.$message({
            type: 'warning',
            message: '请检查是否完成了文本内容的输入'
          });
        }
      }
      // 开始准备发送内容
      this.status = 1;
      this.changeStatusKey = !this.changeStatusKey;
      // 注意这里要把前面几个提示文字去掉
      let chatItem = {
        type: 0,
        content: this.inputContent.replace("【提示内容预览】\n", ""),
      }
      this.steps[step].chats.push(chatItem);
      this.disableInputContent = true;
      let chatItemOn = {
        // 生成一个聊天内容，展示内容正在处理，设置为2防止用户将此项设置为最佳生成文本内容
        type: 2,
        content: "内容生成中，请耐心等待......"
      }
      this.steps[step].chats.push(chatItemOn);

      // 加入setTimeOut，解决网络请求阻塞页面渲染的问题
      setTimeout(() => {
        document.getElementsByClassName("chat-placeholder")[0].scrollIntoView(false);
      }, 0)
      // 注意这里要把inputContent前面的几个提示文字去掉
      let params = {
        patternid: this.pattern.id,
        prompt: this.inputContent.replace("【提示内容预览】\n", ""),
        stepid: this.steps[step].id,
        optoken: this.opToken,
        temperature: this.temperature,
        ppval: this.ppval,
        pfval: this.pfval
      }
      let timestart = Date.now();
      await sendPrompt(params).then((res) => {
        if (res.opToken) {
          // 如果响应的内容中包含了opToken，说明是第一次请求，将opToken保存即可
          this.opToken = res.opToken;
        }
        this.steps[step].chats.pop();
        this.promptTokens += res.promptTokens;
        this.completionTokens += res.completionTokens;
        let chat = {
          type: res.type,
          id: res.id,
          content: res.content
        }
        this.steps[step].chats.push(chat);
        this.$message({
          type: 'success',
          message: '任务处理完成'
        });
        this.processStatus = 1;
        this.lastTime = Date.now() - timestart;
        // 网络请求成功后，将本地pattern更新
        // setToken(this.opToken, JSON.stringify(this.pattern));
        this.timeCost = this.timeCost + this.lastTime; // 获取请求时间
        // 每次将内容添加至数组之后都scroll到最底部
        document.getElementsByClassName("chat-placeholder")[0].scrollIntoView(false);
      }).catch(() => {
        this.processStatus = 0;
        this.$notify({
          type: 'error',
          message: '出了点问题，请重试'
        });
        // 如果请求失败，撤回展示生成过程+自己发送的内容
        this.steps[step].chats = this.steps[step].chats.slice(0, -2);
      }).finally(() => {
        // 恢复状态和按钮事件
        this.status = 0;
        this.disableInputContent = false;
      })
    },
    /**
     * 键盘input事件
     */
    updateCheckboxContent(index, input) {
      this.inputContent = "【提示内容预览】\n";
      for (const {content} of this.steps[this.step].prompts) {
        this.inputContent += content;
      }
    },
    /**
     * 键盘blur事件
     */
    changeCheckBoxStatus(index, input) {
      let step = this.step;
      // 如果不是需要用户输入的类型，直接返回
      if (this.steps[step].prompts[index].category !== 1) {
        return;
      }
      let status = this.steps[step].prompts[index].status;
      if (input && input.trim()) {
        if (!status || status === 0) {
          // 如果用户输入了内容并且当前是未完成状态，那么改变状态
          this.steps[this.step].prompts[index].status = 1;
          this.$nextTick(() => {
            document.getElementsByClassName("el-collapse-item__header")[index].style.color = '#67C23A';
          })
          
          // this.changeCbStatus = !this.changeCbStatus;
          // setTimeout(() => {
          // }, 0)
        }
        return;
      } else {
        // 如果输入的内容为空，并且当前状态是完成状态
        // 遍历当前step，并将相应的位置的文本进行修改，修改为[待插入]这个文本内容
        if (status === 1) {
          this.steps[step].prompts[index].status = 0;
          // this.changeCbStatus = !this.changeCbStatus;
          this.$nextTick(() => {
            document.getElementsByClassName("el-collapse-item__header")[index].style.color = '#F56C6C';
          })
          
        }
      }
    },
    /**
     * 重置输入框内容
     */
    resetInputContent() {
      let step = this.step;
      // 重置部分输入框的title文本内容
      for (const item of this.steps[step].prompts) {
        if (item.category === 1) {
          item.content = '';
          item.status = 0;
        }
      }
      // 在上一步的基础上，重新拼接输入框内容
      this.inputContent = "【提示内容预览】\n"
      for (const {content, name} of this.steps[step].prompts) {
        this.inputContent += content?content:`[待插入:${name}]`;
      }
      // 重置部分输入框的提示颜色
      const inputBoxes = document.getElementsByClassName("el-collapse-item__header");
      this.changeCbStatus = !this.changeCbStatus;
      this.steps[step].prompts.forEach((item, index) => {
        if (item.category === 1) {
          inputBoxes[index].style.color = '#67C23A';
        }
      })
    },
    // 清空对话内容
    clearChatHistory() {
      let step = this.step;
      // 首先将内容清空
      this.steps[step].chats = this.steps[step].chats.slice(0, 1);
      // 将最佳生成文本设置为false
      this.hasSetIdealContent = false;
    },
    /**
     * 标记理想文本
     */
    setIdealContent(index) {
      // 更新最佳生成文本
      let step = this.step;
      for (let i=0;i<this.steps[step].chats.length;i++) {
        if (index === i) {
          this.steps[step].chats[index].ideal = 1;
          continue;
        }
        this.steps[step].chats[index].ideal = null;
      }
      // 刷新页面
      this.idealStar = !this.idealStar;
      this.hasSetIdealContent = true;
    }
  }
}
</script>

<style lang="less" scoped>
.input-box-focus {
  border-color: #0081ff!important;
  transition: all ease .2s;
}
.steps-bg {
  padding: 0.5rem 1rem;
}
:deep(.el-steps) {
  box-shadow: 0 0 1rem #bbb;
  background-color: #4780b9;
  overflow-x: auto;
  margin-bottom: 1rem;
}
:deep(.el-step) {
  min-width: 8rem;
}
:deep(.is-success), :deep(.is-process) {
  color: #fff;
  border-color: #fff;
}
:deep(.el-icon-copy-document) {
  pointer-events: none;
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
:deep(.el-button+.el-button) {
  margin: 0;
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
:deep(.el-collapse-item__header) {
  font-size: 1rem;
}
:deep(.slider) {
  width: 60px !important;
}
.con {
  display: flex;
  flex-direction: column;
  height: 100%;
  .content-con {
    flex: 1;
    height: 100%;
    width: 100%;
    overflow: hidden;
    display: flex;
    position: relative;
    font-size: 16px;
    .step-con {
      margin-bottom: 24px;
    }
    .box-chat {
      height: 100%;
      flex: 1;
      box-sizing: border-box;
      overflow: hidden;
      border-radius: 0.25rem;
      display: flex;
      flex-direction: column;
      box-shadow: 0 0 1rem #bbb;
      background-color: #fff;
      .chat-items {
        width: 100%;
        border-radius: 0.24rem;
        height: calc(100% - 200px - 1rem);
        box-sizing: border-box;
        flex: 1;
        overflow-y: auto;
        padding: 1rem 1rem 0 1rem;
        .chat-item:nth-last-child(2) {
          border: none;
        }
        .chat-item {
          width: 100%;
          padding: 1rem;
          box-sizing: border-box;
          display: flex;
          border-bottom: 1px solid rgba(0,0,0,0.1);
          cursor: pointer;
          position: relative;
          transition: all ease .2s;
          min-height: 4rem;
          .item-avatar {
            width: 2rem;
            border-radius: 0.125rem;
            overflow: hidden;
            margin-right: 1.4rem;
            .ideal-star {
              // display: none;
              visibility: hidden;
              width: 100%;
              text-align: center;
              font-size: 1.5rem;
              padding-top: 0.25rem;
              svg {
                width: 1.25rem;
                height: 1.25rem;
              }
              &:hover {
                color: #409EFF!important;
              }
            }
            img {
              width: 2rem;
              height: 2rem;
              border-radius: 0.125rem;
              background-color: #ddd;
            }
          }
          .item-content {
            flex: 1;
            line-height: 1.5rem;
            text-align: justify;
            overflow: hidden;
            word-break: break-all;
            white-space: pre-wrap;
          }
          .item-tool {
            position: absolute;
            right: 0;
            top: 0;
            padding: 0.5rem 1rem;
            background-color: #D9ECFF;
            border-radius: 0 0.25rem 0 0.25rem;
            visibility: hidden;
            .tool-copy {
              font-size: 0.75rem;
              border-radius: 0.25rem;
              color: #409EFF;
            }
          }
          &:hover {
            background-color: rgba(0,0,0,0.02);
            .item-tool, .ideal-star {
              visibility: visible;
            }
          }
        }
        .chat-placeholder {
          width: 100%;
          height: 2rem;
          scroll-behavior: smooth;
        }
      }
      .input-box {
        position: relative;
        border-radius: 0.25rem;
        padding: 1.5rem 2rem 1rem 2rem;
        margin-right: 10px;
        z-index: 1;
        box-shadow: -0.125rem -1.5rem  2rem #FFFFFF;
        .calcs {
          position: absolute;
          right: 2rem;
          top: 0;
          display: flex;
          gap: 1rem;
          width: 100%;
          justify-content: flex-end;
          color: #999999;
        }
        .cur-status {
          position: absolute;
          left: 2rem;
          top: 0;
        }
        .main-input :deep(textarea) {
          resize: none;
          font-family: 'microsoft yahei';
          font-size: 1rem;
          border: none;
          color: #2c3e50;
          background-color: transparent;
          padding: 0.6rem;
          background-color: #eee;
          text-align: justify;
        }
      }
      .statistic {
        margin-bottom: 1rem;
        padding: 0 42px 0 2rem;
        color: #666;
        font-weight: bold;
        .input-btns {
          // gap: 1rem;
          text-align: right;
          display: flex;
          align-items: center;
          justify-content: flex-end;
          .resend-btn {
            outline: none;
            border-radius: 0.25rem;
            padding: 0.25rem;
            cursor: pointer;
            border: none;
            transition: all ease .2s;
            margin-right: 1rem;
            background-color: #fff;
            font-size: 18px;
            .el-icon-refresh-right {
              font-size: 1.2rem;
              padding: 0.4rem 0.8rem;
            }
            &:hover {
              color: #0081ff;
            }
          }
          .send-btn {
            border: none;
            outline: none;
            border-radius: 0.25rem;
            padding: 0.5rem 1.5rem;
            cursor: pointer;
            background-color: #409EFF;
            transition: all ease .2s;
            color: #fff;
            .el-icon-position, .el-icon-loading {
              font-size: 1.2rem;
              padding: 0.4rem;
            }
            &:hover {
              background-color: #66BEFF;
            }
            &[disabled] {
              background-color: #66BEFF;
            }
          }
        }
      }
    }
    .detail {
      width: 40%;
      height: 100%;
      box-sizing: border-box;
      margin-left: 1rem;
      border-radius: 0.125rem;
      background-color: #fff;
      box-shadow: 0 0 1rem #bbb;
      padding: 1rem;
      .config {
        width: 100%;
        height: 3rem;
        box-sizing: border-box;
        border-bottom: 1px solid #EBEEF5;
        display: flex;
        gap: 0.6rem;
        position: relative;
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
        .reset-config {
          position: absolute;
          right: 0;
          top: 0.5rem;
          content: "";
          width: 2rem;
          height: 2rem;
          transition: all ease 0.2s;
          border-radius: 0.25rem;
          .el-icon-refresh-right {
            font-size: 1.4rem;
            margin: 0.3rem;
            pointer-events: none;
          }
          &:hover {
            background-color: #eee;
          }
        }
      }
      .btn-area {
        padding-bottom: 1rem;
        border-bottom: 1px solid #EBEEF5;
        box-sizing: border-box;
        height: 4rem;
        .double-btn {
          display: flex;
          justify-content: flex-start;
          gap: 1rem;
        }
        .descrip {
          margin-top: 1rem;
          .info-btn {
            width: 30%;
          }
        }
      }
      .description {
        width: 100%;
        height: calc(100% - 7rem);
        position: relative;
        box-sizing: border-box;
        overflow-y: scroll;
        line-height: 1.5rem;
        .double-btn {
          display: flex;
          justify-content: space-between;
        }
        .descrip {
          .desc-title {
            display: inline-block;
            position: relative;
            z-index: 1;
            padding-right: 0.4rem;
            font-weight: bold;
            margin-top: 1rem;
            &::after {
              content: "";
              position: absolute;
              height: 0.8rem;
              width: 100%;
              background-color: #d3e9ff;
              left: 0;
              bottom: 0;
              z-index: -1;
            }
          }
          .desc-content {
            margin-top: 0.2rem;
            .btn-check {
              width: 100%;
              margin: 0.25rem 0;
              text-align: left;
            }
            .checkinput {
              // background: #eee;
              padding: 0.2rem;
              box-sizing: border-box;
              border-radius: 0.25rem;
            }
            .ori-warning :deep(.el-collapse-item__header) {
              color: #F56C6C;
            }
            & :deep(.el-collapse) {
              border-bottom: 1px solid #EBEEF5;
              border-top: none;
            }
            & :deep(.el-collapse-item__header) {
              line-height: 1rem;
              padding: 1rem 0;
              font-size: 14px;
              height: auto;
            }
            & :deep(textarea) {
              width: 100%;
              padding: 0.5rem;
              font-size: 14px;
              font-family: 'microsoft yahei';
              background-color: transparent;
              border: none;
              outline: none;
            }
            & :deep(textarea:focus) {
              border: none;
            }
          }
          .mark-ideal {
            line-height: 1.5rem;
            margin-top: 1rem;
            border-bottom: 1px solid #EBEEF5;
            font-size: 14px;
            padding-bottom: 0.5rem;
          }
        }
      }
    }
  }
 }
.pattern-info {
  .info-item {
    line-height: 2rem;
    font-size: 16px;
  }
}
</style>