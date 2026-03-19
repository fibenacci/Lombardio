import { useToast } from "primevue/usetoast";

export function useAppToast() {
  const toast = useToast();

  function notify(severity, summary, detail = summary) {
    toast.add({
      severity,
      summary,
      detail,
      life: 3200
    });
  }

  return {
    success(summary, detail) {
      notify("success", summary, detail);
    },
    error(summary, detail) {
      notify("error", summary, detail);
    },
    info(summary, detail) {
      notify("info", summary, detail);
    }
  };
}
