import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 匹配所有/api开头的请求路径（dev环境）
      "/api": {
        target: "http://127.0.0.1:8080",
      },
    },
  },
});
