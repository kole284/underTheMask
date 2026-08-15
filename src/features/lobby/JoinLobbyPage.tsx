import { FormEvent, useState } from 'react';
import { Link, useHistory } from 'react-router-dom';
import { ArrowLeft, KeyRound, LogIn, Radio, UserRound } from 'lucide-react';
import { getApiErrorMessage, joinLobby } from '../../shared/api/lobbyService';
import { Button } from '../../shared/components/Button';
import { Field } from '../../shared/components/Field';
import { saveLobbySession } from '../../shared/storage/sessionStorage';
import { BrandMark } from '../../shared/components/BrandMark';

export function JoinLobbyPage() {
  const history = useHistory();
  const [playerName, setPlayerName] = useState('');
  const [lobbyCode, setLobbyCode] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const session = await joinLobby(lobbyCode, { playerName: playerName.trim() });
      saveLobbySession(session);
      history.push(`/lobbies/${session.lobbyCode}`);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  const normalizedCode = lobbyCode.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 6);

  return (
    <section className="screen form-screen">
      <header className="screen-topbar">
        <BrandMark linkToHome />
        <Link to="/" className="back-link compact"><ArrowLeft size={18} /> Početna</Link>
      </header>

      <div className="setup-layout join-layout">
        <div className="setup-intro">
          <p className="eyebrow">Pridruživanje</p>
          <h1>Maska je spremna. Sto te čeka.</h1>
          <p>Unesi kod koji je podelio host i ime po kome će te ekipa prepoznati.</p>
          <div className="setup-notes">
            <span><KeyRound size={18} /> Kod ima 6 znakova</span>
            <span><UserRound size={18} /> Ime ostaje tokom runde</span>
            <span><Radio size={18} /> Automatsko povezivanje sa sobom</span>
          </div>
        </div>

        <form className="panel form-panel join-form" onSubmit={handleSubmit}>
          <div className="form-panel-heading">
            <span className="step-number">01</span>
            <div><span className="control-label">Ulazak u sobu</span><h2>Tvoji podaci</h2></div>
          </div>
          <Field
            label="Ime igrača"
            placeholder="npr. Luka"
            value={playerName}
            autoComplete="nickname"
            minLength={2}
            maxLength={18}
            required
            onChange={(event) => setPlayerName(event.target.value)}
          />
          <Field
            label="Kod lobija"
            placeholder="BK7M2P"
            value={normalizedCode}
            autoComplete="off"
            inputMode="text"
            minLength={6}
            maxLength={6}
            required
            className="code-input"
            hint="6 slova ili brojeva, bez razmaka."
            onChange={(event) => setLobbyCode(event.target.value)}
          />
          {errorMessage ? <div className="form-error" role="alert">{errorMessage}</div> : null}
          <Button type="submit" icon={<LogIn size={20} />} disabled={isSubmitting || playerName.trim().length < 2 || normalizedCode.length !== 6}>
            {isSubmitting ? 'Ulazim...' : 'Pridruži se lobiju'}
          </Button>
        </form>
      </div>
    </section>
  );
}
