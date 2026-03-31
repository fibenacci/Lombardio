import { computed, defineComponent } from "vue";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "AppDataTable",
  inheritAttrs: false,
  props: {
    value: {
      type: Array,
      default: () => []
    },
    dataKey: {
      type: String,
      default: null
    },
    rows: {
      type: Number,
      default: 10
    },
    rowsPerPageOptions: {
      type: Array,
      default: () => [10, 25, 50]
    },
    paginator: {
      type: Boolean,
      default: true
    },
    alwaysShowPaginator: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    loadingRows: {
      type: Number,
      default: 6
    },
    loadingColumns: {
      type: Number,
      default: 5
    },
    size: {
      type: String,
      default: "small"
    },
    sortMode: {
      type: String,
      default: "single"
    },
    removableSort: {
      type: Boolean,
      default: true
    },
    emptyMessage: {
      type: String,
      default: null
    }
  },
  setup(props) {
    const { t } = useI18n();
    const loadingRowIndexes = computed(() => Array.from({ length: props.loadingRows }, (_, index) => index));
    const loadingColumnIndexes = computed(() => Array.from({ length: props.loadingColumns }, (_, index) => index));
    const resolvedEmptyMessage = computed(() =>
      props.emptyMessage ?? t("common.noRecordsFound")
    );

    return {
      loadingColumnIndexes,
      loadingRowIndexes,
      resolvedEmptyMessage
    };
  },
  template
});
