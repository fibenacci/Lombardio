import { describe, expect, it } from "vitest";
import { normalizeDocumentImageSrc } from "./document-data-url";

describe("normalizeDocumentImageSrc", () => {
  it("keeps existing data urls unchanged", () => {
    expect(normalizeDocumentImageSrc("data:image/png;base64,abc")).toBe("data:image/png;base64,abc");
  });

  it("normalizes legacy raw base64 png values to data urls", () => {
    const pngBase64 =
      "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO7Z4xkAAAAASUVORK5CYII=";

    expect(normalizeDocumentImageSrc(pngBase64)).toBe(`data:image/png;base64,${pngBase64}`);
  });
});
