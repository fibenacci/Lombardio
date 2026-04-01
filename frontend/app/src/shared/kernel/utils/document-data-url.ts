function inferMimeType(base64Value: string) {
  if (base64Value.startsWith("/9j/")) {
    return "image/jpeg";
  }
  if (base64Value.startsWith("iVBOR")) {
    return "image/png";
  }
  if (base64Value.startsWith("R0lGOD")) {
    return "image/gif";
  }
  if (base64Value.startsWith("UklGR")) {
    return "image/webp";
  }
  return "image/png";
}

export function normalizeDocumentImageSrc(value: unknown) {
  const normalizedValue = String(value ?? "").trim();

  if (!normalizedValue) {
    return "";
  }

  if (
    normalizedValue.startsWith("data:")
    || normalizedValue.startsWith("blob:")
    || normalizedValue.startsWith("http://")
    || normalizedValue.startsWith("https://")
  ) {
    return normalizedValue;
  }

  return `data:${inferMimeType(normalizedValue)};base64,${normalizedValue}`;
}
