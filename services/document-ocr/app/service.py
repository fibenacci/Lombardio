from __future__ import annotations

import base64
import io
import re
from dataclasses import dataclass
from datetime import date
from typing import Any
from urllib.parse import unquote_to_bytes

import numpy as np
from PIL import Image

try:
    import easyocr
except Exception:  # pragma: no cover
    easyocr = None

try:
    import cv2
except Exception:  # pragma: no cover
    cv2 = None

try:
    from paddleocr import PaddleOCR
except Exception:  # pragma: no cover
    PaddleOCR = None


DOCUMENT_NUMBER_PATTERN = re.compile(
    r"(?:DOCNO|DOCUMENT_NUMBER|AUSWEISNUMMER|DOKUMENTENNUMMER|DOCUMENT NO\.?)[:= ]+([A-Z0-9<\- ]+)",
    re.IGNORECASE,
)
VALID_UNTIL_PATTERN = re.compile(
    r"(?:VALID_UNTIL|GUELTIG[_\s-]?BIS|EXPIRY|EXPIRES|GUELTIGKEIT)[:= ]+(\d{4}[-.]\d{2}[-.]\d{2}|\d{2}[.-]\d{2}[.-]\d{4})",
    re.IGNORECASE,
)
MRZ_LINE_PATTERN = re.compile(r"[A-Z0-9<]{30,}")
GENERIC_DOCUMENT_NUMBER_PATTERN = re.compile(
    r"(?<![A-Z0-9<\-])(?=[A-Z0-9<\-]{6,15})(?=.*[A-Z])(?=.*\d)([A-Z0-9<\-]+)(?![A-Z0-9<\-])",
    re.IGNORECASE,
)
GENERIC_DATE_PATTERN = re.compile(
    r"(?<!\d)((?:\d{2}[./-]\d{2}[./-]\d{4})|(?:\d{4}[./-]\d{2}[./-]\d{2}))(?!\d)"
)


@dataclass
class DecodedDocument:
    text: str | None
    image: np.ndarray | None
    source: str


class DocumentOcrService:
    _ocr_instance: Any = None
    _easyocr_reader: Any = None

    def __init__(self) -> None:
        self._ocr = self._get_ocr()

    def prefill(self, tenant_id: str, front_image_data_url: str, back_image_data_url: str) -> dict[str, Any]:
        del tenant_id
        front = self._decode_document(front_image_data_url)
        back = self._decode_document(back_image_data_url)

        extracted_texts: list[str] = []
        confidence_values: list[float] = []
        provider_name = "document-ocr-fallback"

        for decoded in (front, back):
            if decoded.text:
                extracted_texts.append(decoded.text)
            elif decoded.image is not None:
                text, confidences, detected_provider = self._read_text_from_image(decoded.image)
                if text:
                    extracted_texts.append(text)
                    confidence_values.extend(confidences)
                    provider_name = detected_provider

        combined_text = "\n".join(extracted_texts)
        document_type = self._extract_document_type(combined_text)
        document_number = self._extract_document_number(combined_text)
        valid_until_value = self._extract_valid_until(combined_text)

        if document_number is None or valid_until_value is None or document_type is None:
            mrz_result = self._extract_from_mrz(combined_text)
            if mrz_result is not None:
                document_type = document_type or mrz_result["documentType"]
                document_number = document_number or mrz_result["documentNumber"]
                valid_until_value = valid_until_value or mrz_result["documentValidUntil"]

        if document_type is None and document_number is None and valid_until_value is None:
            return {
                "matched": False,
                "documentType": None,
                "documentNumber": None,
                "documentValidUntil": None,
                "providerName": provider_name,
                "confidence": 0.0,
            }

        confidence = round(sum(confidence_values) / len(confidence_values), 2) if confidence_values else 0.61
        valid_until = None
        if valid_until_value:
            valid_until = date.fromisoformat(valid_until_value).isoformat()

        return {
            "matched": True,
            "documentType": document_type,
            "documentNumber": document_number,
            "documentValidUntil": valid_until,
            "providerName": provider_name,
            "confidence": confidence,
        }

    def _read_with_paddle(self, image: np.ndarray) -> tuple[str, list[float]]:
        if self._ocr is None:
            return "", []
        try:
            result = self._ocr.ocr(image, cls=True)
        except Exception:
            return "", []

        extracted_lines: list[str] = []
        confidences: list[float] = []

        if not result:
            return "", []

        for block in result:
            if not block:
                continue
            for item in block:
                if len(item) < 2:
                    continue
                text_data = item[1]
                if not isinstance(text_data, (list, tuple)) or len(text_data) < 2:
                    continue
                line_text = str(text_data[0]).strip()
                if line_text:
                    extracted_lines.append(line_text)
                try:
                    confidences.append(float(text_data[1]))
                except (TypeError, ValueError):
                    continue

        return "\n".join(extracted_lines), confidences

    def _read_text_from_image(self, image: np.ndarray) -> tuple[str, list[float], str]:
        oriented_image = self._normalize_orientation(image)
        text, confidences = self._read_with_easyocr(oriented_image)
        if text:
            return text, confidences, "easyocr"

        text, confidences = self._read_with_paddle(oriented_image)
        if text:
            return text, confidences, "paddleocr"

        return "", [], "document-ocr-fallback"

    def _read_with_easyocr(self, image: np.ndarray) -> tuple[str, list[float]]:
        reader = self._get_easyocr_reader()
        if reader is None:
            return "", []

        grayscale = self._prepare_grayscale(image)
        if grayscale is None:
            return "", []

        try:
            result = reader.readtext(grayscale, detail=1, contrast_ths=0.05, rotation_info=1)
        except Exception:
            return "", []

        extracted_lines: list[str] = []
        confidences: list[float] = []
        for _, text, confidence in result:
            line_text = str(text).strip()
            if not line_text:
                continue
            extracted_lines.append(line_text)
            try:
                confidences.append(float(confidence))
            except (TypeError, ValueError):
                continue

        return "\n".join(extracted_lines), confidences

    def _decode_document(self, data_url: str) -> DecodedDocument:
        if "," not in data_url:
            return DecodedDocument(text=data_url, image=None, source="raw-text")

        metadata, payload = data_url.split(",", 1)
        payload_bytes = self._decode_payload(metadata, payload)
        content_type = metadata.split(":", 1)[1].split(";", 1)[0] if ":" in metadata else ""

        if content_type.startswith("text/"):
            return DecodedDocument(text=payload_bytes.decode("utf-8", errors="ignore"), image=None, source="text")

        try:
            image = Image.open(io.BytesIO(payload_bytes)).convert("RGB")
            return DecodedDocument(text=None, image=np.array(image), source="image")
        except Exception:
            return DecodedDocument(text=payload_bytes.decode("utf-8", errors="ignore"), image=None, source="fallback-text")

    def _decode_payload(self, metadata: str, payload: str) -> bytes:
        if ";base64" in metadata:
            return base64.b64decode(payload)
        return unquote_to_bytes(payload)

    def _extract_document_type(self, value: str) -> str | None:
        normalized = self._normalize_text(value)
        if not normalized:
            return None
        if "PERSONALAUSWEIS" in normalized or "IDENTITY CARD" in normalized:
            return "PERSONALAUSWEIS"
        if "REISEPASS" in normalized or "PASSPORT" in normalized or normalized.startswith("P<"):
            return "REISEPASS"
        if "AUFENTHALTSTITEL" in normalized or "RESIDENCE PERMIT" in normalized:
            return "AUFENTHALTSTITEL"
        return None

    def _extract_document_number(self, value: str) -> str | None:
        if not value:
            return None
        for line in value.splitlines():
            match = DOCUMENT_NUMBER_PATTERN.search(line)
            if match:
                normalized = self._normalize_document_number_candidate(match.group(1))
                return self._clean_document_number(normalized)
        return self._extract_document_number_from_free_text(value)

    def _extract_valid_until(self, value: str) -> str | None:
        if not value:
            return None
        return self._extract_valid_until_with_keywords(value) or self._extract_valid_until_from_free_text(value)

    def _extract_from_mrz(self, value: str) -> dict[str, str] | None:
        mrz_lines = self._collect_mrz_lines(value)
        if len(mrz_lines) < 2:
            return None

        first_line = mrz_lines[0]
        second_line = mrz_lines[1]

        if not first_line.startswith("P<") or len(second_line) < 27:
            return None

        document_number = self._clean_document_number(second_line[0:9])
        expiry_raw = second_line[21:27]
        valid_until = self._normalize_mrz_date(expiry_raw)

        if document_number is None or valid_until is None:
            return None

        return {
            "documentType": "REISEPASS",
            "documentNumber": document_number,
            "documentValidUntil": valid_until,
        }

    def _normalize_text(self, value: str) -> str:
        return value.upper().replace("Ä", "AE").replace("Ö", "OE").replace("Ü", "UE").replace("ß", "SS")

    def _normalize_mrz_line(self, value: str) -> str:
        normalized = self._normalize_text(value)
        normalized = normalized.replace(" ", "").replace("-", "")
        normalized = normalized.replace("O", "0")
        normalized = normalized.replace("I", "1")
        return re.sub(r"[^A-Z0-9<]", "", normalized)

    def _collect_mrz_lines(self, value: str) -> list[str]:
        normalized_lines = [self._normalize_mrz_line(line) for line in value.splitlines()]
        normalized_lines = [line for line in normalized_lines if line]

        direct_mrz_lines = [line for line in normalized_lines if MRZ_LINE_PATTERN.fullmatch(line)]
        if len(direct_mrz_lines) >= 2 and direct_mrz_lines[0].startswith("P<"):
            return direct_mrz_lines

        if len(direct_mrz_lines) >= 2 and any(line.startswith("P<") for line in direct_mrz_lines):
            return direct_mrz_lines

        if len(direct_mrz_lines) >= 2 and len(normalized_lines) == len(direct_mrz_lines):
            return direct_mrz_lines

        merged: list[str] = []
        index = 0
        while index < len(normalized_lines):
            current = normalized_lines[index]
            next_line = normalized_lines[index + 1] if index + 1 < len(normalized_lines) else ""
            if current.startswith("P<") and next_line:
                merged.append(current + next_line)
                index += 2
                continue
            if len(current) < 30 and next_line:
                merged.append(current + next_line)
                index += 2
                continue
            merged.append(current)
            index += 1

        merged_mrz_lines = [line for line in merged if MRZ_LINE_PATTERN.fullmatch(line)]
        if len(merged_mrz_lines) >= 2:
            return merged_mrz_lines

        if len(direct_mrz_lines) >= 2:
            return direct_mrz_lines

        if len(direct_mrz_lines) >= 1:
            return direct_mrz_lines

        if len(merged_mrz_lines) >= 1:
            return merged_mrz_lines

        return []

    def _normalize_document_number_candidate(self, value: str) -> str:
        normalized = self._normalize_text(value)
        normalized = normalized.replace("<", "")
        normalized = normalized.replace("-", "")
        normalized = re.sub(r"\s+", "", normalized)
        return normalized

    def _clean_document_number(self, value: str) -> str | None:
        cleaned = re.sub(r"[^A-Z0-9]", "", value.upper().replace("<", ""))
        return cleaned or None

    def _normalize_date(self, value: str) -> str | None:
        candidate = value.strip().replace(".", "-")
        try:
            if re.fullmatch(r"\d{4}-\d{2}-\d{2}", candidate):
                return date.fromisoformat(candidate).isoformat()
            if re.fullmatch(r"\d{2}-\d{2}-\d{4}", candidate):
                day, month, year = candidate.split("-")
                return date(int(year), int(month), int(day)).isoformat()
        except ValueError:
            return None
        return None

    def _normalize_orientation(self, image: np.ndarray) -> np.ndarray:
        if cv2 is None:
            return image

        height, width = image.shape[:2]
        oriented = image
        if width > height:
            oriented = cv2.rotate(image, cv2.ROTATE_90_COUNTERCLOCKWISE)
        return oriented

    def _prepare_grayscale(self, image: np.ndarray) -> np.ndarray | None:
        if cv2 is None:
            return None

        mono = image
        if mono.ndim == 3:
            try:
                mono = cv2.cvtColor(mono, cv2.COLOR_RGB2GRAY)
            except Exception:
                try:
                    mono = cv2.cvtColor(mono, cv2.COLOR_BGR2GRAY)
                except Exception:
                    return None

        mono = np.clip(mono, 0, 255).astype(np.uint8)
        try:
            mono = cv2.equalizeHist(mono)
            mono = cv2.GaussianBlur(mono, (3, 3), 0)
        except Exception:
            pass
        return mono

    def _extract_document_number_from_free_text(self, value: str) -> str | None:
        normalized = self._normalize_text(value)
        for candidate in GENERIC_DOCUMENT_NUMBER_PATTERN.findall(normalized):
            cleaned = self._clean_document_number(candidate)
            if cleaned:
                return cleaned
        return None

    def _extract_valid_until_with_keywords(self, value: str) -> str | None:
        for line in value.splitlines():
            normalized_line = self._normalize_text(line).replace(":", " ").replace("=", " ")
            match = VALID_UNTIL_PATTERN.search(normalized_line)
            if match:
                return self._normalize_date(match.group(1))
        return None

    def _extract_valid_until_from_free_text(self, value: str) -> str | None:
        match = GENERIC_DATE_PATTERN.search(value)
        if not match:
            return None
        return self._normalize_date(match.group(1))

    def _normalize_mrz_date(self, value: str) -> str | None:
        if not re.fullmatch(r"\d{6}", value):
            return None
        year = int(value[0:2])
        month = int(value[2:4])
        day = int(value[4:6])
        full_year = 1900 + year if year >= 50 else 2000 + year
        try:
            return date(full_year, month, day).isoformat()
        except ValueError:
            return None

    @classmethod
    def _get_ocr(cls) -> Any:
        if PaddleOCR is None:
            return None
        if cls._ocr_instance is None:
            cls._ocr_instance = PaddleOCR(use_angle_cls=True, lang="en", show_log=False)
        return cls._ocr_instance

    @classmethod
    def _get_easyocr_reader(cls) -> Any:
        if easyocr is None:
            return None
        if cls._easyocr_reader is None:
            try:
                cls._easyocr_reader = easyocr.Reader(["en", "de"], gpu=False, silent=True)
            except Exception:
                return None
        return cls._easyocr_reader
