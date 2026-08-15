import { Link } from "react-router-dom";
import {
  ArrowRight,
  Eye,
  LogIn,
  MessageCircle,
  Plus,
  Users,
} from "lucide-react";
import { Button } from "../../shared/components/Button";

export function HomePage() {
  return (
    <section className="screen home-screen">
      <div className="home-layout">
        <div className="home-hero">
          <div className="home-brand-lockup" aria-label="Under The Mask">
            <div className="hero-logo-stage" aria-hidden="true">
              <img src="/under-the-mask-logo.svg" alt="" />
            </div>
            <div className="home-brand-title">
              <strong>UNDER THE MASK</strong>
            </div>
          </div>
          <h1>Ko se krije ispod maske?</h1>
          <p className="hero-copy">
            Jedna tajna reč. Nekoliko pažljivih tragova. I neko za stolom ko
            samo glumi da zna odgovor.
          </p>

          <div className="game-rhythm" aria-label="Tok runde">
            <span>
              <Eye size={17} /> Saznaj ulogu
            </span>
            <span>
              <MessageCircle size={17} /> Daj trag
            </span>
            <span>
              <Users size={17} /> Otkrij impostora
            </span>
          </div>
        </div>

        <aside className="home-entry-panel" aria-labelledby="entry-title">
          <div className="entry-panel-heading">
            <span className="entry-index">01</span>
            <div>
              <p className="eyebrow">Nova partija</p>
              <h2 id="entry-title">Okupi ekipu za stolom</h2>
            </div>
          </div>
          <p className="entry-copy">
            Napravi privatni lobby ili uđi sa kodom koji si dobio od hosta.
          </p>
          <div className="home-actions">
            <Link to="/lobbies/new" className="button-link">
              <Button
                className="link-button home-primary-action"
                icon={<Plus size={20} />}
              >
                Napravi lobby
              </Button>
            </Link>
            <Link to="/lobbies/join" className="button-link">
              <Button
                variant="secondary"
                className="link-button"
                icon={<LogIn size={20} />}
              >
                Pridruži se kodom
              </Button>
            </Link>
          </div>
          <div className="entry-footer">
            <span>3-12 igrača</span>
            <ArrowRight size={17} aria-hidden="true" />
            <span>Bez registracije</span>
          </div>
        </aside>
      </div>
    </section>
  );
}
