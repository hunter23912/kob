<script setup>
import axios from "axios";
import { ref, onMounted } from "vue";

let bots = ref([]);
onMounted(() => {
  axios.get("http://localhost:8080/pk/getbotinfo/").then((resp) => {
    resp = resp.data;
    console.log(resp);
    bots.value = resp;
  });
});
</script>

<template>
  <div v-for="(bot, index) in bots" :key="index">
    <div>bot名称：{{ bot.name }}</div>
    <div>bot战力：{{ bot.rating }}</div>
  </div>
</template>

<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
