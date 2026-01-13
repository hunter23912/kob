<script setup>
import { useRoute } from "vue-router";
import { computed } from "vue";

const route = useRoute();
let route_name = computed(() => route.name);
</script>

<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
      <router-link class="navbar-brand" :to="{ name: 'home' }">King of Bots</router-link>
      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarText"
        aria-controls="navbarText"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarText">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <router-link
              :class="['nav-link', route_name === 'pk_index' && 'active']"
              aria-current="page"
              :to="{ name: 'pk_index' }"
              >对战</router-link
            >
          </li>
          <li class="nav-item">
            <router-link :class="['nav-link', route_name === 'record_index' && 'active']" :to="{ name: 'record_index' }"
              >对局列表</router-link
            >
          </li>
          <li class="nav-item">
            <router-link
              :class="['nav-link', route_name === 'ranklist_index' && 'active']"
              :to="{ name: 'ranklist_index' }"
              >排行榜</router-link
            >
          </li>
        </ul>
        <ul class="navbar-nav">
          <li class="nav-item dropdown">
            <router-link
              :class="['nav-link', 'dropdown-toggle', route_name === 'user_bot_index' && 'active']"
              :to="{ name: 'user_bot_index' }"
              role="button"
              data-bs-toggle="dropdown"
              aria-expanded="false"
            >
              hunter
            </router-link>
            <ul class="dropdown-menu">
              <li><router-link class="dropdown-item" :to="{ name: 'user_bot_index' }">我的军队</router-link></li>
              <li><hr class="dropdown-divider" /></li>
              <li><router-link class="dropdown-item" :to="{ name: '404' }">退出</router-link></li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<style scoped></style>
