import { TrendingUp, TrendingDown } from 'lucide-react';

const StatCard = ({ label, value, icon: Icon, trend, trendValue, className }) => {
  const cardStyle = {
    backgroundColor: 'var(--bg-surface)',
    border: '1px solid var(--bg-border)',
    borderRadius: '8px',
    padding: '1.25rem',
  };

  const iconBoxStyle = {
    width: '40px',
    height: '40px',
    borderRadius: '8px',
    backgroundColor: 'var(--accent-dim)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  };

  const trendStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '0.25rem',
    marginTop: '0.5rem',
    fontSize: '0.75rem',
    fontWeight: 500,
    color: trend === 'up' ? 'var(--green)' : 'var(--red)',
  };

  return (
    <div style={cardStyle} className={className}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '0.25rem' }}>{label}</p>
          <p style={{ fontSize: '1.875rem', fontWeight: 700, color: 'var(--text-primary)', fontFamily: 'var(--font-display)' }}>
            {value}
          </p>

          {trend && (
            <div style={trendStyle}>
              {trend === 'up' ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
              <span>{trendValue}</span>
            </div>
          )}
        </div>

        {Icon && (
          <div style={iconBoxStyle}>
            <Icon size={20} style={{ color: 'var(--accent)' }} />
          </div>
        )}
      </div>
    </div>
  );
};

export default StatCard;
