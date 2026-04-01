import { useToast } from "primevue/usetoast";

export function useAppToast() {
  const toast = useToast();
  type ToastSeverity = "success" | "error" | "info";

  function notify(severity: ToastSeverity, summary: string, detail: string = summary) {
    toast.add({
      severity,
      summary,
      detail,
      life: 3200
    });
  }

  return {
    success(summary: string, detail?: string) {
      notify("success", summary, detail);
    },
    error(summary: string, detail?: string) {
      notify("error", summary, detail);
    },
    info(summary: string, detail?: string) {
      notify("info", summary, detail);
    }
  };
}
