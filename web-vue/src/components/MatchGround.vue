<script setup>
import { useUserStore } from "../store/user";
import { usePkStore } from "../store/pk";
import { ref } from "vue";

const userStore = useUserStore();
const pkStore = usePkStore();

const match_btn_info = ref("开始匹配");

const click_match_btn = () => {
  if (match_btn_info.value === "开始匹配") {
    match_btn_info.value = "取消";
    pkStore.socket.send(
      JSON.stringify({
        event: "start-matching",
      }),
    );
  } else {
    match_btn_info.value = "开始匹配";
    pkStore.socket.send(
      JSON.stringify({
        event: "stop-matching",
      }),
    );
  }
};
</script>

<template>
  <div class="match-ground">
    <div class="row">
      <div class="col-6">
        <div class="user-photo">
          <img :src="userStore.photo" alt="左侧玩家头像" />
        </div>
        <div class="user-username">{{ userStore.username }}</div>
      </div>
      <div class="col-6">
        <div class="user-photo">
          <img :src="pkStore.opponent_photo" alt="右侧侧玩家头像" />
        </div>
        <div class="user-username">{{ pkStore.opponent_username }}</div>
      </div>
      <div class="col-12" style="text-align: center">
        <button @click="click_match_btn" type="button" class="btn btn-warning btn-lg">{{ match_btn_info }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.match-ground {
  height: 75vh;
  margin: 40px auto;
  background-color: rgba(50, 50, 50, 0.7);
}
/* 选择所有具有 user-photo 类的 div 元素 */
div.user-photo {
  text-align: center;
  padding-top: 15vh;
}
div.user-photo img {
  border-radius: 50%;
  width: 30vh;
}
div.user-username {
  text-align: center;
  margin-top: 1.5em;
  font-size: 2em;
  color: white;
  font-weight: bold;
}
</style>
