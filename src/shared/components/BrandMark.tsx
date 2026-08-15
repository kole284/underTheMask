import { Link } from 'react-router-dom';

interface BrandMarkProps {
  compact?: boolean;
  linkToHome?: boolean;
}

export function BrandMark({ compact = false, linkToHome = false }: BrandMarkProps) {
  const content = (
    <>
      <img className="brand-mark-image" src="/under-the-mask-logo.svg" alt="" />
      {!compact ? (
        <span className="brand-mark-copy">
          <strong>Under The Mask</strong>
        </span>
      ) : null}
    </>
  );

  return linkToHome ? (
    <Link to="/" className={`brand-mark ${compact ? 'compact' : ''}`} aria-label="Under The Mask početna">
      {content}
    </Link>
  ) : (
    <div className={`brand-mark ${compact ? 'compact' : ''}`}>{content}</div>
  );
}
