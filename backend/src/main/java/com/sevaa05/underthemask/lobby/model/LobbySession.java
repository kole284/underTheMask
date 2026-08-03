package com.sevaa05.underthemask.lobby.model;

import java.util.UUID;

public record LobbySession(
        String lobbyCode,
        UUID playerId,
        String reconnectToken
) {
}
