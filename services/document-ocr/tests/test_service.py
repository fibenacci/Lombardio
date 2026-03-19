from app.service import DocumentOcrService


def test_prefill_extracts_structured_document_fields_from_text_payload() -> None:
    service = DocumentOcrService()

    result = service.prefill(
        tenant_id="tenant-default",
        front_image_data_url="data:text/plain;base64,UEVSU09OQUxBVVNXRUlTCkRPQ05POiBYSzEyMzQ1Njc=",
        back_image_data_url="data:text/plain;base64,R1VFTFRJR19CSVM6IDIwMzEtMDMtMTg=",
    )

    assert result["matched"] is True
    assert result["documentType"] == "PERSONALAUSWEIS"
    assert result["documentNumber"] == "XK1234567"
    assert result["documentValidUntil"] == "2031-03-18"


def test_prefill_extracts_german_labelled_fields() -> None:
    service = DocumentOcrService()

    result = service.prefill(
        tenant_id="tenant-default",
        front_image_data_url=(
            "data:text/plain;base64,"
            "QnVuZGVzcmVwdWJsaWsgRGV1dHNjaGxhbmQKUGVyc29uYWxhdXN3ZWlzCkF1c3dlaXNudW1tZXI6IEMwMUpYNzU4OQ=="
        ),
        back_image_data_url="data:text/plain;base64,R3VlbHRpZyBiaXM6IDE4LjAzLjIwMzE=",
    )

    assert result["matched"] is True
    assert result["documentType"] == "PERSONALAUSWEIS"
    assert result["documentNumber"] == "C01JX7589"
    assert result["documentValidUntil"] == "2031-03-18"


def test_prefill_extracts_passport_data_from_mrz() -> None:
    service = DocumentOcrService()

    result = service.prefill(
        tenant_id="tenant-default",
        front_image_data_url=(
            "data:text/plain;base64,"
            "UDxVVE9FUklLU1NPTjw8QU5OQTxNQVJJQTw8PDw8PDw8PDw8PDw8PDw8PDw8PDw8PDw8Ckw4OTg5MDJDMzZVVE83NDA4MTIyRjEyMDQxNTlaRTE4NDIyNkI8PDw8PDEw"
        ),
        back_image_data_url="data:text/plain;base64,",
    )

    assert result["matched"] is True
    assert result["documentType"] == "REISEPASS"
    assert result["documentNumber"] == "L898902C3"
    assert result["documentValidUntil"] == "2012-04-15"


def test_prefill_tolerates_common_ocr_noise_in_document_fields() -> None:
    service = DocumentOcrService()

    result = service.prefill(
        tenant_id="tenant-default",
        front_image_data_url=(
            "data:text/plain;base64,"
            "UGVyc29uYWxhdXN3ZWlzCkF1c3dlaXNudW1tZXI6IEMwMSBYIDc1IDg5"
        ),
        back_image_data_url="data:text/plain;base64,R8O8bHRpZy1CaXM6IDE4LTAzLTIwMzE=",
    )

    assert result["matched"] is True
    assert result["documentType"] == "PERSONALAUSWEIS"
    assert result["documentNumber"] == "C01X7589"
    assert result["documentValidUntil"] == "2031-03-18"


def test_prefill_merges_split_mrz_lines_and_normalizes_common_mrz_confusions() -> None:
    service = DocumentOcrService()

    result = service.prefill(
        tenant_id="tenant-default",
        front_image_data_url=(
            "data:text/plain;base64,"
            "UDxVVE8KRVJJS1NTT048PEFOTkE8TUFSSUE8PDw8PDw8PDw8PDw8PDw8PDw8PDw8PDw8PDw8Ckw4OTg5TzJDMwo2VVRPNzQwODEyMkYxMjA0MTU5WkUxODQyMjZCPDw8PDwxMA=="
        ),
        back_image_data_url="data:text/plain;base64,",
    )

    assert result["matched"] is True
    assert result["documentType"] == "REISEPASS"
    assert result["documentNumber"] == "L898902C3"
    assert result["documentValidUntil"] == "2012-04-15"
