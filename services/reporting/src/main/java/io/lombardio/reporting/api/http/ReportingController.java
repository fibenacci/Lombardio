package io.lombardio.reporting.api.http;

import io.lombardio.reporting.application.service.ReportingService;
import io.lombardio.reporting.infrastructure.security.AuthenticatedReportingUser;
import io.lombardio.reporting.infrastructure.security.ReportingAuthorizationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/tenants/{tenantId}")
public class ReportingController {

    private final ReportingService reportingService;
    private final ReportingAuthorizationService authorizationService;

    public ReportingController(
            ReportingService reportingService,
            ReportingAuthorizationService authorizationService
    ) {
        this.reportingService = reportingService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/reporting/dashboard")
    public ReportingDashboardResponse dashboard(
            @AuthenticationPrincipal AuthenticatedReportingUser principal,
            @PathVariable String tenantId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(name = "rangeDays", defaultValue = "14") @Min(7) @Max(90) int rangeDays
    ) {
        authorizationService.requireRead(principal, tenantId);
        return reportingService.getDashboard(tenantId, rangeDays, extractBearerToken(authorization));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Bearer token required");
        }

        return authorization.substring(7);
    }
}
