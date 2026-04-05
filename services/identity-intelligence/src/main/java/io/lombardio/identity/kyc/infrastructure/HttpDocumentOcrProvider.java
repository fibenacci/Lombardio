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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.lombardio.identity.kyc.domain.DocumentOcrProvider;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
      @Value("${regula.base-url:${document-ocr.base-url:http://localhost:8087}}") String baseUrl) {
    String sanitizedBaseUrl = sanitizeForLogs(baseUrl);
    log.info("[OCR] Initializing HttpDocumentOcrProvider with base URL: {}", sanitizedBaseUrl);
    this.restClient = restClientBuilder.baseUrl(baseUrl).build();
  }

  @Override
  public Optional<DocumentOcrResult> prefill(
      String tenantId, String frontImageDataUrl, String backImageDataUrl) {
    String sanitizedTenantId = sanitizeForLogs(tenantId);
    try {
      ProcessRequest request = buildRequest(frontImageDataUrl, backImageDataUrl);
      log.info("[OCR] Sending request to Regula for tenant {}", sanitizedTenantId);

      ProcessResponse response =
          restClient
              .post()
              .uri("/api/process")
              .body(request)
              .retrieve()
              .body(ProcessResponse.class);

      if (response == null
          || response.containerList() == null
          || response.containerList().list() == null) {
        log.warn("[OCR] Regula response is empty or invalid");
        return Optional.empty();
      }

      log.info(
          "[OCR] Received response from Regula with {} containers",
          response.containerList().list().size());

      return mapResponse(response);
    } catch (RestClientException exception) {
      log.error("[OCR] Failed to call Regula API", exception);
      return Optional.empty();
    }
  }

  private ProcessRequest buildRequest(String frontUrl, String backUrl) {
    List<ProcessRequestItem> images = new ArrayList<>();
    if (frontUrl != null && frontUrl.contains(",")) {
      images.add(
          new ProcessRequestItem(
              new ProcessRequestImageData(frontUrl.split(",", 2)[1]), 6, 0)); // 6=White, 0=Front
    }

    if (backUrl != null && backUrl.contains(",")) {
      images.add(
          new ProcessRequestItem(
              new ProcessRequestImageData(backUrl.split(",", 2)[1]), 6, 1)); // 6=White, 1=Back
    }

    return new ProcessRequest(new ProcessParams("FullProcess", new AuthParams(false)), images);
  }

  private Optional<DocumentOcrResult> mapResponse(ProcessResponse response) {
    String firstName = null;
    String lastName = null;
    String birthDateStr = null;
    String docNumber = null;
    String expiryStr = null;
    String docType = "UNKNOWN";
    String candidateDocumentName = null;
    String portraitBase64 = null;
    double confidence = 0.0;

    for (ResultItem item : response.containerList().list()) {
      if (item.text() != null && item.text().fieldList() != null) {
        log.info(
            "[OCR] Parsing text container type: {} with {} fields",
            item.result_type(),
            item.text().fieldList().size());
        for (TextField field : item.text().fieldList()) {
          String value = getBestValue(field);
          if (value == null) continue;

          int type = field.fieldType();
          String normalizedFieldName = normalizeFieldName(field.fieldName());
          double fieldConf = getBestConfidence(field);

          // Map based on Regula TextFieldType enum
          switch (type) {
            case 2, 27, 191 -> {
              docNumber = coalesce(docNumber, value);
              confidence = Math.max(confidence, fieldConf);
            }
            case 3, 91, 102, 251, 339, 637 -> expiryStr = coalesce(expiryStr, value);
            case 5, 110, 592 -> birthDateStr = coalesce(birthDateStr, value);
            case 8 -> lastName = coalesce(lastName, value);
            case 9, 645 -> firstName = coalesce(firstName, value);
            case 25 -> {
              if (lastName == null || firstName == null) {
                String[] parts = value.split("<<|<|,\\s*", 2);
                if (parts.length > 0) {
                  lastName = coalesce(lastName, parts[0].replace("<", " ").trim());
                }
                if (parts.length > 1) {
                  firstName = coalesce(firstName, parts[1].replace("<", " ").trim());
                }
              }
            }
            case 0, 37 -> docType = coalesce(docType, value);
            default -> {
              // Ignore other fields
            }
          }

          if (matchesAny(normalizedFieldName, "surname", "lastname", "familyname", "nachname")) {
            lastName = coalesce(lastName, value);
          }
          if (matchesAny(
              normalizedFieldName, "firstname", "givenname", "name", "vorname", "givennames")) {
            firstName = coalesce(firstName, value);
          }
          if (matchesAny(normalizedFieldName, "birthdate", "dateofbirth", "geburtsdatum")) {
            birthDateStr = coalesce(birthDateStr, value);
          }
          if (matchesAny(
              normalizedFieldName,
              "documentnumber",
              "docnumber",
              "idnumber",
              "documentno",
              "ausweisnummer")) {
            docNumber = coalesce(docNumber, value);
            confidence = Math.max(confidence, fieldConf);
          }
          if (matchesAny(
              normalizedFieldName,
              "expirydate",
              "dateofexpiry",
              "validuntil",
              "ablaufdatum",
              "gueltigbis")) {
            expiryStr = coalesce(expiryStr, value);
          }
          if (matchesAny(
              normalizedFieldName,
              "documentclasscode",
              "documentclassname",
              "documenttype",
              "doctype")) {
            docType = coalesce(docType, value);
          }
        }
      }

      ImageFields imageFields = getImageFields(item);
      if (imageFields != null && imageFields.fieldList() != null) {
        log.info(
            "[OCR] Parsing image container type: {} with {} fields",
            item.result_type(),
            imageFields.fieldList().size());
        for (ImageField field : imageFields.fieldList()) {
          String value = getBestValue(field);
          if (field.fieldType() == 201 && value != null) {
            log.info("[OCR] Found portrait image");
            portraitBase64 = "data:image/jpeg;base64," + value;
          }
        }
      }

      if (item.oneCandidate() != null && item.oneCandidate().documentName() != null) {
        candidateDocumentName = coalesce(candidateDocumentName, item.oneCandidate().documentName());
      }
    }

    if (docNumber == null && lastName == null && firstName == null) {
      log.warn("[OCR] No mapping-relevant identity data found in response");

      return Optional.empty();
    }

    String sanitizedFirstName = sanitizeForLogs(firstName);
    String sanitizedLastName = sanitizeForLogs(lastName);
    String sanitizedDocNumber = sanitizeForLogs(docNumber);
    log.info(
        "[OCR] Success: Extracted {} {} (Doc: {})",
        sanitizedFirstName,
        sanitizedLastName,
        sanitizedDocNumber);

    return Optional.of(
        new DocumentOcrResult(
            firstName,
            lastName,
            parseDate(birthDateStr),
            normalizeDocumentType(
                candidateDocumentName != null && !candidateDocumentName.isBlank()
                    ? candidateDocumentName
                    : docType),
            docNumber,
            parseDate(expiryStr),
            portraitBase64,
            "Regula Forensics",
            confidence > 0 ? confidence : 0.8));
  }

  private String coalesce(String existing, String newValue) {
    return (existing == null || existing.isBlank()) ? newValue : existing;
  }

  private String normalizeFieldName(String fieldName) {
    if (fieldName == null) {
      return "";
    }
    return fieldName.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
  }

  private boolean matchesAny(String value, String... candidates) {
    for (String candidate : candidates) {
      if (value.equals(candidate)) {
        return true;
      }
    }
    return false;
  }

  private String normalizeDocumentType(String documentType) {
    if (documentType == null || documentType.isBlank()) {
      return "PERSONALAUSWEIS";
    }

    String normalized = documentType.trim().toUpperCase(Locale.ROOT);
    if (normalized.contains("PASSPORT") || normalized.contains("REISEPASS")) {
      return "REISEPASS";
    }
    if (normalized.contains("RESIDENCE")
        || normalized.contains("PERMIT")
        || normalized.contains("AUFENTHALT")) {
      return "AUFENTHALTSTITEL";
    }
    if (normalized.contains("ID")
        || normalized.contains("IDENTITY")
        || normalized.contains("PERSONALAUSWEIS")) {
      return "PERSONALAUSWEIS";
    }
    return "PERSONALAUSWEIS";
  }

  private String getBestValue(TextField field) {
    if (field.value() != null && !field.value().isBlank()) {
      return field.value();
    }
    if (field.values() == null || field.values().isEmpty()) return null;
    return field.values().get(0).value();
  }

  private double getBestConfidence(TextField field) {
    if (field.values() == null || field.values().isEmpty()) return 0.0;
    return field.values().get(0).confidence() / 100.0;
  }

  private String getBestValue(ImageField field) {
    if (field.value() != null && !field.value().isBlank()) {
      return field.value();
    }
    if (field.values() == null || field.values().isEmpty()) return null;
    return field.values().get(0).value();
  }

  private ImageFields getImageFields(ResultItem item) {
    if (item.images() != null) {
      return item.images();
    }
    return item.graphics();
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

    String sanitizedDate = sanitizeForLogs(dateStr);
    log.warn("[OCR] Could not parse date: {}", sanitizedDate);
    return null;
  }

  private String sanitizeForLogs(String input) {
    if (input == null) {
      return "null";
    }
    return input.replace('\r', '_').replace('\n', '_').replace('\t', '_');
  }

  // --- Official Regula OpenAPI Models ---

  private record ProcessRequest(
      @JsonProperty("processParam") ProcessParams processParam,
      @JsonProperty("List") List<ProcessRequestItem> list) {
    public ProcessRequest {
      list = List.copyOf(list != null ? list : List.of());
    }
  }

  private record ProcessParams(
      @JsonProperty("scenario") String scenario,
      @JsonProperty("authParams") AuthParams authParams) {}

  private record AuthParams(@JsonProperty("checkLiveness") boolean checkLiveness) {}

  private record ProcessRequestItem(
      @JsonProperty("ImageData") ProcessRequestImageData imageData,
      @JsonProperty("light") int light,
      @JsonProperty("page_idx") int pageIdx) {}

  private record ProcessRequestImageData(@JsonProperty("image") String image) {}

  private record ProcessResponse(@JsonProperty("ContainerList") ContainerList containerList) {}

  private record ContainerList(@JsonProperty("List") List<ResultItem> list) {
    public ContainerList {
      list = List.copyOf(list != null ? list : List.of());
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ResultItem(
      @JsonProperty("result_type") int result_type,
      @JsonProperty("Text") TextFields text,
      @JsonProperty("Images") ImageFields images,
      @JsonProperty("Graphics") ImageFields graphics,
      @JsonProperty("OneCandidate") OneCandidate oneCandidate) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record TextFields(@JsonProperty("fieldList") List<TextField> fieldList) {
    public TextFields {
      fieldList = List.copyOf(fieldList != null ? fieldList : List.of());
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record TextField(
      @JsonProperty("fieldName") String fieldName,
      @JsonProperty("fieldType") int fieldType,
      @JsonProperty("value") String value,
      @JsonAlias("valueList") @JsonProperty("valueList") List<StringResultValue> values) {
    public TextField {
      values = List.copyOf(values != null ? values : List.of());
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record StringResultValue(
      @JsonProperty("value") String value,
      @JsonProperty("sourceType") int sourceType,
      @JsonAlias({"confidence", "probability"}) @JsonProperty("confidence") int confidence) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ImageFields(@JsonProperty("fieldList") List<ImageField> fieldList) {
    public ImageFields {
      fieldList = List.copyOf(fieldList != null ? fieldList : List.of());
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ImageField(
      @JsonProperty("fieldName") String fieldName,
      @JsonProperty("fieldType") int fieldType,
      @JsonProperty("value") String value,
      @JsonAlias("valueList") @JsonProperty("valueList") List<StringResultValue> values) {
    public ImageField {
      values = List.copyOf(values != null ? values : List.of());
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record OneCandidate(@JsonProperty("DocumentName") String documentName) {}
}
