<template>
  <div>
    <div class="select-type">
      <div>
        <el-select v-model="field" size="large" placeholder="目标领域" class="selectb" @change="getPatternList()">
          <el-option v-for="item in fieldList" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </div>
      <div>
        <el-input placeholder="根据名称搜索" style="width: 400px;" v-model="keywords">
          <template #append>
            <el-button slot="append" @click="getPatternList()"><i class="fa fa-search"></i></el-button>
          </template>
        </el-input>
      </div>
    </div>
    <div class="select-res">
      <el-card v-loading="loading" element-loading-text="加载中" class="box-card" shadow="hover">
        <el-table stripe :data="patterns" style="width: 100%">
          <el-table-column type="index"></el-table-column>
          <el-table-column prop="name" label="名称"></el-table-column>
          <el-table-column prop="fieldName" label="领域" width="100">
            <template #default="scope"><el-tag size="small"> {{scope.row.fieldName}} </el-tag></template>
          </el-table-column>
          <el-table-column prop="creator" label="作者" width="200"></el-table-column>
          <el-table-column prop="create_time" label="日期" width="200">
            <template #default="scope">{{ $filters.pdate(scope.row.create_time) }}</template>
          </el-table-column>
          <el-table-column label="" width="260">
            <template #default="scope">
              <!-- <el-button size="default" circle type="primary" plain title="添加到我的库" @click="addToMine(scope.row.id)"><i class="fa fa-solid fa-inbox"></i></el-button> -->
              <el-button size="default" type="success" plain circle title="收藏" @click="addToMine(scope.row.id)"><i class="fa fa-star"></i></el-button>
              <el-button size="default" type="warning" plain circle title="查看" @click="checkPattern(scope.row.id)"><i class="fa fa-eye"></i></el-button>
              <!-- <el-button size="default" type="danger" plain circle title="分享"><i class="fa fa-share-alt"></i></el-button> -->
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination hide-on-single-page background layout="prev, pager, next" :total="total"></el-pagination>
        </div>
      </el-card>
    </div>
    <el-dialog title="模板详情" append-to-body v-if="patternDialogVisiable" v-model="patternDialogVisiable" width="50%">
      <!-- <div class="levela"><i class="el-icon-document"></i>&nbsp;模板名称：{{ config.name }}</div>
      <div class="levela"><i class="el-icon-film"></i>&nbsp;模板描述：{{ config.desc }}</div>
      <div class="levela"><i class="el-icon-date"></i>&nbsp;创建时间：{{ $filters.pdate(config.create_time) }}</div>
      <div class="levela"><i class="el-icon-document-copy"></i>&nbsp;详细内容</div> -->
      <div class="steps">
        <div class="step" v-for="(itema, indexa) in config.steps" :key="indexa">
          <div class="title">第{{ indexa + 1 }}步: {{ itema.name }}</div>
          <div>描述: {{ itema.desc }}</div>
          <div>提示组成: </div>
          <div v-for="(itemb, indexb) in itema.prompts" :key="indexb" class="slot">
            <div>第{{ indexb+1 }}部分: {{ itemb.name }}</div>
            <div>类型：{{ itemb.category===0?'默认文本':(itemb.category===1?'用户输入':'模型生成') }}</div>
            <div>描述：{{ itemb.desc }}</div>
          </div>
          <div></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPatternList, getPatternField, getPatternConfig, addPatternToMine } from '@/api/api.js'
export default {
  data() {
    return {
      loading: false,
      keywords: '',
      field: null,
      fieldList: [],
      page: 0,
      patterns: [],
      total: 0,
      patternDialogVisiable: false,
      config: null
    }
  },
  mounted() {
    this.getPatternField();
    this.getPatternList();
  },
  methods: {
    /**
     * 根据条件获取pattern
     */
    async getPatternList () {
      let params = {
        field: this.field,
        page: this.page,
        keywords: this.keywords
      }
      this.loading = !this.loading;
      await getPatternList(params).then(res => {
        this.patterns = res.list;
        this.total = res.total;
        this.loading = !this.loading;
      })
    },
    /**
     * 获取领域列表
     */
    async getPatternField() {
      await getPatternField().then(res => {
        this.fieldList = res;
      })
    },
    async checkPattern(id) {
      await getPatternConfig({id: id}).then(res => {
        this.config = JSON.parse(res.config);
        this.patternDialogVisiable = true;
      })
    },
    closePatternDialog() {
      this.patternDialogVisiable = false;
    },
    /**
     * 将模板添加到我的库
     */
    async addToMine(id) {
      await addPatternToMine({patternid: id}).then(() => {
        this.$message({
          type: 'success',
          message: '已成功添加至我的模板库中'
        });
      })
    }
  }
}
</script>

<style lang="less" scoped>
.select-type {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}
.pagination {
  margin-top: 20px;
  text-align-last: center;
}
.levela {
  line-height: 1.5rem;
  font-weight: bold;
}
.steps {
  padding-left: 1rem;
  box-sizing: border-box;
  .step {
    line-height: 1.5rem;
    position: relative;
    width: 100%;
    padding-left: 0.75rem;
    margin: 0.5rem 0;
    &::before {
      position: absolute;
      content: "";
      left: 0;
      top: 0;
      width: 0.25rem;
      height: 100%;
      background-color: #C6E2FF;
    }
    .title {
      font-weight: bold;
      font-size: 14px;
      text-decoration: underline;
    }
    .slot {
      margin: 0.25rem 0;
      padding-left: 0.5rem;
      position: relative;
      &::before {
        position: absolute;
        content: "";
        left: 0;
        top: 0;
        width: 0.25rem;
        height: 100%;
        background-color: #D9ECFF;
      }
    }
  }
}
</style>