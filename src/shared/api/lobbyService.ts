import type {
  ApiErrorResponse,
  CreateLobbyRequest,
  JoinLobbyRequest,
  LobbyResponse,
  LobbySession,
  LobbySettings,
} from './types';
import { httpClient } from './httpClient';

export async function createLobby(request: CreateLobbyRequest): Promise<LobbySession> {
  const { data } = await httpClient.post<LobbySession>('/lobbies', request);
  return data;
}

export async function joinLobby(code: string, request: JoinLobbyRequest): Promise<LobbySession> {
  const normalizedCode = code.toUpperCase();
  const { data } = await httpClient.post<LobbySession>(`/lobbies/${normalizedCode}/players`, request);
  return data;
}

export async function getLobby(code: string): Promise<LobbyResponse> {
  const { data } = await httpClient.get<LobbyResponse>(`/lobbies/${code.toUpperCase()}`);
  return data;
}

export async function reconnectToLobby(code: string): Promise<LobbySession> {
  const { data } = await httpClient.post<LobbySession>(`/lobbies/${code.toUpperCase()}/reconnect`);
  return data;
}

export async function updateLobbySettings(code: string, settings: LobbySettings): Promise<LobbyResponse> {
  const { data } = await httpClient.patch<LobbyResponse>(`/lobbies/${code.toUpperCase()}/settings`, settings);
  return data;
}

export async function leaveLobby(code: string): Promise<void> {
  await httpClient.delete(`/lobbies/${code.toUpperCase()}/players/me`);
}

export function getApiErrorMessage(error: unknown): string {
  if (
    typeof error === 'object'
    && error !== null
    && 'response' in error
    && typeof error.response === 'object'
    && error.response !== null
    && 'data' in error.response
  ) {
    const data = error.response.data as Partial<ApiErrorResponse>;
    if (typeof data.message === 'string' && data.message.trim()) {
      return data.message;
    }
  }

  return 'Zahtev nije uspeo. Proveri backend i pokusaj ponovo.';
}
