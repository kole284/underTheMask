package com.sevaa05.underthemask.lobby.dto;

import com.sevaa05.underthemask.lobby.model.Player;
import java.util.UUID;

public record PlayerResponse(
        UUID playerId,
        String playerName,
        boolean connected,
        boolean host
) {

    public static PlayerResponse from(Player player, UUID hostPlayerId) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.isConnected(),
                player.getId().equals(hostPlayerId)
        );
    }
}
