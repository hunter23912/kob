<script setup>
import { useUserStore } from "../store/user";
import { usePkStore } from "../store/pk";
import { onMounted, ref } from "vue";
import axios from "axios";

const userStore = useUserStore();
const pkStore = usePkStore();

const match_btn_info = ref("开始匹配");
const bots = ref([]);
const select_bot = ref(-1); // -1 代表亲自上阵

const click_match_btn = () => {
  if (match_btn_info.value === "开始匹配") {
    match_btn_info.value = "取消";
    // console.log(bots.value);
    console.log("选择的bot id:", select_bot.value);
    pkStore.socket.send(
      JSON.stringify({
        event: "start-matching",
        bot_id: select_bot.value,
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

const refresh_bots = async () => {
  try {
    const resp = await axios.get("http://localhost:8080/user/bot/getlist/", {
      headers: {
        Authorization: `Bearer ${userStore.token}`,
      },
    });
    bots.value = resp.data.map((bot) => ({ ...bot, language: "c_cpp" }));
  } catch (err) {
    console.log("获取失败", err);
  }
};

onMounted(() => {
  refresh_bots(); // 从云端动态获取bot列表
});
</script>

<template>
  <div class="match-ground">
    <div class="row">
      <div class="col-4">
        <div class="user-photo">
          <img :src="userStore.photo" alt="左侧玩家头像" />
        </div>
        <div class="user-username">{{ userStore.username }}</div>
      </div>
      <div class="col-4">
        <div class="user-select-bot">
          <select class="form-select" v-model="select_bot">
            <option value="-1" selected>亲自上阵</option>
            <option v-for="bot in bots" :key="bot.id" :value="bot.id">{{ bot.title }}</option>
          </select>
        </div>
      </div>
      <div class="col-4">
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
div.user-select-bot {
  text-align: center;
  padding-top: 25vh;
}

div.user-select-bot > select {
  width: 50%;
  margin: 0 auto;
}
</style>
