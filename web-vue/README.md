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

#### ref 和 computed 的结合用法

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

> `ref`绑定变量，`reactive`绑定对象，`computed`计算属性

#### 基于 jwt 的退出逻辑

在前端删除`token`

#### git 代码回滚和恢复

```bash
git reset --hard HEAD~1/HEAD^  # 回滚到上一个版本
git reset --hard <commit_id>  # 回滚到指定版本
git checkout -b <new_branch_name> <commit_id>  # 从指定版本创建
git reflog  # 查看操作历史
git reset --hard <reflog_id>  # 恢复到之前的某个操作
```

#### axios的post和get写法

- `post`请求在第三个参数中传递`headers`，第二个参数传递`data`。
- `get`请求在第二个参数中传递`headers`和`params`

#### axios的.then和async/await写法区别

- `.then`写法是基于回调函数的，适合简单的请求处理，但容易导致回调地狱，指使用多个嵌套的`.then()`或回调函数，导致代码缩进加深，难以阅读和维护。

  ```js
  axios
    .get("http://example.com")
    .then((resp1) => {
      axios
        .get(resp1.data.url)
        .then((resp2) => {
          axios
            .get(resp2.data.url)
            .then((resp3) => {
              // 处理 resp3
              console.log("完成");
            })
            .catch((err) => console.error(err));
        })
        .catch((err) => console.error(err));
    })
    .catch((err) => console.error(err));
  ```

- `async/await`写法更简洁，易读，适合复杂的请求处理，性能上几乎相同，都不阻塞主线程。

  ```js
  async function fetchData() {
    try {
      const resp1 = await axios.get("http://example.com");
      const resp2 = await axios.get(resp1.data.url);
      const resp3 = await axios.get(resp2.data.url);
      // 处理 resp3
      console.log("完成");
    } catch (err) {
      console.error(err);
    }
  }

  fetchData();
  ```

  - 通常用`resp`或`response`表示响应结果。解析`resp.data`时使用`data`表示数据。

- `await`让代码顺序执行，看起来慢，实际是异步执行；`then()`允许后续代码立即执行，但如果需要顺序处理结果，会导致回调地狱。`await`更间接，避免嵌套。

#### `#`在JS开和web开发中的特殊含义

- `#`表示锚点链接，指向页面内的特定位置，点击后页面会滚动到该位置。
- `#`后面的内容不会被发送到服务器，仅在浏览器端处理。
- 例如，`http://example.com/page#section1`表示跳转到`page`页面的`section1`位置。
