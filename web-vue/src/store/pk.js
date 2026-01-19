import { defineStore } from "pinia";
import { ref, reactive } from "vue";

export const usePkStore = defineStore("pk", () => {
  const socket = ref(null);
  const opponent_username = ref("");
  const opponent_photo = ref("");
  const status = ref("matching");
  const gamemap_array = ref(null);
  const players = reactive({
    playerA: {
      id: 0,
      sx: 0,
      sy: 0,
    },
    playerB: {
      id: 0,
      sx: 0,
      sy: 0,
    },
  });
  const gamemap_object = ref(null);
  const loser = ref("none"); // none, all, A, B

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

  const updateGame = (newGame) => {
    gamemap_array.value = newGame.map;
    players.playerA.id = newGame.a_id;
    players.playerA.sx = newGame.a_sx;
    players.playerA.sy = newGame.a_sy;
    players.playerB.id = newGame.b_id;
    players.playerB.sx = newGame.b_sx;
    players.playerB.sy = newGame.b_sy;
  };

  const updateGamemapObject = (newGamemapObject) => {
    gamemap_object.value = newGamemapObject;
  };

  const updateLoser = (newLoser) => {
    loser.value = newLoser;
  };

  return {
    socket,
    opponent_username,
    opponent_photo,
    status,
    gamemap_array,
    players,
    gamemap_object,
    loser,
    updateSocket,
    updateOpponent,
    updateStatus,
    updateGame,
    updateGamemapObject,
    updateLoser,
  };
});
