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
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
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
    GroupRepresentation group = new GroupRepresentation();
    group.setName(tenantId);
    group.setAttributes(Map.of("displayName", Collections.singletonList(displayName)));

    RealmResource realmResource = keycloak.realm(props.realm());
    Response response = realmResource.groups().add(group);

    if (response.getStatus() != 201) {
      throw new RuntimeException(
          "Failed to create Keycloak group for tenant: " + response.getStatus());
    }

    // Get created group ID
    String path = response.getLocation().getPath();
    return path.substring(path.lastIndexOf('/') + 1);
  }

  public String createTenantUser(
      String tenantId, String email, String password, List<String> roles) {
    UserRepresentation user = new UserRepresentation();
    user.setUsername(email);
    user.setEmail(email);
    user.setEnabled(true);
    user.setAttributes(Map.of("tenantId", Collections.singletonList(tenantId)));

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

    // Assign to group
    GroupRepresentation group =
        realmResource.groups().groups(tenantId, 0, 1).stream()
            .filter(g -> g.getName().equals(tenantId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Tenant group not found: " + tenantId));

    usersResource.get(userId).joinGroup(group.getId());

    // Assign roles
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

    return userId;
  }

  public List<String> getAvailableRoles() {
    RealmResource realmResource = keycloak.realm(props.realm());
    return realmResource.roles().list().stream()
        .map(role -> role.getName())
        .filter(
            name ->
                !name.startsWith("default-roles-")) // Filter out Keycloak default roles if desired
        .toList();
  }
}
