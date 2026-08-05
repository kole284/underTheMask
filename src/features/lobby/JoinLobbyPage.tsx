import { FormEvent, useState } from 'react';
import { Link, useHistory } from 'react-router-dom';
import { ArrowLeft, LogIn } from 'lucide-react';
import { getApiErrorMessage, joinLobby } from '../../shared/api/lobbyService';
import { Button } from '../../shared/components/Button';
import { Field } from '../../shared/components/Field';
import { saveLobbySession } from '../../shared/storage/sessionStorage';

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
      <Link to="/" className="back-link">
        <ArrowLeft size={18} />
        Pocetna
      </Link>

      <div className="screen-heading">
        <p className="eyebrow">Pridruzivanje</p>
        <h1>Unesi kod i ime igraca</h1>
      </div>

      <form className="panel form-panel" onSubmit={handleSubmit}>
        <Field
          label="Ime"
          placeholder="npr. Luka"
          value={playerName}
          minLength={2}
          maxLength={18}
          required
          onChange={(event) => setPlayerName(event.target.value)}
        />
        <Field
          label="Kod lobija"
          placeholder="BK7M2P"
          value={normalizedCode}
          minLength={6}
          maxLength={6}
          required
          className="code-input"
          hint="Kod ima 6 slova ili brojeva."
          onChange={(event) => setLobbyCode(event.target.value)}
        />
        {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
        <Button
          type="submit"
          icon={<LogIn size={20} />}
          disabled={isSubmitting || playerName.trim().length < 2 || normalizedCode.length !== 6}
        >
          {isSubmitting ? 'Ulazim...' : 'Pridruzi se lobiju'}
        </Button>
      </form>
    </section>
  );
}
