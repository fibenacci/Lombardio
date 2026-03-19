package io.lombardio.onlineauction.domain;

public interface RealtimePublisher {
    void publish(String channel, Object payload);
}
