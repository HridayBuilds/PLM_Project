import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Topbar from './Topbar';
import Sidebar from './Sidebar';

const AppShell = () => {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  const toggleSidebar = () => {
    setSidebarCollapsed(!sidebarCollapsed);
  };

  return (
    <div className="app-shell">
      <Topbar onMenuClick={toggleSidebar} />
      <Sidebar collapsed={sidebarCollapsed} onToggle={toggleSidebar} />

      <main
        className={sidebarCollapsed ? 'main-content sidebar-collapsed' : 'main-content'}
      >
        <div className="page-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default AppShell;
