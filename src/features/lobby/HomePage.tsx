import { Link } from 'react-router-dom';
import { LogIn, Plus } from 'lucide-react';
import { Button } from '../../shared/components/Button';

export function HomePage() {
  return (
    <section className="screen home-screen">
      <div className="brand-block">
        <div className="mask-mark" aria-hidden="true">
          <span />
          <span />
        </div>
        <p className="eyebrow">Under The Mask</p>
        <h1>Ko zna rec, a ko samo glumi?</h1>
      </div>

      <div className="home-actions">
        <Link to="/lobbies/new" className="button-link">
          <Button className="link-button" icon={<Plus size={20} />}>
            Napravi lobby
          </Button>
        </Link>
        <Link to="/lobbies/join" className="button-link">
          <Button variant="secondary" className="link-button" icon={<LogIn size={20} />}>
            Pridruzi se lobiju
          </Button>
        </Link>
      </div>
    </section>
  );
}
