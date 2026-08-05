package com.sevaa05.underthemask.game.dto;

import com.sevaa05.underthemask.game.model.PlayerRole;

public record GameStateResponse(
        GamePublicResponse game,
        PlayerRole role,
        String secretWord,
        String hint,
        boolean hasSubmittedVote
) {
}
