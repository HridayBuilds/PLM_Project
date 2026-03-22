import clsx from 'clsx';
import { ArrowUp, ArrowDown, Plus, X, Minus } from 'lucide-react';

const BomComparisonTable = ({ components, operations }) => {
  const getChangeType = (item) => {
    if (item.isNew) return 'added';
    if (item.isRemoved) return 'removed';
    if (item.oldQty !== item.newQty || item.oldDuration !== item.newDuration) return 'changed';
    return 'unchanged';
  };

  const getChangeIcon = (item) => {
    const type = getChangeType(item);
    if (type === 'added') return <Plus size={14} />;
    if (type === 'removed') return <X size={14} />;
    if (item.newQty > item.oldQty || item.newDuration > item.oldDuration) {
      return <ArrowUp size={14} />;
    }
    if (item.newQty < item.oldQty || item.newDuration < item.oldDuration) {
      return <ArrowDown size={14} />;
    }
    return <Minus size={14} />;
  };

  const getChangeLabel = (item, field = 'qty') => {
    const type = getChangeType(item);
    if (type === 'added') return '+ Added';
    if (type === 'removed') return '× Removed';

    const oldVal = field === 'qty' ? item.oldQty : item.oldDuration;
    const newVal = field === 'qty' ? item.newQty : item.newDuration;

    if (oldVal === newVal) return '—';

    const diff = newVal - oldVal;
    return diff > 0 ? `▲ +${diff}` : `▼ ${diff}`;
  };

  const getRowClasses = (item) => {
    const type = getChangeType(item);
    return clsx(
      type === 'added' && 'bg-[var(--green-dim)]',
      type === 'removed' && 'bg-[var(--red-dim)]',
      type === 'changed' && 'bg-[var(--yellow-dim)]'
    );
  };

  const getTextClasses = (item) => {
    const type = getChangeType(item);
    return clsx(
      type === 'added' && 'text-[var(--green)]',
      type === 'removed' && 'text-[var(--red)]',
      type === 'changed' && 'text-[var(--yellow)]',
      type === 'unchanged' && 'text-[var(--text-muted)]'
    );
  };

  return (
    <div className="space-y-6">
      {/* Component Changes */}
      {components && components.length > 0 && (
        <div>
          <h4 className="text-sm font-semibold text-[var(--text-primary)] mb-3">
            Component Changes
          </h4>
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg overflow-hidden">
            <table className="table">
              <thead>
                <tr>
                  <th>Component</th>
                  <th className="text-center">Old Qty</th>
                  <th className="text-center">New Qty</th>
                  <th className="text-center">Change</th>
                </tr>
              </thead>
              <tbody>
                {components.map((item, index) => (
                  <tr key={index} className={getRowClasses(item)}>
                    <td className="font-medium">{item.name}</td>
                    <td className="text-center">
                      {item.isNew ? '—' : item.oldQty}
                    </td>
                    <td className="text-center">
                      {item.isRemoved ? '—' : item.newQty}
                    </td>
                    <td className={clsx('text-center font-medium', getTextClasses(item))}>
                      <span className="inline-flex items-center gap-1">
                        {getChangeLabel(item)}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Operation Changes */}
      {operations && operations.length > 0 && (
        <div>
          <h4 className="text-sm font-semibold text-[var(--text-primary)] mb-3">
            Operation Changes
          </h4>
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg overflow-hidden">
            <table className="table">
              <thead>
                <tr>
                  <th>Operation</th>
                  <th>Work Center</th>
                  <th className="text-center">Old Duration</th>
                  <th className="text-center">New Duration</th>
                  <th className="text-center">Change</th>
                </tr>
              </thead>
              <tbody>
                {operations.map((item, index) => (
                  <tr key={index} className={getRowClasses(item)}>
                    <td className="font-medium">{item.name}</td>
                    <td>{item.workCenter}</td>
                    <td className="text-center">
                      {item.isNew ? '—' : `${item.oldDuration} mins`}
                    </td>
                    <td className="text-center">
                      {item.isRemoved ? '—' : `${item.newDuration} mins`}
                    </td>
                    <td className={clsx('text-center font-medium', getTextClasses(item))}>
                      <span className="inline-flex items-center gap-1">
                        {getChangeLabel(item, 'duration')}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default BomComparisonTable;
