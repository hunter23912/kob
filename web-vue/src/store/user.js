// 只存放会修改的全局变量（如注册功能，不会修改值，所以不放这里）
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import axios from "axios";

export const useUserStore = defineStore("user", () => {
  const id = ref("");
  const username = ref("");
  const photo = ref("");
  const token = ref("");
  const is_login = ref(false);
  const pulling_info = ref(true); // 是否正在拉取用户信息

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
        localStorage.setItem("jwt_token", resp.token);
        data.success(resp); // 登录成功，回调成功处理函数
      }
    } catch (e) {
      data.error(); // 登录失败，回调错误处理函数
    }
  };

  const autoLogin = async () => {
    const jwt_token = localStorage.getItem("jwt_token");
    if (jwt_token) {
      updateToken(jwt_token);
      await getInfo({
        success: () => {},
        error: () => {},
      });
    }
    updatePullingInfo(false);
  };

  const updateToken = (newToken) => {
    token.value = newToken;
  };

  const updatePullingInfo = (status) => {
    pulling_info.value = status;
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

  const logout = () => {
    id.value = "";
    username.value = "";
    photo.value = "";
    token.value = "";
    is_login.value = false;
    localStorage.removeItem("jwt_token");
  };

  return {
    id,
    username,
    photo,
    token,
    is_login,
    pulling_info,
    user,
    login,
    autoLogin,
    updateToken,
    updatePullingInfo,
    getInfo,
    logout,
  };
});
