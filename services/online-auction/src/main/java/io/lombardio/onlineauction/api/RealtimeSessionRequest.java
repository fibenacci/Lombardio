package io.lombardio.onlineauction.api;

import jakarta.validation.constraints.NotBlank;

public record RealtimeSessionRequest(@NotBlank String accessToken) {
}
