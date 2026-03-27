package io.lombardio.identity.infrastructure.persistence.repository;

import io.lombardio.identity.infrastructure.persistence.entity.CustomerEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, String> {

    @Query("""
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
