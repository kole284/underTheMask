package com.sevaa05.underthemask.lobby.service;

import com.sevaa05.underthemask.lobby.model.Lobby;
import com.sevaa05.underthemask.lobby.store.LobbyStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LobbyCleanupService {

    private static final Duration MAX_LOBBY_LIFETIME = Duration.ofHours(12);
    private static final Duration INACTIVE_TIMEOUT = Duration.ofHours(2);

    private final LobbyStore lobbyStore;
    private final Clock clock;

    @Autowired
    public LobbyCleanupService(LobbyStore lobbyStore) {
        this(lobbyStore, Clock.systemUTC());
    }

    LobbyCleanupService(LobbyStore lobbyStore, Clock clock) {
        this.lobbyStore = lobbyStore;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${underthemask.lobby.cleanup.fixed-delay-ms:300000}")
    public void removeExpiredOrInactiveLobbies() {
        Instant now = clock.instant();
        for (Lobby lobby : lobbyStore.findAll()) {
            boolean shouldRemove;
            synchronized (lobby) {
                shouldRemove = lobby.isExpired(now, MAX_LOBBY_LIFETIME, INACTIVE_TIMEOUT);
            }
            if (shouldRemove) {
                lobbyStore.remove(lobby.getCode());
            }
        }
    }
}
