<script setup>
import ContentField from "../../../components/ContentField.vue";
import { ref } from "vue";
import { useUserStore } from "../../../store/user";
import router from "../../../router";
import axios from "axios";

const userStore = useUserStore();
const username = ref("");
const password = ref("");
const confirmed_password = ref("");
const error_message = ref("");

// 该功能不会修改全局的 user 状态，因此不需要调用 userStore 相关的方法
const register = async () => {
  error_message.value = "";

  const registerResp = await axios.post("/api/user/account/register/", {
    username: username.value,
    password: password.value,
    confirmed_password: confirmed_password.value,
  });

  const resp = registerResp.data;
  if (resp.error_message === "success") {
    router.push({ name: "user_account_login" });
  } else {
    error_message.value = resp.error_message;
  }
};
</script>

<template>
  <ContentField>
    <div class="row justify-content-center mb-4">
      <div class="col-3">
        <form @submit.prevent="register">
          <div class="mb-3">
            <label for="username" class="form-label">用户名</label>
            <input v-model="username" type="text" class="form-control" id="username" placeholder="请输入用户名" />
          </div>
          <div class="mb-3">
            <label for="password" class="form-label">密码</label>
            <input v-model="password" type="password" class="form-control" id="password" placeholder="请输入密码" />
          </div>
          <div class="mb-3">
            <label for="confirmed_password" class="form-label">确认密码</label>
            <input
              v-model="confirmed_password"
              type="password"
              class="form-control"
              id="confirmed_password"
              placeholder="请确认密码"
            />
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
