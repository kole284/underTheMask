import type { GameStateResponse, LobbyResponse, SubmitClueRequest, SubmitVoteRequest } from './types';
import { httpClient } from './httpClient';

export async function startGame(code: string): Promise<GameStateResponse> {
  const { data } = await httpClient.post<GameStateResponse>(`/lobbies/${code.toUpperCase()}/game/start`);
  return data;
}

export async function getGame(code: string): Promise<GameStateResponse> {
  const { data } = await httpClient.get<GameStateResponse>(`/lobbies/${code.toUpperCase()}/game`);
  return data;
}

export async function submitClue(code: string, request: SubmitClueRequest): Promise<GameStateResponse> {
  const { data } = await httpClient.post<GameStateResponse>(
    `/lobbies/${code.toUpperCase()}/game/clues`,
    request,
  );
  return data;
}

export async function submitVote(code: string, request: SubmitVoteRequest): Promise<GameStateResponse> {
  const { data } = await httpClient.post<GameStateResponse>(
    `/lobbies/${code.toUpperCase()}/game/votes`,
    request,
  );
  return data;
}

export async function resetGame(code: string): Promise<LobbyResponse> {
  const { data } = await httpClient.post<LobbyResponse>(`/lobbies/${code.toUpperCase()}/game/reset`);
  return data;
}
