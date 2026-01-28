<script setup>
import ContentField from "../../../components/ContentField.vue";
import { useUserStore } from "../../../store/user";
import { ref, onMounted } from "vue";
import router from "../../../router";
import axios from "axios";

const userStore = useUserStore();

// 组件本地的响应式状态
const username = ref("");
const password = ref("");
const error_message = ref("");

onMounted(async () => {
  await userStore.autoLogin();
  if (userStore.is_login) {
    router.push({ name: "home" });
  }
});

const login = () => {
  error_message.value = "";
  userStore.login({
    username: username.value,
    password: password.value,
    success() {
      userStore.getInfo({
        success() {
          router.push({ name: "home" });
          console.log(userStore.user);
        },
      });
    },
    error(err) {
      error_message.value = `用户名或密码错误`;
    },
  });
};

const acwing_login = async () => {
  const resp = await axios.get("/api/user/account/acwing/web/apply_code/");
  const data = resp.data;
  if (data) {
    if (data.result === "success") {
      window.location.replace(data.apply_code_url);
    }
  }
};
</script>

<template>
  <ContentField v-if="!userStore.pulling_info">
    <div class="row justify-content-center mb-4">
      <div class="col-3">
        <form @submit.prevent="login">
          <div class="mb-3">
            <label for="username" class="form-label">用户名</label>
            <input v-model="username" type="text" class="form-control" id="username" placeholder="请输入用户名" />
          </div>
          <div class="mb-3">
            <label for="password" class="form-label">密码</label>
            <input v-model="password" type="password" class="form-control" id="password" placeholder="请输入密码" />
          </div>
          <div class="error-message">{{ error_message }}</div>
          <button type="submit" class="btn btn-primary">提交</button>
        </form>
        <div style="text-align: center; margin-top: 1rem; cursor: pointer">
          <img
            @click="acwing_login"
            width="30"
            src="https://app165.acapp.acwing.com.cn/static/image/settings/acwing_logo.png"
            alt=""
          />
          <div style="font-size: 0.8rem">使用 AcWing 账号登录</div>
        </div>
      </div>
    </div>
  </ContentField>
</template>

<style scoped>
button {
  width: 100%;
}
.error-message {
  color: red;
  margin-bottom: 10px;
}
</style>
