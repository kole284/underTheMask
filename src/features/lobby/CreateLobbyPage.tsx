import { FormEvent, useState } from 'react';
import { Link, useHistory } from 'react-router-dom';
import { ArrowLeft, Check, Copy, Plus, Settings2, ShieldQuestion, UserRound } from 'lucide-react';
import { createLobby, getApiErrorMessage } from '../../shared/api/lobbyService';
import type { HintType } from '../../shared/api/types';
import { Button } from '../../shared/components/Button';
import { Field } from '../../shared/components/Field';
import { SegmentedControl } from '../../shared/components/SegmentedControl';
import { BrandMark } from '../../shared/components/BrandMark';
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
      <header className="screen-topbar">
        <BrandMark linkToHome />
        <Link to="/" className="back-link compact"><ArrowLeft size={18} /> Početna</Link>
      </header>

      <div className="setup-layout">
        <div className="setup-intro">
          <p className="eyebrow">Novi lobby</p>
          <h1>Postavi pravila za novu rundu.</h1>
          <p>Ti si host. Podešavanja ostaju otključana dok ne pokreneš igru.</p>
          <div className="setup-notes">
            <span><UserRound size={18} /> Izaberi ime hosta</span>
            <span><ShieldQuestion size={18} /> Podesi broj impostora</span>
            <span><Settings2 size={18} /> Promeni opcije i kasnije</span>
          </div>
        </div>

        <div className="setup-workspace">
          <form className="panel form-panel" onSubmit={handleSubmit}>
            <div className="form-panel-heading">
              <span className="step-number">01</span>
              <div><span className="control-label">Podešavanje sobe</span><h2>Detalji partije</h2></div>
            </div>

            <Field
              label="Ime hosta"
              placeholder="npr. Mina"
              value={hostName}
              autoComplete="nickname"
              minLength={2}
              maxLength={18}
              required
              onChange={(event) => setHostName(event.target.value)}
            />

            <div className="form-divider" />
            <SegmentedControl
              label="Broj impostora"
              value={impostorCount}
              options={[{ label: '1 impostor', value: 1 }, { label: '2 impostora', value: 2 }]}
              onChange={setImpostorCount}
            />

            <SegmentedControl
              label="Pomoc za impostora"
              value={hintType}
              options={[{ label: 'Kategorija', value: 'CATEGORY' }, { label: 'Asocijacija', value: 'ASSOCIATION' }]}
              onChange={setHintType}
            />

            {errorMessage ? <div className="form-error" role="alert">{errorMessage}</div> : null}

            <Button type="submit" icon={<Plus size={20} />} disabled={isSubmitting || hostName.trim().length < 2}>
              {isSubmitting ? 'Pravim lobby...' : 'Napravi lobby'}
            </Button>
          </form>

          {createdCode ? (
            <div className="panel created-panel" aria-live="polite">
              <div className="created-heading"><Check size={20} /><span>Lobby je spreman</span></div>
              <span className="control-label">Kod lobija</span>
              <div className="lobby-code-row">
                <strong>{createdCode}</strong>
                <Button type="button" variant="ghost" className="icon-button" aria-label="Kopiraj kod" title="Kopiraj kod" icon={<Copy size={18} />} onClick={() => navigator.clipboard?.writeText(createdCode)} />
              </div>
              <Button type="button" onClick={goToLobby}>Uđi u lobby</Button>
            </div>
          ) : null}
        </div>
      </div>
    </section>
  );
}
