import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      vue: "vue/dist/vue.esm-bundler.js"
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes("node_modules")) {
            return undefined;
          }

          if (id.includes("primeicons")) {
            return "icons";
          }

          if (id.includes("@primeuix")) {
            return "primevue-theme";
          }

          if (id.includes("primevue/datatable") || id.includes("primevue/column")) {
            return "primevue-data";
          }

          if (id.includes("primevue")) {
            return "primevue";
          }

          return "vendor";
        }
      }
    }
  },
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: "./src/test/setup.js"
  },
  server: {
    port: 5173,
    watch: {
      usePolling: true,
      interval: 300
    }
  }
});
