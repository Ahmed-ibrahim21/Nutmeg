import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../AuthContext';
import {
  getLeagues,
  getLeagueClubs,
  assignClub,
  getBalance,
  getMySquad,
  getFinancialSummary,
  getMyTactics,
} from '../api';

function formatCurrency(amount) {
  if (amount >= 1_000_000) return `£${(amount / 1_000_000).toFixed(1)}M`;
  if (amount >= 1_000) return `£${(amount / 1_000).toFixed(0)}K`;
  return `£${amount.toLocaleString()}`;
}

export default function HomePage() {
  const { token, manager, logout } = useAuth();

  // State
  const [hasClub, setHasClub] = useState(null); // null = loading
  const [clubs, setClubs] = useState([]);
  const [leagues, setLeagues] = useState([]);
  const [selectedLeague, setSelectedLeague] = useState(null);
  const [dashboardData, setDashboardData] = useState(null);
  const [loadingClubs, setLoadingClubs] = useState(false);
  const [assigningClub, setAssigningClub] = useState(null);
  const [error, setError] = useState('');

  // Check if manager has a club by trying to fetch balance
  const checkClubStatus = useCallback(async () => {
    try {
      const balance = await getBalance(token);
      // If we get a balance, the manager has a club
      setHasClub(true);
      loadDashboard();
    } catch (err) {
      if (err.message.includes('409') || err.message.includes('does not manage')) {
        setHasClub(false);
        loadLeagues();
      } else {
        setHasClub(false);
        loadLeagues();
      }
    }
  }, [token]);

  const loadLeagues = async () => {
    try {
      const data = await getLeagues(0, token);
      setLeagues(data.leagues || []);
      if (data.leagues && data.leagues.length > 0) {
        setSelectedLeague(data.leagues[0]);
        loadClubsForLeague(data.leagues[0].id);
      }
    } catch (err) {
      setError('Failed to load leagues');
    }
  };

  const loadClubsForLeague = async (leagueId) => {
    setLoadingClubs(true);
    try {
      const clubList = await getLeagueClubs(leagueId, token);
      setClubs(clubList || []);
    } catch (err) {
      setError('Failed to load clubs');
    } finally {
      setLoadingClubs(false);
    }
  };

  const loadDashboard = async () => {
    try {
      const [balance, summary, squad, tactics] = await Promise.allSettled([
        getBalance(token),
        getFinancialSummary(token),
        getMySquad(token),
        getMyTactics(token),
      ]);

      setDashboardData({
        balance: balance.status === 'fulfilled' ? balance.value : null,
        summary: summary.status === 'fulfilled' ? summary.value : null,
        squad: squad.status === 'fulfilled' ? squad.value : [],
        tactics: tactics.status === 'fulfilled' ? tactics.value : null,
      });
    } catch (err) {
      setError('Failed to load dashboard data');
    }
  };

  const handleAssignClub = async (clubId) => {
    setAssigningClub(clubId);
    setError('');
    try {
      await assignClub(clubId, token);
      setHasClub(true);
      loadDashboard();
    } catch (err) {
      setError(err.message || 'Failed to assign club');
    } finally {
      setAssigningClub(null);
    }
  };

  useEffect(() => {
    checkClubStatus();
  }, [checkClubStatus]);

  // ── Render: Top Navigation ──
  const renderNav = () => (
    <nav className="topnav">
      <div className="topnav__brand">
        <div className="topnav__brand-icon">⚽</div>
        <span className="topnav__brand-name">Nutmeg</span>
      </div>
      <div className="topnav__user">
        <span className="topnav__username">{manager?.username}</span>
        <div className="topnav__avatar">
          {manager?.username?.charAt(0).toUpperCase() || 'M'}
        </div>
        <button id="logout-btn" className="topnav__logout" onClick={logout}>
          Sign Out
        </button>
      </div>
    </nav>
  );

  // ── Render: Club Selection (no club assigned yet) ──
  const renderClubSelection = () => (
    <div className="dashboard">
      <div className="dashboard__hero">
        <div className="dashboard__hero-greeting">Welcome, Manager</div>
        <h1 className="dashboard__hero-title">Choose Your Club</h1>
        <p className="dashboard__hero-subtitle">
          Select a club to manage. Build your squad, set tactics, and lead them to victory in the league.
        </p>
      </div>

      {error && (
        <div className="alert alert--error" role="alert">
          <span>⚠</span>
          <span>{error}</span>
        </div>
      )}

      {selectedLeague && (
        <div className="section-header">
          <h2 className="section-header__title">
            {selectedLeague.name} — {selectedLeague.country}
          </h2>
          <span className="section-header__action" style={{ color: 'var(--clr-text-muted)' }}>
            Tier {selectedLeague.tier}
          </span>
        </div>
      )}

      {loadingClubs ? (
        <div className="clubs-grid">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="skeleton skeleton--card" />
          ))}
        </div>
      ) : (
        <div className="clubs-grid">
          {clubs.map((club, idx) => (
            <div
              key={club.id}
              className="club-card animate-fade-in"
              style={{ animationDelay: `${idx * 0.08}s` }}
            >
              <div className="club-card__content">
                <div className="club-card__header">
                  <div className="club-card__logo">🏟️</div>
                  <div>
                    <div className="club-card__name">{club.name}</div>
                    <div className="club-card__short">{club.shortName}</div>
                  </div>
                </div>

                <div className="club-card__details">
                  <div className="club-card__detail">
                    <span className="club-card__detail-label">Stadium</span>
                    <span className="club-card__detail-value">{club.stadiumName}</span>
                  </div>
                  <div className="club-card__detail">
                    <span className="club-card__detail-label">Budget</span>
                    <span className="club-card__detail-value">{formatCurrency(club.balance)}</span>
                  </div>
                </div>

                <button
                  className="club-card__select-btn"
                  onClick={() => handleAssignClub(club.id)}
                  disabled={assigningClub === club.id}
                >
                  {assigningClub === club.id ? 'Assigning…' : 'Manage This Club'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {!loadingClubs && clubs.length === 0 && (
        <div className="empty-state">
          <div className="empty-state__icon">🏟️</div>
          <div className="empty-state__title">No clubs available</div>
          <div className="empty-state__desc">
            All clubs in this league already have managers. Check back later.
          </div>
        </div>
      )}
    </div>
  );

  // ── Render: Dashboard (club assigned) ──
  const renderDashboard = () => {
    const squad = dashboardData?.squad || [];
    const balance = dashboardData?.balance;
    const summary = dashboardData?.summary;
    const tactics = dashboardData?.tactics;

    return (
      <div className="dashboard">
        {/* Hero */}
        <div className="dashboard__hero">
          <div className="dashboard__hero-greeting">Welcome back</div>
          <h1 className="dashboard__hero-title">
            {manager?.username}'s Dashboard
          </h1>
          <p className="dashboard__hero-subtitle">
            {tactics?.formation
              ? `Playing ${tactics.formation} formation • Coherence: ${tactics.coherenceScore || '—'}%`
              : 'Manage your team, transfers, and finances from here.'}
          </p>
        </div>

        {/* Stats */}
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-card__icon stat-card__icon--accent">💰</div>
            <div className="stat-card__label">Club Balance</div>
            <div className="stat-card__value">
              {balance ? formatCurrency(balance.balance) : '—'}
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-card__icon stat-card__icon--secondary">👥</div>
            <div className="stat-card__label">Squad Size</div>
            <div className="stat-card__value">{squad.length || '—'}</div>
            <div className="stat-card__trend">
              {squad.length > 0 ? `${squad.filter(p => p.listedOnMarket).length} on market` : ''}
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-card__icon stat-card__icon--warning">⚙️</div>
            <div className="stat-card__label">Formation</div>
            <div className="stat-card__value">{tactics?.formation || '—'}</div>
          </div>

          <div className="stat-card">
            <div className="stat-card__icon stat-card__icon--danger">📊</div>
            <div className="stat-card__label">Weekly Wages</div>
            <div className="stat-card__value">
              {summary?.weeklyWageBill ? formatCurrency(summary.weeklyWageBill) : '—'}
            </div>
            {summary?.netProfitOrLoss != null && (
              <div className="stat-card__trend" style={{
                color: summary.netProfitOrLoss >= 0 ? 'var(--clr-accent)' : 'var(--clr-danger)',
              }}>
                {summary.netProfitOrLoss >= 0 ? '↑' : '↓'} {formatCurrency(Math.abs(summary.netProfitOrLoss))} net
              </div>
            )}
          </div>
        </div>

        {/* Quick Actions */}
        <div className="section-header">
          <h2 className="section-header__title">Quick Actions</h2>
        </div>

        <div className="quick-actions">
          <div className="quick-action">
            <div className="quick-action__icon stat-card__icon--accent">📋</div>
            <div className="quick-action__text">
              <div className="quick-action__title">Manage Tactics</div>
              <div className="quick-action__desc">Set formation & game plan</div>
            </div>
          </div>

          <div className="quick-action">
            <div className="quick-action__icon stat-card__icon--secondary">⚡</div>
            <div className="quick-action__text">
              <div className="quick-action__title">Set Lineup</div>
              <div className="quick-action__desc">Pick your starting XI</div>
            </div>
          </div>

          <div className="quick-action">
            <div className="quick-action__icon stat-card__icon--warning">🔄</div>
            <div className="quick-action__text">
              <div className="quick-action__title">Transfer Market</div>
              <div className="quick-action__desc">Buy & sell players</div>
            </div>
          </div>

          <div className="quick-action">
            <div className="quick-action__icon stat-card__icon--danger">💵</div>
            <div className="quick-action__text">
              <div className="quick-action__title">Finances</div>
              <div className="quick-action__desc">View balance & transactions</div>
            </div>
          </div>
        </div>

        {/* Squad Overview */}
        {squad.length > 0 && (
          <>
            <div className="section-header">
              <h2 className="section-header__title">Squad Overview</h2>
              <span className="section-header__action">{squad.length} players</span>
            </div>

            <div className="clubs-grid">
              {squad.slice(0, 6).map((player, idx) => (
                <div
                  key={player.id}
                  className="club-card animate-fade-in"
                  style={{ animationDelay: `${idx * 0.05}s`, cursor: 'default' }}
                >
                  <div className="club-card__content">
                    <div className="club-card__header">
                      <div className="club-card__logo" style={{
                        background: player.listedOnMarket
                          ? 'rgba(251, 191, 36, 0.15)'
                          : 'var(--clr-surface)',
                        fontSize: '1.2rem',
                      }}>
                        {player.listedOnMarket ? '🏷️' : '⚽'}
                      </div>
                      <div>
                        <div className="club-card__name">{player.name}</div>
                        <div className="club-card__short">
                          {player.position} • OVR {player.overall}
                        </div>
                      </div>
                    </div>

                    <div className="club-card__details">
                      <div className="club-card__detail">
                        <span className="club-card__detail-label">Market Value</span>
                        <span className="club-card__detail-value">
                          {formatCurrency(player.marketValue)}
                        </span>
                      </div>
                      <div className="club-card__detail">
                        <span className="club-card__detail-label">Weekly Wage</span>
                        <span className="club-card__detail-value">
                          {formatCurrency(player.weeklyWage)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {squad.length > 6 && (
              <p style={{
                textAlign: 'center',
                color: 'var(--clr-text-muted)',
                marginTop: 'var(--space-md)',
                fontSize: '0.85rem',
              }}>
                + {squad.length - 6} more players in your squad
              </p>
            )}
          </>
        )}
      </div>
    );
  };

  // ── Loading State ──
  if (hasClub === null) {
    return (
      <>
        {renderNav()}
        <div className="dashboard">
          <div className="stats-grid">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="skeleton skeleton--card" style={{ height: 140 }} />
            ))}
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      {renderNav()}
      {hasClub ? renderDashboard() : renderClubSelection()}
    </>
  );
}
