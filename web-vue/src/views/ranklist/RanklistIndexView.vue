<script setup>
import { ref, onMounted } from "vue";
import ContentField from "../../components/ContentField.vue";
import { useUserStore } from "../../store/user";
import { usePkStore } from "../../store/pk.js";
import axios from "axios";

const userStore = useUserStore();

const users = ref([]);
let current_page = 1;
let total_users = 0;
const pages = ref([]);

// 点击页码
const click_page = (page) => {
  // 传入页码数
  if (page === -2) {
    page = current_page - 1;
  } else if (page === -1) {
    page = current_page + 1;
  } else if (page === -4) {
    // 首页
    page = 1;
  } else if (page === -3) {
    // 尾页
    page = parseInt(Math.ceil(total_users / 10));
  }
  let max_pages = parseInt(Math.ceil(total_users / 10));

  if (page >= 1 && page <= max_pages) {
    current_page = page;
    pull_page(page);
  }
};

const update_pages = () => {
  let max_pages = parseInt(Math.ceil(total_users / 10));
  let new_pages = [];
  for (let i = current_page - 2; i <= current_page + 2; i++) {
    // 显示当前页前后各两页
    if (i >= 1 && i <= max_pages) {
      new_pages.push({
        number: i,
        is_active: i === current_page ? "active" : "",
      });
    }
  }
  pages.value = new_pages;
};

// 向后端获取用户列表
const pull_page = async (page) => {
  // 传入页码数
  current_page = page;
  const resp = await axios.get("http://localhost:8080/api/ranklist/getlist/", {
    params: {
      page,
    },
    headers: {
      Authorization: `Bearer ${userStore.token}`,
    },
  });
  const data = resp.data;
  users.value = data.users;
  total_users = data.users_count;
  update_pages();
};

onMounted(() => {
  pull_page(current_page);
});
</script>

<template>
  <ContentField>
    <table class="table table-light table-striped table-hover">
      <thead>
        <tr>
          <th>用户</th>
          <th>天梯分</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>
            <img :src="user.a_photo" alt="" class="user-photo" />&nbsp;
            <span class="user-username">{{ user.username }}</span>
          </td>

          <td>
            {{ user.rating }}
          </td>
        </tr>
      </tbody>
    </table>
    <nav>
      <ul class="pagination" style="float: right">
        <li class="page-item" @click="click_page(-2)">
          <a class="page-link" href="#">
            <span>&laquo;</span>
          </a>
        </li>

        <li
          :class="['page-item', page.is_active]"
          v-for="page in pages"
          :key="page.number"
          @click="click_page(page.number)"
        >
          <a class="page-link" href="#">{{ page.number }}</a>
        </li>

        <li class="page-item" @click="click_page(-1)">
          <a class="page-link" href="#">
            <span>&raquo;</span>
          </a>
        </li>
      </ul>
    </nav>
  </ContentField>
</template>

<style scoped>
img.user-photo {
  width: 5vh;
  border-radius: 50%;
  vertical-align: middle;
}
table {
  text-align: center;
  vertical-align: middle;
  outline: none;
  user-select: none;
}
thead th {
  border-bottom: 3px solid black;
}
</style>
