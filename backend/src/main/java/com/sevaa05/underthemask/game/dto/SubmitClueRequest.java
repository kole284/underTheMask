package com.sevaa05.underthemask.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitClueRequest(
        @NotBlank
        @Size(max = 80)
        String clue
) {
}
