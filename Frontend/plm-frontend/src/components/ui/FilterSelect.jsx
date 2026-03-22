import { ChevronDown } from 'lucide-react';
import clsx from 'clsx';

const FilterSelect = ({ value, onChange, options, placeholder = 'Select...', className }) => {
  return (
    <div className={clsx('relative', className)}>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="input appearance-none pr-10 cursor-pointer"
      >
        {placeholder && (
          <option value="" disabled>
            {placeholder}
          </option>
        )}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      <ChevronDown
        size={18}
        className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)] pointer-events-none"
      />
    </div>
  );
};

export default FilterSelect;
