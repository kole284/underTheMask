import type { InputHTMLAttributes, ReactNode } from 'react';

interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  hint?: string;
  action?: ReactNode;
}

export function Field({ label, hint, action, className = '', ...props }: FieldProps) {
  return (
    <label className="field">
      <span className="field-row">
        <span>{label}</span>
        {action}
      </span>
      <input className={`input ${className}`} {...props} />
      {hint ? <span className="field-hint">{hint}</span> : null}
    </label>
  );
}
