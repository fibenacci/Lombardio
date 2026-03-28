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
package io.lombardio.identity.infrastructure.persistence.repository;

import io.lombardio.identity.infrastructure.persistence.entity.CustomerEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, String> {

  @Query(
      """
    select customer
    from CustomerEntity customer
    where customer.tenantId = :tenantId
      and (
          :query = ''
          or lower(customer.customerNumber) like lower(concat('%', :query, '%'))
          or lower(concat(customer.firstName, ' ', customer.lastName)) like lower(concat('%', :query, '%'))
          or lower(customer.phone) like lower(concat('%', :query, '%'))
      )
    order by customer.customerNumber
    """)
  List<CustomerEntity> search(String tenantId, String query);

  Optional<CustomerEntity> findByEmailIgnoreCase(String email);
}
