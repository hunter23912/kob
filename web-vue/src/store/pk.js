import { defineStore } from "pinia";
import { ref } from "vue";

export const usePkStore = defineStore("pk", () => {
  const socket = ref(null);
  const opponent_username = ref("");
  const opponent_photo = ref("");
  const status = ref("matching");
  const gamemap = ref(null);

  const updateSocket = (newSocket) => {
    socket.value = newSocket;
  };

  const updateOpponent = (opponent) => {
    opponent_username.value = opponent.username;
    opponent_photo.value = opponent.photo;
  };

  const updateStatus = (newStatus) => {
    status.value = newStatus;
  };

  const updateGamemap = (newGamemap) => {
    gamemap.value = newGamemap;
  };

  return {
    socket,
    opponent_username,
    opponent_photo,
    status,
    gamemap,
    updateSocket,
    updateOpponent,
    updateStatus,
    updateGamemap,
  };
});
