import type { ReactNode } from 'react';

interface AppLayoutProps {
  children: ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
  return (
    <main className="app-shell">
      <div className="phone-frame">
        {children}
      </div>
    </main>
  );
}
