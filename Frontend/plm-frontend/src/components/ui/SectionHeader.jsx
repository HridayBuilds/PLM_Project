const SectionHeader = ({ title, count, action, className }) => {
  const containerStyle = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: '1rem',
  };

  const countStyle = {
    padding: '0.125rem 0.5rem',
    fontSize: '0.75rem',
    fontWeight: 500,
    backgroundColor: 'var(--bg-elevated)',
    color: 'var(--text-secondary)',
    borderRadius: '4px',
  };

  return (
    <div style={containerStyle} className={className}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 600, color: 'var(--text-primary)' }}>{title}</h2>
        {count !== undefined && (
          <span style={countStyle}>
            {count}
          </span>
        )}
      </div>
      {action}
    </div>
  );
};

export default SectionHeader;
