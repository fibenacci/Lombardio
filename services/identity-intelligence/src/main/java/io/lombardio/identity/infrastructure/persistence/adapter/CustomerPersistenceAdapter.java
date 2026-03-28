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
package io.lombardio.identity.infrastructure.persistence.adapter;

import io.lombardio.identity.domain.model.Customer;
import io.lombardio.identity.domain.port.CustomerRepository;
import io.lombardio.identity.infrastructure.persistence.entity.CustomerEntity;
import io.lombardio.identity.infrastructure.persistence.repository.SpringDataCustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerPersistenceAdapter implements CustomerRepository {

  private final SpringDataCustomerRepository repository;

  public CustomerPersistenceAdapter(SpringDataCustomerRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Customer> search(String tenantId, String query) {
    String normalized = query == null ? "" : query.trim();
    return repository.search(tenantId, normalized).stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<Customer> findById(String id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Customer> findByEmail(String email) {
    return repository.findByEmailIgnoreCase(email).map(this::toDomain);
  }

  @Override
  public Customer save(Customer customer) {
    return toDomain(repository.save(toEntity(customer)));
  }

  private CustomerEntity toEntity(Customer customer) {
    CustomerEntity entity = new CustomerEntity();
    entity.setId(customer.id());
    entity.setTenantId(customer.tenantId());
    entity.setCustomerNumber(customer.customerNumber());
    entity.setFirstName(customer.firstName());
    entity.setLastName(customer.lastName());
    entity.setBirthDate(customer.birthDate());
    entity.setPhone(customer.phone());
    entity.setEmail(customer.email());
    entity.setWantsDigitalPawnTicket(customer.wantsDigitalPawnTicket());
    entity.setOnlineAccessStatus(customer.onlineAccessStatus());
    entity.setStreet(customer.street());
    entity.setPostalCode(customer.postalCode());
    entity.setCity(customer.city());
    return entity;
  }

  private Customer toDomain(CustomerEntity entity) {
    return new Customer(
        entity.getId(),
        entity.getTenantId(),
        entity.getCustomerNumber(),
        entity.getFirstName(),
        entity.getLastName(),
        entity.getBirthDate(),
        entity.getPhone(),
        entity.getEmail(),
        entity.isWantsDigitalPawnTicket(),
        entity.getOnlineAccessStatus(),
        entity.getStreet(),
        entity.getPostalCode(),
        entity.getCity());
  }
}
