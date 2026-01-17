<script setup>
import ContentField from "../../../components/ContentField.vue";
import axios from "axios";
import { useUserStore } from "../../../store/user";

const userStore = useUserStore();

// axios
//   .post(
//     "http://localhost:8080/user/bot/update/",
//     {
//       bot_id: 3,
//       title: "Bot的标题3",
//       description: "Bot的描述3",
//       content: "Bot的代码3",
//     },
//     {
//       headers: {
//         Authorization: `Bearer ${userStore.token}`,
//       },
//     }
//   )
//   .then((resp) => {
//     resp = resp.data;
//     console.log(resp);
//   })
//   .catch((err) => {
//     console.log(err);
//   });

// axios
//   .post(
//     "http://localhost:8080/user/bot/remove/",
//     {
//       bot_id: 4,
//     },
//     {
//       headers: {
//         Authorization: `Bearer ${userStore.token}`,
//       },
//     }
//   )
//   .then((resp) => {
//     resp = resp.data;
//     console.log(resp);
//   })
//   .catch((err) => {
//     console.log(err);
//   });

axios
  .get("http://localhost:8080/user/bot/getlist/", {
    headers: {
      Authorization: `Bearer ${userStore.token}`,
    },
  })
  .then((resp) => {
    resp = resp.data;
    console.log(resp);
  })
  .catch((err) => {
    console.log(err);
  });
</script>

<template>
  <ContentField> 我的军队 </ContentField>
</template>

<style scoped></style>
