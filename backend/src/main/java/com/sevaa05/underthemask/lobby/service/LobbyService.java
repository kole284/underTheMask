package com.sevaa05.underthemask.lobby.service;

import com.sevaa05.underthemask.lobby.dto.LobbyResponse;
import com.sevaa05.underthemask.lobby.model.GameSettings;
import com.sevaa05.underthemask.lobby.model.Lobby;
import com.sevaa05.underthemask.lobby.model.LobbySession;
import com.sevaa05.underthemask.lobby.model.LobbyStatus;
import com.sevaa05.underthemask.lobby.model.Player;
import com.sevaa05.underthemask.lobby.service.exception.DuplicatePlayerNameException;
import com.sevaa05.underthemask.lobby.service.exception.InvalidLobbyCodeException;
import com.sevaa05.underthemask.lobby.service.exception.LobbyFullException;
import com.sevaa05.underthemask.lobby.service.exception.LobbyNotFoundException;
import com.sevaa05.underthemask.lobby.service.exception.LobbyStateException;
import com.sevaa05.underthemask.lobby.service.exception.OnlyHostCanUpdateSettingsException;
import com.sevaa05.underthemask.lobby.service.exception.SettingsLockedException;
import com.sevaa05.underthemask.lobby.service.exception.UnauthorizedPlayerTokenException;
import com.sevaa05.underthemask.lobby.store.LobbyStore;
import com.sevaa05.underthemask.realtime.service.RealtimeEventPublisher;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LobbyService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LOBBY_CODE_LENGTH = 6;
    private static final int TOKEN_BYTES = 32;
    private static final int MAX_CODE_ATTEMPTS = 100;
    private static final Pattern LOBBY_CODE_PATTERN = Pattern.compile("^[A-HJ-NP-Z2-9]{6}$");

    private final LobbyStore lobbyStore;
    private final RealtimeEventPublisher eventPublisher;
    private final SecureRandom secureRandom;
    private final Clock clock;

    @Autowired
    public LobbyService(LobbyStore lobbyStore, RealtimeEventPublisher eventPublisher) {
        this(lobbyStore, eventPublisher, new SecureRandom(), Clock.systemUTC());
    }

    LobbyService(LobbyStore lobbyStore, RealtimeEventPublisher eventPublisher,
                 SecureRandom secureRandom, Clock clock) {
        this.lobbyStore = lobbyStore;
        this.eventPublisher = eventPublisher;
        this.secureRandom = secureRandom;
        this.clock = clock;
    }

    public LobbySession createLobby(String hostName, GameSettings settings) {
        String normalizedHostName = normalizePlayerName(hostName);
        Instant now = clock.instant();

        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            Lobby lobby = new Lobby(generateLobbyCode(), settings, now);
            Player host = new Player(UUID.randomUUID(), normalizedHostName, generateReconnectToken(), now);
            lobby.addPlayer(host);
            lobby.touch(now);

            if (lobbyStore.saveIfAbsent(lobby)) {
                return toSession(lobby, host);
            }
        }

        throw new LobbyStateException("LOBBY_CODE_GENERATION_FAILED", "Could not generate an unused lobby code.");
    }

    public LobbySession joinLobby(String code, String playerName) {
        Lobby lobby = requireLobby(code);
        LobbySession session;
        LobbyResponse eventPayload;

        synchronized (lobby) {
            ensureWaiting(lobby);

            String normalizedPlayerName = normalizePlayerName(playerName);
            if (lobby.hasPlayerNamed(normalizedPlayerName)) {
                throw new DuplicatePlayerNameException();
            }
            if (lobby.isFull()) {
                throw new LobbyFullException();
            }

            Instant now = clock.instant();
            Player player = new Player(UUID.randomUUID(), normalizedPlayerName, generateUniqueToken(lobby), now);
            lobby.addPlayer(player);
            lobby.touch(now);
            session = toSession(lobby, player);
            eventPayload = LobbyResponse.from(lobby);
        }

        eventPublisher.publishLobbyUpdated(lobby.getCode(), eventPayload);
        return session;
    }

    public LobbyResponse getLobby(String code) {
        Lobby lobby = requireLobby(code);
        synchronized (lobby) {
            return LobbyResponse.from(lobby);
        }
    }

    public LobbySession reconnect(String code, String reconnectToken) {
        Lobby lobby = requireLobby(code);
        LobbySession session;
        LobbyResponse eventPayload;

        synchronized (lobby) {
            Player player = requirePlayerByToken(lobby, reconnectToken);
            Instant now = clock.instant();
            player.reconnect(now);
            lobby.touch(now);
            session = toSession(lobby, player);
            eventPayload = LobbyResponse.from(lobby);
        }

        eventPublisher.publishLobbyUpdated(lobby.getCode(), eventPayload);
        return session;
    }

    public void leaveLobby(String code, String reconnectToken) {
        Lobby lobby = requireLobby(code);
        LobbyResponse eventPayload = null;
        boolean removeLobby = false;

        synchronized (lobby) {
            Player player = requirePlayerByToken(lobby, reconnectToken);
            if (lobby.getStatus() != LobbyStatus.WAITING) {
                lobby.resetGame();
            }
            lobby.removePlayer(player.getId());
            Instant now = clock.instant();
            lobby.touch(now);

            if (lobby.isEmpty()) {
                lobby.close();
                removeLobby = true;
            } else {
                eventPayload = LobbyResponse.from(lobby);
            }
        }

        if (removeLobby) {
            lobbyStore.remove(lobby.getCode());
        } else {
            eventPublisher.publishLobbyUpdated(lobby.getCode(), eventPayload);
        }
    }

    public LobbyResponse updateSettings(String code, String reconnectToken, GameSettings settings) {
        Lobby lobby = requireLobby(code);
        LobbyResponse response;

        synchronized (lobby) {
            Player player = requirePlayerByToken(lobby, reconnectToken);
            if (!player.getId().equals(lobby.getHostPlayerId())) {
                throw new OnlyHostCanUpdateSettingsException();
            }
            if (lobby.getStatus() != LobbyStatus.WAITING) {
                throw new SettingsLockedException();
            }

            lobby.setSettings(settings);
            lobby.touch(clock.instant());
            response = LobbyResponse.from(lobby);
        }

        eventPublisher.publishLobbyUpdated(lobby.getCode(), response);
        return response;
    }

    public void disconnectPlayer(String code, String reconnectToken) {
        Lobby lobby = requireLobby(code);
        LobbyResponse eventPayload;

        synchronized (lobby) {
            Player player = requirePlayerByToken(lobby, reconnectToken);
            Instant now = clock.instant();
            player.disconnect(now);
            lobby.touch(now);
            eventPayload = LobbyResponse.from(lobby);
        }

        eventPublisher.publishLobbyUpdated(lobby.getCode(), eventPayload);
    }

    public void removeExpiredLobby(String code) {
        lobbyStore.remove(code);
    }

    private Lobby requireLobby(String code) {
        String normalizedCode = normalizeLobbyCode(code);
        return lobbyStore.findByCode(normalizedCode).orElseThrow(LobbyNotFoundException::new);
    }

    private void ensureWaiting(Lobby lobby) {
        if (lobby.getStatus() != LobbyStatus.WAITING) {
            throw new LobbyStateException("LOBBY_NOT_WAITING", "This action is allowed only while the lobby is waiting.");
        }
    }

    private Player requirePlayerByToken(Lobby lobby, String reconnectToken) {
        if (reconnectToken == null || reconnectToken.isBlank()) {
            throw new UnauthorizedPlayerTokenException();
        }
        return lobby.findPlayerByReconnectToken(reconnectToken)
                .orElseThrow(UnauthorizedPlayerTokenException::new);
    }

    private String normalizeLobbyCode(String code) {
        if (code == null || !LOBBY_CODE_PATTERN.matcher(code).matches()) {
            throw new InvalidLobbyCodeException();
        }
        return code;
    }

    private String normalizePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new LobbyStateException("INVALID_PLAYER_NAME", "Player name is required.");
        }
        return playerName.trim();
    }

    private String generateLobbyCode() {
        StringBuilder code = new StringBuilder(LOBBY_CODE_LENGTH);
        for (int i = 0; i < LOBBY_CODE_LENGTH; i++) {
            code.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString().toUpperCase(Locale.ROOT);
    }

    private String generateUniqueToken(Lobby lobby) {
        String token = generateReconnectToken();
        while (lobby.hasReconnectToken(token)) {
            token = generateReconnectToken();
        }
        return token;
    }

    private String generateReconnectToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private LobbySession toSession(Lobby lobby, Player player) {
        return new LobbySession(lobby.getCode(), player.getId(), player.getReconnectToken());
    }
}
