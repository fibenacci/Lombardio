export function readFileAsDataUrl(file: File, errorMessage: string) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(typeof reader.result === "string" ? reader.result : "");
    reader.onerror = () => reject(new Error(errorMessage));
    reader.readAsDataURL(file);
  });
}

export function firstSelectedFile(event: unknown): File | null {
  const payload = event as { files?: File[]; target?: { files?: File[] } } | undefined;
  return payload?.files?.[0] ?? payload?.target?.files?.[0] ?? null;
}
