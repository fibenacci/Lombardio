function readRuntimeConfig() {
  if (typeof window === "undefined") {
    return {};
  }

  return window.__LOMBARDIO_CONFIG__ ?? {};
}

export function readRuntimeValue(key, fallback) {
  const runtimeConfig = readRuntimeConfig();
  const runtimeValue = runtimeConfig[key];

  if (typeof runtimeValue === "string" && runtimeValue.trim() !== "") {
    return runtimeValue;
  }

  return fallback;
}
