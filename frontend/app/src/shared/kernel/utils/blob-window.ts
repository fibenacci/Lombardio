export function openBlobInWindow(blob: Blob, options: { printMode?: boolean } = {}) {
  const documentUrl = URL.createObjectURL(blob);
  const popup = window.open(documentUrl, "_blank", "noopener,noreferrer");

  const revoke = () => {
    window.setTimeout(() => URL.revokeObjectURL(documentUrl), 1_000);
  };

  if (!popup) {
    revoke();
    return;
  }

  popup.addEventListener(
    "load",
    () => {
      if (options.printMode) {
        popup.focus();
        popup.print();
      }
      revoke();
    },
    { once: true }
  );
}
