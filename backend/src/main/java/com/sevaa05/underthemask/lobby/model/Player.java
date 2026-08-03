package com.sevaa05.underthemask.lobby.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Player {

    private final UUID id;
    private final String name;
    private final String reconnectToken;
    private final Instant joinedAt;
    private boolean connected;
    private Instant lastSeenAt;

    public Player(UUID id, String name, String reconnectToken, Instant now) {
        this.id = Objects.requireNonNull(id, "id is required.");
        this.name = Objects.requireNonNull(name, "name is required.");
        this.reconnectToken = Objects.requireNonNull(reconnectToken, "reconnectToken is required.");
        this.joinedAt = Objects.requireNonNull(now, "now is required.");
        this.lastSeenAt = now;
        this.connected = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getReconnectToken() {
        return reconnectToken;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public boolean isConnected() {
        return connected;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void disconnect(Instant now) {
        connected = false;
        lastSeenAt = now;
    }

    public void reconnect(Instant now) {
        connected = true;
        lastSeenAt = now;
    }
}
