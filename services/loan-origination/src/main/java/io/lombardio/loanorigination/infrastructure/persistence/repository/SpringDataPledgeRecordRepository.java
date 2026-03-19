package io.lombardio.loanorigination.infrastructure.persistence.repository;

import io.lombardio.loanorigination.infrastructure.persistence.entity.PledgeRecordEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPledgeRecordRepository extends JpaRepository<PledgeRecordEntity, String> {
}
