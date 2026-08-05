import { Client, type StompSubscription } from '@stomp/stompjs';
import type { GamePublicState, LobbyResponse, RealtimeEvent } from '../api/types';

export type LobbyConnectionStatus = 'connecting' | 'connected' | 'disconnected' | 'error';

interface LobbyRealtimeHandlers {
  onLobbyUpdated: (lobby: LobbyResponse) => void;
  onGameUpdated: (game: GamePublicState) => void;
  onStatusChange: (status: LobbyConnectionStatus) => void;
}

const wsUrl = import.meta.env.VITE_WS_URL;

if (!wsUrl) {
  throw new Error('VITE_WS_URL is required.');
}

export function createLobbyRealtimeClient(code: string, handlers: LobbyRealtimeHandlers) {
  let subscription: StompSubscription | null = null;

  const client = new Client({
    brokerURL: wsUrl,
    reconnectDelay: 5000,
    debug: () => undefined,
    beforeConnect: () => {
      handlers.onStatusChange('connecting');
    },
    onConnect: () => {
      handlers.onStatusChange('connected');
      subscription = client.subscribe(`/topic/lobbies/${code.toUpperCase()}`, (message) => {
        const event = JSON.parse(message.body) as RealtimeEvent<LobbyResponse | GamePublicState>;
        if (event.type === 'LOBBY_UPDATED') {
          handlers.onLobbyUpdated(event.payload as LobbyResponse);
        }
        if (event.type === 'GAME_UPDATED') {
          handlers.onGameUpdated(event.payload as GamePublicState);
        }
      });
    },
    onStompError: () => {
      handlers.onStatusChange('error');
    },
    onWebSocketError: () => {
      handlers.onStatusChange('error');
    },
    onWebSocketClose: () => {
      handlers.onStatusChange('disconnected');
    },
  });

  return {
    activate: () => client.activate(),
    deactivate: () => {
      subscription?.unsubscribe();
      subscription = null;
      return client.deactivate();
    },
  };
}
