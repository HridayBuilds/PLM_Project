import { FileX } from 'lucide-react';

const EmptyState = ({ icon: Icon = FileX, title, message, action, className }) => {
  const containerStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '4rem 0',
    textAlign: 'center',
  };

  const iconBoxStyle = {
    width: '64px',
    height: '64px',
    borderRadius: '50%',
    backgroundColor: 'var(--bg-elevated)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: '1rem',
  };

  return (
    <div style={containerStyle} className={className}>
      <div style={iconBoxStyle}>
        <Icon size={32} style={{ color: 'var(--text-muted)' }} />
      </div>
      <h3 style={{ fontSize: '1.125rem', fontWeight: 500, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>{title}</h3>
      <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', maxWidth: '24rem', marginBottom: '1rem' }}>{message}</p>
      {action}
    </div>
  );
};

export default EmptyState;
