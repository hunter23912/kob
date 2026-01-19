<script setup>
import { computed } from "vue";
import { usePkStore } from "../store/pk";
import { useUserStore } from "../store/user";

const pkStore = usePkStore();
const userStore = useUserStore();

const resultText = computed(() => {
  if (pkStore.loser === "all") return "Draw";
  if (
    (pkStore.loser === "A" && pkStore.players.playerA.id == userStore.id) ||
    (pkStore.loser === "B" && pkStore.players.playerB.id == userStore.id)
  ) {
    return "Lose";
  }
  return "Win";
});

const restart = () => {
  pkStore.updateStatus("matching");
  pkStore.updateLoser("none");
  pkStore.updateOpponent({
    username: "对手玩家",
    photo: "https://pica.zhimg.com/v2-061ff5e30cb0334a6b014fca715d8ac0_1440w.jpg",
  });
};
</script>

<template>
  <div class="result-board">
    <div class="result-board-text">{{ resultText }}</div>
    <div class="result-board-btn">
      <button @click="restart" type="button" class="btn btn-warning btn-md">再来!</button>
    </div>
  </div>
</template>

<style scoped>
div.result-board {
  width: 30vw;
  height: 30vh;
  background-color: rgba(50, 50, 50, 0.5);
  position: absolute;
  top: 30vh;
  left: 35vw;
}

div.result-board-text {
  text-align: center;
  color: white;
  font-size: 10vh;
  font-weight: 600;
  font-style: italic;
  padding-top: 4vh;
}

div.result-board-btn {
  text-align: center;
  padding-top: 1.5vh;
}
</style>
