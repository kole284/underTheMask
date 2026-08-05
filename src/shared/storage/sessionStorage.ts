import type { LobbySession } from '../api/types';

const STORAGE_KEY = 'under-the-mask:lobby-sessions:v2';
const LEGACY_STORAGE_KEY = 'under-the-mask:lobby-session';
const ACTIVE_SESSION_KEY = 'under-the-mask:active-session';

interface StoredSessions {
  version: 2;
  sessions: LobbySession[];
}

function getSessionKey(session: LobbySession) {
  return `${session.lobbyCode}:${session.playerId}`;
}

function readStoredSessions(): LobbySession[] {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (raw) {
    try {
      const stored = JSON.parse(raw) as Partial<StoredSessions>;
      if (stored.version === 2 && Array.isArray(stored.sessions)) {
        return stored.sessions;
      }
    } catch {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  const legacyRaw = localStorage.getItem(LEGACY_STORAGE_KEY);
  if (!legacyRaw) {
    return [];
  }

  try {
    const legacySession = JSON.parse(legacyRaw) as LobbySession;
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ version: 2, sessions: [legacySession] }));
    localStorage.removeItem(LEGACY_STORAGE_KEY);
    return [legacySession];
  } catch {
    localStorage.removeItem(LEGACY_STORAGE_KEY);
    return [];
  }
}

export function saveLobbySession(session: LobbySession) {
  const sessions = readStoredSessions();
  const sessionKey = getSessionKey(session);
  const nextSessions = sessions.filter((stored) => getSessionKey(stored) !== sessionKey);
  nextSessions.push(session);

  localStorage.setItem(STORAGE_KEY, JSON.stringify({ version: 2, sessions: nextSessions }));
  sessionStorage.setItem(ACTIVE_SESSION_KEY, sessionKey);
}

export function readLobbySession(lobbyCode?: string): LobbySession | null {
  const sessions = readStoredSessions();
  const activeSessionKey = sessionStorage.getItem(ACTIVE_SESSION_KEY);
  const activeSession = sessions.find((session) => getSessionKey(session) === activeSessionKey);

  if (activeSession && (!lobbyCode || activeSession.lobbyCode === lobbyCode.toUpperCase())) {
    return activeSession;
  }

  const candidates = lobbyCode
    ? sessions.filter((session) => session.lobbyCode === lobbyCode.toUpperCase())
    : sessions;
  if (candidates.length === 1) {
    sessionStorage.setItem(ACTIVE_SESSION_KEY, getSessionKey(candidates[0]));
    return candidates[0];
  }

  return null;
}

export function clearLobbySession() {
  const activeSessionKey = sessionStorage.getItem(ACTIVE_SESSION_KEY);
  if (activeSessionKey) {
    const remainingSessions = readStoredSessions()
      .filter((session) => getSessionKey(session) !== activeSessionKey);
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ version: 2, sessions: remainingSessions }));
  }
  sessionStorage.removeItem(ACTIVE_SESSION_KEY);
}
