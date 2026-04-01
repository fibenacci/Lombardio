import { computed } from "vue";
import { useI18n } from "../i18n";

const LOCALE_TAGS = {
  de: "de-DE",
  en: "en-GB"
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
];

function resolveLocaleTag(locale) {
  return LOCALE_TAGS[locale] ?? LOCALE_TAGS.de;
}

function parseDate(value) {
  if (!value) {
    return null;
  }

  const date = value instanceof Date ? value : new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function pickDateTimeFormatOptions(options) {
  return DATE_TIME_OPTION_KEYS.reduce((result, key) => {
    if (options[key] !== undefined) {
      result[key] = options[key];
    }
    return result;
  }, {});
}

export function useFormatters() {
  const { locale, t } = useI18n();
  const localeTag = computed(() => resolveLocaleTag(locale.value));

  function formatCurrency(value, options = {}) {
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

  function formatDate(value, options = {}) {
    if (!value) {
      return options.emptyValue ?? t("common.notAvailable");
    }

    const date = parseDate(value);
    if (!date) {
      return String(value);
    }

    const { emptyValue: _emptyValue, formatOptions = {}, ...dateOptions } = options;
    const pickedFormatOptions = pickDateTimeFormatOptions(formatOptions);
    const intlOptions = {
      ...pickedFormatOptions
    };
    if (Object.keys(pickedFormatOptions).length === 0) {
      intlOptions.dateStyle = dateOptions.dateStyle ?? "medium";
    }

    return new Intl.DateTimeFormat(localeTag.value, intlOptions).format(date);
  }

  function formatDateTime(value, options = {}) {
    if (!value) {
      return options.emptyValue ?? t("common.notAvailable");
    }

    const date = parseDate(value);
    if (!date) {
      return String(value);
    }

    const { emptyValue: _emptyValue, formatOptions = {}, ...dateTimeOptions } = options;
    const pickedFormatOptions = pickDateTimeFormatOptions(formatOptions);
    const intlOptions = {
      ...pickedFormatOptions
    };
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
