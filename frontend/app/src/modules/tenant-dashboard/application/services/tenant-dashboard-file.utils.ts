export function readFileAsDataUrl(file: File, t: (key: string) => string) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(typeof reader.result === "string" ? reader.result : "");
    reader.onerror = () => reject(new Error(t("common.fileReadFailed")));
    reader.readAsDataURL(file);
  });
}

export function firstSelectedFile(event: unknown) {
  const payload = event as { files?: File[]; target?: { files?: File[] } } | undefined;
  return payload?.files?.[0] ?? payload?.target?.files?.[0] ?? null;
}
