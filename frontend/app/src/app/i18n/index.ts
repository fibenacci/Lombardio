import { computed, reactive } from "vue";
import de from "./locales/de.json";
import en from "./locales/en.json";

const STORAGE_KEY = "lombardio.locale";
const SUPPORTED_LOCALES = ["de", "en"] as const;
const FALLBACK_LOCALE = "de" as const;

const dictionaries = { de, en };
type Locale = (typeof SUPPORTED_LOCALES)[number];
interface Dictionary {
  [key: string]: string | Dictionary | undefined;
}

function detectInitialLocale(): Locale {
  const savedLocale = typeof localStorage !== "undefined" ? localStorage.getItem(STORAGE_KEY) : null;
  if (savedLocale && SUPPORTED_LOCALES.includes(savedLocale as Locale)) {
    return savedLocale as Locale;
  }

  const browserLocale = typeof navigator !== "undefined" ? navigator.language?.slice(0, 2).toLowerCase() : null;
  if (browserLocale && SUPPORTED_LOCALES.includes(browserLocale as Locale)) {
    return browserLocale as Locale;
  }

  return FALLBACK_LOCALE;
}

const state = reactive({
  locale: detectInitialLocale()
});

function lookup(dictionary: Dictionary, key: string): string | undefined {
  const value = key.split(".").reduce<string | Dictionary | undefined>((current, segment) => {
    if (typeof current === "object" && current !== null && segment in current) {
      return current[segment];
    }
    return undefined;
  }, dictionary);

  return typeof value === "string" ? value : undefined;
}

function interpolate(message: string, params: Record<string, string | number> = {}): string {
  if (typeof message !== "string") {
    return String(message);
  }

  return Object.entries(params ?? {}).reduce(
    (result, [paramKey, value]) => result.replaceAll(`{${paramKey}}`, String(value)),
    message
  );
}

export function setLocale(locale: string): void {
  const nextLocale = SUPPORTED_LOCALES.includes(locale as Locale) ? (locale as Locale) : FALLBACK_LOCALE;
  state.locale = nextLocale;

  if (typeof localStorage !== "undefined") {
    localStorage.setItem(STORAGE_KEY, nextLocale);
  }
}

export function translate(key: string, params: Record<string, string | number> = {}): string {
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
