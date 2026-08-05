package com.sevaa05.underthemask.game.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SubmitVoteRequest(
        @NotEmpty
        List<@NotNull UUID> suspectedPlayerIds
) {
}
