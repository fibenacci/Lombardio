package io.lombardio.loanorigination.domain.port;

public interface KycDirectory {

    boolean isApproved(String tenantId, String customerId);
}
