package io.lombardio.onlineauction.api;

public record RealtimeSessionResponse(
        String wsUrl,
        String channel,
        String connectionToken,
        String subscriptionToken
) {
}
