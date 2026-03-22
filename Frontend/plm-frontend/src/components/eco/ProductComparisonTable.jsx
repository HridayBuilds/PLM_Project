import clsx from 'clsx';
import { ArrowUp, ArrowDown, Plus, FileText } from 'lucide-react';

const ProductComparisonTable = ({ changes }) => {
  if (!changes || changes.length === 0) return null;

  const formatValue = (value, field) => {
    if (field === 'salePrice' || field === 'costPrice') {
      return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 0,
      }).format(value);
    }
    return value;
  };

  const getChangeIndicator = (item) => {
    if (item.isNew) return { icon: Plus, color: 'green', label: '(new)' };
    if (item.newValue > item.currentValue) return { icon: ArrowUp, color: 'green', label: '▲' };
    if (item.newValue < item.currentValue) return { icon: ArrowDown, color: 'red', label: '▼' };
    return { icon: null, color: 'muted', label: '' };
  };

  return (
    <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg overflow-hidden">
      <div className="p-4 border-b border-[var(--bg-border)]">
        <h4 className="text-sm font-semibold text-[var(--text-primary)]">
          Side-by-Side Comparison
        </h4>
      </div>
      <table className="table">
        <thead>
          <tr>
            <th>Field</th>
            <th className="text-center">Current</th>
            <th className="text-center">Proposed</th>
          </tr>
        </thead>
        <tbody>
          {changes.map((item, index) => {
            const indicator = getChangeIndicator(item);
            const hasChanged = item.currentValue !== item.newValue;

            return (
              <tr
                key={index}
                className={clsx(hasChanged && 'bg-[var(--yellow-dim)]')}
              >
                <td className="font-medium text-[var(--text-primary)]">
                  {item.label}
                </td>
                <td className="text-center text-[var(--text-secondary)]">
                  {item.isNew ? '—' : formatValue(item.currentValue, item.field)}
                </td>
                <td className="text-center">
                  <span
                    className={clsx(
                      'inline-flex items-center gap-1 font-medium',
                      `text-[var(--${indicator.color})]`
                    )}
                  >
                    {formatValue(item.newValue, item.field)}
                    {indicator.label && (
                      <span className="text-xs">{indicator.label}</span>
                    )}
                  </span>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default ProductComparisonTable;
