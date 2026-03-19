package io.lombardio.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataBranchRepository extends JpaRepository<BranchEntity, String> {

    List<BranchEntity> findAllByOrderByDisplayNameAsc();

    Optional<BranchEntity> findFirstByTenantIdAndKey(String tenantId, String key);
}
