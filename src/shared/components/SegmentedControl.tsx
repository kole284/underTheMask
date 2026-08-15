interface SegmentedControlOption<TValue extends string | number> {
  label: string;
  value: TValue;
}

interface SegmentedControlProps<TValue extends string | number> {
  label: string;
  disabled?: boolean;
  options: SegmentedControlOption<TValue>[];
  value: TValue;
  onChange: (value: TValue) => void;
}

export function SegmentedControl<TValue extends string | number>({
  disabled = false,
  label,
  options,
  value,
  onChange,
}: SegmentedControlProps<TValue>) {
  return (
    <div className="segmented-field">
      <span className="control-label">{label}</span>
      <div className="segmented-control" role="group" aria-label={label}>
        {options.map((option) => (
          <button
            key={option.value}
            type="button"
            className={option.value === value ? 'segment active' : 'segment'}
            disabled={disabled}
            aria-pressed={option.value === value}
            onClick={() => onChange(option.value)}
          >
            {option.label}
          </button>
        ))}
      </div>
    </div>
  );
}
