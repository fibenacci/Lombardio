package io.lombardio.onlineauction.domain;

public interface RealtimeSessionTokenService {
    RealtimeSession createSession(String subject, String channel);
}
