package io.lombardio.platform.integration.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IntegrationOutboxEventRepository {

    IntegrationOutboxEvent save(IntegrationOutboxEvent event);

    Optional<IntegrationOutboxEvent> findById(String id);

    List<IntegrationOutboxEvent> findClaimable(Instant now, int limit);
}
