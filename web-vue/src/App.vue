<script setup>
import NavBar from "./components/NavBar.vue";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.min.js";
import { RouterView } from "vue-router";
import axios from "axios";

let token = "";
const loginAndGetUserInfo = async () => {
  try {
    const loginResp = await axios.post("http://localhost:8080/user/account/token/", {
      username: "wjh",
      password: "123",
    });
    console.log("成功了", loginResp.data);
    token = loginResp.data.token;

    const userResp = await axios.get("http://localhost:8080/user/account/info/", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    console.log("获取用户信息成功", userResp.data);
  } catch (err) {
    console.log("操作失败", err);
  }
};

const registerUser = async () => {
  axios
    .post("http://localhost:8080/user/account/register/", {
      username: "wjh3",
      password: "123",
      confirmed_password: "123",
    })
    .then((resp) => {
      resp = resp.data;
      console.log("注册成功", resp);
    })
    .catch((err) => {
      console.log("注册失败", err);
    });
};

// loginAndGetUserInfo();
registerUser();
</script>

<template>
  <NavBar />
  <RouterView />
</template>

<style>
body {
  min-height: 100vh;
  background-image: url("./assets/images/background.jpg");
  background-size: cover;
}
</style>
