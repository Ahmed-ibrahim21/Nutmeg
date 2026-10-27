import { NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../AuthContext';

const NAV_ITEMS = [
  { to: '/',          icon: '🏠', label: 'Dashboard' },
  { to: '/squad',     icon: '👥', label: 'Squad' },
  { to: '/tactics',   icon: '📋', label: 'Tactics' },
  { to: '/lineup',    icon: '⚡', label: 'Lineup' },
  { to: '/league',    icon: '🏆', label: 'League' },
  { to: '/transfers', icon: '🔄', label: 'Market' },
  { to: '/transfers/my', icon: '📨', label: 'My Transfers' },
  { to: '/finances',  icon: '💰', label: 'Finances' },
];

export default function AppShell({ children }) {
  const { manager, logout } = useAuth();
  const location = useLocation();

  return (
    <div className="app-shell">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar__brand">
          <div className="sidebar__brand-icon">⚽</div>
          <span className="sidebar__brand-name">Nutmeg</span>
        </div>

        <nav className="sidebar__nav">
          {NAV_ITEMS.map(({ to, icon, label }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) =>
                `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
              }
            >
              <span className="sidebar__link-icon">{icon}</span>
              <span className="sidebar__link-label">{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar__footer">
          <div className="sidebar__user">
            <div className="sidebar__avatar">
              {manager?.username?.charAt(0).toUpperCase() || 'M'}
            </div>
            <div className="sidebar__user-info">
              <span className="sidebar__username">{manager?.username}</span>
              <span className="sidebar__role">Manager</span>
            </div>
          </div>
          <button className="sidebar__logout" onClick={logout} title="Sign Out">
            ↪
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
