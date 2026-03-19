package io.lombardio.onlineauction.domain;

public record RealtimeSession(
        String wsUrl,
        String channel,
        String connectionToken,
        String subscriptionToken
) {
}
