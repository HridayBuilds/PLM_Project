import { Menu } from 'lucide-react';

const Topbar = ({ onMenuClick }) => {
  const headerStyle = {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    height: '56px',
    backgroundColor: 'var(--sidebar-bg)',
    borderBottom: '1px solid var(--sidebar-border)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 1rem',
    zIndex: 50,
  };

  const iconBtnStyle = {
    padding: '0.5rem',
    borderRadius: '4px',
    border: 'none',
    background: 'transparent',
    color: 'var(--text-secondary)',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'all 0.15s ease',
  };

  const logoBoxStyle = {
    width: '32px',
    height: '32px',
    backgroundColor: 'var(--accent)',
    borderRadius: '6px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    boxShadow: '0 2px 8px rgba(45, 212, 191, 0.3)',
  };

  return (
    <header style={headerStyle}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <button onClick={onMenuClick} style={iconBtnStyle} aria-label="Toggle sidebar">
          <Menu size={20} />
        </button>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
          <div style={logoBoxStyle}>
            <span style={{ color: 'white', fontWeight: 700, fontSize: '1rem', lineHeight: 1 }}>E</span>
          </div>
          <span style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-primary)', letterSpacing: '-0.02em' }}>
            Ecova
          </span>
        </div>
      </div>
    </header>
  );
};

export default Topbar;
