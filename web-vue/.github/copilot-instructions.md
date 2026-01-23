# Copilot / AI Agent Instructions for `web-vue` 🧭

Quick, actionable notes to get an AI coding agent productive in this repo.

## Quick start ✅

- Install: `pnpm install` (fallback `npm install`)
- Dev: `pnpm dev` (Vite, default port 5173). Build: `pnpm build`. Preview: `pnpm preview`.
- Backend: API base is `http://localhost:8080`. WebSocket URL: `ws://localhost:8080/websocket/${token}`.

## Architecture & key files 🔧

- Frontend: Vue 3 + Vite. Entry: `src/main.js`, root: `App.vue`.
- Core feature: 2-player canvas snake game.
  - Engine: `src/assets/scripts/AcGameObject.js` (global registry `AC_GAME_OBJECTS`). Extend `AcGameObject` and implement `start()` and `update()`; DO NOT create parallel animation loops.
  - Game scripts: `src/assets/scripts/{GameMap, Snake, Wall, Cell}.js`.
  - UI glue: `src/components/GameMap.vue`, view: `src/views/pk/PkIndexView.vue`.
- Stores & networking: Pinia stores live in `src/store/` (`user.js`, `pk.js`, `record.js`). `pk` store holds socket state and `gamemap_object`.

## WebSocket schema (examples) 📡

- Outgoing move: `{ player_id, event: 'move', direction }`
- Incoming messages:
  - `match-success`: `{ event:'match-success', opponent_username, opponent_photo, game }`
  - `move`: `{ event:'move', a_direction, b_direction }`
  - `result`: `{ event:'result', ... }`

## Conventions & patterns 🧭

- Components: `<script setup>`, PascalCase filenames.
- Stores: Pinia composition-style (refs/reactive + returned functions).
- Network code: prefer `async/await`.
- Canvas sizing: `GameMap.update_size()` computes tile `L` from parent size—CSS/layout changes affect visuals.

## Known issues & quick fixes ⚠️

- Missing `userStore` import in `src/assets/scripts/GameMap.js`. Fix by adding:

```javascript
import { useUserStore } from "../../store/user.js";
const userStore = useUserStore();
```

- Race: ensure `pkStore.updateGamemapObject(...)` runs before socket handlers access `pkStore.gamemap_object`.
- Canvas focus: losing focus stops input; re-focus canvas or call `ctx.canvas.focus()` when showing the game.

## Debugging & testing tips 🔍

- Breakpoints: `AcGameObject.step`, `GameMap.update()`, `PkIndexView.vue socket.onmessage`, `GameMap.add_listening_events()`.
- Manual test flow:
  1. Start backend + frontend.
  2. Login, open `/pk/`, confirm WebSocket connected.
  3. Trigger `match-success`, play, verify `move` events update both snakes.

## PR checklist ✅

- Preserve `AcGameObject` lifecycle; avoid extra `requestAnimationFrame` loops.
- When changing message formats or socket logic, update both `PkIndexView.vue` and `GameMap.add_listening_events()` and include a short manual test plan in the PR description.

---

If you'd like, I can apply the `userStore` import fix or directly update the original `.github/copilot-instructions.md` with this concise version—tell me which and I'll open a PR.
