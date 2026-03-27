package io.lombardio.identity.portal.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerPortalAcceptInvitationRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 120) String password
) {
}
