import type { ReactNode } from 'react';

interface AppLayoutProps {
  children: ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
  return (
    <main className="app-shell">
      <div className="app-backdrop" aria-hidden="true" />
      <div className="app-content">{children}</div>
    </main>
  );
}
