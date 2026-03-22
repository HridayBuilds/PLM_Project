import {
  LayoutDashboard,
  Package,
  GitBranch,
  ClipboardList,
  BarChart3,
  Settings,
  ChevronLeft,
  ChevronRight,
  Moon,
  Sun,
  Bell,
  LogOut,
  User,
  Check,
  AlertCircle,
  Info
} from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import SidebarNavItem from './SidebarNavItem';
import useAuthStore from '../../context/authStore';
import useThemeStore from '../../context/themeStore';
import { can, getRoleDisplayName, getRoleBadgeColor } from '../../utils/roleGuards';

// Mock notifications - in real app, this would come from API
const mockNotifications = [
  {
    id: 1,
    type: 'approval',
    title: 'ECO Pending Approval',
    message: 'ECO-000012 requires your approval',
    time: '5 min ago',
    read: false,
  },
  {
    id: 2,
    type: 'success',
    title: 'ECO Approved',
    message: 'ECO-000010 has been approved by John Doe',
    time: '1 hour ago',
    read: false,
  },
  {
    id: 3,
    type: 'info',
    title: 'BOM Updated',
    message: 'BOM for Product XYZ-001 was updated',
    time: '2 hours ago',
    read: true,
  },
];

const Sidebar = ({ collapsed, onToggle }) => {
  const navigate = useNavigate();
  const { user, role, logout } = useAuthStore();
  const { theme, toggleTheme } = useThemeStore();
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const [notifications, setNotifications] = useState(mockNotifications);
  const notificationsRef = useRef(null);
  const profileRef = useRef(null);

  const unreadCount = notifications.filter(n => !n.read).length;

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (notificationsRef.current && !notificationsRef.current.contains(event.target)) {
        setNotificationsOpen(false);
      }
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setProfileMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const markAsRead = (id) => {
    setNotifications(prev =>
      prev.map(n => n.id === id ? { ...n, read: true } : n)
    );
  };

  const markAllAsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'approval':
        return <AlertCircle size={16} style={{ color: 'var(--yellow)' }} />;
      case 'success':
        return <Check size={16} style={{ color: 'var(--green)' }} />;
      case 'info':
      default:
        return <Info size={16} style={{ color: 'var(--blue)' }} />;
    }
  };

  const navItems = [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard', roles: null },
    { to: '/products', icon: Package, label: 'Products', roles: null },
    { to: '/bom', icon: GitBranch, label: 'Bill of Materials', permission: 'nav.bom' },
    { to: '/eco', icon: ClipboardList, label: 'Change Orders', permission: 'nav.eco' },
    { to: '/reports', icon: BarChart3, label: 'Reports', roles: null },
    { to: '/settings', icon: Settings, label: 'Product Pipeline', permission: 'nav.settings' },
  ];

  const filteredNavItems = navItems.filter((item) => {
    if (!item.permission && !item.roles) return true;
    if (item.permission) return can(role, item.permission);
    if (item.roles) return item.roles.includes(role);
    return false;
  });

  const sidebarStyle = {
    position: 'fixed',
    left: 0,
    top: '56px',
    height: 'calc(100vh - 56px)',
    backgroundColor: 'var(--sidebar-bg)',
    borderRight: '1px solid var(--sidebar-border)',
    display: 'flex',
    flexDirection: 'column',
    transition: 'width 0.2s ease',
    zIndex: 40,
    width: collapsed ? '64px' : '240px',
  };

  const toggleBtnStyle = {
    position: 'absolute',
    right: '-12px',
    top: '24px',
    width: '24px',
    height: '24px',
    backgroundColor: 'var(--bg-elevated)',
    border: '1px solid var(--bg-border)',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: 'var(--text-muted)',
    cursor: 'pointer',
  };

  const navStyle = {
    flex: 1,
    padding: '1rem 0.5rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '0.25rem',
    overflowY: 'auto',
  };

  const avatarStyle = {
    width: '32px',
    height: '32px',
    borderRadius: '50%',
    backgroundColor: 'var(--accent-dim)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: 'var(--accent)',
    fontWeight: 500,
    fontSize: '0.875rem',
    flexShrink: 0,
  };

  return (
    <aside style={sidebarStyle}>
      <button
        onClick={onToggle}
        style={toggleBtnStyle}
        aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
      >
        {collapsed ? <ChevronRight size={14} /> : <ChevronLeft size={14} />}
      </button>

      <nav style={navStyle}>
        {filteredNavItems.map((item) => (
          <SidebarNavItem
            key={item.to}
            to={item.to}
            icon={item.icon}
            label={item.label}
            collapsed={collapsed}
          />
        ))}
      </nav>

      {/* Bottom Section: Theme, Notifications, Profile */}
      <div style={{
        padding: collapsed ? '0.5rem' : '0.75rem',
        borderTop: '1px solid var(--sidebar-border)',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.5rem',
      }}>
        {/* Theme & Notifications Row */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'flex-start',
          gap: '0.5rem',
        }}>
          {/* Theme Toggle */}
          <button
            onClick={toggleTheme}
            style={{
              padding: '0.5rem',
              borderRadius: '6px',
              border: 'none',
              background: 'var(--bg-elevated)',
              color: 'var(--text-secondary)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
            title={theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme'}
          >
            {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
          </button>

          {/* Notifications */}
          {!collapsed && (
            <div style={{ position: 'relative' }} ref={notificationsRef}>
              <button
                onClick={() => setNotificationsOpen(!notificationsOpen)}
                style={{
                  padding: '0.5rem',
                  borderRadius: '6px',
                  border: 'none',
                  background: 'var(--bg-elevated)',
                  color: 'var(--text-secondary)',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  position: 'relative',
                }}
              >
                <Bell size={18} />
                {unreadCount > 0 && (
                  <span style={{
                    position: 'absolute',
                    top: '2px',
                    right: '2px',
                    minWidth: '14px',
                    height: '14px',
                    backgroundColor: 'var(--red)',
                    borderRadius: '50%',
                    fontSize: '0.6rem',
                    fontWeight: 600,
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}>
                    {unreadCount}
                  </span>
                )}
              </button>

              {notificationsOpen && (
                <div style={{
                  position: 'absolute',
                  left: '100%',
                  bottom: 0,
                  marginLeft: '0.5rem',
                  width: '320px',
                  backgroundColor: 'var(--bg-surface)',
                  border: '1px solid var(--bg-border)',
                  borderRadius: '8px',
                  boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
                  zIndex: 100,
                  maxHeight: '400px',
                  overflow: 'hidden',
                }}>
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '0.75rem 1rem',
                    borderBottom: '1px solid var(--bg-border)',
                  }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                      Notifications
                    </span>
                    {unreadCount > 0 && (
                      <button
                        onClick={markAllAsRead}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: 'var(--accent)',
                          fontSize: '0.75rem',
                          cursor: 'pointer',
                        }}
                      >
                        Mark all read
                      </button>
                    )}
                  </div>
                  <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                    {notifications.length === 0 ? (
                      <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                        No notifications
                      </div>
                    ) : (
                      notifications.map(notification => (
                        <div
                          key={notification.id}
                          onClick={() => markAsRead(notification.id)}
                          style={{
                            padding: '0.75rem 1rem',
                            display: 'flex',
                            gap: '0.75rem',
                            cursor: 'pointer',
                            backgroundColor: notification.read ? 'transparent' : 'var(--accent-dim)',
                            borderBottom: '1px solid var(--bg-border)',
                          }}
                        >
                          <div style={{
                            width: '28px',
                            height: '28px',
                            borderRadius: '50%',
                            backgroundColor: 'var(--bg-elevated)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            flexShrink: 0,
                          }}>
                            {getNotificationIcon(notification.type)}
                          </div>
                          <div style={{ flex: 1, minWidth: 0 }}>
                            <div style={{ fontWeight: 500, fontSize: '0.8125rem', color: 'var(--text-primary)' }}>
                              {notification.title}
                            </div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                              {notification.message}
                            </div>
                            <div style={{ fontSize: '0.6875rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                              {notification.time}
                            </div>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Profile Section */}
        {user && (
          <div style={{ position: 'relative' }} ref={profileRef}>
            <button
              onClick={() => setProfileMenuOpen(!profileMenuOpen)}
              style={{
                width: '100%',
                display: 'flex',
                alignItems: 'center',
                gap: '0.75rem',
                padding: collapsed ? '0.5rem' : '0.5rem 0.75rem',
                borderRadius: '8px',
                backgroundColor: profileMenuOpen ? 'var(--bg-elevated)' : 'transparent',
                border: 'none',
                cursor: 'pointer',
                justifyContent: collapsed ? 'center' : 'flex-start',
              }}
            >
              <div style={avatarStyle}>
                {(user.firstName || user.name || user.loginId)?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              {!collapsed && (
                <div style={{ flex: 1, minWidth: 0, textAlign: 'left' }}>
                  <p style={{
                    fontSize: '0.875rem',
                    fontWeight: 600,
                    color: 'var(--text-primary)',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    margin: 0,
                  }}>
                    {user.name || user.firstName || user.loginId}
                  </p>
                  <span className={`badge badge-${getRoleBadgeColor(role)}`} style={{
                    padding: '0.125rem 0.375rem',
                    fontSize: '0.625rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.04em',
                  }}>
                    {getRoleDisplayName(role)}
                  </span>
                </div>
              )}
            </button>

            {profileMenuOpen && (
              <div style={{
                position: 'absolute',
                left: collapsed ? '100%' : '0',
                bottom: '100%',
                marginBottom: '0.5rem',
                marginLeft: collapsed ? '0.5rem' : '0',
                width: collapsed ? '180px' : '100%',
                backgroundColor: 'var(--bg-surface)',
                border: '1px solid var(--bg-border)',
                borderRadius: '8px',
                boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
                zIndex: 100,
                padding: '0.25rem 0',
              }}>
                <button
                  onClick={() => { setProfileMenuOpen(false); navigate('/profile'); }}
                  style={{
                    width: '100%',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    padding: '0.625rem 1rem',
                    fontSize: '0.875rem',
                    color: 'var(--text-secondary)',
                    border: 'none',
                    background: 'transparent',
                    cursor: 'pointer',
                    textAlign: 'left',
                  }}
                >
                  <User size={16} />
                  Profile
                </button>
                <div style={{ height: '1px', backgroundColor: 'var(--bg-border)', margin: '0.25rem 0' }} />
                <button
                  onClick={handleLogout}
                  style={{
                    width: '100%',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    padding: '0.625rem 1rem',
                    fontSize: '0.875rem',
                    color: 'var(--red)',
                    border: 'none',
                    background: 'transparent',
                    cursor: 'pointer',
                    textAlign: 'left',
                  }}
                >
                  <LogOut size={16} />
                  Logout
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </aside>
  );
};

export default Sidebar;
