import { defineStore } from "pinia";
import { ref } from "vue";

export const usePkStore = defineStore("pk", () => {
  const socket = ref(null);
  const opponent_username = ref("");
  const opponent_photo = ref("");
  const status = ref("matching");

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

  return {
    socket,
    opponent_username,
    opponent_photo,
    status,
    updateSocket,
    updateOpponent,
    updateStatus,
  };
});
