<script setup>
import ContentField from "../../../components/ContentField.vue";
import { useUserStore } from "../../../store/user";
import { ref } from "vue";
import router from "../../../router";

const userStore = useUserStore();

// 组件本地的响应式状态
const username = ref("");
const password = ref("");
const error_message = ref("");

const jwt_token = localStorage.getItem("jwt_token");
if (jwt_token) {
  userStore.updateToken(jwt_token);
  userStore.getInfo({
    success() {
      router.push({ name: "home" });
      userStore.updatePullingInfo(false);
    },
    error() {
      userStore.updatePullingInfo(false);
    },
  });
} else {
  userStore.updatePullingInfo(false);
}

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
