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
package io.lombardio.identity.integration;

import static org.junit.jupiter.api.Assertions.*;

import io.lombardio.identity.aml.api.http.AmlStatusResponse;
import io.lombardio.identity.aml.api.http.OriginationAssessmentRequest;
import io.lombardio.identity.aml.application.service.AmlService;
import io.lombardio.identity.api.http.CreateCustomerRequest;
import io.lombardio.identity.api.http.CustomerResponse;
import io.lombardio.identity.application.service.CustomerService;
import io.lombardio.identity.kyc.api.UpdateKycStatusRequest;
import io.lombardio.identity.kyc.application.KycService;
import io.lombardio.identity.kyc.domain.KycStatus;
import io.lombardio.identity.kyc.domain.KycVerificationMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class IdentityIntelligencePipelineIntegrationTest {

  @Autowired private CustomerService customerService;

  @Autowired private KycService kycService;

  @Autowired private AmlService amlService;

  @Test
  public void testFullIdentityPipeline() {
    String tenantId = "test-tenant";

    // 1. Create Customer (Stammdaten)
    CreateCustomerRequest createRequest =
        new CreateCustomerRequest(
            "C12345",
            "John",
            "Doe",
            LocalDate.of(1980, 1, 1),
            "+49123456789",
            "john.doe@example.com",
            false,
            "Musterstraße 1",
            "12345",
            "Musterstadt");
    CustomerResponse customer = customerService.create(tenantId, createRequest);
    assertNotNull(customer.id());
    assertEquals("NOT_STARTED", customer.kycStatus());

    // 2. Simulate KYC Process (Verifizierung)
    UpdateKycStatusRequest kycRequest =
        new UpdateKycStatusRequest(
            KycStatus.APPROVED,
            KycVerificationMode.MANUAL,
            LocalDate.now().plusYears(1),
            "PERSONALAUSWEIS",
            "123456789",
            LocalDate.now().plusYears(5),
            "data:image/png;base64,front",
            "data:image/png;base64,back",
            "Manual approval",
            null,
            null,
            null);
    kycService.updateStatus(tenantId, customer.id(), kycRequest);

    // Check if Customer Profile now reflects the KYC status (In-JVM integration)
    CustomerResponse updatedCustomer = customerService.requireById(tenantId, customer.id());
    assertEquals("APPROVED", updatedCustomer.kycStatus());
    assertTrue(updatedCustomer.kycApproved());

    // 3. Simulate AML Check (Risiko-Prüfung)
    OriginationAssessmentRequest amlRequest =
        new OriginationAssessmentRequest(new BigDecimal("500.00"));
    AmlStatusResponse amlResponse =
        amlService.assessForOrigination(tenantId, customer.id(), amlRequest);
    assertNotNull(amlResponse);
    // Default might be allowed if no flags are present and loan amount is low
    assertTrue(amlResponse.originationAllowed());
  }
}
