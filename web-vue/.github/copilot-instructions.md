# Copilot / AI Agent Instructions for `web-vue` 🧭

Quick, actionable notes to get an AI coding agent productive in this repo.

## 1) High-level overview

- Single-page frontend built with **Vue 3** + **Vite**. App entry: `src/main.js`, root component: `App.vue`.
- The core feature is a canvas-based 2-player snake game (player-vs-player). Game logic lives under `src/assets/scripts/` and is composed with Vue components under `src/components/`.
- Real-time multiplayer is implemented using a WebSocket connection to a Spring Boot backend (`ws://localhost:8080/websocket/{token}`) and REST calls via `axios` to `http://localhost:8080/...`.
- Notable routes: `/pk/` (match & play), `/record/`, `/ranklist/`, `/user/bot/`.
- No automated tests or CI configured. Use `pnpm` (preferred) — see `pnpm-lock.yaml`.

## 2) Useful commands (run from project root)

- Install deps: `pnpm install` (fallback `npm install`)
- Start dev server: `pnpm dev` (Vite, default port 5173)
- Build: `pnpm build`
- Preview production build: `pnpm preview`

## 3) Engine & lifecycle (very important)

- Central loop: `src/assets/scripts/AcGameObject.js` implements a global registry (`AC_GAME_OBJECTS`) and a single `requestAnimationFrame` step.
  - Game objects should extend `AcGameObject` and implement `start()` (runs once) and `update()` (runs each frame). Optional: `destroy()` / `on_destroy()`.
  - Game objects are registered automatically when constructed — avoid creating parallel animation loops.
- Debugging tip: set breakpoints in `AcGameObject.step` and object `update()` methods to inspect `timedelta` and frame flow.

## 4) Canvas, sizing, and keyboard input

- `GameMap` (class: `src/assets/scripts/GameMap.js`; wrapper: `src/components/GameMap.vue`) controls canvas rendering, sizing, and input.
  - `update_size()` computes tile size `L` from `parent.clientWidth` / `parent.clientHeight` and adjusts `canvas.width` / `canvas.height`.
  - Layout/CSS changes to parent containers (e.g., `PlayGround.vue` uses `height: 75vh`) directly affect visuals and `L`.
- Keyboard handling:
  - Canvas is focusable via `tabindex="0"` and focused with `ctx.canvas.focus()` in `GameMap.add_listening_events()`.
  - Input mapping: Player 0 uses `w/a/s/d`; Player 1 uses arrow keys.
  - Sending moves: `GameMap.add_listening_events()` sends `{ player_id, event: 'move', direction }` over the `pkStore.socket`.

## 5) WebSocket & message schema ✅

- WebSocket is opened in `src/views/pk/PkIndexView.vue`:
  - URL: `ws://localhost:8080/websocket/${userStore.token}`
  - Incoming events handled: `match-success`, `move`, `result`.
- Message examples:
  - Incoming `match-success`: { event: 'match-success', opponent_username, opponent_photo, game }
  - Incoming `move`: { event: 'move', a_direction, b_direction }
  - Outgoing `move` (from `GameMap`): { player_id, event: 'move', direction }
- Where the `GameMap` instance is stored: `src/components/GameMap.vue` calls `pkStore.updateGamemapObject(new GameMap(...))`. The store exposes `gamemap_object` so other code (e.g., socket handlers) can access `game_object.snakes`.

## 6) State, auth & backend integration

- Auth is in `src/store/user.js` (Pinia): `id`, `token`, `is_login`, `autoLogin()`, `getInfo()` and JWT storage in `localStorage` as `jwt_token`.
- REST calls use hardcoded base URLs (http://localhost:8080). If you refactor, update all `axios` calls in stores/components.
- Router guard (`src/router/index.js`) requires `meta.requestAuth` and checks `useUserStore().is_login`.

## 7) Project conventions & patterns

- Components use `<script setup>` and **PascalCase** file names.
- Game scripts live under `src/assets/scripts/` and are intentionally simple—add small modules there for gameplay code.
- Pinia stores use the composition-style API (refs/reactive + returned functions).
- Prefer `async/await` for network calls, `for...of` to iterate arrays.

## 8) Known issues & small fixes to watch for ⚠️

- Missing `userStore` reference in `src/assets/scripts/GameMap.js`:
  - `add_listening_events()` sends `{ player_id: userStore.id, ... }` but `userStore` is not imported there — this causes a runtime ReferenceError. Fix by importing and using the store, e.g.:

    ```javascript
    import { useUserStore } from "../../store/user.js";
    // inside add_listening_events or constructor
    const userStore = useUserStore();
    ```

- Ensure `pkStore.updateGamemapObject(...)` is called before socket messages try to access `pkStore.gamemap_object` (race conditions can happen on very fast message flows).
- Canvas focus: losing focus will stop input; tests / manual QA should include re-focusing verification.

## 9) Debugging & testing tips 🔧

- To inspect real-time events, set breakpoints in `PkIndexView.vue` (`socket.onmessage`) and in `GameMap.add_listening_events()` for outgoing messages.
- Manual tests to validate after changes:
  1. Start backend + frontend.
  2. Login, open `/pk/`, ensure socket connects (`connected` log appears).
  3. Confirm `match-success` triggers `pkStore.updateGame()` and game transitions to `playing`.
  4. Verify `move` events are sent/received and both snakes update direction.

## 10) PR & maintenance guidance ✅

- Preserve `AcGameObject` lifecycle; avoid adding competing game loops.
- When changing layouts, check `GameMap.update_size()` visually across screen sizes.
- If modifying WebSocket or game message formats, update `PkIndexView.vue` and `GameMap.add_listening_events()` together and include a short manual test plan in the PR description.

---

If you'd like, I can apply the small code fix for the `userStore` reference and add a short developer checklist to `README.md` — tell me which you'd prefer and I'll open a PR. 💡
