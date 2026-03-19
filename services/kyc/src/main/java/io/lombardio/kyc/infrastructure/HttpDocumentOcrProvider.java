package io.lombardio.kyc.infrastructure;

import io.lombardio.kyc.domain.DocumentOcrProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class HttpDocumentOcrProvider implements DocumentOcrProvider {

    private final RestClient restClient;

    public HttpDocumentOcrProvider(
            RestClient.Builder restClientBuilder,
            @Value("${document-ocr.base-url:http://localhost:8087}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Optional<DocumentOcrResult> prefill(String tenantId, String frontImageDataUrl, String backImageDataUrl) {
        try {
            OcrPrefillResponse response = restClient.post()
                    .uri("/api/v1/ocr/prefill")
                    .body(new OcrPrefillRequest(tenantId, frontImageDataUrl, backImageDataUrl))
                    .retrieve()
                    .body(OcrPrefillResponse.class);

            if (response == null || !response.matched()) {
                return Optional.empty();
            }

            return Optional.of(new DocumentOcrResult(
                    response.documentType(),
                    response.documentNumber(),
                    response.documentValidUntil(),
                    response.providerName(),
                    response.confidence()
            ));
        } catch (RestClientException exception) {
            return Optional.empty();
        }
    }

    private record OcrPrefillRequest(
            String tenantId,
            String documentFrontImageDataUrl,
            String documentBackImageDataUrl
    ) {
    }

    private record OcrPrefillResponse(
            boolean matched,
            String documentType,
            String documentNumber,
            LocalDate documentValidUntil,
            String providerName,
            double confidence
    ) {
    }
}
