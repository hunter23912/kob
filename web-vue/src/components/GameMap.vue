<script setup>
import { GameMap } from "../assets/scripts/GameMap";
import { ref, onMounted } from "vue";
import { usePkStore } from "../store/pk";

const parent = ref(null);
const canvas = ref(null);
const pkStore = usePkStore();

onMounted(() => {
  new GameMap(canvas.value.getContext("2d"), parent.value, pkStore.gamemap);
});
</script>

<template>
  <div class="gamemap" ref="parent">
    <canvas ref="canvas" tabindex="0"></canvas>
  </div>
</template>

<style scoped>
.gamemap {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
