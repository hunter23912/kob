<script setup>
import axios from "axios";
import { useUserStore } from "../../../store/user";
import { onMounted, ref, reactive } from "vue";
import { Modal } from "bootstrap/dist/js/bootstrap";
const userStore = useUserStore();

const bots = ref([]);

const refresh_bots = async () => {
  try {
    const resp = await axios.get("http://localhost:8080/user/bot/getlist/", {
      headers: {
        Authorization: `Bearer ${userStore.token}`,
      },
    });
    bots.value = resp.data;
  } catch (err) {
    console.log("获取失败", err);
  }
};

// 收集前端页面中的表单数据
const botadd = reactive({
  title: "",
  description: "",
  content: "",
  error_message: "",
});

// 将表单数据传给后端
const add_bot = async () => {
  try {
    const resp = await axios.post(
      "http://localhost:8080/user/bot/add/",
      {
        title: botadd.title,
        description: botadd.description,
        content: botadd.content,
      },
      {
        headers: {
          Authorization: `Bearer ${userStore.token}`,
        },
      },
    );
    const data = resp.data;
    if (data.error_message === "success") {
      botadd.title = "";
      botadd.description = "";
      botadd.content = "";
      botadd.error_message = "";
      Modal.getInstance("#add-bot-btn").hide(); // 关闭浮动窗口,需要Bootstrap的Modal组件支持
      refresh_bots();
    } else {
      botadd.error_message = data.message;
    }
  } catch (err) {
    console.log("创建失败", err);
  }
};

// 组件完全挂载后执行
onMounted(() => {
  refresh_bots();
});
</script>

<template>
  <div class="container">
    <div class="row">
      <div class="col-3">
        <div class="card" style="margin-top: 20px">
          <div class="card-body">
            <img :src="userStore.photo" alt="用户头像" style="width: 100%" />
          </div>
        </div>
      </div>
      <div class="col-9">
        <div class="card" style="margin-top: 20px">
          <div class="card-header">
            <span style="font-size: 130%">我的Bot</span>
            <button
              type="button"
              class="btn btn-primary float-end"
              data-bs-toggle="modal"
              data-bs-target="#add-bot-btn"
            >
              创建Bot
            </button>

            <!-- 一个浮动窗口 -->
            <div
              class="modal fade"
              id="add-bot-btn"
              tabindex="-1"
              aria-labelledby="exampleModalLabel"
              aria-hidden="true"
            >
              <div class="modal-dialog modal-xl">
                <div class="modal-content">
                  <div class="modal-header">
                    <h1 class="modal-title fs-5" id="exampleModalLabel">创建Bot</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                  </div>
                  <div class="modal-body">
                    <!-- 一个form结构 -->
                    <div class="mb-3">
                      <label for="add-bot-title" class="form-label">名称</label>
                      <input
                        v-model="botadd.title"
                        type="text"
                        class="form-control"
                        id="add-bot-title"
                        placeholder="请输入Bot名称"
                      />
                    </div>
                    <div class="mb-3">
                      <label for="add-bot-description" class="form-label">简介</label>
                      <textarea
                        v-model="botadd.description"
                        class="form-control"
                        id="add-bot-description"
                        rows="3"
                        placeholder="请输入Bot简介"
                      ></textarea>
                    </div>
                    <div class="mb-3">
                      <label for="add-bot-code" class="form-label">代码</label>
                      <textarea
                        v-model="botadd.content"
                        class="form-control"
                        id="add-bot-code"
                        rows="10"
                        placeholder="请编写Bot脚本"
                      ></textarea>
                    </div>
                  </div>
                  <div class="modal-footer">
                    <div class="error_message"></div>
                    <button type="button" class="btn btn-primary" @click="add_bot">创建</button>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="card-body">
            <table class="table table-success table-striped table-hover">
              <thead>
                <tr>
                  <th>Bot名称</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="bot in bots" :key="bot.id">
                  <td>{{ bot.title }}</td>
                  <td>{{ bot.createtime }}</td>
                  <td>
                    <button type="button" class="btn btn-secondary" style="margin-right: 10px">修改</button>
                    <button type="button" class="btn btn-danger">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.error_message {
  color: red;
}
</style>
