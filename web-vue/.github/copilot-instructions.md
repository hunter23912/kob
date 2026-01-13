# Copilot / AI Agent Instructions for `web-vue` 🧭

Quick, actionable notes to get an AI coding agent productive in this repo.

## 1) High-level overview

- Frontend single-page app built with **Vue 3** + **Vite**. Entry: `src/main.js`, root component: `App.vue`.
- Contains a simple canvas-based Snake-like game implemented as plain ES modules under `src/assets/scripts/` and wired into Vue components under `src/components/`.
- Router lives in `src/router/index.js` and exposes routes like `/pk/`, `/record/`, `/ranklist/`, `/user/bot/`.
- No tests or CI files detected; `pnpm-lock.yaml` implies `pnpm` is the preferred package manager.

## 2) Useful commands (run from project root)

- Install dependencies: `pnpm install` (or `npm install` as fallback)
- Start dev server: `pnpm dev` (runs `vite`) — default port 5173
- Build for production: `pnpm build`
- Preview built app: `pnpm preview`

## 3) Key files & patterns (be explicit when editing)

- Game loop: `src/assets/scripts/AcGameObject.js` — objects register by constructing classes that extend `AcGameObject`. New game objects should use that lifecycle instead of introducing separate loops.
- Canvas + sizing: `src/components/GameMap.vue` constructs `new GameMap(canvas.value.getContext('2d'), parent.value)`; `GameMap.update_size()` uses `parent.clientWidth/Height` to set canvas size (see `src/assets/scripts/GameMap.js`). Keep `canvas` tabbable (`tabindex="0"`) so keyboard events work.
- Keyboard controls: mapped in `GameMap.add_listening_events()` — Snake 0 uses `w/a/s/d`, Snake 1 uses Arrow keys. Tests or changes to controls should update both the JS handler and any documentation/UI affordances.
- Map generation: `GameMap.create_walls()` builds symmetric walls and validates connectivity using `check_connectivity()` — avoid asymmetric changes without revalidating logic.

## 4) Vue conventions observed

- Components use `<script setup>` single-file components (SFCs). Follow this concise style when adding components.
- Components use PascalCase filenames (e.g., `GameMap.vue`, `PlayGround.vue`).
- `Pinia` is present as a dependency but `src/store/` is empty — don't assume an existing store architecture.

## 5) Debugging & common gotchas 🔧

- Canvas must have focus to receive `keydown` events — ensure `canvas.focus()` or user clicks it. `GameMap.vue` sets `tabindex="0"` intentionally to allow focus.
- Animation uses `requestAnimationFrame` via `AcGameObject`. To debug, add logs in `update()` of specific objects or set breakpoints in the browser DevTools in `AcGameObject.step` and `GameMap.update()`.
- Resizing: `GameMap.update_size()` recalculates tile size `L` from parent dimensions. If layout changes, verify canvas pixel dimensions and also CSS style affecting parent container (e.g., height set in `PlayGround.vue`).

## 6) Integration & external dependencies

- `axios`, `pinia`, `bootstrap` are installed but little/no usage found. If adding API calls, follow the existing router paths and prefer a single `axios` base instance (add to `src/services/` or similar).
- This frontend may be served by a Spring Boot backend (outside this folder); when implementing APIs, align routing and base paths with backend expectations. Use `vite` proxy config if needed for local development.

## 7) PR guidance & safe edits ✅

- Preserve existing lifecycle behavior: prefer extending `AcGameObject` and registering via constructor rather than adding bespoke loops.
- For visual/game changes, validate in dev server and test keyboard controls and multi-snake interactions manually; the project lacks automated tests.
- Keep changes minimal in `GameMap.create_walls()` and `check_connectivity()` — those functions encode important map invariants.

## 8) Examples & direct references

- Lifecycle loop: `src/assets/scripts/AcGameObject.js`
- Canvas + focus: `src/components/GameMap.vue` and `src/assets/scripts/GameMap.js` (see `update_size()` and `add_listening_events()`)
- Route configuration: `src/router/index.js`

---

If any of these areas need more detail (e.g., preferred `axios` base URL, linting rules, or adding tests), tell me what you'd like expanded and I'll iterate. 💡
