package com.sevaa05.underthemask.lobby.store;

import com.sevaa05.underthemask.lobby.model.Lobby;
import java.util.Collection;
import java.util.Optional;

public interface LobbyStore {

    Optional<Lobby> findByCode(String code);

    boolean saveIfAbsent(Lobby lobby);

    Collection<Lobby> findAll();

    void remove(String code);
}
