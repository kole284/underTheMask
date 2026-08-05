import axios from 'axios';
import { readLobbySession } from '../storage/sessionStorage';

const apiUrl = import.meta.env.VITE_API_URL;

if (!apiUrl) {
  throw new Error('VITE_API_URL is required.');
}

export const httpClient = axios.create({
  baseURL: apiUrl,
});

httpClient.interceptors.request.use((config) => {
  const session = readLobbySession();

  if (session?.reconnectToken) {
    config.headers.Authorization = `Bearer ${session.reconnectToken}`;
  }

  return config;
});
