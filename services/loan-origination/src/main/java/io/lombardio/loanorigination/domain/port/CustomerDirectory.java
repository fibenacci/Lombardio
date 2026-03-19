package io.lombardio.loanorigination.domain.port;

import io.lombardio.loanorigination.domain.model.CustomerProfile;

public interface CustomerDirectory {

    CustomerProfile requireById(String tenantId, String customerId);
}
