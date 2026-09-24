<template>
  <div class="login">
    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ title }}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          :type="passwordVisible ? 'text' : 'password'"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
          <template #suffix>
            <el-icon class="eye-icon" @click="togglePasswordVisible">
              <component :is="passwordVisible ? View : Hide" />
            </el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          size="large"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleLogin"
          @keyup.esc="clearCode"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img" alt="验证码"/>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right; padding-top: 10px;">
          <el-link href="https://www.gyjerp.com/" :underline="false" target="_blank" style="float: left">返回官网</el-link>
          <el-link href="https://www.gyjerp.com/iot" :underline="false" target="_blank" style="float: left; margin-left: 20px">查看文档</el-link>
          <router-link v-if="register" class="link-type" :to="'/register'" style="float: left; margin-left: 20px">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2025-2030 gyjerp.com All Rights Reserved.</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg, getRegisterFlag } from "@/api/login";
import { getPlatformName } from '@/api/index'
import Cookies from "js-cookie";
import { encrypt, decrypt } from "@/utils/jsencrypt";
import useUserStore from '@/store/modules/user'
import { View, Hide } from '@element-plus/icons-vue'

const userStore = useUserStore()
const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: ""
});

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
};

const title = ref("");
const codeUrl = ref("");
const loading = ref(false);
// 密码明文/密文切换
const passwordVisible = ref(false);
function togglePasswordVisible() {
  passwordVisible.value = !passwordVisible.value;
}
// 验证码开关
const captchaEnabled = ref(true);
// 注册开关
const register = ref(false);
const redirect = ref(undefined);

// 获取平台名称
async function getPlatformInfo() {
  try {
    const res = await getPlatformName();
    title.value = res.platformName || '物联网平台';
  } catch (error) {
    console.error('获取平台名称失败:', error);
    title.value = '物联网平台'; // 默认标题
  }
}

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect;
}, { immediate: true });

// 处理登录
async function handleLogin() {
  try {
    const valid = await proxy.$refs.loginRef.validate();
    if (!valid) return;

    loading.value = true;

    // 如果勾选了记住密码，保存到cookie
    if (loginForm.value.rememberMe) {
      saveCredentials();
    } else {
      clearCredentials();
    }

    // 调用登录action
    await userStore.login(loginForm.value);

    // 登录成功后的路由跳转
    const query = route.query;
    const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
      if (cur !== "redirect") {
        acc[cur] = query[cur];
      }
      return acc;
    }, {});

    proxy.$modal.msgSuccess("登录成功");
    router.push({ path: redirect.value || "/", query: otherQueryParams });

  } catch (error) {
    loading.value = false;

    // 重新获取验证码
    if (captchaEnabled.value) {
      getCode();
    }
  }
}

// 保存凭证到cookie
function saveCredentials() {
  Cookies.set("username", loginForm.value.username, { expires: 30 });
  Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 });
  Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 });
}

// 清除cookie中的凭证
function clearCredentials() {
  Cookies.remove("username");
  Cookies.remove("password");
  Cookies.remove("rememberMe");
}

// 获取验证码
async function getCode() {
  try {
    const res = await getCodeImg();
    captchaEnabled.value = res.captchaEnabled !== undefined ? res.captchaEnabled : true;
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img;
      loginForm.value.uuid = res.uuid;
    }
  } catch (error) {
    console.error('获取验证码失败:', error);
    proxy.$modal.msgError("获取验证码失败，请重试");
  }
}

// 获取注册功能开关状态
async function getRegisterUserFlag() {
  try {
    const res = await getRegisterFlag();
    register.value = res.registerUser === undefined ? true : res.registerUser === 'true';
  } catch (error) {
    console.error('获取注册状态失败:', error);
    register.value = true; // 默认允许注册
  }
}

// 从cookie中读取保存的凭证
function getCookie() {
  const username = Cookies.get("username");
  const password = Cookies.get("password");
  const rememberMe = Cookies.get("rememberMe");

  loginForm.value = {
    ...loginForm.value,
    username: username || loginForm.value.username,
    password: password ? decrypt(password) : loginForm.value.password,
    rememberMe: rememberMe ? Boolean(rememberMe) : false
  };
}

// 清除验证码输入
function clearCode() {
  loginForm.value.code = "";
}

// 初始化
onMounted(() => {
  getPlatformInfo();
  getRegisterUserFlag();
  getCookie();
  getCode();
});
</script>

<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
  .eye-icon {
    cursor: pointer;
    font-size: 16px;
    color: #8c939d;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 40px;
  padding-left: 12px;
}
</style>
