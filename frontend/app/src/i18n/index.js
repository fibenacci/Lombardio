import { computed, reactive } from "vue";
import de from "./locales/de.json";
import en from "./locales/en.json";

const STORAGE_KEY = "lombardio.locale";
const SUPPORTED_LOCALES = ["de", "en"];
const FALLBACK_LOCALE = "de";

const dictionaries = { de, en };

function detectInitialLocale() {
  const savedLocale = typeof localStorage !== "undefined" ? localStorage.getItem(STORAGE_KEY) : null;
  if (savedLocale && SUPPORTED_LOCALES.includes(savedLocale)) {
    return savedLocale;
  }

  const browserLocale = typeof navigator !== "undefined" ? navigator.language?.slice(0, 2).toLowerCase() : null;
  if (browserLocale && SUPPORTED_LOCALES.includes(browserLocale)) {
    return browserLocale;
  }

  return FALLBACK_LOCALE;
}

const state = reactive({
  locale: detectInitialLocale()
});

function lookup(dictionary, key) {
  return key.split(".").reduce((current, segment) => current?.[segment], dictionary);
}

function interpolate(message, params) {
  if (typeof message !== "string") {
    return message;
  }

  return Object.entries(params ?? {}).reduce(
    (result, [paramKey, value]) => result.replaceAll(`{${paramKey}}`, String(value)),
    message
  );
}

export function setLocale(locale) {
  const nextLocale = SUPPORTED_LOCALES.includes(locale) ? locale : FALLBACK_LOCALE;
  state.locale = nextLocale;

  if (typeof localStorage !== "undefined") {
    localStorage.setItem(STORAGE_KEY, nextLocale);
  }
}

export function translate(key, params = {}) {
  const dictionary = dictionaries[state.locale] ?? dictionaries[FALLBACK_LOCALE];
  const fallbackDictionary = dictionaries[FALLBACK_LOCALE];
  const message = lookup(dictionary, key) ?? lookup(fallbackDictionary, key) ?? key;
  return interpolate(message, params);
}

export function useI18n() {
  return {
    availableLocales: SUPPORTED_LOCALES,
    locale: computed(() => state.locale),
    setLocale,
    t: translate
  };
}
