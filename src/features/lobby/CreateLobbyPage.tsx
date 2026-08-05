import { FormEvent, useState } from 'react';
import { Link, useHistory } from 'react-router-dom';
import { ArrowLeft, Copy, Plus } from 'lucide-react';
import { createLobby, getApiErrorMessage } from '../../shared/api/lobbyService';
import type { HintType } from '../../shared/api/types';
import { Button } from '../../shared/components/Button';
import { Field } from '../../shared/components/Field';
import { SegmentedControl } from '../../shared/components/SegmentedControl';
import { saveLobbySession } from '../../shared/storage/sessionStorage';

export function CreateLobbyPage() {
  const history = useHistory();
  const [hostName, setHostName] = useState('');
  const [impostorCount, setImpostorCount] = useState<1 | 2>(1);
  const [hintType, setHintType] = useState<HintType>('CATEGORY');
  const [createdCode, setCreatedCode] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const session = await createLobby({
        hostName: hostName.trim(),
        impostorCount,
        hintType,
      });

      saveLobbySession(session);
      setCreatedCode(session.lobbyCode);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  function goToLobby() {
    history.push(`/lobbies/${createdCode}`);
  }

  return (
    <section className="screen form-screen">
      <Link to="/" className="back-link">
        <ArrowLeft size={18} />
        Pocetna
      </Link>

      <div className="screen-heading">
        <p className="eyebrow">Novi lobby</p>
        <h1>Napravi sobu za ekipu</h1>
      </div>

      <form className="panel form-panel" onSubmit={handleSubmit}>
        <Field
          label="Ime hosta"
          placeholder="npr. Mina"
          value={hostName}
          minLength={2}
          maxLength={18}
          required
          onChange={(event) => setHostName(event.target.value)}
        />

        <SegmentedControl
          label="Broj impostora"
          value={impostorCount}
          options={[
            { label: '1', value: 1 },
            { label: '2', value: 2 },
          ]}
          onChange={setImpostorCount}
        />

        <SegmentedControl
          label="Pomoc za impostora"
          value={hintType}
          options={[
            { label: 'Kategorija', value: 'CATEGORY' },
            { label: 'Asocijacija', value: 'ASSOCIATION' },
          ]}
          onChange={setHintType}
        />

        {errorMessage ? <p className="form-error">{errorMessage}</p> : null}

        <Button type="submit" icon={<Plus size={20} />} disabled={isSubmitting || hostName.trim().length < 2}>
          {isSubmitting ? 'Pravim lobby...' : 'Napravi lobby'}
        </Button>
      </form>

      {createdCode ? (
        <div className="panel created-panel">
          <span className="control-label">Kod lobija</span>
          <div className="lobby-code-row">
            <strong>{createdCode}</strong>
            <Button
              type="button"
              variant="ghost"
              className="icon-button"
              aria-label="Kopiraj kod"
              icon={<Copy size={18} />}
              onClick={() => navigator.clipboard?.writeText(createdCode)}
            />
          </div>
          <Button type="button" onClick={goToLobby}>
            Udji u lobby
          </Button>
        </div>
      ) : null}
    </section>
  );
}
