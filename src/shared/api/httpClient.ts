import axios from 'axios';
import { apiBaseUrl } from './config';
import { readLobbySession } from '../storage/sessionStorage';

export const httpClient = axios.create({
  baseURL: apiBaseUrl,
});

httpClient.interceptors.request.use((config) => {
  const session = readLobbySession();

  if (session?.reconnectToken) {
    config.headers.Authorization = `Bearer ${session.reconnectToken}`;
  }

  return config;
});
