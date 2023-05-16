<template>
  <div style="height:100%">
    <div class="choices-container" v-if="!hasSelected" v-loading="loading" element-loading-text="处理中...">
      <div class="choice-container">
        <div class="choice-left" @click="selectedNew()">
          <div class="choice-icon" style="color: #E6A23C"><i class="fa fa-lightbulb-o"></i></div>
          <div>新建模板</div>
        </div>
        <div class="choice-right" @click="selectedRepository()">
          <div class="choice-icon" style="color: #409EFF"><i class="fa fa-folder-open"></i></div>
          <div>从模板库创建</div>
        </div>
      </div>
    </div>

    <div v-else class="repository-area" v-loading="loading" :element-loading-text="loadingText">
      <div :class="[showPickCard?'pick-area-bg':'', 'pattern-design']">
        <!-- 选择模板的弹窗界面 -->
        <div v-if="showRepositoryArea" :class="['pick-pattern', showPickCard?'':'hide-pick-pattern']">
          <div class="search-box">
            <el-input placeholder="搜索" v-model="keywords">
              <template #append>
                <el-button @click="getMyPattern()"><i class="fa fa-search"></i></el-button>
              </template>
            </el-input>
          </div>
          <!-- 模板列表 -->
          <div class="pattern-list scrollbar scrollbar-dark">
            <div class="pattern-item" v-for="(item, index) in patterns" :key="index">
              <button href="javascript:void(0)" @click="setOnProcess(item.id)"><i class="fa fa-file-o"></i>&nbsp;{{ item.name }}</button>
            </div>
            <div class="load-btn"><i class="fa fa-magic"></i>&nbsp;{{ patternListLoading }}</div>
          </div>
          <!-- 隐藏图标 -->
          <div class="hide-card" @click="setShowPickCard()">
            <i :class="[showPickCard?'fa fa-chevron-circle-left':'fa fa-arrow-circle-right']"></i>
          </div>
        </div>

        <!-- 主要编辑板块 -->
        <div v-if="hasPattern" class="main-area">
          <!-- 上方功能区 -->
          <div class="main-area-tool">
            <el-button type="warning" plain @click="openModifyPatternInfoDialog()" title="修改模板基本信息"><i class="fa fa-edit"></i>&nbsp;模板信息</el-button>
            <el-button type="danger" plain @click="resetPattern()" title="将配置重置为上次保存后的内容，不包括模板基本信息"><i class="fa fa-refresh"></i>&nbsp;重置</el-button>
            <el-button type="success" plain @click="modifyPattern()"><i class="fa fa-save"></i>&nbsp;提交保存</el-button>
          </div>

          <!-- 下方编辑区 -->
          <div class="main-area-main">

            <!-- 左边步骤树状图 -->
            <div class="steps-area">
              <div class="step-items" v-for="num in configNew.steps.length" :key="num">
                <div :class="['step-item single-item', pos===num-1?'active-step':'']" @click="pos=num-1">
                  <span><i class="fa fa-flask"></i>&nbsp;{{num}}&nbsp;{{ configNew.steps[num-1].name }}</span>
                  <div class="add-new-step">
                    <div title="预览" class="add-new-step-btn" @click="showPromptEffect()">
                      <i class="fa fa-eye"></i>
                    </div>
                    <div title="编辑" class="add-new-step-btn" @click="modifyStepDialogVisiable=true">
                      <i class="fa fa-edit"></i>
                    </div>
                    <div title="删除" class="add-new-step-btn" @click="deleteStep($event, num-1)">
                      <i class="fa fa-trash-o"></i>
                    </div>
                    <div title="往前插入" class="add-new-step-btn" @click="addStep($event, num-1)">
                      <i class="fa fa-hand-o-up"></i>
                    </div>
                    <div title="往后插入" class="add-new-step-btn" @click="addStep($event, num)">
                      <i class="fa fa-hand-o-down"></i>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="configNew.steps.length===0">
                <el-button size="large" type="primary" @click="addStep($event, 0)"><i class="fa fa-puzzle-piece"></i>&nbsp;开始设计</el-button>
              </div>
            </div>

            <!-- 右边主要编辑区 -->
            <div class="detail-area scrollbar scrollbar-light">
              <div class="step-design">
                <!-- 步骤信息 -->
                <div>
                  <el-form label-position="top" :model="configNew.steps[pos]">
                    <div class="prompts-pattern">
                      <div class="prompt-box">
                        <!-- 初始箭头 -->
                        <div class="prompt-arraw" @click="addPrompt(0)" v-if="configNew.steps.length > 0">
                          <div class="prompt-arraw-add" title="插入一项"><i class="fa fa-plus"></i></div>
                          <div class="prompt-arraw-icon"><i class="fa fa-caret-down"></i></div>
                        </div>
                      </div>
                      <!-- 提示列表 -->
                      <div v-if="configNew.steps.length > 0">
                        <div class="prompt-box" v-for="(item, index) in configNew.steps[pos].prompts">
                          <div class="prompt" @click="editPrompt(index)">
                            <div class="prompt-tool-delete" @click="deletePrompt($event, index)">✖</div>
                            <!-- 提示内容 -->
                            <!-- <el-input v-if="item.category===0" class="prompt-input" :autosize="{maxRows: 5, minRows: 2}" type="textarea" resize="none" v-model="configNew.steps[pos].prompts[index].content"></el-input> -->
                            <div class="prompt-input" v-if="item.category===0">{{ configNew.steps[pos].prompts[index].content }}</div>
                            <div class="prompt-extra">
                              <!-- 基础信息 -->
                              <div class="prompt-info">
                                <el-tag class="prompt-type" :type="item.category===0?'warning':item.category===1?'danger':'success'">
                                  <i :class="['fa ',item.category===0?'fa-file-text-o':item.category===1?'fa-pencil':'fa-quote-left']"></i>
                                  {{ item.category===0?'默认模板':item.category===1?'用户输入':'引用文本' }}
                                </el-tag>
                                <el-tag type="info">{{ item.name }}</el-tag>
                              </div>
                            </div>
                          </div>
                          <div class="prompt-arraw">
                            <div class="prompt-arraw-add" @click="addPrompt(index+1)" title="插入一项"><i class="fa fa-plus"></i></div>
                            <div class="prompt-arraw-icon"><i class="fa fa-caret-down"></i></div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </el-form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加步骤信息的dialog -->
    <el-dialog v-model="addStepDialogVisible" title="添加步骤" width="40%">
      <el-form :model="addStepItem" label-width="auto" label-position="top" size="default">
        <el-form-item label="步骤名称">
          <el-input v-model="addStepItem.name" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="步骤描述">
          <el-input type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" resize="none" v-model="addStepItem.desc" />
        </el-form-item>
        <el-button type="primary" @click="submitAddStep()">确认添加</el-button>
      </el-form>
    </el-dialog>

    <!-- 修改模板信息的弹窗 -->
    <el-dialog v-model="modifyPatternDialogVisiable" title="修改模板基本信息" width="40%">
      <el-form :model="configNew" label-width="auto" label-position="top" size="default">
        <el-form-item label="模板名称">
          <el-input v-model="patternInfo.name" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" resize="none" v-model="patternInfo.desc" />
        </el-form-item>
        <el-form-item label="模板领域">
          <el-select v-model="patternInfo.field" placeholder="选择模板所属领域" size="large">
            <el-option v-for="item in fields" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="公开状态">
          <el-select v-model="patternInfo.public" placeholder="选择模板公开状态" size="large">
            <el-option v-for="item in ispublic" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="submitModifyPatternInfo()">确认修改</el-button>
      </el-form>
    </el-dialog>

    <!-- 修改某个步骤信息的弹窗 -->
    <el-dialog v-model="modifyStepDialogVisiable" title="修改步骤信息" width="40%">
      <el-form :model="configNew" label-width="auto" label-position="top" size="default">
        <el-form-item label="步骤名称">
          <el-input v-model="configNew.steps[pos].name" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="步骤描述">
          <el-input type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" resize="none" v-model="configNew.steps[pos].desc" />
        </el-form-item>
        <el-button type="primary" @click="modifyStepDialogVisiable=false">确认修改</el-button>
      </el-form>
    </el-dialog>

    <!-- 修改某个提示内容的弹窗 -->
    <el-dialog v-model="modifyPromptDialogVisiable" title="修改提示信息" width="40%">
      <el-form :model="configNew" label-width="auto" label-position="top" size="default">
        <el-form-item label="提示名称">
          <el-input v-model="configNew.steps[pos].prompts[promptIndex].name" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="提示描述">
          <el-input type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" resize="none" v-model="configNew.steps[pos].prompts[promptIndex].desc" />
        </el-form-item>
        <el-form-item label="提示类型">
          <el-select v-model="configNew.steps[pos].prompts[promptIndex].category" class="m-2" placeholder="Select" size="large">
            <el-option v-for="item in promptTypes" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="引用内容来源" v-if="configNew.steps[pos].prompts[promptIndex].category===2">
          <el-select v-model="configNew.steps[pos].prompts[promptIndex].relyid" placeholder="选择模板所属领域" size="large">
            <!-- 展示当前步骤之前的内容便于引用 -->
            <el-option v-for="item in configNew.steps.slice(0, pos)" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" v-if="configNew.steps[pos].prompts[promptIndex].category===0">
          <el-input type="textarea" :autosize="{max: 6, min: 3}" resize="none" v-model="configNew.steps[pos].prompts[promptIndex].content"></el-input>
        </el-form-item>
        <el-button type="primary" @click="modifyPromptDialogVisiable=false">确认修改</el-button>
      </el-form>
    </el-dialog>
    
    <!-- 新增一项提示内容 -->
    <el-dialog v-model="addPromptDialogVisiable" title="增加一项提示内容" width="40%" :before-close="handleAddPromtDialogClose">
      <el-form :model="configNew" label-width="auto" label-position="top" size="default">
        <el-form-item label="提示名称">
          <el-input v-model="addPromptItem.name" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="提示描述">
          <el-input type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" resize="none" v-model="addPromptItem.desc" />
        </el-form-item>
        <el-form-item label="提示类型">
          <el-select v-model="addPromptItem.category" class="m-2" placeholder="Select" size="large">
            <el-option v-for="item in promptTypes" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="引用内容来源" v-if="addPromptItem.category===2">
          <el-select v-model="addPromptItem.relyid" placeholder="选择模板所属领域" size="large">
            <el-option v-for="item in configNew.steps.slice(0, pos)" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" v-if="addPromptItem.category===0">
          <el-input type="textarea" :autosize="{max: 6, min: 3}" resize="none" v-model="addPromptItem.content"></el-input>
        </el-form-item>
        <el-button type="primary" @click="submitPromptAdd()">确认修改</el-button>
      </el-form>
    </el-dialog>

    <!-- 效果预览 -->
    <el-dialog v-model="promptEffectDialogVisiable" title="提示效果组合" width="40%">
      <div style="line-height:1.5rem;font-size:16px;">{{ promptEffectContent }}</div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyPattern, getPatternField, modifyPatternConfig, getMyPatternInfo, modifyPatternInfo } from '@/api/api.js'
export default {
  data() {
    return {
      loading: false,
      addStepDialogVisible: false,
      modifyPatternDialogVisiable: false,
      modifyStepDialogVisiable: false,
      modifyPromptDialogVisiable: false,
      addPromptDialogVisiable: false,
      promptEffectDialogVisiable: false,
      hasSelected: false,
      showNewArea: false,
      patternSelected: false,
      showRepositoryArea: false,
      promptEffectContent: null,
      keywords: '',
      loadingText: '正在查找......',
      patternListLoading: '',
      patterns: [],
      hasPattern: false, // 数据是否加载完成,
      config: {},
      showPickCard: true,
      // step: -1,
      pos: 0, // 当前位置，格式为步骤index+插槽index，例如01表示步骤1的第二个插槽，20表示第三个步骤的第一个插槽
      // configNew: {},
      fields: [],
      patternid: null,
      configNew: {},
      addStepItem: {},
      addIndex: 0, // 被添加步骤的位置索引
      promptIndex: 0, // 保存提示的id，用于修改提示信息的时候
      promptTypes: [
        {
          id: 0,
          name: '默认模板'
        }, {
          id: 1,
          name: '用户输入'
        }, {
          id: 2,
          name: '引用文本'
        }
      ],
      addPromptItem: {}, //新增的一项提示内容
      patternInfo: {}, //存储模板基本信息
      ispublic: [
        {
          id: 0,
          name: '不公开'
        }, {
          id: 1,
          name: '公开'
        }
      ],
      isNewPattern: false, // 标记是否为新建模板
    }
  },
  methods: {
    // 获取领域列表
    async getPatternField() {
      await getPatternField().then((res) => {
        this.fields = res;
      })
    },
    // 选择模板
    async selectedNew() {
      this.loading = true;
      this.loadingText = "准备中...";
      this.hasSelected = !this.hasSelected;
      this.showNewArea = !this.showNewArea;
      this.showPickCard = false;
      this.hasPattern = true;
      await this.getPatternField().finally(() => {
        this.loading = false;
        this.loadingText = "";
      });
      // 初始化参数
      this.config = {
        steps: []
      },
      this.configNew = JSON.parse(JSON.stringify(this.config));
      console.log(this.configNew)
      this.patternInfo = {
        patternid: '',
        name: '',
        desc: '',
        field: this.fields[0].id,
        public: 0
      };
      this.isNewPattern = true;
      // 如果是新建模板，则自动打开填写模板信息的弹窗
      // this.checkPatternInfoModify();
      // this.openModifyPatternInfoDialog();
    },
    // 检查模板信息填写内容
    checkPatternInfoModify() {
      // 如果是新建账号
      if (this.isNewPattern) {
        if (!this.patternInfo.name.trim() || !this.patternInfo.desc.trim()) {
          this.$message({
            type: 'warning',
            message: '请点击【模板信息】按钮请完善模板基本信息'
          })
          return false;
        }
      }
      return true;
    },
    // 从模板库中选择
    selectedRepository() {
      this.hasSelected = !this.hasSelected;
      this.showRepositoryArea = !this.showRepositoryArea;
      this.getPatternField();
      this.getMyPattern();
    },
    // 打开修改模板信息弹窗
    openModifyPatternInfoDialog() {
      this.modifyPatternDialogVisiable = true;
    },
    // 请求我的模板列表
    async getMyPattern() {
      this.patternListLoading = '正在查找';
      let params = {
        keywords: this.keywords,
        field: ''
      }
      await getMyPattern(params).then(res => {
        // 将res.collection拆开并放入patterns当中
        this.patterns = res.patterns;
        // 上面语句有下面三种替代方式
        // Array.prototype.push.apply(this.patterns, res.collection);
        // this.patterns.concat(res.collection);
        // this.patterns = [...this.patterns, res.collection];
        this.patternListLoading = `共找到${res.total}条记录`
      })
    },
    // 开始一项模板内容
    async setOnProcess(id) {
      // 当前存在内容
      if (this.hasPattern && this.configNew) {
        await this.$confirm('是否在离开之前保存对模板的修改?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          // 离开之间将编辑的内容进行保存
          await this.modifyPatternConfig({config: this.configNew, id: this.patternid});
          this.$message({
            type: 'success',
            message: '保存成功'
          })
        }).catch(() => {
          return;
        }).finally(() => {
          this.loading = true;
          this.loadingText = '处理中......'
        })
      }
      // 获取某个特定的pattern
      await getMyPatternInfo({ patternid: id }).then(res => {
        this.patternInfo = res;
        this.config = JSON.parse(res.config);
        // 深拷贝，将config拷贝给configNew
        this.configNew = JSON.parse(JSON.stringify(this.config));
        // 重置configNew，并深拷贝新的config
        // this.configNew = Object.assign({}, this.config);
        this.hasPattern = true;
        this.showPickCard = false;
        this.patternid = id;
      }).finally(() => {
        this.loading = false;
        this.loadingText = '';
      })
    },
    // 展示选择模板弹窗
    setShowPickCard() {
      this.showPickCard = !this.showPickCard;
    },
    // 保存修改
    async modifyPattern() {
      // 对修改后的模板进行检查
      // 检查每一步至少包含一个提示
      for (const {prompts} of this.configNew.steps) {
        if (prompts.length === 0) {
          return this.$message({
            tyep: 'warning',
            message: '一个步骤至少要包含一个提示内容'
          })
        }
      }
      // 检查如果被引用了的步骤，添加字段needed为1
      // 首先把需要以来的步骤的id记下来，然后再加上，使用set保证多个内容引用同一个生成文本的情况
      // 使用arr.findLast()倒序遍历，不需要二次遍历
      let isNeedArr = new Set();
      let needNum = 0;
      this.configNew.steps.findLast((item) => {
        if (isNeedArr.has(item.id)) {
          if (!item.needed || item.needed === 0) {
            item.needed = 1;
            isNeedArr.delete(item.id);
          } else {
            item.needed = 0;
          }
        }
        for (const {category, relyid} of item.prompts) {
          if (category === 2) {
            isNeedArr.add(relyid);
            needNum++;
          }
        }
      })
      // 判断有没有错误的引用，如果有，则提醒用户
      // 错误的引用的原因是后面某个步骤引用了前面某个步骤的内容，但是前面某个步骤可能被删掉了
      if (needNum > 0 && isNeedArr.size > 0) {
        return this.$notify({
          type: 'error',
          title: '提示',
          message: '某个提示中引用了不存在的生成文本，请修正'
        })
      }
      // 检查是否是新建的模板,检查模板基本信息是否填写
      if (!this.checkPatternInfoModify()) {
        return;
      }
      this.loading = true;
      this.loadingText = "保存中...";
      await modifyPatternConfig({ config: JSON.stringify(this.configNew), id: this.patternid }).then(() => {
        // 注意修改之后要同步更新config，防止用户选择恢复上次模板的时候不能及时更新
        this.config = {};
        Object.assign(this.config, this.configNew);
        this.$message({
          type: 'success',
          message: '提示模板修改保存成功!'
        })
      }).finally(() => {
        this.loading = false;
        this.loadingText = ""
      })
    },
    // 在弹窗中提交对模板信息的修改
    async submitModifyPatternInfo() {
      // 检查内容是否填写完整
      if (!this.patternInfo.name || !this.patternInfo.name.trim()) {
        return this.$message({
          type: 'warning',
          message: '内容不能为空'
        })
      }
      if (!this.patternInfo.desc || !this.patternInfo.desc.trim()) {
        return this.$message({
          type: 'warning',
          message: '内容不能为空'
        })
      }
      // 如果是新建模板，还必须要先添加了步骤之后才能保存
      if (this.isNewPattern) {
        if (!this.config.steps) {
          return this.$notify({
            type: "warning",
            title: '提示',
            message: '请先创建步骤并提交保存之后再'
          })
        }
      }
      this.checkPatternInfoModify();
      let params = {
        patternid: this.patternid,
        ispublic: this.patternInfo.public,
        name: this.patternInfo.name,
        desc: this.patternInfo.desc,
        field: this.patternInfo.field
      }
      await modifyPatternInfo(params).then((res) => {
        // 如果是新增的模板，则将新增模板的id返回
        this.patternid = res;
        return this.$message({
          type: 'success',
          message: '操作成功'
        })
      })
      this.modifyPatternDialogVisiable = false;
    },
    // 将模板恢复至上次保存的内容
    resetPattern() {
      this.$confirm(
        '将恢复至上次保存之前的内容?',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        // 将configNew重新赋值为config
        this.configNew = JSON.parse(JSON.stringify(this.config));
      })
    },
    // 打开添加一个步骤的弹窗
    addStep(e, num) {
      this.addIndex = num;
      // 取消冒泡行为
      e.stopPropagation();
      this.addStepDialogVisible = true;
      this.addStepItem = {};
    },
    // 添加一个步骤
    submitAddStep() {
      if (!this.addStepItem.name || !this.addStepItem.name.trim()) {
        return this.$message({
          type: 'warning',
          message: '请输入步骤描述'
        })
      }
      let params = {
        id: crypto.randomUUID(),
        desc: this.addStepItem.desc,
        name: this.addStepItem.name,
        needed: 0, //暂时先将这个参数设置为0，之后再检验修改
        prompts: []
      }
      this.configNew.steps.splice(this.addIndex, 0, params);
      this.$message({
        type: 'success',
        message: '添加成功'
      })
      this.addStepDialogVisible = false;
    },
    // 删除一个步骤
    deleteStep(e, num) {
      this.$confirm(
        "确定删除此步骤？提示：如果此步骤的生成文本被后续步骤所依赖，需要及时修改后续文本的引用类型提示的引用来源。",
        "确认操作",
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        this.configNew.steps.splice(num ,1);
        this.$message({
          type: 'success',
          message: '删除成功，保存后生效'
        })
        // 删除步骤之后，自动切换pos数值
      }).catch(() => {})
      e.stopPropagation()
    },
    // 打开添加一项提示的弹窗
    addPrompt(index) {
      this.promptIndex = index;
      this.addPromptDialogVisiable = true;
    },
    // 在弹窗中确认添加一项提示
    submitPromptAdd() {
      // 判断内容有没有填写完整
      if (!this.addPromptItem.name || !this.addPromptItem.name.trim()) {
        return this.$message({
          type: 'warning',
          message: '提示名称不能为空'
        })
      }
      if (this.addPromptItem.category === null) {
        return this.$message({
          type: 'warning',
          message: '请选择提示类型'
        })
      }
      if (this.addPromptItem.category === 2 && !this.addPromptItem.relyid) {
        return this.$message({
          type: 'warning',
          message: '请选择引用文本来源'
        })
      }
      if (this.addPromptItem.category === 0 && (!this.addPromptItem.content || !this.addPromptItem.content.trim())) {
        return this.$message({
          type: 'warning',
          message: '请输入文本内容'
        })
      }
      if (!this.addPromptItem.desc || !this.addPromptItem.desc.trim()) {
        this.addPromptItem.desc = ""
      }
      // 一切都满足条件之后，再添加一个uuid()
      this.addPromptItem.id = this.$filters.getUUID();
      this.configNew.steps[this.pos].prompts.splice(this.promptIndex, 0, this.addPromptItem);
      this.addPromptDialogVisiable = false;
      this.addPromptItem = {};
    },
    // 删除一项提示
    deletePrompt(e, id) {
      this.$confirm(
        "确定删除此步骤?删除之后可以点击重置按钮恢复上次保存时的内容",
        "确认操作",
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        this.configNew.steps[this.pos].prompts.splice(id, 1);
        this.$message({
          type: 'success',
          message: '删除成功，保存后生效'
        })
      })
      e.stopPropagation() 
    },
    // 编辑一项提示
    editPrompt(index) {
      this.promptIndex = index;
      this.modifyPromptDialogVisiable = true;
    },
    // 如果用户主动关闭添加提示弹窗
    handleAddPromtDialogClose() {
      this.addPromptItem = {};
      this.addPromptDialogVisiable = false;
    },
    // 展示提示组合效果
    showPromptEffect() {
      this.promptEffectDialogVisiable = true;
      // 先清空，再拼接
      this.promptEffectContent = '';
      for (const {category, name, content} of this.configNew.steps[this.pos].prompts) {
        if (category === 1) {
          this.promptEffectContent += ` [用户输入:${name}] `;
        } else if (category === 2) {
          this.promptEffectContent += ` [引用文本: ${name}] `;
        } else {
          this.promptEffectContent += content;
        }
      }
    }
  }
}
</script>

<style lang="less" scoped>
:deep(textarea) {
  font-family: 'microsoft yahei';
  font-size: 14px;
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
.prompt :deep(textarea) {
  background-color: #FFFFFF;
}
.choices-container {
  width: 100%;
  height: 100%;
  .choice-container {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1;
    .choice-left, .choice-right {
      width: 400px;
      height: 600px;
      border-radius: 0.5rem;
      background-color: #fff;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      transition: all ease .2s;
      gap: 1rem;
      box-shadow: 0 0 2rem 0.25rem rgba(0,0,0,0.2);
      .choice-img {
        width: 80px;
        height: 80px;
        overflow: hidden;
        img {
          width: 100%;
          height: 100%;
        }
      }
      .choice-icon {
        font-size: 2rem;
      }
    }
    .choice-left {
      z-index: 3;
    }
    .choice-right {
      transform: translateX(-2rem);
      z-index: 2;
      transform-origin: right center;
      &:hover {
        animation: show-card linear .5s;
        animation-fill-mode: forwards;
      }
    }
  }
}
.new-area {

}
.repository-area {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
  .pattern-design {
    flex: 1;
    height: 100%;
    overflow-y: auto;
    background: #fff;
    box-shadow: 0 0 0.5rem rgba(0,0,0,0.1);
    padding: 1rem;
    box-sizing: border-box;
    position: relative;
    .pick-pattern {
      width: 340px;
      height: 100%;
      background: #fff;
      margin-right: 2rem;
      padding: 0.75rem 0 0 0;
      box-sizing: border-box;
      position: absolute;
      left: 0;
      top: 0;
      display: flex;
      flex-direction: column;
      transition: all ease .2s;
      z-index: 3;
      .search-box {
        border-bottom: 1px solid #F2F6FC;
        margin: 0 0.75rem 0 0.75rem;
        padding-bottom: 1.5rem;
      }
      .pattern-list {
        flex: 1;
        padding: 0 1rem 0 0.75rem;
        overflow-y: auto;
        height: calc(100% - 1.5rem - 41px);
        .pattern-item {
          line-height: 1.5rem;
          transition: background-color ease .2s;
          button {
            padding: 1.25rem 0.25rem;
            display: block;
            width: 100%;
            height: 100%;
            border: none;
            outline: none;
            background-color: #fff;
            text-align: left;
            &:hover {
              background-color: #f6f6f6;
              border-radius: 0.25rem;
            }
          }
        }
        .load-btn {
          width: 100%;
          padding: 1rem 0;
          text-align: center;
          font-size: 14px;
        }
      }
      .hide-card {
        position: absolute;
        width: 2rem;
        height: 2rem;
        text-align: center;
        line-height: 2rem;
        right: -2rem;
        top: 0;
        background-color: #FFFFFF;
        box-shadow: 0.25rem 0 0.5rem #DDDDDD;
        color: #409EFF;
        cursor: pointer;
        font-size: 1.5rem;
        
      }
    }
    .hide-pick-pattern {
      transform: translateX(-100%);
    }
    .main-area {
      width: 100;
      height: 100%;
      z-index: 1;
      padding-left: 1.5rem;
      box-sizing: border-box;
      overflow: hidden;
      .main-area-tool {
        width: 100%;
        height: 2rem;
        text-align: right;
        margin-bottom: 1rem;
        gap: 1rem;
      }
      .main-area-main {
        display: flex;
        height: 100%;
        .steps-area {
          width: 400px;
          height: 100%;
          margin-right: 2rem;
          border-right: 1px solid #DCDFE6;
          padding-right: 1rem;
          font-size: 14px;
          .step-items-main {
            padding: 0.75rem 0 0.75rem 1rem;
            line-height: 1.5rem;
            border-radius: 0.25rem;
            transition: all ease .2s;
            cursor: pointer;
          }
          .step-items {
            padding: 0.5rem 0 0.5rem 0;
            .step-item {
              line-height: 1.5rem;
              padding: 0.75rem 0 0.75rem 1rem;
              cursor: pointer;
              border-radius: 0.25rem;
              position: relative;
              &:hover .add-new-step {
                visibility: visible;
              }
              .add-new-step {
                visibility: hidden;
                position: absolute;
                right: 0;
                top: 0;
                display: flex;
                align-items: center;
                height: 100%;
                gap: 0.5rem;
                padding: 0 0.75rem;
                background-color:#409EFF;
                .add-new-step-btn {
                  // float: right;
                  // line-height: 1.5rem;
                  padding: 0 0.25rem;
                  border-radius: 0.125rem;
                  color: #c6e2ff;
                  &:hover {
                    background-color: #79bbff;
                  }
                }
                &::before {
                  position: absolute;
                  content: "";
                  left: -2rem;
                  top: 0;
                  width: 2rem;
                  height: 100%;
                  background-image: linear-gradient(to right, rgba(0,0,0,0), #409EFF);
                }
              }
            }
            .slot {
              padding-left: 2rem;
            }
            
          }
          .single-item {
            &:hover {
              background-color: #79bbff;
            }
          }
          .active-step {
            background-color: #79bbff;
          }
        }
        .detail-area {
          flex: 1;
          height: calc(100% - 4rem);
          overflow-y: scroll;
          padding-right: 1rem;
          .step-design {
            .prompts-pattern {
              margin-top: 1rem;
              .prompt-box {
                display: flex;
                flex-direction: column;
                align-items: center;
                .prompt {
                  background-color: #d9ecff;
                  gap: 1rem;
                  width: 100%;
                  margin: 1rem 0;
                  border-radius: 0.25rem;
                  position: relative;
                  padding: 1rem 1.5rem;
                  box-sizing: border-box;
                  cursor: pointer;
                  z-index: 0;
                  .prompt-tool-delete {
                    position: absolute;
                    right: 0.5rem;
                    top: 0.5rem;
                    width: 1rem;
                    height: 1rem;
                    visibility: hidden;
                    font-size: 1rem;
                    transition: all ease .2s;
                    color: #79bbff;
                    &:hover {
                      // transform: rotateZ(90deg);
                      color: #337ecc;
                    }
                  }
                  .prompt-input {
                    margin: 1rem 0;
                    line-height: 1.5rem;
                    font-size: 14px;
                    z-index: 1;
                  }
                  .prompt-extra {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    z-index: 1;
                    .prompt-info {
                      .prompt-type {
                        margin-right: 0.5rem;
                      }
                    }
                    // .prompt-tools {
                    //   display: flex;
                    //   align-items: center;
                    //   gap: 0.25rem;
                    //   visibility: hidden;
                    //   .prompt-tool-btn {
                    //     padding: 0.75rem 0.5rem;
                    //     cursor: pointer;
                    //     &:hover {
                    //       color: #409EFF;
                    //     }
                    //   }
                    // }
                  }
                  &:hover {
                    // background-color: #a0cfff;
                    // .prompt-tools {
                    //   visibility: visible;
                    // }
                    .prompt-tool-delete {
                      visibility: visible;
                    }
                  }
                  &:hover::after {
                    transform: scaleX(1);
                    // background-image: red;
                  }
                  &::after {
                    position: absolute;
                    content: "";
                    left: 0;
                    top: 0;
                    width: 100%;
                    transform: scaleX(0);
                    transform-origin: 0;
                    height: 100%;
                    background: #c6e2ff;
                    z-index: -1;
                    transition: all ease .2s;
                  }
                }
                .prompt-arraw {
                  width: 2rem;
                  height: 2rem;
                  display: inline;
                  font-size: 2rem;
                  text-align: center;
                  color: #999999;
                  overflow: hidden;
                  .prompt-arraw-add {
                    width: 2rem;
                    height: 2rem;
                    margin-top: -2rem;
                    transition: all ease .5s;
                    color:#67C23A;
                    cursor: pointer;
                  }
                  .prompt-arraw-icon {
                    width: 2rem;
                    height: 2rem;
                    transition: all ease .5s;
                    color:  #eebe77;
                  }
                  &:hover {
                    .prompt-arraw-add {
                      margin-top: 0;
                    }
                    .prompt-arraw-icon {
                      margin-top: 2rem;
                      opacity: 0;
                    }
                  }
                }
              }
            }
          }
        }
      }
      
    }
    // .steps {
    //   width: 240px;
    //   height: 100%;
    //   background-color: #999;
    // }
  }
  .pick-area-bg::before {
    position: absolute;
    content: "";
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    background-color: rgba(0,0,0,0.4)!important;
    z-index: 2;
  }
}
@keyframes show-card {
  0% {
    transform: translateX(-2rem);
    z-index: 2;
  }
  25% {
    z-index: 2;
  }
  50% {
    transform: translateX(2rem);
    z-index: 3;
  }
  75% {
    transform: translateX(0);
    z-index: 3;
  }
  100% {
    transform: translateX(-2rem);
    z-index: 3;
  }
}
</style>