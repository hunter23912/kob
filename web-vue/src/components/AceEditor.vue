<script setup>
import { ref, watch } from "vue";
import { VAceEditor } from "vue3-ace-editor";
import ace from "ace-builds";
import "ace-builds/src-noconflict/ext-language_tools";

ace.config.set("basePath", "https://cdn.jsdelivr.net/npm/ace-builds@1.32.0/src-noconflict/");

const props = defineProps({
  modelValue: String,
  language: String,
  theme: String,
  height: String,
});

const emit = defineEmits(["update:modelValue"]);

const innerValue = ref(props.modelValue || "");
let editorInstance = null;

watch(
  () => props.modelValue,
  (newVal) => {
    if (innerValue.value !== newVal) {
      innerValue.value = newVal;
    }
  },
);

watch(innerValue, (newVal) => {
  emit("update:modelValue", newVal);
});

const mergedOptions = ref({
  fontSize: "16px",
  enableBasicAutocompletion: true,
  enableLiveAutocompletion: true,
  enableSnippets: true,
  showPrintMargin: true,
});

const onInit = (editor) => {
  editorInstance = editor;

  // 根据语言动态设置模式
  if (props.language) {
    editor.session.setMode(`ace/mode/${props.language}`);
  }
  if (props.theme) {
    editor.setTheme(`ace/theme/${props.theme}`);
  }
};

watch(
  () => props.language,
  (newLang) => {
    if (editorInstance && newLang) {
      editorInstance.session.setMode(`ace/mode/${newLang}`);
    }
  },
);
</script>

<template>
  <div class="ace-container">
    <!-- 传递给官方的组件参数 -->
    <VAceEditor
      v-model:value="innerValue"
      @init="onInit"
      :lang="language"
      :theme="theme"
      :options="mergedOptions"
      :style="{ width: '100%', height: height || '400px' }"
    />
  </div>
</template>

<style scoped>
.ace-container {
  border: 1px solid #ced4da;
  border-radius: 0.25rem;
  overflow: hidden; /* 防止圆角溢出 */
}
.loading {
  padding: 1rem;
  color: #666;
}
</style>
