<script setup>
import axios from "axios";
import { useUserStore } from "../../../store/user";
import { onMounted, ref, reactive } from "vue";
import { Modal } from "bootstrap/dist/js/bootstrap";
import AceEditor from "../../../components/AceEditor.vue";

const userStore = useUserStore();

const bots = ref([]);

const code_languages = [
  { label: "C/C++", value: "c_cpp" },
  { label: "Java", value: "java" },
  { label: "Python", value: "python" },
  { label: "JavaScript", value: "javascript" },
];

const selectLanguage = (bot, lang) => {
  bot.language = lang;
};

const refresh_bots = async () => {
  try {
    const resp = await axios.get("/api/user/bot/getlist/", {
      headers: {
        Authorization: `Bearer ${userStore.token}`,
      },
    });
    bots.value = resp.data.map((bot) => ({ ...bot, language: "c_cpp" }));
  } catch (err) {
    console.log("获取失败", err);
  }
};

// 组件完全挂载后执行
onMounted(() => {
  refresh_bots();
});

// 收集前端页面中的表单数据
const botadd = reactive({
  title: "",
  description: "",
  content: "",
  language: "c_cpp", // 默认语言
  error_message: "",
});

// 将表单数据传给后端
const add_bot = async () => {
  try {
    const resp = await axios.post(
      "/api/user/bot/add/",
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

const remove_bot = async (bot) => {
  const resp = await axios.post(
    "/api/user/bot/remove/",
    {
      bot_id: bot.id,
    },
    {
      headers: {
        Authorization: `Bearer ${userStore.token}`,
      },
    },
  );

  const data = resp.data;
  if (data.error_message === "success") {
    refresh_bots();
  }
};

const update_bot = async (bot) => {
  const resp = await axios.post(
    "/api/user/bot/update/",
    {
      bot_id: bot.id,
      title: bot.title,
      description: bot.description,
      content: bot.content,
    },
    {
      headers: {
        Authorization: `Bearer ${userStore.token}`,
      },
    },
  );

  const data = resp.data;
  if (data.error_message === "success") {
    Modal.getInstance(`#update-bot-btn-${bot.id}`).hide();
    refresh_bots();
  }
};
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
            <div class="modal fade" id="add-bot-btn">
              <div class="modal-dialog modal-xl">
                <div class="modal-content">
                  <div class="modal-header">
                    <h1 class="modal-title fs-5">创建Bot</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
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
                      <div class="code-editor-head" style="">
                        <label class="form-label">代码</label>
                        <div class="btn-group">
                          <button
                            type="button"
                            class="btn btn-secondary dropdown-toggle language-btn"
                            data-bs-toggle="dropdown"
                          >
                            {{ code_languages.find((param) => param.value === botadd.language)?.label || "选择语言" }}
                          </button>
                          <ul class="dropdown-menu">
                            <li v-for="lang in code_languages" :key="lang.value">
                              <a
                                class="dropdown-item"
                                href="#"
                                style="font-size: 0.8em"
                                @click.prevent="selectLanguage(botadd, lang.value)"
                                >{{ lang.label }}</a
                              >
                            </li>
                          </ul>
                        </div>
                      </div>
                      <AceEditor v-model="botadd.content" :language="botadd.language" theme="dracula" height="500px" />
                    </div>
                  </div>
                  <div class="modal-footer">
                    <div class="error_message">{{ botadd.error_message }}</div>
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
                    <button
                      type="button"
                      class="btn btn-secondary"
                      data-bs-toggle="modal"
                      :data-bs-target="`#update-bot-btn-${bot.id}`"
                      style="margin-right: 10px"
                    >
                      修改
                    </button>
                    <button @click="remove_bot(bot)" type="button" class="btn btn-danger">删除</button>

                    <!-- 修改浮动窗口 -->
                    <div class="modal fade" :id="`update-bot-btn-${bot.id}`">
                      <div class="modal-dialog modal-xl">
                        <div class="modal-content">
                          <div class="modal-header">
                            <h1 class="modal-title fs-5">更新Bot</h1>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                          </div>
                          <div class="modal-body">
                            <!-- 一个form结构 -->
                            <div class="mb-3 bot-title">
                              <label for="update-bot-title" class="form-label">名称</label>
                              <input
                                v-model="bot.title"
                                type="text"
                                class="form-control"
                                id="update-bot-title"
                                placeholder="请输入Bot名称"
                              />
                            </div>
                            <div class="mb-3 bot-description">
                              <label for="update-bot-description" class="form-label">简介</label>
                              <textarea
                                v-model="bot.description"
                                class="form-control"
                                id="update-bot-description"
                                rows="3"
                                placeholder="请输入Bot简介"
                              ></textarea>
                            </div>
                            <div class="mb-3">
                              <div class="code-editor-head">
                                <label class="form-label">代码</label>
                                <div class="btn-group">
                                  <button
                                    type="button"
                                    class="btn btn-secondary dropdown-toggle language-btn"
                                    data-bs-toggle="dropdown"
                                  >
                                    {{
                                      code_languages.find((param) => param.value === bot.language)?.label || "选择语言"
                                    }}
                                  </button>
                                  <ul class="dropdown-menu">
                                    <li v-for="lang in code_languages" :key="lang.value">
                                      <a
                                        class="dropdown-item"
                                        href="#"
                                        style="font-size: 0.8em"
                                        @click.prevent="selectLanguage(bot, lang.value)"
                                        >{{ lang.label }}</a
                                      >
                                    </li>
                                  </ul>
                                </div>
                              </div>
                              <AceEditor
                                v-model="bot.content"
                                :language="bot.language"
                                theme="dracula"
                                height="500px"
                              />
                            </div>
                          </div>
                          <div class="modal-footer">
                            <div class="error_message">{{ bot.error_message }}</div>
                            <button type="button" class="btn btn-primary" @click="update_bot(bot)">保存</button>
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                          </div>
                        </div>
                      </div>
                    </div>
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

.code-editor-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.code-editor-head .language-btn {
  font-size: 0.8em;
}

table {
  text-align: center;
  vertical-align: middle;
}

.bot-title,
.bot-description {
  text-align: left;
}

thead th {
  border-bottom: 3px solid black;
}
</style>
