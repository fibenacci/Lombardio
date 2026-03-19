package io.lombardio.identityaccess.access.domain;

import java.util.List;
import java.util.Optional;

public interface BranchRepository {

    List<Branch> findAll();

    Optional<Branch> findById(String id);

    Branch save(Branch branch);
}
