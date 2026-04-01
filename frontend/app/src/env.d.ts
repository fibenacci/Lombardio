declare module "*.html?raw" {
  const content: string;
  export default content;
}

declare module "*.css";

interface Window {
  __LOMBARDIO_CONFIG__?: Record<string, string | undefined>;
}
