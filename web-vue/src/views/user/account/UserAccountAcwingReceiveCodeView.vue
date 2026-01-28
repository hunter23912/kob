<script setup>
import axios from "axios";
import router from "../../../router";
import { onMounted } from "vue";
import { useUserStore } from "../../../store/user";
import { useRoute } from "vue-router";
const userStore = useUserStore();
const myRoute = useRoute(); // 获取当前组件页面中的路由信息，查询参数。

onMounted(async () => {
  const resp = await axios.get("/api/user/account/acwing/web/receive_code/", {
    params: {
      code: myRoute.query.code,
      state: myRoute.query.state,
    },
  });
  const data = resp.data;

  if (data.result === "success") {
    localStorage.setItem("jwt_token", data.jwt_token);
    userStore.updateToken(data.jwt_token);
    router.push({ name: "home" });
    userStore.updatePullingInfo(false);
  } else {
    router.push({ name: "user_account_login" });
  }
});
</script>

<template>
  <!-- 当前页面是oauth2的重定向地址 -->
  <div></div>
</template>

<style></style>
