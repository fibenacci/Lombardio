package io.lombardio.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataBranchRepository extends JpaRepository<BranchEntity, String> {

    List<BranchEntity> findAllByOrderByDisplayNameAsc();
}
