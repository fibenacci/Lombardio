import { bootstrapApp } from "./app/main";

bootstrapApp().catch((error) => {
  console.error("[Bootstrap] Fatal error", error);
});
