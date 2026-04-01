import { defineComponent } from "vue";
import { useAuthStore } from "../../../../session/state/auth.store";
import { useI18n } from "../../../../../app/i18n";
import { useSecurityPage } from "../../composables/use-security-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "SecurityPage",
  setup() {
    const authStore = useAuthStore();
    const { availableLocales, locale, setLocale, t } = useI18n();
    const page = useSecurityPage({
      authStore,
      availableLocales,
      locale,
      setLocale,
      t
    });

    return {
      ...page,
      t
    };
  },
  template
});
