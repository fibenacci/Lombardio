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
package io.lombardio.platform.iam.application;

import io.lombardio.platform.config.KeycloakProperties;
import io.lombardio.platform.tenant.api.TenantUserResponse;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
public class KeycloakService {

  private final Keycloak keycloak;
  private final KeycloakProperties props;

  public KeycloakService(Keycloak keycloak, KeycloakProperties props) {
    this.keycloak = keycloak;
    this.props = props;
  }

  public String createTenantGroup(String tenantId, String displayName) {
    return execute(
        "create tenant group",
        () -> {
          RealmResource realmResource = keycloak.realm(props.realm());
          GroupRepresentation existingGroup = findTenantGroupOptional(realmResource, tenantId);
          if (existingGroup != null) {
            return existingGroup.getId();
          }

          GroupRepresentation group = new GroupRepresentation();
          group.setName(tenantId);
          group.setAttributes(Map.of("displayName", Collections.singletonList(displayName)));

          Response response = realmResource.groups().add(group);

          if (response.getStatus() != 201) {
            throw new RuntimeException(
                "Failed to create Keycloak group for tenant: " + response.getStatus());
          }

          String path = response.getLocation().getPath();
          return path.substring(path.lastIndexOf('/') + 1);
        });
  }

  public TenantUserResponse createTenantUser(
      String tenantId,
      String email,
      String password,
      String displayName,
      List<String> roles,
      List<String> branchIds) {
    return execute(
        "create tenant user",
        () -> {
          UserRepresentation user = new UserRepresentation();
          user.setUsername(email);
          user.setEmail(email);
          user.setEnabled(true);
          user.setFirstName(displayName);
          user.setAttributes(userAttributes(tenantId, branchIds));

          CredentialRepresentation cred = new CredentialRepresentation();
          cred.setType(CredentialRepresentation.PASSWORD);
          cred.setValue(password);
          cred.setTemporary(false);
          user.setCredentials(Collections.singletonList(cred));

          RealmResource realmResource = keycloak.realm(props.realm());
          UsersResource usersResource = realmResource.users();

          Response response = usersResource.create(user);
          if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create Keycloak user: " + response.getStatus());
          }

          String userId =
              response
                  .getLocation()
                  .getPath()
                  .substring(response.getLocation().getPath().lastIndexOf('/') + 1);

          GroupRepresentation group = findTenantGroup(realmResource, tenantId);
          usersResource.get(userId).joinGroup(group.getId());

          if (roles != null && !roles.isEmpty()) {
            realmResource
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(
                    roles.stream()
                        .map(roleName -> realmResource.roles().get(roleName).toRepresentation())
                        .toList());
          }

          return new TenantUserResponse(
              userId,
              email,
              email,
              displayName,
              "ACTIVE",
              safeRoles(roles),
              safeBranchIds(branchIds));
        });
  }

  public List<String> getAvailableRoles() {
    return execute(
        "list available roles",
        () -> {
          RealmResource realmResource = keycloak.realm(props.realm());
          return realmResource.roles().list().stream()
              .map(role -> role.getName())
              .filter(name -> !name.startsWith("default-roles-"))
              .toList();
        });
  }

  public List<TenantUserResponse> listTenantUsers(String tenantId) {
    return execute(
        "list tenant users",
        () -> {
          RealmResource realmResource = keycloak.realm(props.realm());
          GroupRepresentation group = findTenantGroup(realmResource, tenantId);
          GroupResource groupResource = realmResource.groups().group(group.getId());

          return groupResource.members().stream()
              .map(user -> toTenantUserResponse(realmResource, user))
              .toList();
        });
  }

  public TenantUserResponse updateTenantUser(
      String tenantId,
      String userId,
      String email,
      String displayName,
      String status,
      List<String> roles,
      List<String> branchIds) {
    return execute(
        "update tenant user",
        () -> {
          RealmResource realmResource = keycloak.realm(props.realm());
          UserRepresentation user = realmResource.users().get(userId).toRepresentation();
          requireTenantMembership(user, tenantId);

          user.setUsername(email);
          user.setEmail(email);
          user.setFirstName(displayName);
          user.setEnabled(isEnabledStatus(status));
          user.setAttributes(userAttributes(tenantId, branchIds));

          realmResource.users().get(userId).update(user);
          realmResource
              .users()
              .get(userId)
              .roles()
              .realmLevel()
              .remove(realmResource.users().get(userId).roles().realmLevel().listAll());

          if (roles != null && !roles.isEmpty()) {
            realmResource
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(
                    roles.stream()
                        .map(roleName -> realmResource.roles().get(roleName).toRepresentation())
                        .toList());
          }

          return toTenantUserResponse(
              realmResource, realmResource.users().get(userId).toRepresentation());
        });
  }

  public boolean canReachAdminApi() {
    return execute(
        "check Keycloak admin connectivity",
        () -> {
          keycloak.realm(props.realm()).roles().list(0, 1);
          return true;
        });
  }

  private GroupRepresentation findTenantGroup(RealmResource realmResource, String tenantId) {
    GroupRepresentation group = findTenantGroupOptional(realmResource, tenantId);
    if (group == null) {
      throw new RuntimeException("Tenant group not found: " + tenantId);
    }
    return group;
  }

  private GroupRepresentation findTenantGroupOptional(
      RealmResource realmResource, String tenantId) {
    return realmResource.groups().groups(tenantId, 0, 1).stream()
        .filter(g -> g.getName().equals(tenantId))
        .findFirst()
        .orElse(null);
  }

  private void requireTenantMembership(UserRepresentation user, String tenantId) {
    List<String> userTenants =
        user.getAttributes() == null
            ? Collections.emptyList()
            : user.getAttributes().get("tenantId");
    if (userTenants == null || userTenants.stream().noneMatch(tenantId::equals)) {
      throw new RuntimeException("User does not belong to tenant: " + tenantId);
    }
  }

  private TenantUserResponse toTenantUserResponse(
      RealmResource realmResource, UserRepresentation user) {
    List<String> roleIds =
        realmResource.users().get(user.getId()).roles().realmLevel().listAll().stream()
            .map(RoleRepresentation::getName)
            .filter(Objects::nonNull)
            .filter(name -> !name.startsWith("default-roles-"))
            .toList();

    return new TenantUserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName() == null || user.getFirstName().isBlank()
            ? user.getUsername()
            : user.getFirstName(),
        user.isEnabled() ? "ACTIVE" : "INACTIVE",
        roleIds,
        safeBranchIds(
            user.getAttributes() == null
                ? Collections.emptyList()
                : user.getAttributes().get("branchIds")));
  }

  private List<String> safeRoles(List<String> roles) {
    return roles == null ? Collections.emptyList() : roles;
  }

  private List<String> safeBranchIds(List<String> branchIds) {
    return branchIds == null ? Collections.emptyList() : branchIds;
  }

  private boolean isEnabledStatus(String status) {
    return status == null || !"INACTIVE".equals(status);
  }

  private Map<String, List<String>> userAttributes(String tenantId, List<String> branchIds) {
    return Map.of(
        "tenantId", Collections.singletonList(tenantId),
        "branchIds", safeBranchIds(branchIds));
  }

  private <T> T execute(String action, KeycloakOperation<T> operation) {
    try {
      return operation.run();
    } catch (NotAuthorizedException | ProcessingException exception) {
      throw new IdentityProviderUnavailableException(
          "Keycloak admin access failed while attempting to " + action, exception);
    }
  }

  @FunctionalInterface
  private interface KeycloakOperation<T> {
    T run();
  }
}
