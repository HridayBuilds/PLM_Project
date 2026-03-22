import { Search } from 'lucide-react';
import clsx from 'clsx';

const SearchInput = ({ value, onChange, placeholder = 'Search...', className }) => {
  return (
    <div className={clsx('relative', className)}>
      <Search
        size={18}
        className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)]"
      />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="input pl-10 w-full"
      />
    </div>
  );
};

export default SearchInput;
