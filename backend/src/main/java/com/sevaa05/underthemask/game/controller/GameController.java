package com.sevaa05.underthemask.game.controller;

import com.sevaa05.underthemask.game.dto.GameStateResponse;
import com.sevaa05.underthemask.game.dto.SubmitClueRequest;
import com.sevaa05.underthemask.game.dto.SubmitVoteRequest;
import com.sevaa05.underthemask.game.service.GameService;
import com.sevaa05.underthemask.lobby.dto.LobbyResponse;
import com.sevaa05.underthemask.lobby.service.exception.UnauthorizedPlayerTokenException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lobbies/{code}/game")
public class GameController {

    private static final String BEARER_PREFIX = "Bearer ";
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public GameStateResponse startGame(@PathVariable String code,
                                       @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
                                       String authorization) {
        return gameService.startGame(code, extractBearerToken(authorization));
    }

    @GetMapping
    public GameStateResponse getGame(@PathVariable String code,
                                     @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
                                     String authorization) {
        return gameService.getGame(code, extractBearerToken(authorization));
    }

    @PostMapping("/clues")
    public GameStateResponse submitClue(@PathVariable String code,
                                        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
                                        String authorization,
                                        @Valid @RequestBody SubmitClueRequest request) {
        return gameService.submitClue(code, extractBearerToken(authorization), request.clue());
    }

    @PostMapping("/votes")
    public GameStateResponse submitVote(@PathVariable String code,
                                        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
                                        String authorization,
                                        @Valid @RequestBody SubmitVoteRequest request) {
        return gameService.submitVote(code, extractBearerToken(authorization), request.suspectedPlayerIds());
    }

    @PostMapping("/reset")
    public LobbyResponse resetGame(@PathVariable String code,
                                   @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
                                   String authorization) {
        return gameService.resetGame(code, extractBearerToken(authorization));
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedPlayerTokenException();
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new UnauthorizedPlayerTokenException();
        }
        return token;
    }
}
