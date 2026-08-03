package com.sevaa05.underthemask.lobby.dto;

import com.sevaa05.underthemask.lobby.model.LobbySession;
import java.util.UUID;

public record LobbySessionResponse(
        String lobbyCode,
        UUID playerId,
        String reconnectToken
) {

    public static LobbySessionResponse from(LobbySession session) {
        return new LobbySessionResponse(session.lobbyCode(), session.playerId(), session.reconnectToken());
    }
}
