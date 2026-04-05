declare module "*.html?raw" {
  const content: string;
  export default content;
}

declare module "*.css";

interface Window {
  __LOMBARDIO_CONFIG__?: Record<string, string | undefined>;
}

interface ImportMetaEnv {
  readonly VITE_PLATFORM_API_BASE_URL?: string;
  readonly VITE_CUSTOMER_API_BASE_URL?: string;
  readonly VITE_ONLINE_AUCTION_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
