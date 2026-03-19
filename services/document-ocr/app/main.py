from fastapi import FastAPI
from pydantic import BaseModel

from app.service import DocumentOcrService


class DocumentPrefillRequest(BaseModel):
    tenantId: str
    documentFrontImageDataUrl: str
    documentBackImageDataUrl: str


class DocumentPrefillResponse(BaseModel):
    matched: bool
    documentType: str | None = None
    documentNumber: str | None = None
    documentValidUntil: str | None = None
    providerName: str | None = None
    confidence: float = 0.0


service = DocumentOcrService()
app = FastAPI(title="Lombardio Document OCR API", version="0.1.0")


@app.post("/api/v1/ocr/prefill", response_model=DocumentPrefillResponse)
def prefill_document(request: DocumentPrefillRequest) -> DocumentPrefillResponse:
    result = service.prefill(
        tenant_id=request.tenantId,
        front_image_data_url=request.documentFrontImageDataUrl,
        back_image_data_url=request.documentBackImageDataUrl,
    )
    return DocumentPrefillResponse(**result)


@app.get("/api/v1/ocr/health")
def health() -> dict[str, str]:
    return {"status": "ok"}
