<script setup>
import PlayGround from "../../components/PlayGround.vue";
import { onMounted, onUnmounted } from "vue";
import { useUserStore } from "../../store/user.js";
import { usePkStore } from "../../store/pk.js";
import MatchGround from "../../components/MatchGround.vue";
import ResultBoard from "../../components/ResultBoard.vue";

const userStore = useUserStore();
const pkStore = usePkStore();
let socket = null;
const socketUrl = `/websocket/${userStore.token}`;

onMounted(() => {
  pkStore.updateLoser("none");
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
      pkStore.updateGame(data.game);

      setTimeout(() => {
        pkStore.updateStatus("playing");
      }, 200);
    } else if (data.event == "move") {
      const game_object = pkStore.gamemap_object;
      const [snake0, snake1] = game_object.snakes;

      snake0.set_direction(data.a_direction);
      snake1.set_direction(data.b_direction);
    } else if (data.event == "result") {
      const game_object = pkStore.gamemap_object;
      const [snake0, snake1] = game_object.snakes;

      if (data.loser === "all" || data.loser === "A") {
        snake0.status = "die";
      }
      if (data.loser === "all" || data.loser === "B") {
        snake1.status = "die";
      }
      pkStore.updateLoser(data.loser);
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
  <PlayGround></PlayGround>
</template>

<style scoped></style>
