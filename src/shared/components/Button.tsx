import type { ButtonHTMLAttributes, ReactNode } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  icon?: ReactNode;
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
}

export function Button({ children, className = '', icon, variant = 'primary', ...props }: ButtonProps) {
  return (
    <button className={`button button-${variant} ${className}`} {...props}>
      {icon ? <span className="button-icon" aria-hidden="true">{icon}</span> : null}
      {children ? <span>{children}</span> : null}
    </button>
  );
}
