## Vue 前端 web

> 记录前端项目中的一些规范

### js 代码规范

JS 中，of 遍历的是元素，in 遍历的是下标

### 前后端接口写法规范

对于一般的接口请求，推荐使用 `async/await`（异步风格），因为它更简洁、易读，与 `Vue 3` 的 `<script setup>` 风格一致，避免 `.then()` 的嵌套回调。

### import 中什么时候用{}括号，什么时候不用？

- 当导入的是默认导出时，不需要使用 `{}` 括号。
- 当导入的是命名导出时，需要使用 `{}` 括号。
- 当导入的是混合导出时，默认导出不需要 `{}` 括号，命名导出需要使用 `{}` 括号。

### pinia 全局变量存储规范

#### vue2 风格的 options 写法

```js
// store/counter.js
export const useCounterStore = defineStore("counter", {
  state: () => ({
    count: 0,
  }),
  getters: {
    doubleCount: (state) => state.count * 2,
  },
  actions: {
    increment() {
      this.count++;
    },
  },
});
```

#### vue3 风格的 composition 写法

```js
// store/counter.js
export const useCounterStore = defineStore("counter", () => {
  const count = ref(0); // 相当于 state
  const doubleCount = computed(() => count.value * 2); // 相当于 getters

  function increment() {
    // 相当于 actions
    count.value++;
  }

  return { count, doubleCount, increment };
});
```

#### pinia store 的引入写法

```js
import { useUserStore } from "../../../store/user";

const userStore = useUserStore();
```

#### ref and computed 的结合用法

```js
const id = ref("");
const username = ref("");
const photo = ref("");
const token = ref("");
const is_login = ref(false);

// computed专门根据其他ref计算出一个新值
const user = computed(() => ({
  id: id.value,
  username: username.value,
  photo: photo.value,
  is_login: is_login.value,
}));
```

#### 基于 jwt 的退出逻辑

在前端删除`token`
