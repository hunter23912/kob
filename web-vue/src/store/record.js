import { defineStore } from "pinia";
import { ref, reactive } from "vue";

export const useRecordStore = defineStore("record", () => {
  const is_record = ref(false);
  const record_loser = ref("");
  const steps = reactive({
    a_steps: [],
    b_steps: [],
  });

  const updateIsRecord = (status) => {
    is_record.value = status;
  };
  const updateRecordLoser = (loser) => {
    record_loser.value = loser;
  };
  const updateSteps = (a_steps, b_steps) => {
    steps.a_steps = a_steps;
    steps.b_steps = b_steps;
  };

  return {
    is_record,
    record_loser,
    steps,
    updateIsRecord,
    updateRecordLoser,
    updateSteps,
  };
});
