package com.sevaa05.underthemask.game.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GameRound {

    private final UUID id;
    private final String secretWord;
    private final String category;
    private final String impostorHint;
    private final Set<UUID> impostorPlayerIds;
    private final List<UUID> turnOrder;
    private final LinkedHashMap<UUID, String> clues = new LinkedHashMap<>();
    private final LinkedHashMap<UUID, List<UUID>> votes = new LinkedHashMap<>();
    private GamePhase phase = GamePhase.CLUES;

    public GameRound(UUID id, String secretWord, String category, String impostorHint,
                     Set<UUID> impostorPlayerIds, List<UUID> turnOrder) {
        this.id = Objects.requireNonNull(id, "id is required.");
        this.secretWord = Objects.requireNonNull(secretWord, "secretWord is required.");
        this.category = Objects.requireNonNull(category, "category is required.");
        this.impostorHint = Objects.requireNonNull(impostorHint, "impostorHint is required.");
        this.impostorPlayerIds = Set.copyOf(impostorPlayerIds);
        this.turnOrder = List.copyOf(turnOrder);
    }

    public UUID getId() {
        return id;
    }

    public String getSecretWord() {
        return secretWord;
    }

    public String getCategory() {
        return category;
    }

    public String getImpostorHint() {
        return impostorHint;
    }

    public Set<UUID> getImpostorPlayerIds() {
        return impostorPlayerIds;
    }

    public List<UUID> getTurnOrder() {
        return turnOrder;
    }

    public Map<UUID, String> getClues() {
        return Map.copyOf(clues);
    }

    public Map<UUID, List<UUID>> getVotes() {
        return Map.copyOf(votes);
    }

    public GamePhase getPhase() {
        return phase;
    }

    public Optional<UUID> getCurrentPlayerId() {
        if (phase != GamePhase.CLUES || clues.size() >= turnOrder.size()) {
            return Optional.empty();
        }
        return Optional.of(turnOrder.get(clues.size()));
    }

    public boolean isImpostor(UUID playerId) {
        return impostorPlayerIds.contains(playerId);
    }

    public boolean hasVoted(UUID playerId) {
        return votes.containsKey(playerId);
    }

    public void submitClue(UUID playerId, String clue) {
        UUID currentPlayerId = getCurrentPlayerId().orElseThrow();
        if (!currentPlayerId.equals(playerId)) {
            throw new IllegalStateException("It is not this player's turn.");
        }
        clues.put(playerId, clue);
        if (clues.size() == turnOrder.size()) {
            phase = GamePhase.VOTING;
        }
    }

    public void submitVote(UUID playerId, List<UUID> suspectedPlayerIds) {
        if (phase != GamePhase.VOTING) {
            throw new IllegalStateException("Voting is not active.");
        }
        if (votes.containsKey(playerId)) {
            throw new IllegalStateException("Player has already voted.");
        }
        votes.put(playerId, List.copyOf(suspectedPlayerIds));
        if (votes.size() == turnOrder.size()) {
            phase = GamePhase.FINISHED;
        }
    }

    public VoteOutcome calculateOutcome() {
        if (phase != GamePhase.FINISHED) {
            throw new IllegalStateException("Game has not finished.");
        }

        LinkedHashMap<UUID, Integer> tallies = new LinkedHashMap<>();
        turnOrder.forEach(playerId -> tallies.put(playerId, 0));
        votes.values().stream()
                .flatMap(List::stream)
                .forEach(playerId -> tallies.computeIfPresent(playerId, (ignored, count) -> count + 1));

        List<Integer> rankedCounts = tallies.values().stream()
                .sorted((left, right) -> Integer.compare(right, left))
                .toList();
        int cutoff = rankedCounts.get(impostorPlayerIds.size() - 1);
        Set<UUID> mostVoted = new LinkedHashSet<>();
        tallies.forEach((playerId, count) -> {
            if (count >= cutoff) {
                mostVoted.add(playerId);
            }
        });

        boolean tie = mostVoted.size() != impostorPlayerIds.size();
        boolean impostorsCaught = !tie && mostVoted.equals(impostorPlayerIds);
        return new VoteOutcome(
                impostorsCaught ? GameWinner.CREWMATES : GameWinner.IMPOSTORS,
                List.copyOf(mostVoted),
                tie,
                Map.copyOf(tallies)
        );
    }

    public record VoteOutcome(
            GameWinner winner,
            List<UUID> mostVotedPlayerIds,
            boolean tie,
            Map<UUID, Integer> tallies
    ) {
    }
}
