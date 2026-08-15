import { useEffect, useMemo, useRef, useState, type FormEvent } from 'react';
import { Check, Eye, EyeOff, LogOut, MessageCircle, RotateCcw, Send, Users, Vote } from 'lucide-react';
import type { GameClue, GamePhase, GameStateResponse } from '../../shared/api/types';
import { Button } from '../../shared/components/Button';
import type { LobbyConnectionStatus } from '../../shared/realtime/lobbyRealtime';

interface GameViewProps {
  lobbyCode: string;
  playerId: string;
  isHost: boolean;
  state: GameStateResponse;
  connectionStatus: LobbyConnectionStatus;
  errorMessage: string;
  isBusy: boolean;
  onSubmitClue: (clue: string) => Promise<void>;
  onSubmitVote: (playerIds: string[]) => Promise<void>;
  onReset: () => Promise<void>;
  onLeave: () => Promise<void>;
}

interface ClueChatProps {
  clues: GameClue[];
  currentPlayerId: string | null;
  currentPlayerName?: string;
  phase: GamePhase;
  viewerPlayerId: string;
}

function ClueChat({
  clues,
  currentPlayerId,
  currentPlayerName,
  phase,
  viewerPlayerId,
}: ClueChatProps) {
  const logRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const log = logRef.current;
    if (log) {
      log.scrollTop = log.scrollHeight;
    }
  }, [clues.length, currentPlayerId]);

  return (
    <section className="panel clue-chat-panel">
      <div className="clue-chat-heading">
        <div className="clue-chat-title">
          <MessageCircle size={18} />
          <h2>Chat tragova</h2>
        </div>
        <span>{clues.length}</span>
      </div>
      <div
        ref={logRef}
        className="clue-chat-log"
        role="log"
        aria-live="polite"
        aria-relevant="additions"
      >
        {clues.length === 0 ? (
          <p className="clue-chat-empty">Prvi trag jos nije poslat.</p>
        ) : null}
        {clues.map((item, index) => {
          const isMine = item.playerId === viewerPlayerId;
          return (
            <div key={item.playerId} className={`clue-message ${isMine ? 'mine' : ''}`}>
              <div className="clue-message-meta">
                <span className="clue-avatar" aria-hidden="true">
                  {item.playerName.charAt(0).toUpperCase()}
                </span>
                <strong>{item.playerName}{isMine ? ' (ti)' : ''}</strong>
                <small>#{index + 1}</small>
              </div>
              <p>{item.clue}</p>
            </div>
          );
        })}
        {phase === 'CLUES' && currentPlayerId ? (
          <div className="clue-chat-waiting">
            <span aria-hidden="true" />
            {currentPlayerId === viewerPlayerId ? 'Ti si na potezu' : `${currentPlayerName ?? 'Igrac'} bira trag`}
          </div>
        ) : null}
      </div>
    </section>
  );
}

export function GameView({
  lobbyCode,
  playerId,
  isHost,
  state,
  connectionStatus,
  errorMessage,
  isBusy,
  onSubmitClue,
  onSubmitVote,
  onReset,
  onLeave,
}: GameViewProps) {
  const [roleRevealed, setRoleRevealed] = useState(false);
  const [clue, setClue] = useState('');
  const [selectedPlayerIds, setSelectedPlayerIds] = useState<string[]>([]);
  const { game } = state;

  useEffect(() => {
    setRoleRevealed(false);
    setClue('');
    setSelectedPlayerIds([]);
  }, [game.roundId]);

  const playersById = useMemo(
    () => new Map(game.players.map((player) => [player.playerId, player])),
    [game.players],
  );
  const currentPlayer = game.currentPlayerId ? playersById.get(game.currentPlayerId) : null;
  const isMyTurn = game.currentPlayerId === playerId;

  function toggleVote(playerIdToToggle: string) {
    setSelectedPlayerIds((current) => {
      if (current.includes(playerIdToToggle)) {
        return current.filter((id) => id !== playerIdToToggle);
      }
      if (current.length >= game.requiredSuspectCount) {
        return current;
      }
      return [...current, playerIdToToggle];
    });
  }

  async function handleClueSubmit(event: FormEvent) {
    event.preventDefault();
    const normalizedClue = clue.trim();
    if (!normalizedClue) {
      return;
    }
    await onSubmitClue(normalizedClue);
    setClue('');
  }

  async function handleVoteSubmit(event: FormEvent) {
    event.preventDefault();
    if (selectedPlayerIds.length !== game.requiredSuspectCount) {
      return;
    }
    await onSubmitVote(selectedPlayerIds);
  }

  const phaseLabel = game.phase === 'CLUES' ? 'Tragovi' : game.phase === 'VOTING' ? 'Glasanje' : 'Rezultat';
  const roleLabel = state.role === 'IMPOSTOR' ? 'IMPOSTOR' : 'IGRAC';

  return (
    <section className="screen game-screen">
      <div className="lobby-topbar">
        <div>
          <span className="eyebrow">Lobby</span>
          <strong className="compact-code">{lobbyCode}</strong>
        </div>
        <Button
          type="button"
          variant="ghost"
          className="small-button"
          icon={<LogOut size={17} />}
          disabled={isBusy}
          onClick={() => void onLeave()}
        >
          Izadji
        </Button>
      </div>

      {connectionStatus !== 'connected' ? (
        <div className={`connection-note ${connectionStatus}`}>
          {connectionStatus === 'connecting' ? 'Povezivanje u toku...' : 'Real-time veza je prekinuta.'}
        </div>
      ) : null}
      {errorMessage ? <div className="connection-note error">{errorMessage}</div> : null}

      <header className="game-header">
        <div>
          <p className="eyebrow">Faza</p>
          <h1>{phaseLabel}</h1>
        </div>
        <div className="capacity-pill">
          <Users size={16} />
          {game.totalPlayers}
        </div>
      </header>

      {game.phase === 'CLUES' ? (
        <div className="game-phase-layout clue-phase-layout">
          <div className="game-side-column">
            <section className={`role-panel ${state.role === 'IMPOSTOR' ? 'impostor' : 'crewmate'}`}>
              <div className="role-panel-heading">
                <div>
                  <span className="eyebrow">Tvoja uloga</span>
                  <strong>{roleRevealed ? roleLabel : 'SAKRIVENO'}</strong>
                </div>
                <Button
                  type="button"
                  variant="ghost"
                  className="icon-button"
                  aria-label={roleRevealed ? 'Sakrij ulogu' : 'Prikazi ulogu'}
                  title={roleRevealed ? 'Sakrij ulogu' : 'Prikazi ulogu'}
                  icon={roleRevealed ? <EyeOff size={19} /> : <Eye size={19} />}
                  onClick={() => setRoleRevealed((visible) => !visible)}
                />
              </div>
              {roleRevealed ? (
                <div className="role-content">
                  <span>{state.role === 'IMPOSTOR' ? 'Tvoj hint' : 'Tajna rec'}</span>
                  <strong>{state.role === 'IMPOSTOR' ? state.hint : state.secretWord}</strong>
                  {state.role === 'CREWMATE' ? <small>Kategorija: {state.hint}</small> : null}
                </div>
              ) : (
                <div className="role-concealed">Privatni podatak</div>
              )}
            </section>

            <section className="panel game-status-panel">
              <span className="eyebrow">Na potezu</span>
              <h2>{isMyTurn ? 'Ti dajes trag' : currentPlayer?.playerName}</h2>
            </section>

            {isMyTurn ? (
              <form className="sticky-actions game-action-form" onSubmit={handleClueSubmit}>
                <label htmlFor="clue" className="sr-only">Tvoj trag</label>
                <input
                  id="clue"
                  className="input"
                  maxLength={80}
                  placeholder="Upisi jedan trag"
                  value={clue}
                  onChange={(event) => setClue(event.target.value)}
                />
                <Button type="submit" icon={<Send size={18} />} disabled={isBusy || !clue.trim()}>
                  Posalji
                </Button>
              </form>
            ) : (
              <div className="sticky-actions muted"><p>Ceka se trag igraca {currentPlayer?.playerName}.</p></div>
            )}
          </div>

          <ClueChat
            clues={game.clues}
            currentPlayerId={game.currentPlayerId}
            currentPlayerName={currentPlayer?.playerName}
            phase={game.phase}
            viewerPlayerId={playerId}
          />
        </div>
      ) : null}

      {game.phase === 'VOTING' ? (
        <div className="game-phase-layout voting-phase-layout">
          <div className="game-side-column">
            <section className={`role-panel ${state.role === 'IMPOSTOR' ? 'impostor' : 'crewmate'}`}>
              <div className="role-panel-heading">
                <div>
                  <span className="eyebrow">Tvoja uloga</span>
                  <strong>{roleRevealed ? roleLabel : 'SAKRIVENO'}</strong>
                </div>
                <Button
                  type="button"
                  variant="ghost"
                  className="icon-button"
                  aria-label={roleRevealed ? 'Sakrij ulogu' : 'Prikazi ulogu'}
                  title={roleRevealed ? 'Sakrij ulogu' : 'Prikazi ulogu'}
                  icon={roleRevealed ? <EyeOff size={19} /> : <Eye size={19} />}
                  onClick={() => setRoleRevealed((visible) => !visible)}
                />
              </div>
              {roleRevealed ? (
                <div className="role-content">
                  <span>{state.role === 'IMPOSTOR' ? 'Tvoj hint' : 'Tajna rec'}</span>
                  <strong>{state.role === 'IMPOSTOR' ? state.hint : state.secretWord}</strong>
                  {state.role === 'CREWMATE' ? <small>Kategorija: {state.hint}</small> : null}
                </div>
              ) : (
                <div className="role-concealed">Privatni podatak</div>
              )}
            </section>
            <div className="sticky-actions muted">
              <p>Glasalo je {game.votesSubmitted} od {game.totalPlayers} igraca.</p>
            </div>
          </div>

          <div className="game-main-column">
            <ClueChat
              clues={game.clues}
              currentPlayerId={game.currentPlayerId}
              phase={game.phase}
              viewerPlayerId={playerId}
            />

            <section className="panel vote-panel">
              <div className="panel-title-row">
                <h2>Izaberi osumnjicene</h2>
                <span>{selectedPlayerIds.length}/{game.requiredSuspectCount}</span>
              </div>
              {state.hasSubmittedVote ? (
                <div className="vote-complete"><Check size={20} /> Glas je zabelezen</div>
              ) : (
                <form onSubmit={handleVoteSubmit}>
                  <div className="vote-options">
                    {game.players.filter((player) => player.playerId !== playerId).map((player) => (
                      <label key={player.playerId} className="vote-option">
                        <input
                          type="checkbox"
                          checked={selectedPlayerIds.includes(player.playerId)}
                          disabled={isBusy || (
                            !selectedPlayerIds.includes(player.playerId)
                            && selectedPlayerIds.length >= game.requiredSuspectCount
                          )}
                          onChange={() => toggleVote(player.playerId)}
                        />
                        <span>{player.playerName}</span>
                      </label>
                    ))}
                  </div>
                  <Button
                    type="submit"
                    icon={<Vote size={18} />}
                    disabled={isBusy || selectedPlayerIds.length !== game.requiredSuspectCount}
                  >
                    Potvrdi glas
                  </Button>
                </form>
              )}
            </section>
          </div>
        </div>
      ) : null}

      {game.phase === 'FINISHED' && game.result ? (
        <div className="result-layout">
          <section className={`result-banner ${game.result.winner === 'CREWMATES' ? 'crewmates' : 'impostors'}`}>
            <span className="eyebrow">Pobednici</span>
            <h2>{game.result.winner === 'CREWMATES' ? 'IGRACI' : 'IMPOSTORI'}</h2>
            {game.result.tie ? <p>Neresen vrh glasanja ide u korist impostora.</p> : null}
          </section>

          <div className="result-grid">
            <section className="panel reveal-panel">
              <span className="eyebrow">Tajna rec</span>
              <strong>{game.result.secretWord}</strong>
              <span>Impostor{game.result.impostorPlayerIds.length > 1 ? 'i' : ''}: {
                game.result.impostorPlayerIds.map((id) => playersById.get(id)?.playerName).join(', ')
              }</span>
            </section>

            <section className="panel tally-panel">
              <div className="panel-title-row"><h2>Glasovi</h2></div>
              <ul>
                {[...game.result.tallies].sort((left, right) => right.votes - left.votes).map((tally) => (
                  <li key={tally.playerId}>
                    <span>{tally.playerName}</span>
                    <strong>{tally.votes}</strong>
                  </li>
                ))}
              </ul>
            </section>
          </div>

          <div className="sticky-actions">
            {isHost ? (
              <Button type="button" icon={<RotateCcw size={18} />} disabled={isBusy} onClick={() => void onReset()}>
                Nazad u lobby
              </Button>
            ) : <p>Ceka se host za novu rundu.</p>}
          </div>
        </div>
      ) : null}
    </section>
  );
}
