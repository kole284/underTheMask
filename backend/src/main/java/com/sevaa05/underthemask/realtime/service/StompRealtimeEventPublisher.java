package com.sevaa05.underthemask.realtime.service;

import com.sevaa05.underthemask.realtime.event.EventType;
import com.sevaa05.underthemask.realtime.event.RealtimeEvent;
import java.time.Instant;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class StompRealtimeEventPublisher implements RealtimeEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public StompRealtimeEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishLobbyUpdated(String lobbyCode, Object payload) {
        RealtimeEvent<Object> event = new RealtimeEvent<>(EventType.LOBBY_UPDATED, payload, Instant.now());
        messagingTemplate.convertAndSend("/topic/lobbies/" + lobbyCode, event);
    }

    @Override
    public void publishGameUpdated(String lobbyCode, Object payload) {
        RealtimeEvent<Object> event = new RealtimeEvent<>(EventType.GAME_UPDATED, payload, Instant.now());
        messagingTemplate.convertAndSend("/topic/lobbies/" + lobbyCode, event);
    }
}
