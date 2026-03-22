import { useState, useMemo } from 'react';
import { ChevronUp, ChevronDown, ChevronLeft, ChevronRight } from 'lucide-react';
import EmptyState from './EmptyState';

const DataTable = ({
  columns,
  data,
  onRowClick,
  sortable = true,
  pagination = true,
  pageSize: initialPageSize = 10,
  emptyState,
  isLoading = false,
}) => {
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(initialPageSize);

  const sortedData = useMemo(() => {
    if (!sortConfig.key || !sortable) return data;

    return [...data].sort((a, b) => {
      const aValue = a[sortConfig.key];
      const bValue = b[sortConfig.key];

      if (aValue === null || aValue === undefined) return 1;
      if (bValue === null || bValue === undefined) return -1;

      if (typeof aValue === 'string') {
        const comparison = aValue.localeCompare(bValue);
        return sortConfig.direction === 'asc' ? comparison : -comparison;
      }

      if (sortConfig.direction === 'asc') {
        return aValue > bValue ? 1 : -1;
      }
      return aValue < bValue ? 1 : -1;
    });
  }, [data, sortConfig, sortable]);

  const paginatedData = useMemo(() => {
    if (!pagination) return sortedData;
    const start = (currentPage - 1) * pageSize;
    return sortedData.slice(start, start + pageSize);
  }, [sortedData, currentPage, pageSize, pagination]);

  const totalPages = Math.ceil(data.length / pageSize);
  const startRecord = (currentPage - 1) * pageSize + 1;
  const endRecord = Math.min(currentPage * pageSize, data.length);

  const handleSort = (key) => {
    if (!sortable) return;
    setSortConfig((prev) => ({
      key,
      direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc',
    }));
  };

  const containerStyle = {
    backgroundColor: 'var(--bg-surface)',
    border: '1px solid var(--bg-border)',
    borderRadius: '8px',
    overflow: 'hidden',
  };

  const thStyle = (column) => ({
    backgroundColor: 'var(--bg-elevated)',
    textTransform: 'uppercase',
    fontSize: '0.75rem',
    fontWeight: 500,
    letterSpacing: '0.08em',
    color: 'var(--text-muted)',
    padding: '0.75rem 1rem',
    textAlign: column.align || 'left',
    borderBottom: '1px solid var(--bg-border)',
    cursor: column.sortable !== false && sortable ? 'pointer' : 'default',
    userSelect: 'none',
    width: column.width,
  });

  const tdStyle = (column) => ({
    padding: '0.875rem 1rem',
    borderBottom: '1px solid var(--bg-border)',
    color: 'var(--text-primary)',
    textAlign: column.align || 'left',
    fontFamily: column.mono ? 'var(--font-mono)' : 'inherit',
    fontSize: column.mono ? '0.875rem' : 'inherit',
  });

  const trStyle = (index) => ({
    backgroundColor: index % 2 === 0 ? 'var(--bg-surface)' : 'var(--bg-base)',
    cursor: onRowClick ? 'pointer' : 'default',
  });

  if (isLoading) {
    return (
      <div style={containerStyle}>
        <div style={{ padding: '2rem' }}>
          <div style={{ height: '1rem', backgroundColor: 'var(--bg-elevated)', borderRadius: '4px', width: '25%', marginBottom: '1rem' }} />
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {[...Array(5)].map((_, i) => (
              <div key={i} style={{ height: '2.5rem', backgroundColor: 'var(--bg-elevated)', borderRadius: '4px' }} />
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div style={containerStyle}>
        {emptyState || (
          <EmptyState
            title="No data found"
            message="There are no records to display."
          />
        )}
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <div style={{ overflowX: 'auto' }}>
        <table className="table">
          <thead>
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  style={thStyle(column)}
                  onClick={() => column.sortable !== false && handleSort(column.key)}
                >
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.25rem',
                    justifyContent: column.align === 'right' ? 'flex-end' : column.align === 'center' ? 'center' : 'flex-start'
                  }}>
                    {column.header}
                    {sortable && column.sortable !== false && (
                      <span style={{ display: 'flex', flexDirection: 'column' }}>
                        <ChevronUp
                          size={12}
                          style={{
                            marginBottom: '-4px',
                            color: sortConfig.key === column.key && sortConfig.direction === 'asc' ? 'var(--accent)' : 'var(--text-muted)'
                          }}
                        />
                        <ChevronDown
                          size={12}
                          style={{
                            color: sortConfig.key === column.key && sortConfig.direction === 'desc' ? 'var(--accent)' : 'var(--text-muted)'
                          }}
                        />
                      </span>
                    )}
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {paginatedData.map((row, rowIndex) => (
              <tr
                key={row.id || rowIndex}
                onClick={() => onRowClick?.(row)}
                style={trStyle(rowIndex)}
              >
                {columns.map((column) => (
                  <td key={column.key} style={tdStyle(column)}>
                    {column.render ? column.render(row[column.key], row) : row[column.key]}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {pagination && data.length > 0 && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '0.75rem 1rem',
          borderTop: '1px solid var(--bg-border)',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            <span>
              Showing {startRecord}–{endRecord} of {data.length}
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Per page:</span>
              <select
                value={pageSize}
                onChange={(e) => {
                  setPageSize(Number(e.target.value));
                  setCurrentPage(1);
                }}
                style={{
                  backgroundColor: 'var(--bg-elevated)',
                  border: '1px solid var(--bg-border)',
                  borderRadius: '4px',
                  padding: '0.25rem 0.5rem',
                  color: 'var(--text-primary)',
                  fontSize: '0.875rem',
                }}
              >
                <option value={10}>10</option>
                <option value={25}>25</option>
                <option value={50}>50</option>
              </select>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
              <button
                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                style={{
                  padding: '0.375rem',
                  borderRadius: '4px',
                  border: 'none',
                  background: 'transparent',
                  color: 'var(--text-secondary)',
                  cursor: currentPage === 1 ? 'not-allowed' : 'pointer',
                  opacity: currentPage === 1 ? 0.5 : 1,
                }}
              >
                <ChevronLeft size={18} />
              </button>
              <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', minWidth: '80px', textAlign: 'center' }}>
                Page {currentPage} of {totalPages}
              </span>
              <button
                onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                style={{
                  padding: '0.375rem',
                  borderRadius: '4px',
                  border: 'none',
                  background: 'transparent',
                  color: 'var(--text-secondary)',
                  cursor: currentPage === totalPages ? 'not-allowed' : 'pointer',
                  opacity: currentPage === totalPages ? 0.5 : 1,
                }}
              >
                <ChevronRight size={18} />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DataTable;
