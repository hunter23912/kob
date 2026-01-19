# Copilot / AI Agent Instructions for `web-vue` 🧭

Quick, actionable notes to get an AI coding agent productive in this repo.

## 1) High-level overview

- Single-page frontend built with **Vue 3** + **Vite**. App entry: `src/main.js`, root component: `App.vue`.
- A small canvas-based snake game is the core feature; game code lives under `src/assets/scripts/` and is composed with Vue components in `src/components/`.
- Routes are defined in `src/router/index.js` (notable paths: `/pk/`, `/record/`, `/ranklist/`, `/user/bot/`).
- No test suites or CI present. `pnpm` is the intended package manager (see `pnpm-lock.yaml`).

## 2) Useful commands (run from project root)

- Install: `pnpm install` (fallback `npm install`)
- Dev server: `pnpm dev` (Vite, default port 5173)
- Build: `pnpm build`
- Preview production build: `pnpm preview`

## 3) Engine & lifecycle (very important)

- Central loop: `src/assets/scripts/AcGameObject.js` implements a global registry (`AC_GAME_OBJECTS`) and a single `requestAnimationFrame` step. Objects should:
  - Extend `AcGameObject`
  - Implement `start()` (runs once), `update()` (runs every frame), and optionally `on_destroy()`/`destroy()`
  - Be registered by constructing them; they are automatically stepped by the engine
- Debugging tip: set breakpoints in `AcGameObject.step` and component `update()` methods to observe the loop and frame `timedelta`.

## 4) Canvas, sizing, and keyboard input

- `GameMap` (class: `src/assets/scripts/GameMap.js`, Vue wrapper: `src/components/GameMap.vue`) manages canvas rendering and sizing.
  - `update_size()` computes tile size `L` from `parent.clientWidth` / `parent.clientHeight` and sets `canvas.width` / `canvas.height`.
  - Parent layout CSS (e.g., `PlayGround.vue` uses `height: 75vh`) affects `L` — changing layout may break visuals.
- Keyboard handling:
  - Canvas is made focusable via `tabindex="0"` and `ctx.canvas.focus()` is called in `GameMap.add_listening_events()`.
  - Keys: Player 0 -> `w/a/s/d`; Player 1 -> `ArrowUp/Right/Down/Left`.
  - If adding controls, mirror the event handling in `add_listening_events()` and ensure canvas focus remains.

## 5) Map & gameplay rules to preserve

- Wall generation is symmetric and randomized in `GameMap.create_walls()`; it uses `check_connectivity()` to guarantee a path between starting points.
  - `inner_walls_count` defaults to 20; `start()` retries wall generation up to 1000 times to get a valid map.
  - Avoid asymmetric or ad-hoc changes to wall generation without testing connectivity logic thoroughly.
- Collision & movement: `Snake` (in `src/assets/scripts/Snake.js`) uses a step/round model — `next_step()` sets the target cell, movement is smoothed in `update_move()`.

## 6) State, authentication & network calls

- Pinia store: `src/store/user.js` manages auth state (`is_login`, `token`, `user`), pulls info with `getInfo()`, and stores `jwt_token` in `localStorage`.
- Network calls use `axios` with hardcoded `http://localhost:8080/...` endpoints in the store — treat these as tightly coupled to a Spring Boot backend.
  - If adding API work, consider centralizing axios (e.g., `src/services/axios.js`) but note current code uses direct calls.
- Router guard: `src/router/index.js` uses a global `beforeEach` to redirect to login when `meta.requestAuth` is true and `useUserStore().is_login` is false.
- Logout: Clears user data and removes `jwt_token` from `localStorage`.

## 7) Project conventions & patterns

- Vue SFCs use `<script setup>` and **PascalCase** filenames (`GameMap.vue`, `PlayGround.vue`, `NavBar.vue`).
- Small, direct modules in `src/assets/scripts/` (no build step for game code) — prefer adding small modules there for game logic.
- Use the existing lifecycle (extend `AcGameObject`) rather than creating new animation loops.
- Import style: Use `{}` for named exports, no braces for default exports (e.g., `import GameMap from './GameMap.vue'`).
- Pinia stores use composition API style with `ref`, `computed`, and functions (see `src/store/user.js`).
- Prefer `async/await` for API calls over `.then()` chains.
- JS iteration: `for...of` for elements, `for...in` for indices.

## 8) Debugging & common gotchas 🔧

- Canvas must be focused to receive keyboard input — losing focus is a frequent source of "controls not working" bugs.
- Visual regressions usually stem from parent CSS affecting `update_size()` (check `PlayGround.vue` and component wrappers).
- Use `console.log` inside `update()`/`start()` or set breakpoints in DevTools at `AcGameObject.step` and `GameMap.update()`.
- Git rollback: `git reset --hard HEAD~1` for last commit, `git reset --hard <commit_id>` for specific, `git reflog` to view history.

## 9) PR guidance & safe edits ✅

- Preserve the `AcGameObject` lifecycle and avoid adding parallel game loops.
- Verify keyboard focus, input mapping, and responsive sizing when changing components that affect layout.
- When touching `create_walls()` or `check_connectivity()`, add manual validation (play several random maps) — the project has no automated tests.
- If changing API URLs or auth flow, update `src/store/user.js` and any example calls in `App.vue`.

## 10) Quick references (files to look at)

- Engine lifecycle: `src/assets/scripts/AcGameObject.js` 🔧
- Game logic: `src/assets/scripts/GameMap.js`, `Snake.js`, `Wall.js`, `Cell.js` 🧱
- Vue wrappers & layout: `src/components/GameMap.vue`, `src/components/PlayGround.vue`, `src/components/NavBar.vue` 🖼️
- Router & auth guard: `src/router/index.js` 🔐
- User store / axios usage: `src/store/user.js` 🌐

---

If you want, I can make small follow-up edits (e.g., centralize axios to `src/services/axios.js`, add an explicit `autoLogin()` call in `App.vue`, or add a short developer checklist) — tell me which you'd prefer and I'll update the file. 💡
