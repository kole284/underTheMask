import { useEffect, useMemo, useState } from 'react';
import { Link, useHistory, useParams } from 'react-router-dom';
import { ArrowLeft, Crown, LogOut, Play, RefreshCw, Users } from 'lucide-react';
import { GameView } from '../game/GameView';
import {
  getGame,
  resetGame,
  startGame,
  submitClue,
  submitVote,
} from '../../shared/api/gameService';
import {
  getApiErrorMessage,
  getLobby,
  leaveLobby,
  reconnectToLobby,
  updateLobbySettings,
} from '../../shared/api/lobbyService';
import type { GameStateResponse, LobbyResponse, LobbySession } from '../../shared/api/types';
import { Button } from '../../shared/components/Button';
import { SegmentedControl } from '../../shared/components/SegmentedControl';
import { createLobbyRealtimeClient, type LobbyConnectionStatus } from '../../shared/realtime/lobbyRealtime';
import { clearLobbySession, readLobbySession, saveLobbySession } from '../../shared/storage/sessionStorage';

export function LobbyPage() {
  const { code = '' } = useParams<{ code: string }>();
  const history = useHistory();
  const normalizedCode = code.toUpperCase();
  const [lobby, setLobby] = useState<LobbyResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isLeaving, setIsLeaving] = useState(false);
  const [isGameActionPending, setIsGameActionPending] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [connectionStatus, setConnectionStatus] = useState<LobbyConnectionStatus>('connecting');
  const [session, setSession] = useState<LobbySession | null>(() => readLobbySession(normalizedCode));
  const [gameState, setGameState] = useState<GameStateResponse | null>(null);
  const hasLobby = Boolean(lobby);

  const currentPlayer = useMemo(
    () => lobby?.players.find((player) => player.playerId === session?.playerId),
    [lobby?.players, session?.playerId],
  );
  const isHost = Boolean(session?.playerId && lobby?.hostPlayerId === session.playerId);

  useEffect(() => {
    let ignore = false;

    async function loadLobby() {
      setIsLoading(true);
      setErrorMessage('');

      try {
        const storedSession = readLobbySession(normalizedCode);

        if (storedSession?.lobbyCode === normalizedCode) {
          const reconnectedSession = await reconnectToLobby(normalizedCode);
          saveLobbySession(reconnectedSession);
          if (!ignore) {
            setSession(reconnectedSession);
          }
        } else if (!ignore) {
          setSession(null);
        }

        const result = await getLobby(normalizedCode);
        let loadedGame: GameStateResponse | null = null;
        if (storedSession?.lobbyCode === normalizedCode && result.status !== 'WAITING') {
          loadedGame = await getGame(normalizedCode);
        }

        if (!ignore) {
          setLobby(result);
          setGameState(loadedGame);
        }
      } catch (error) {
        if (!ignore) {
          setErrorMessage(getApiErrorMessage(error));
        }
      } finally {
        if (!ignore) {
          setIsLoading(false);
        }
      }
    }

    loadLobby();

    return () => {
      ignore = true;
    };
  }, [normalizedCode]);

  useEffect(() => {
    if (!hasLobby) {
      return undefined;
    }

    const realtime = createLobbyRealtimeClient(normalizedCode, {
      onLobbyUpdated: (updatedLobby) => {
        setLobby(updatedLobby);
        if (updatedLobby.status === 'WAITING') {
          setGameState(null);
        }
      },
      onGameUpdated: () => {
        if (readLobbySession(normalizedCode)?.lobbyCode !== normalizedCode) {
          return;
        }
        void getGame(normalizedCode)
          .then(setGameState)
          .catch((error) => setErrorMessage(getApiErrorMessage(error)));
      },
      onStatusChange: setConnectionStatus,
    });

    realtime.activate();

    return () => {
      void realtime.deactivate();
    };
  }, [hasLobby, normalizedCode]);

  async function handleSettingsChange<TKey extends keyof LobbyResponse['settings']>(
    key: TKey,
    value: LobbyResponse['settings'][TKey],
  ) {
    if (!lobby) {
      return;
    }

    const nextSettings = {
      ...lobby.settings,
      [key]: value,
    };

    setLobby({ ...lobby, settings: nextSettings });
    setErrorMessage('');

    try {
      const updatedLobby = await updateLobbySettings(lobby.lobbyCode, nextSettings);
      setLobby(updatedLobby);
    } catch (error) {
      setLobby(lobby);
      setErrorMessage(getApiErrorMessage(error));
    }
  }

  async function handleLeave() {
    if (!lobby) {
      return;
    }

    setIsLeaving(true);
    setErrorMessage('');

    try {
      await leaveLobby(lobby.lobbyCode);
      clearLobbySession();
      setSession(null);
      history.push('/');
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsLeaving(false);
    }
  }

  async function handleStartGame() {
    setIsGameActionPending(true);
    setErrorMessage('');
    try {
      const state = await startGame(normalizedCode);
      setGameState(state);
      setLobby((current) => current ? { ...current, status: 'IN_GAME' } : current);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsGameActionPending(false);
    }
  }

  async function handleSubmitClue(clue: string) {
    setIsGameActionPending(true);
    setErrorMessage('');
    try {
      setGameState(await submitClue(normalizedCode, { clue }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsGameActionPending(false);
    }
  }

  async function handleSubmitVote(suspectedPlayerIds: string[]) {
    setIsGameActionPending(true);
    setErrorMessage('');
    try {
      setGameState(await submitVote(normalizedCode, { suspectedPlayerIds }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsGameActionPending(false);
    }
  }

  async function handleResetGame() {
    setIsGameActionPending(true);
    setErrorMessage('');
    try {
      const resetLobby = await resetGame(normalizedCode);
      setLobby(resetLobby);
      setGameState(null);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsGameActionPending(false);
    }
  }

  if (isLoading || !lobby) {
    return (
      <section className="screen lobby-screen center-screen">
        <RefreshCw className="spin" size={26} />
        <p>Ucitavam lobby...</p>
        {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
      </section>
    );
  }

  if (gameState && session && lobby.status !== 'WAITING') {
    return (
      <GameView
        lobbyCode={normalizedCode}
        playerId={session.playerId}
        isHost={isHost}
        state={gameState}
        connectionStatus={connectionStatus}
        errorMessage={errorMessage}
        isBusy={isGameActionPending || isLeaving}
        onSubmitClue={handleSubmitClue}
        onSubmitVote={handleSubmitVote}
        onReset={handleResetGame}
        onLeave={handleLeave}
      />
    );
  }

  return (
    <section className="screen lobby-screen">
      <div className="lobby-topbar">
        <Link to="/" className="back-link compact">
          <ArrowLeft size={18} />
          Pocetna
        </Link>
        <Button
          type="button"
          variant="ghost"
          className="small-button"
          icon={<LogOut size={17} />}
          disabled={isLeaving}
          onClick={handleLeave}
        >
          {isLeaving ? 'Izlazim...' : 'Izadji'}
        </Button>
      </div>

      {connectionStatus !== 'connected' ? (
        <div className={`connection-note ${connectionStatus}`}>
          {connectionStatus === 'connecting' ? 'Povezivanje u toku...' : 'Veza sa lobby serverom je prekinuta.'}
        </div>
      ) : null}

      {errorMessage ? <div className="connection-note error">{errorMessage}</div> : null}

      <header className="lobby-header">
        <div>
          <p className="eyebrow">Lobby kod</p>
          <h1>{lobby.lobbyCode}</h1>
        </div>
        <div className="lobby-meta">
          <div className="capacity-pill">{lobby.status}</div>
          <div className="capacity-pill">
            <Users size={16} />
            {lobby.playerCount}/{lobby.maxPlayers}
          </div>
        </div>
      </header>

      <section className="panel settings-panel">
        <div className="panel-title-row">
          <h2>Podesavanja</h2>
          <span>{isHost ? 'Host kontrola' : 'Ceka se host'}</span>
        </div>

        <SegmentedControl
          label="Broj impostora"
          disabled={!isHost || lobby.status !== 'WAITING'}
          value={lobby.settings.impostorCount}
          options={[
            { label: '1', value: 1 },
            { label: '2', value: 2 },
          ]}
          onChange={(value) => handleSettingsChange('impostorCount', value)}
        />

        <SegmentedControl
          label="Pomoc za impostora"
          disabled={!isHost || lobby.status !== 'WAITING'}
          value={lobby.settings.hintType}
          options={[
            { label: 'Kategorija', value: 'CATEGORY' },
            { label: 'Asocijacija', value: 'ASSOCIATION' },
          ]}
          onChange={(value) => handleSettingsChange('hintType', value)}
        />

        {!isHost ? <div className="disabled-note">Samo host moze da menja opcije pre pocetka.</div> : null}
      </section>

      <section className="panel players-panel">
        <div className="panel-title-row">
          <h2>Igraci</h2>
          <span>Maks. 12</span>
        </div>

        <ul className="player-list">
          {lobby.players.map((player) => (
            <li key={player.playerId} className="player-row">
              <div className="player-avatar">{player.playerName.slice(0, 1).toUpperCase()}</div>
              <div className="player-copy">
                <strong>{player.playerName}</strong>
                <span>
                  {player.host ? 'Host' : 'Igrac'}
                  {player.playerId === currentPlayer?.playerId ? ' - ti' : ''}
                </span>
              </div>
              {player.host ? <Crown className="host-icon" size={17} /> : null}
              <span className={`status-dot ${player.connected ? 'connected' : 'disconnected'}`} />
            </li>
          ))}
        </ul>
      </section>

      <div className={`sticky-actions ${isHost ? '' : 'muted'}`}>
        {isHost ? (
          <Button
            type="button"
            icon={<Play size={18} />}
            disabled={isGameActionPending || lobby.playerCount < 3 || lobby.settings.impostorCount >= lobby.playerCount}
            onClick={handleStartGame}
          >
            {isGameActionPending ? 'Pokrecem...' : 'Pokreni igru'}
          </Button>
        ) : <p>Cekas hosta u lobbyju.</p>}
      </div>
    </section>
  );
}
