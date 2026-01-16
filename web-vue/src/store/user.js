import { defineStore } from "pinia";
import { ref, computed } from "vue";
import axios from "axios";

export const useUserStore = defineStore("user", () => {
  const id = ref("");
  const username = ref("");
  const photo = ref("");
  const token = ref("");
  const is_login = ref(false);

  // computed专门根据其他ref计算出一个新值
  const user = computed(() => ({
    id: id.value,
    username: username.value,
    photo: photo.value,
    is_login: is_login.value,
  }));

  const login = async (data) => {
    try {
      const loginResp = await axios.post("http://localhost:8080/user/account/token/", {
        username: data.username,
        password: data.password,
      });
      const resp = loginResp.data;

      if (resp.error_message === "success") {
        token.value = resp.token;
        data.success(resp); // 登录成功，回调成功处理函数
      }
    } catch (e) {
      data.error(); // 登录失败，回调错误处理函数
    }
  };

  const getInfo = async (data) => {
    try {
      const geInfoResp = await axios.get("http://localhost:8080/user/account/info/", {
        headers: {
          Authorization: `Bearer ${token.value}`,
        },
      });
      const resp = geInfoResp.data;
      if (resp.error_message === "success") {
        id.value = resp.id;
        username.value = resp.username;
        photo.value = resp.photo;
        is_login.value = true;
        data.success();
      } else {
        data.error(resp.error_message);
      }
    } catch (e) {
      data.error("网络异常，请稍后重试", e);
    }
  };

  return {
    id,
    username,
    photo,
    token,
    is_login,
    user,
    login,
    getInfo,
  };
});
