import { NavLink } from 'react-router-dom';

const SidebarNavItem = ({ to, icon: Icon, label, collapsed }) => {
  const baseStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    height: '40px',
    padding: '0 0.75rem',
    borderRadius: '4px',
    textDecoration: 'none',
    transition: 'all 0.15s ease',
    justifyContent: collapsed ? 'center' : 'flex-start',
  };

  const activeStyle = {
    ...baseStyle,
    backgroundColor: 'var(--accent-dim)',
    borderLeft: '2px solid var(--accent)',
    color: 'var(--accent)',
  };

  const inactiveStyle = {
    ...baseStyle,
    color: 'var(--text-secondary)',
  };

  return (
    <NavLink
      to={to}
      style={({ isActive }) => isActive ? activeStyle : inactiveStyle}
      title={collapsed ? label : undefined}
    >
      <Icon size={20} style={{ flexShrink: 0 }} />
      {!collapsed && (
        <span style={{ fontSize: '0.875rem', fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{label}</span>
      )}
    </NavLink>
  );
};

export default SidebarNavItem;
