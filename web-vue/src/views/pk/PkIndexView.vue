<script setup>
import PlayGround from "../../components/PlayGround.vue";
import { onMounted, onUnmounted } from "vue";
import { useUserStore } from "../../store/user.js";
import { usePkStore } from "../../store/pk.js";
import MatchGround from "../../components/MatchGround.vue";

const userStore = useUserStore();
const pkStore = usePkStore();
let socket = null;
const socketUrl = `ws://localhost:8080/websocket/${userStore.token}`;

onMounted(() => {
  pkStore.updateOpponent({
    username: "对手玩家",
    photo: "https://pica.zhimg.com/v2-061ff5e30cb0334a6b014fca715d8ac0_1440w.jpg",
  });
  socket = new WebSocket(socketUrl);

  socket.onopen = () => {
    console.log("connected");
    pkStore.updateSocket(socket);
  };

  socket.onmessage = (msg) => {
    const data = JSON.parse(msg.data);
    if (data.event == "match-success") {
      pkStore.updateOpponent({
        username: data.opponent_username,
        photo: data.opponent_photo,
      });
      pkStore.updateGamemap(data.gamemap);

      setTimeout(() => {
        pkStore.updateStatus("playing");
      }, 2000);
    }
  };

  socket.onclose = () => {
    console.log("disconnected");
  };
});

onUnmounted(() => {
  socket.close();
  pkStore.updateStatus("matching");
});
</script>

<template>
  <PlayGround v-if="pkStore.status === 'playing'"></PlayGround>
  <MatchGround v-if="pkStore.status === 'matching'"></MatchGround>
</template>

<style scoped></style>
