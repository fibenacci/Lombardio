import { computed } from "vue";
import { useI18n } from "../../../app/i18n";

const LOCALE_TAGS = {
  de: "de-DE",
  en: "en-GB"
} as const;
type SupportedLocale = keyof typeof LOCALE_TAGS;
type DateLike = string | number | Date | null | undefined;
type CurrencyFormatOptions = {
  currency?: string;
  emptyValue?: string;
  maximumFractionDigits?: number;
  minimumFractionDigits?: number;
};
type DateFormatOptions = {
  dateStyle?: Intl.DateTimeFormatOptions["dateStyle"];
  emptyValue?: string;
  formatOptions?: Intl.DateTimeFormatOptions;
};
type DateTimeFormatOptions = {
  dateStyle?: Intl.DateTimeFormatOptions["dateStyle"];
  emptyValue?: string;
  formatOptions?: Intl.DateTimeFormatOptions;
  timeStyle?: Intl.DateTimeFormatOptions["timeStyle"];
};
const DATE_TIME_OPTION_KEYS = [
  "calendar",
  "dateStyle",
  "day",
  "dayPeriod",
  "era",
  "formatMatcher",
  "fractionalSecondDigits",
  "hour",
  "hour12",
  "hourCycle",
  "minute",
  "month",
  "numberingSystem",
  "second",
  "timeStyle",
  "timeZone",
  "timeZoneName",
  "weekday",
  "year"
 ] as const;

function resolveLocaleTag(locale: string): string {
  return LOCALE_TAGS[locale as SupportedLocale] ?? LOCALE_TAGS.de;
}

function parseDate(value: DateLike): Date | null {
  if (!value) {
    return null;
  }

  const date = value instanceof Date ? value : new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function pickDateTimeFormatOptions(options: Intl.DateTimeFormatOptions = {}): Intl.DateTimeFormatOptions {
  return DATE_TIME_OPTION_KEYS.reduce((result, key) => {
    const value = options[key];
    if (value !== undefined) {
      result[key] = value as never;
    }
    return result;
  }, {} as Intl.DateTimeFormatOptions);
}

export function useFormatters() {
  const { locale, t } = useI18n();
  const localeTag = computed(() => resolveLocaleTag(locale.value));

  function formatCurrency(value: string | number | null | undefined, options: CurrencyFormatOptions = {}) {
    if (value === null || value === undefined || value === "") {
      return options.emptyValue ?? t("common.notAvailable");
    }

    const amount = Number(value);
    if (Number.isNaN(amount)) {
      return String(value);
    }

    return new Intl.NumberFormat(localeTag.value, {
      style: "currency",
      currency: options.currency ?? "EUR",
      minimumFractionDigits: options.minimumFractionDigits ?? 2,
      maximumFractionDigits: options.maximumFractionDigits ?? 2
    }).format(amount);
  }

  function formatDate(value: DateLike, options: DateFormatOptions = {}) {
    if (!value) {
      return options.emptyValue ?? t("common.notAvailable");
    }

    const date = parseDate(value);
    if (!date) {
      return String(value);
    }

    const { emptyValue: _emptyValue, formatOptions = {}, ...dateOptions } = options;
    const pickedFormatOptions = pickDateTimeFormatOptions(formatOptions);
    const intlOptions = { ...pickedFormatOptions };
    if (Object.keys(pickedFormatOptions).length === 0) {
      intlOptions.dateStyle = dateOptions.dateStyle ?? "medium";
    }

    return new Intl.DateTimeFormat(localeTag.value, intlOptions).format(date);
  }

  function formatDateTime(value: DateLike, options: DateTimeFormatOptions = {}) {
    if (!value) {
      return options.emptyValue ?? t("common.notAvailable");
    }

    const date = parseDate(value);
    if (!date) {
      return String(value);
    }

    const { emptyValue: _emptyValue, formatOptions = {}, ...dateTimeOptions } = options;
    const pickedFormatOptions = pickDateTimeFormatOptions(formatOptions);
    const intlOptions = { ...pickedFormatOptions };
    if (Object.keys(pickedFormatOptions).length === 0) {
      intlOptions.dateStyle = dateTimeOptions.dateStyle ?? "medium";
      intlOptions.timeStyle = dateTimeOptions.timeStyle ?? "short";
    }

    return new Intl.DateTimeFormat(localeTag.value, intlOptions).format(date);
  }

  return {
    formatCurrency,
    formatDate,
    formatDateTime,
    localeTag
  };
}
