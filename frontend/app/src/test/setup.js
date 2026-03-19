import { afterEach, beforeEach, vi } from "vitest";
import { authStore } from "../stores/auth";
import { tenantStore } from "../stores/tenant";

beforeEach(() => {
  window.localStorage.clear();
  authStore.resetForTests();
  tenantStore.resetForTests();
});

afterEach(() => {
  vi.restoreAllMocks();
});
