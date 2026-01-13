<script setup>
import NavBar from "./components/NavBar.vue";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.min.js";
import { RouterView } from "vue-router";
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
