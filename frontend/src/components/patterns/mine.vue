<template>
  <div>
    <div class="select-type">
      <div>
        <el-select v-model="field" placeholder="目标领域" size="large" class="selectb">
          <el-option v-for="item in fieldList" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </div>
      <!-- <div>
        <el-input placeholder="根据名称搜索" style="width: 400px;" v-model="keywords">
          <template #append>
            <el-button slot="append" @click="getPatternCollection()"><i class="fa fa-search"></i></el-button>
          </template>
        </el-input>
      </div> -->
    </div>
    <div class="select-res" v-loading="loading" element-loading-text="加载中">
      <el-card class="box-card" shadow="hover">
        <el-table stripe :data="patterns" style="width: 100%">
          <el-table-column type="index"></el-table-column>
          <el-table-column prop="name" label="名称"></el-table-column>
          <el-table-column prop="fieldName" label="领域" width="100">
            <template #default="scope"><el-tag size="small">{{ scope.row.fieldName }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="type" label="来源" width="100">
            <template #default="scope"><el-tag :type="scope.row.type===0?'success':'warning'">{{ scope.row.type===0?'自制':'收藏' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="creator" label="作者" width="160"></el-table-column>
          <el-table-column prop="create_time" label="日期" width="140">
            <template #default="scope">{{ $filters.pdate(scope.row.create_time) }}</template>
          </el-table-column>
          <el-table-column label="" width="260">
            <template #default="scope">
              <el-button size="default" type="primary" plain circle title="立即使用" @click="usePattern(scope.row.id)"><i class="fa fa-rocket"></i></el-button>
              <!-- <el-button size="default" plain type="warning" circle title="查看"><i class="fa fa-eye"></i></el-button> -->
              <el-button size="default" type="warning" plain circle title="查看" @click="checkPattern(scope.row.id)"><i class="fa fa-eye"></i></el-button>
              <el-button size="default" plain type="info" circle title="复制" @click="copyPattern(scope.row.id)"><i class="fa fa-copy"></i></el-button>
              <el-button size="default" plain type="danger" circle title="删除" @click="deletePattern(scope.$index, scope.row.id)"><i class="fa fa-trash"></i></el-button>
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
import { getMyPattern, getPatternField, addPatternToMine, deletePattern, getPatternConfig } from '@/api/api.js'
export default {
  data() {
    return {
      loading: false,
      keywords: '',
      type: null,
      typeList: [],
      field: null,
      fieldList: [],
      page: 0,
      patterns: [],
      total: 0,
      patternDialogVisiable: false,
      config: {}
    }
  },
  mounted() {
    this.getMyPattern();
  },
  methods: {
    async getMyPattern () {
      this.loading = !this.loading;
      await this.getPatternField();
      let params = {
        type: this.type,
        field: this.field,
        page: this.page,
        keywords: this.keywords
      }
      await getMyPattern(params).then(res => {
        this.patterns = res.patterns;
        this.total = res.total;
      }).finally(() => {
        this.loading = !this.loading;
      })
    },
    async getPatternField() {
      await getPatternField().then(res => {
        this.fieldList = res;
      })
    },
    usePattern(id) {
      let that =this;
      this.$confirm('即将前往使用该模板', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        that.$router.push({ path: '/patterns/process', query: { patternid: id } });
      }).catch(() => {
        return;         
      });
    },
    async deletePattern(index, id) {
      await deletePattern({id: id}).then(() => {
        this.$message({
          type: 'success',
          message: '删除成功'
        })
        this.patterns.splice(index, 1);
      })
    },
    async copyPattern(id) {
      await addPatternToMine({patternid: id}).then(() => {
        this.$message({
          type: 'success',
          message: '复制成功'
        })
        // location.reload();
        // 成功之后再次请求信息
        this.getMyPattern();
      })
    },
    // 查看一项模板信息
    async checkPattern(id) {
      await getPatternConfig({id: id}).then(res => {
        this.config = JSON.parse(res.config);
        this.patternDialogVisiable = true;
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