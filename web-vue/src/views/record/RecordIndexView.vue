<script setup>
import { ref, onMounted } from "vue";
import ContentField from "../../components/ContentField.vue";
import { useUserStore } from "../../store/user";
import { useRecordStore } from "../../store/record";
import { usePkStore } from "../../store/pk.js";
import router from "../../router/index.js";
import axios from "axios";

const userStore = useUserStore();
const recordStore = useRecordStore();
const pkStore = usePkStore();

const records = ref([]);
let current_page = 1;
let total_records = 0;
const pages = ref([]);

// 点击页码
const click_page = (page) => {
  if (page === -2) {
    page = current_page - 1;
  } else if (page === -1) {
    page = current_page + 1;
  } else if (page === -4) {
    page = 1;
  } else if (page === -3) {
    page = parseInt(Math.ceil(total_records / 10));
  }
  let max_pages = parseInt(Math.ceil(total_records / 10));

  if (page >= 1 && page <= max_pages) {
    current_page = page;
    pull_page(page);
  }
};

const update_pages = () => {
  let max_pages = parseInt(Math.ceil(total_records / 10));
  let new_pages = [];
  for (let i = current_page - 2; i <= current_page + 2; i++) {
    // 显示当前页前后各两页
    if (i >= 1 && i <= max_pages) {
      new_pages.push({
        number: i,
        is_active: i === current_page ? "active" : "",
      });
    }
  }
  pages.value = new_pages;
};

// 向后端获取对战历史
const pull_page = async (page) => {
  current_page = page;
  const resp = await axios.get("http://localhost:8080/record/getlist/", {
    params: {
      page,
    },
    headers: {
      Authorization: `Bearer ${userStore.token}`,
    },
  });
  const data = resp.data;
  records.value = data.records;
  total_records = data.records_count;
  update_pages();
};

// 辅助函数：将字符串形式的地图转换为二维数组形式
const stringTo2D = (map) => {
  let g = [];
  for (let i = 0, k = 0; i < 15; i++) {
    let line = [];
    for (let j = 0; j < 15; j++, k++) {
      if (map[k] === "0") line.push(0);
      else line.push(1);
    }
    g.push(line);
  }
  return g;
};

const open_record_content = (recordId) => {
  for (const record of records.value) {
    if (record.record.id === recordId) {
      recordStore.updateIsRecord(true);
      pkStore.updateGame({
        map: stringTo2D(record.record.map),
        a_id: record.record.aId,
        a_sx: record.record.aSx,
        a_sy: record.record.aSy,
        b_id: record.record.bId,
        b_sx: record.record.bSx,
        b_sy: record.record.bSy,
      });
      console.log("in open_record_content", pkStore.gamemap_array);

      recordStore.updateSteps(record.record.aSteps, record.record.bSteps);
      recordStore.updateRecordLoser(record.record.loser);
      router.push({
        name: "record_content",
        params: {
          recordId,
        },
      });
      console.log("in open_record_content", recordStore.steps);

      break;
    }
  }
};

onMounted(() => {
  pull_page(current_page);
});
</script>

<template>
  <ContentField>
    <table class="table table-primary table-striped table-hover">
      <thead>
        <tr>
          <th>用户A</th>
          <th>用户B</th>
          <th>对战结果</th>
          <th>对战时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="record in records" :key="record.record.id">
          <td>
            <img :src="record.a_photo" alt="" class="record-user-photo" />&nbsp;
            <span class="record-user-username">{{ record.a_username }}</span>
          </td>
          <td>
            <img :src="record.b_photo" alt="" class="record-user-photo" />&nbsp;
            <span class="record-user-username">{{ record.b_username }}</span>
          </td>
          <td>{{ record.result }}</td>
          <td>{{ record.record.createtime }}</td>

          <td>
            <button @click="open_record_content(record.record.id)" type="button" class="btn btn-sm btn-secondary">
              查看录像
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <nav>
      <ul class="pagination" style="float: right">
        <li class="page-item" @click="click_page(-2)">
          <a class="page-link" href="#">
            <span>&laquo;</span>
          </a>
        </li>
        <li class="page-item" @click="click_page(-4)">
          <a class="page-link" href="#">
            <span>首页</span>
          </a>
        </li>
        <li
          :class="['page-item', page.is_active]"
          v-for="page in pages"
          :key="page.number"
          @click="click_page(page.number)"
        >
          <a class="page-link" href="#">{{ page.number }}</a>
        </li>

        <li class="page-item" @click="click_page(-1)">
          <a class="page-link" href="#">
            <span>&raquo;</span>
          </a>
        </li>
        <li class="page-item" @click="click_page(-3)">
          <a class="page-link" href="#">
            <span>尾页</span>
          </a>
        </li>
      </ul>
    </nav>
  </ContentField>
</template>

<style scoped>
img.record-user-photo {
  width: 5vh;
  border-radius: 50%;
  vertical-align: middle;
}
table {
  text-align: center;
  vertical-align: middle;
  outline: none;
  user-select: none;
}
</style>
