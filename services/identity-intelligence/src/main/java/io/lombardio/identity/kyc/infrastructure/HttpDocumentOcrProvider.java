/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.identity.kyc.infrastructure;

import io.lombardio.identity.kyc.domain.DocumentOcrProvider;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Advanced implementation of DocumentOcrProvider using Regula Forensics Web API. Patterns and
 * models aligned with the official JS Client and OpenAPI. Supports Visual, MRZ, Barcode and
 * Graphics results.
 */
@Component
public class HttpDocumentOcrProvider implements DocumentOcrProvider {

  private static final Logger log = LoggerFactory.getLogger(HttpDocumentOcrProvider.class);
  private final RestClient restClient;

  public HttpDocumentOcrProvider(
      RestClient.Builder restClientBuilder,
      @Value("${document-ocr.base-url:http://localhost:8087}") String baseUrl) {
    log.info("[OCR] Initializing HttpDocumentOcrProvider with base URL: {}", baseUrl);
    this.restClient = restClientBuilder.baseUrl(baseUrl).build();
  }

  @Override
  public Optional<DocumentOcrResult> prefill(
      String tenantId, String frontImageDataUrl, String backImageDataUrl) {
    try {
      ProcessRequest request = buildRequest(frontImageDataUrl, backImageDataUrl);
      log.info("[OCR] Sending request to Regula for tenant {}", tenantId);

      ProcessResponse response =
          restClient
              .post()
              .uri("/api/process")
              .body(request)
              .retrieve()
              .body(ProcessResponse.class);

      if (response.ContainerList().List() == null) {
        log.warn("[OCR] Regula response is empty or invalid");
        return Optional.empty();
      }

      log.info(
          "[OCR] Received response from Regula with {} containers",
          response.ContainerList().List().size());

      return mapResponse(response);
    } catch (RestClientException exception) {
      log.error("[OCR] Failed to call Regula API", exception);
      return Optional.empty();
    }
  }

  private ProcessRequest buildRequest(String frontUrl, String backUrl) {
    List<ProcessRequestImage> images = new ArrayList<>();
    if (frontUrl != null && frontUrl.contains(",")) {
      images.add(new ProcessRequestImage(frontUrl.split(",")[1], 6, 0)); // 6=White, 0=Front
    }

    if (backUrl != null && backUrl.contains(",")) {
      images.add(new ProcessRequestImage(backUrl.split(",")[1], 6, 1)); // 6=White, 1=Back
    }

    return new ProcessRequest(new ProcessParams("FullProcess", true), images);
  }

  private Optional<DocumentOcrResult> mapResponse(ProcessResponse response) {
    String firstName = null;
    String lastName = null;
    String birthDateStr = null;
    String docNumber = null;
    String expiryStr = null;
    String docType = "UNKNOWN";
    String portraitBase64 = null;
    double confidence = 0.0;

    for (ResultItem item : response.ContainerList().List()) {
      if (item.Text() != null && item.Text().fieldList() != null) {
        log.info(
            "[OCR] Parsing text container type: {} with {} fields",
            item.result_type(),
            item.Text().fieldList().size());
        for (TextField field : item.Text().fieldList()) {
          String value = getBestValue(field);
          if (value == null) continue;

          int type = field.fieldType();
          double fieldConf = getBestConfidence(field);

          // Map based on Regula eVisualFieldType
          switch (type) {
            case 1 -> lastName = coalesce(lastName, value);
            case 2 -> firstName = coalesce(firstName, value);
            case 3 -> birthDateStr = coalesce(birthDateStr, value);
            case 5 -> expiryStr = coalesce(expiryStr, value);
            case 9 -> {
              docNumber = coalesce(docNumber, value);
              confidence = Math.max(confidence, fieldConf);
            }
            case 11 -> docType = coalesce(docType, value);
          }
        }
      }

      if (item.Graphics() != null && item.Graphics().fieldList() != null) {
        log.info(
            "[OCR] Parsing graphics container type: {} with {} fields",
            item.result_type(),
            item.Graphics().fieldList().size());
        for (GraphicField field : item.Graphics().fieldList()) {
          if (field.fieldType() == 201 && field.value() != null) {
            log.info("[OCR] Found portrait image");
            portraitBase64 = "data:image/jpeg;base64," + field.value();
          }
        }
      }
    }

    if (docNumber == null && lastName == null && firstName == null) {
      log.warn("[OCR] No mapping-relevant identity data found in response");

      return Optional.empty();
    }

    log.info("[OCR] Success: Extracted {} {} (Doc: {})", firstName, lastName, docNumber);

    return Optional.of(
        new DocumentOcrResult(
            firstName,
            lastName,
            parseDate(birthDateStr),
            docType,
            docNumber,
            parseDate(expiryStr),
            portraitBase64,
            "Regula Forensics",
            confidence > 0 ? confidence : 0.8));
  }

  private String coalesce(String existing, String newValue) {
    return (existing == null || existing.isBlank()) ? newValue : existing;
  }

  private String getBestValue(TextField field) {
    if (field.values() == null || field.values().isEmpty()) return null;
    return field.values().get(0).value();
  }

  private double getBestConfidence(TextField field) {
    if (field.values() == null || field.values().isEmpty()) return 0.0;
    return field.values().get(0).confidence() / 100.0;
  }

  private LocalDate parseDate(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) return null;

    String cleaned = dateStr.replaceAll("[^0-9./-]", "");
    List<String> formats =
        List.of("dd.MM.yyyy", "yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy", "yyMMdd");

    for (String format : formats) {
      try {
        return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern(format));
      } catch (Exception ignored) {
      }
    }

    log.warn("[OCR] Could not parse date: {}", dateStr);
    return null;
  }

  // --- Official Regula OpenAPI Models ---

  private record ProcessRequest(ProcessParams processParam, List<ProcessRequestImage> List) {}

  private record ProcessParams(String scenario, boolean doublePageSpread) {}

  private record ProcessRequestImage(String ImageData, int light, int page_idx) {}

  private record ProcessResponse(ContainerList ContainerList) {}

  private record ContainerList(List<ResultItem> List) {}

  private record ResultItem(int result_type, TextFields Text, GraphicsFields Graphics) {}

  private record TextFields(List<TextField> fieldList) {}

  private record TextField(String fieldName, int fieldType, List<StringResultValue> values) {}

  private record StringResultValue(String value, int sourceType, int confidence) {}

  private record GraphicsFields(List<GraphicField> fieldList) {}

  private record GraphicField(String fieldName, int fieldType, String value) {}
}
