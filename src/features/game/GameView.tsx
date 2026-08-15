import { useEffect, useMemo, useRef, useState, type CSSProperties, type FormEvent } from 'react';
import {
  Check,
  Eye,
  EyeOff,
  Fingerprint,
  LogOut,
  MessageCircle,
  RotateCcw,
  Send,
  ShieldAlert,
  ShieldCheck,
  Trophy,
  Users,
  Vote,
} from 'lucide-react';
import type { GameClue, GamePhase, GameStateResponse, PlayerRole } from '../../shared/api/types';
import { BrandMark } from '../../shared/components/BrandMark';
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
  compact?: boolean;
}

interface RoleCardProps {
  role: PlayerRole;
  hint: string;
  secretWord: string | null;
  revealed: boolean;
  onToggle: () => void;
}

function RoleCard({ role, hint, secretWord, revealed, onToggle }: RoleCardProps) {
  const isImpostor = role === 'IMPOSTOR';

  return (
    <section className={`role-panel ${isImpostor ? 'impostor' : 'crewmate'} ${revealed ? 'revealed' : 'concealed'}`}>
      <div className="role-panel-heading">
        <div className="role-heading-copy">
          <span className="section-icon" aria-hidden="true">{isImpostor ? <ShieldAlert size={18} /> : <ShieldCheck size={18} />}</span>
          <div><span className="eyebrow">Samo za tvoje oči</span><strong>{revealed ? (isImpostor ? 'IMPOSTOR' : 'IGRAČ') : 'ULOGA SAKRIVENA'}</strong></div>
        </div>
        <Button type="button" variant="ghost" className="icon-button" aria-label={revealed ? 'Sakrij ulogu' : 'Prikaži ulogu'} title={revealed ? 'Sakrij ulogu' : 'Prikaži ulogu'} icon={revealed ? <EyeOff size={19} /> : <Eye size={19} />} onClick={onToggle} />
      </div>

      {revealed ? (
        <div className="role-content">
          <span>{isImpostor ? 'Tvoj hint' : 'Tajna reč'}</span>
          <strong>{isImpostor ? hint : secretWord}</strong>
          <small>{isImpostor ? 'Uklopi se. Ne odaj da ne znaš reč.' : `Kategorija: ${hint}`}</small>
        </div>
      ) : (
        <button type="button" className="role-concealed" onClick={onToggle}>
          <span className="concealed-seal"><Fingerprint size={30} /></span>
          <strong>Dodirni da otkriješ</strong>
          <small>Sakrij ekran od ostalih igrača</small>
        </button>
      )}
    </section>
  );
}

function ClueChat({ clues, currentPlayerId, currentPlayerName, phase, viewerPlayerId, compact = false }: ClueChatProps) {
  const logRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const log = logRef.current;
    if (log) {
      log.scrollTop = log.scrollHeight;
    }
  }, [clues.length, currentPlayerId]);

  return (
    <section className={`panel clue-chat-panel ${compact ? 'compact-chat' : ''}`}>
      <div className="clue-chat-heading">
        <div className="clue-chat-title">
          <span className="section-icon"><MessageCircle size={18} /></span>
          <div><p className="eyebrow">Tok runde</p><h2>Tragovi za stolom</h2></div>
        </div>
        <span className="count-badge">{clues.length}</span>
      </div>
      <div ref={logRef} className="clue-chat-log" role="log" aria-live="polite" aria-relevant="additions">
        {clues.length === 0 ? (
          <div className="clue-chat-empty">
            <span><MessageCircle size={24} /></span>
            <strong>Sto je još tih</strong>
            <p>Prvi trag će se pojaviti ovde.</p>
          </div>
        ) : null}
        {clues.map((item, index) => {
          const isMine = item.playerId === viewerPlayerId;
          return (
            <article key={`${item.playerId}-${index}`} className={`clue-message ${isMine ? 'mine' : ''}`}>
              <div className="clue-message-meta">
                <span className="clue-avatar" aria-hidden="true">{item.playerName.charAt(0).toUpperCase()}</span>
                <strong>{item.playerName}{isMine ? ' (ti)' : ''}</strong>
                <small>Trag {index + 1}</small>
              </div>
              <p>{item.clue}</p>
            </article>
          );
        })}
        {phase === 'CLUES' && currentPlayerId ? (
          <div className="clue-chat-waiting">
            <span aria-hidden="true" />
            {currentPlayerId === viewerPlayerId ? 'Tvoj potez je aktivan' : `${currentPlayerName ?? 'Igrač'} bira trag`}
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

  const playersById = useMemo(() => new Map(game.players.map((player) => [player.playerId, player])), [game.players]);
  const currentPlayer = game.currentPlayerId ? playersById.get(game.currentPlayerId) : null;
  const isMyTurn = game.currentPlayerId === playerId;
  const phaseIndex = game.phase === 'CLUES' ? 0 : game.phase === 'VOTING' ? 1 : 2;

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
    if (!normalizedClue) return;
    await onSubmitClue(normalizedClue);
    setClue('');
  }

  async function handleVoteSubmit(event: FormEvent) {
    event.preventDefault();
    if (selectedPlayerIds.length !== game.requiredSuspectCount) return;
    await onSubmitVote(selectedPlayerIds);
  }

  const phaseLabel = game.phase === 'CLUES' ? 'Tragovi' : game.phase === 'VOTING' ? 'Glasanje' : 'Ishod runde';
  const phaseDescription = game.phase === 'CLUES'
    ? 'Svaki trag menja sliku. Slušaj pažljivo.'
    : game.phase === 'VOTING'
      ? 'Izaberi ko se nije dovoljno dobro uklopio.'
      : 'Maske padaju. Sto sada zna istinu.';

  return (
    <section className={`screen game-screen phase-${game.phase.toLowerCase()}`}>
      <header className="screen-topbar game-topbar">
        <BrandMark linkToHome />
        <div className="game-room-meta"><span>Lobby</span><strong>{lobbyCode}</strong></div>
        <Button type="button" variant="ghost" className="small-button" icon={<LogOut size={17} />} disabled={isBusy} onClick={() => void onLeave()}>Izađi</Button>
      </header>

      {connectionStatus !== 'connected' ? (
        <div className={`connection-note ${connectionStatus}`} role="status">
          <div><strong>{connectionStatus === 'connecting' ? 'Ponovno povezivanje' : 'Real-time veza je prekinuta'}</strong><span>{connectionStatus === 'connecting' ? 'Vraćamo stanje partije...' : 'Pokušavamo da obnovimo vezu bez gubitka runde.'}</span></div>
        </div>
      ) : null}
      {errorMessage ? <div className="connection-note error" role="alert">{errorMessage}</div> : null}

      <div className="game-status-bar" aria-label="Napredak runde">
        {['Tragovi', 'Glasanje', 'Rezultat'].map((label, index) => (
          <div key={label} className={`phase-step ${index === phaseIndex ? 'active' : ''} ${index < phaseIndex ? 'complete' : ''}`}>
            <span>{index < phaseIndex ? <Check size={14} /> : index + 1}</span><strong>{label}</strong>
          </div>
        ))}
      </div>

      <header className="game-header">
        <div><p className="eyebrow">Aktivna faza</p><h1>{phaseLabel}</h1><p>{phaseDescription}</p></div>
        <div className="game-player-count"><Users size={20} /><div><strong>{game.totalPlayers}</strong><span>igrača u rundi</span></div></div>
      </header>

      {game.phase === 'CLUES' ? (
        <div className="game-phase-layout clue-phase-layout">
          <div className="game-side-column">
            <RoleCard role={state.role} hint={state.hint} secretWord={state.secretWord} revealed={roleRevealed} onToggle={() => setRoleRevealed((visible) => !visible)} />

            <section className={`turn-panel ${isMyTurn ? 'mine' : ''}`}>
              <div className="turn-avatar" aria-hidden="true">{isMyTurn ? 'TI' : currentPlayer?.playerName.charAt(0).toUpperCase()}</div>
              <div><span className="eyebrow">Na potezu</span><h2>{isMyTurn ? 'Tvoj trag' : currentPlayer?.playerName}</h2><p>{isMyTurn ? 'Jedna reč. Bez direktnog otkrivanja.' : 'Posmatraj trag i reakcije za stolom.'}</p></div>
              <span className="turn-pulse" aria-hidden="true" />
            </section>

            {isMyTurn ? (
              <form className="game-action-panel game-action-form" onSubmit={handleClueSubmit}>
                <label htmlFor="clue"><span>Tvoj trag</span><small>{clue.length}/80</small></label>
                <div className="action-input-row">
                  <input id="clue" className="input" maxLength={80} placeholder="Upiši jedan trag" value={clue} autoComplete="off" onChange={(event) => setClue(event.target.value)} />
                  <Button type="submit" className="send-button" aria-label="Pošalji trag" title="Pošalji trag" icon={<Send size={19} />} disabled={isBusy || !clue.trim()} />
                </div>
              </form>
            ) : (
              <div className="game-action-panel waiting-action"><span className="waiting-dots"><i /><i /><i /></span><p>Čeka se trag igrača <strong>{currentPlayer?.playerName}</strong></p></div>
            )}
          </div>

          <ClueChat clues={game.clues} currentPlayerId={game.currentPlayerId} currentPlayerName={currentPlayer?.playerName} phase={game.phase} viewerPlayerId={playerId} />
        </div>
      ) : null}

      {game.phase === 'VOTING' ? (
        <div className="voting-workspace">
          <aside className="voting-context">
            <RoleCard role={state.role} hint={state.hint} secretWord={state.secretWord} revealed={roleRevealed} onToggle={() => setRoleRevealed((visible) => !visible)} />
            <div className="vote-progress-panel">
              <div className="vote-progress-copy"><span className="section-icon"><Vote size={18} /></span><div><span className="eyebrow">Status glasanja</span><strong>{game.votesSubmitted}/{game.totalPlayers} glasova</strong></div></div>
              <div className="progress-track"><span style={{ width: `${(game.votesSubmitted / game.totalPlayers) * 100}%` }} /></div>
            </div>
          </aside>

          <div className="voting-main">
            <ClueChat clues={game.clues} currentPlayerId={game.currentPlayerId} phase={game.phase} viewerPlayerId={playerId} compact />

            <section className="panel vote-panel">
              <div className="panel-heading">
                <div><span className="section-icon"><Fingerprint size={18} /></span><div><p className="eyebrow">Tvoja odluka</p><h2>Ko je impostor?</h2></div></div>
                <span className={`selection-counter ${selectedPlayerIds.length === game.requiredSuspectCount ? 'complete' : ''}`}>{selectedPlayerIds.length}/{game.requiredSuspectCount}</span>
              </div>
              {state.hasSubmittedVote ? (
                <div className="vote-complete"><span><Check size={22} /></span><div><strong>Glas je zabeležen</strong><p>Čekamo da ostali igrači donesu odluku.</p></div></div>
              ) : (
                <form onSubmit={handleVoteSubmit}>
                  <div className="vote-options">
                    {game.players.filter((player) => player.playerId !== playerId).map((player) => {
                      const selected = selectedPlayerIds.includes(player.playerId);
                      const disabled = isBusy || (!selected && selectedPlayerIds.length >= game.requiredSuspectCount);
                      return (
                        <label key={player.playerId} className={`vote-option ${selected ? 'selected' : ''} ${disabled ? 'disabled' : ''}`}>
                          <input type="checkbox" checked={selected} disabled={disabled} onChange={() => toggleVote(player.playerId)} />
                          <span className="vote-avatar" aria-hidden="true">{player.playerName.charAt(0).toUpperCase()}</span>
                          <span className="vote-player-copy"><strong>{player.playerName}</strong><small>{player.connected ? 'Za stolom' : 'Van mreže'}</small></span>
                          <span className="selection-mark"><Check size={16} /></span>
                        </label>
                      );
                    })}
                  </div>
                  <div className="vote-submit-row">
                    <p>Izaberi tačno {game.requiredSuspectCount} {game.requiredSuspectCount === 1 ? 'igrača' : 'igrača'}.</p>
                    <Button type="submit" icon={<Vote size={18} />} disabled={isBusy || selectedPlayerIds.length !== game.requiredSuspectCount}>Potvrdi glas</Button>
                  </div>
                </form>
              )}
            </section>
          </div>
        </div>
      ) : null}

      {game.phase === 'FINISHED' && game.result ? (() => {
        const crewWon = game.result.winner === 'CREWMATES';
        const maxVotes = Math.max(1, ...game.result.tallies.map((tally) => tally.votes));
        return (
          <div className={`result-layout ${crewWon ? 'crewmates' : 'impostors'}`}>
            <section className="result-banner">
              <span className="result-emblem" aria-hidden="true">{crewWon ? <ShieldCheck size={38} /> : <ShieldAlert size={38} />}</span>
              <p className="eyebrow">Runda je završena</p>
              <h2>{crewWon ? 'IGRAČI POBEĐUJU' : 'IMPOSTORI POBEĐUJU'}</h2>
              <p>{crewWon ? 'Sto je pročitao tragove i skinuo pravu masku.' : 'Sumnja je otišla na pogrešnu stranu stola.'}</p>
              {game.result.tie ? <span className="tie-note">Nerešen vrh glasanja ide u korist impostora.</span> : null}
            </section>

            <div className="result-grid">
              <section className="reveal-panel">
                <div className="secret-word-block"><span className="eyebrow">Tajna reč</span><strong>{game.result.secretWord}</strong></div>
                <div className="impostor-reveal"><span className="section-icon"><Fingerprint size={19} /></span><div><span>Maska je pripadala</span><strong>{game.result.impostorPlayerIds.map((id) => playersById.get(id)?.playerName).join(', ')}</strong></div></div>
              </section>

              <section className="panel tally-panel">
                <div className="panel-heading compact-heading"><div><span className="section-icon"><Trophy size={18} /></span><div><p className="eyebrow">Odluka stola</p><h2>Glasovi</h2></div></div></div>
                <ol>
                  {[...game.result.tallies].sort((left, right) => right.votes - left.votes).map((tally, index) => (
                    <li key={tally.playerId} style={{ '--vote-share': `${(tally.votes / maxVotes) * 100}%` } as CSSProperties}>
                      <span className="tally-rank">{String(index + 1).padStart(2, '0')}</span>
                      <span className="tally-name">{tally.playerName}</span>
                      <span className="tally-bar"><i /></span>
                      <strong>{tally.votes}</strong>
                    </li>
                  ))}
                </ol>
              </section>
            </div>

            <div className="result-action">
              {isHost ? (
                <Button type="button" icon={<RotateCcw size={18} />} disabled={isBusy} onClick={() => void onReset()}>Nazad u lobby</Button>
              ) : <div className="waiting-action"><span className="waiting-dots"><i /><i /><i /></span><p>Čeka se host za novu rundu.</p></div>}
            </div>
          </div>
        );
      })() : null}
    </section>
  );
}
