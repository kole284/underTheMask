package com.sevaa05.underthemask.lobby.store;

import com.sevaa05.underthemask.lobby.model.Lobby;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryLobbyStore implements LobbyStore {

    private final ConcurrentMap<String, Lobby> lobbies = new ConcurrentHashMap<>();

    @Override
    public Optional<Lobby> findByCode(String code) {
        return Optional.ofNullable(lobbies.get(code));
    }

    @Override
    public boolean saveIfAbsent(Lobby lobby) {
        return lobbies.putIfAbsent(lobby.getCode(), lobby) == null;
    }

    @Override
    public Collection<Lobby> findAll() {
        return lobbies.values();
    }

    @Override
    public void remove(String code) {
        lobbies.remove(code);
    }
}
