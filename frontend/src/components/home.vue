<template>
  <div id="all-container">
    <div id="nav-bar">
      <div class="logo">
        <a href="/patterns/index">
          <img src="@/assets/imgs/logo.png" />
        </a>
      </div>
      <div class="cur-page">{{ $route.meta.title }}</div>
      <div class="nav-info">
        <div class="nav-info-area">
          <div class="nav-nickname">{{ userInfo.username }}</div>
          <div class="nav-avatar">
            <el-avatar shape="square" :size="38" src="https://img.alicdn.com/imgextra/i1/O1CN01EI93PS1xWbnJ87dXX_!!6000000006451-2-tps-150-150.png"></el-avatar>
          </div>
          <div class="more-info">
            <div style="height: 0.75rem"></div>
            <ul class="more-info-ul">
              <li class="more-info-li" @click="showProfile()"><a href="javascript:void 0;"><i class="fa fa-address-card-o"></i>&nbsp;我的资料</a></li>
              <!-- <li class="more-info-li"><a href="javascript:void 0;"><i class="fa fa-bell"></i>&nbsp;查看通知</a></li> -->
              <li class="more-info-li" @click="logout()"><a href="javascript:void 0;"><i class="fa fa-sign-out"></i>&nbsp;退出登录</a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <!-- 左侧工具栏 -->
    <div id="main-container">
      <el-menu :default-active="$route.path" router id="nav-bar-col" :collapse="isNavCollapse">
        <el-menu-item index="/patterns/index" title="主页">
          <svg t="1679053326471" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1369" width="1.8em" height="1.8em"><path d="M460.8 1024h204.8v-409.6c0-30.72-20.48-51.2-51.2-51.2h-102.4c-30.72 0-51.2 20.48-51.2 51.2v409.6z" fill="#5AC8FA" p-id="1370"></path><path d="M358.4 972.8v-409.6c0-56.32 46.08-102.4 102.4-102.4h102.4c56.32 0 102.4 46.08 102.4 102.4v409.6h204.8c30.72 0 51.2-20.48 51.2-51.2V404.48c0-15.36-5.12-30.72-20.48-40.96l-358.4-302.08c-20.48-15.36-46.08-15.36-66.56 0l-358.4 302.08c-10.24 10.24-15.36 25.6-15.36 40.96V921.6c0 30.72 20.48 51.2 51.2 51.2h204.8zM87.04 327.68l358.4-302.08c35.84-30.72 92.16-30.72 133.12 0l358.4 302.08c20.48 20.48 35.84 46.08 35.84 76.8V921.6c0 56.32-46.08 102.4-102.4 102.4H153.6c-56.32 0-102.4-46.08-102.4-102.4V404.48c0-30.72 15.36-56.32 35.84-76.8zM409.6 972.8h204.8v-409.6c0-30.72-20.48-51.2-51.2-51.2H460.8c-30.72 0-51.2 20.48-51.2 51.2v409.6z" fill="" p-id="1371"></path></svg>
          <span></span>
        </el-menu-item>
        <el-menu-item class="c-menu-item" index="/patterns/repository" title="公共模板">
          <svg t="1679053419630" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1716" width="1.8em" height="1.8em"><path d="M640 307.2h153.6C865.28 307.2 921.6 363.52 921.6 435.2v409.6c0 71.68-56.32 128-128 128h-153.6c-71.68 0-128-56.32-128-128v-409.6C512 363.52 568.32 307.2 640 307.2z" fill="#5AC8FA" p-id="1717"></path><path d="M358.4 921.6V102.4H204.8c-30.72 0-51.2 20.48-51.2 51.2v716.8c0 30.72 20.48 51.2 51.2 51.2h153.6z m51.2 0h409.6c30.72 0 51.2-20.48 51.2-51.2V153.6c0-30.72-20.48-51.2-51.2-51.2H409.6v819.2zM204.8 51.2h614.4c56.32 0 102.4 46.08 102.4 102.4v716.8c0 56.32-46.08 102.4-102.4 102.4H204.8c-56.32 0-102.4-46.08-102.4-102.4V153.6c0-56.32 46.08-102.4 102.4-102.4z" fill="" p-id="1718"></path></svg>
          <span></span>
        </el-menu-item>
        <el-menu-item class="c-menu-item" index="/patterns/mine" title="我的模板">
          <svg t="1679053648331" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2218" width="1.8em" height="1.8em"><path d="M409.6 409.6h460.8c56.32 0 102.4 46.08 102.4 102.4v307.2c0 56.32-46.08 102.4-102.4 102.4H409.6c-56.32 0-102.4-46.08-102.4-102.4v-307.2c0-56.32 46.08-102.4 102.4-102.4z" fill="#5AC8FA" p-id="2219"></path><path d="M153.6 153.6c-30.72 0-51.2 25.6-51.2 51.2v614.4c0 30.72 20.48 51.2 51.2 51.2h716.8c30.72 0 51.2-20.48 51.2-51.2V307.2c0-30.72-25.6-51.2-51.2-51.2h-337.92c-25.6 0-51.2-10.24-71.68-30.72L404.48 168.96c-10.24-10.24-25.6-15.36-35.84-15.36H153.6z m0-51.2h215.04c25.6 0 51.2 10.24 71.68 30.72l56.32 56.32c10.24 10.24 20.48 15.36 35.84 15.36H870.4c56.32 0 102.4 46.08 102.4 102.4v512c0 56.32-46.08 102.4-102.4 102.4H153.6c-56.32 0-102.4-46.08-102.4-102.4V204.8c0-56.32 46.08-102.4 102.4-102.4z" fill="" p-id="2220"></path></svg>
          <span></span>
        </el-menu-item>
        <el-menu-item class="c-menu-item" index="/patterns/design" title="设计模板">
          <svg t="1679053711048" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2469" width="1.8em" height="1.8em"><path d="M358.4 153.6l92.16 327.68c5.12 10.24-5.12 25.6-15.36 30.72H291.84c-15.36 0-25.6-10.24-25.6-25.6v-5.12L358.4 153.6zM665.6 870.4l-92.16-327.68v-5.12c0-15.36 10.24-25.6 25.6-25.6H742.4c15.36 5.12 20.48 20.48 15.36 30.72L665.6 870.4z" fill="#5AC8FA" p-id="2470"></path><path d="M25.6 537.6c-15.36 0-25.6-10.24-25.6-25.6s10.24-25.6 25.6-25.6h143.36c10.24 0 20.48-5.12 25.6-20.48L307.2 71.68c10.24-25.6 35.84-40.96 61.44-35.84 20.48 10.24 35.84 20.48 40.96 35.84l256 860.16 117.76-394.24c10.24-30.72 40.96-56.32 71.68-56.32h143.36c15.36 0 25.6 10.24 25.6 25.6s-10.24 25.6-25.6 25.6h-143.36c-10.24 0-20.48 5.12-25.6 20.48L716.8 947.2c-5.12 15.36-15.36 30.72-35.84 35.84-25.6 10.24-56.32-5.12-61.44-35.84L358.4 87.04 240.64 481.28c-10.24 30.72-40.96 56.32-71.68 56.32H25.6z" fill="" p-id="2471"></path></svg>
          <span></span>
        </el-menu-item>
        <el-menu-item class="c-menu-item" index="/func/chat" title="AI交互">
          <svg t="1680007499669" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1369" width="1.8em" height="1.8em"><path d="M691.2 686.08m-256 0a256 256 0 1 0 512 0 256 256 0 1 0-512 0Z" fill="#5AC8FA" p-id="1370"></path><path d="M71.68 957.44l215.04-46.08 15.36 10.24c66.56 35.84 138.24 51.2 209.92 51.2 256 0 460.8-204.8 460.8-460.8s-204.8-460.8-460.8-460.8-460.8 204.8-460.8 460.8c0 81.92 20.48 158.72 56.32 225.28l10.24 15.36-46.08 204.8z m10.24 51.2c-25.6 5.12-56.32-10.24-61.44-40.96v-20.48l46.08-184.32C25.6 686.08 0 604.16 0 512 0 230.4 230.4 0 512 0s512 230.4 512 512-230.4 512-512 512c-87.04 0-163.84-20.48-235.52-56.32l-194.56 40.96z" fill="" p-id="1371"></path></svg>
          <span></span>
        </el-menu-item>
      </el-menu>
      <!-- 子组件 -->
      <div id="container">
        <router-view :key="$route.fullPath" class="router-view" v-slot="{ Component }">
          <transition :name="transitionName" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
    <!-- 用户资料弹窗 -->
    <el-dialog v-model="showPersonalInfo" title="账号信息" width="30%" :before-close="handleClose">
      <p class="info-item">用户ID：{{ userInfo.userid }}</p>
      <p class="info-item">用户昵称：{{ userInfo.username }}</p>
      <p class="info-item">加入时间：{{ $filters.pdate(userInfo.create_time) }}</p>
      <p class="info-item">绑定邮箱：{{ userInfo.email }}</p>
    </el-dialog>
  </div>
</template>

<script>
import { getPersonalProfile, logout } from '@/api/api.js';
import { removeToken } from '@/utils/tokenUtil';
export default {
  data() {
    return {
      transitionName: "slide-left",
      userInfo: {
        username: '游客',
        roleNum: -1,
        userid: '',
        create_time: '',
        email: ''
      },
      showPersonalInfo: false
    }
  },
  created() {
    this.getPersonalProfile();
  },
  methods: {
    async getPersonalProfile() {
      await getPersonalProfile().then(res => {
        this.userInfo = res
      })
    },
    async logout() {
      await logout().then(() => {
        removeToken("bearer");
        this.$message({
          type: 'success',
          message: '退出成功'
        })
        this.$router.replace({ path: '/login' })
      })
    },
    showProfile() {
      this.showPersonalInfo = true;
    }
  }
}
</script>

<style lang="less" scoped>
:deep(.el-menu-item.is-active::before) {
  position: absolute;
  content: "";
  left: 0;
  top: 25%;
  width: 0.4rem;
  height: 50%;
  background-color: #0081ff;
  border-radius: 0 0.5rem 0.5rem 0;
}
#all-container {
  width: 100%;
  height: 100%;
  #nav-bar {
    width: 100%;
    height: 4rem;
    border-bottom: 1px solid #dddddd;
    z-index: 9;
    padding: 0 2rem;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    .logo {
      overflow: hidden;
      width: 30%;
      img {
        height: 2rem;
        margin: 1rem 0;
      }
    }
    .cur-page {
      font-size: 20px;
      font-weight: bold;
      padding: 22px;
      width: 40%;
      text-align: center;
    }
    .nav-info {
      flex: 1;
      height: 100%;
      display: flex;
      justify-content: flex-end;
      overflow: visible;
      .nav-info-area {
        min-width: 10rem;
        max-width: 15rem;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        position: relative;
        .more-info {
          position: absolute;
          bottom: -126px;
          width: 100%;
          height: 140px;
          right: 0;
          z-index: 9;
          display: none;
          &::before {
            position: absolute;
            content: "";
            width: 0.5rem;
            height: 0.5rem;
            top: 0.75rem;
            left: 50%;
            background-color: #FFFFFF;
            transform: translate(-50%, -5px) rotateZ(45deg);
          }
          .more-info-ul {
            list-style: none;
            background-color: #FFFFFF;
            border-radius: 0.25rem;
            box-shadow: 0 0 0.5rem #dddddd;
            padding: 0.25rem 0;
            .more-info-li {
              display: inline-block;
              transition: background-color ease .2s;
              font-size: 14px;
              width: 100%;
              a {
                display: inline-block;
                text-decoration: none;
                color: #333;
                width: 100%;
                text-align: center;
                padding: 0.75rem 0;
                &:hover {
                  // background-color: #ecf5ff;
                  color: #409EFF;
                }
              }
            }
          }
        }
        &:hover {
          background-color: #f6f6f6;
          cursor: pointer;
          .more-info {
            display: block;
          }
        }
        .nav-nickname {
          padding-right: 1.2rem;
        }
        .nav-avatar {
          overflow: visible;
          position: relative;
          margin-top: 26px;
          height: 100%;
        }
      }
    }
  }
  #main-container {
    position: relative;
    height: calc(100% - 4em);
    box-sizing: border-box;
    z-index: 1;
    #nav-bar-col {
      width: 4rem;
      height: 100%;
      position: absolute;
      left: 0;
      top: 0;
      border-right: 1px solid #dddddd;
      z-index: 9;
      .nav-icon {
        font-size: 2rem;
      }
    }
    #container {
      margin-left: 60px;
      height: 100%;
      position: relative;
      background-color: #eeeeee;
      box-sizing: border-box;
      padding: 1rem 2rem;
      overflow: hidden;
    }
  }
  .info-item {
    line-height: 1.5rem;
    font-size: 1rem;

  }
  /* 页面切换效果 */
  .slide-left-enter-from {
    transform: translateX( -20px);
    opacity: 0;
  }
  .slide-left-enter-to {
    transform: translateX(0px);
  }
  .slide-left-leave-from {
    transform: translateX(0);
  }
  .slide-left-leave-to {
    transform: translateX(20px);
    opacity: 0;
  }
  .slide-left-enter-active,
  .slide-left-leave-active {
    transition: all 0.2s;
  }
}
</style>
