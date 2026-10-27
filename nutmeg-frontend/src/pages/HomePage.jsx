import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
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
  const { token, manager } = useAuth();

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

  // ── Render: Club Selection (no club assigned yet) ──
  const renderClubSelection = () => (
    <div className="page">
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
      <div className="page">
        {/* Hero */}
        <div className="dashboard__hero">
          <div className="dashboard__hero-greeting">Welcome back</div>
          <h1 className="dashboard__hero-title">
            {manager?.username}'s Dashboard
          </h1>
          <p className="dashboard__hero-subtitle">
            {tactics?.formation
              ? `Playing ${tactics.formation.replace('F_', '').replace(/_/g, '-')} formation • Coherence: ${Math.round((tactics.coherenceScore || 0) * 100)}%`
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
            <div className="stat-card__value">
              {tactics?.formation ? tactics.formation.replace('F_', '').replace(/_/g, '-') : '—'}
            </div>
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
          <Link to="/tactics" className="quick-action">
            <div className="quick-action__icon stat-card__icon--accent">📋</div>
            <div className="quick-action__text">
              <div className="quick-action__title">Manage Tactics</div>
              <div className="quick-action__desc">Set formation & game plan</div>
            </div>
          </Link>

          <Link to="/lineup" className="quick-action">
            <div className="quick-action__icon stat-card__icon--secondary">⚡</div>
            <div className="quick-action__text">
              <div className="quick-action__title">Set Lineup</div>
              <div className="quick-action__desc">Pick your starting XI</div>
            </div>
          </Link>

          <Link to="/transfers" className="quick-action">
            <div className="quick-action__icon stat-card__icon--warning">🔄</div>
            <div className="quick-action__text">
              <div className="quick-action__title">Transfer Market</div>
              <div className="quick-action__desc">Buy & sell players</div>
            </div>
          </Link>

          <Link to="/finances" className="quick-action">
            <div className="quick-action__icon stat-card__icon--danger">💵</div>
            <div className="quick-action__text">
              <div className="quick-action__title">Finances</div>
              <div className="quick-action__desc">View balance & transactions</div>
            </div>
          </Link>
        </div>

        {/* Squad Overview */}
        {squad.length > 0 && (
          <>
            <div className="section-header">
              <h2 className="section-header__title">Squad Overview</h2>
              <Link to="/squad" className="section-header__action">{squad.length} players →</Link>
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
                        <div className="club-card__name">{player.fullName}</div>
                        <div className="club-card__short">
                          {player.position} • OVR {player.overallRating}
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
                <Link to="/squad" style={{ color: 'var(--clr-accent)' }}>
                  View all {squad.length} players →
                </Link>
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
      <div className="page">
        <div className="stats-grid">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="skeleton skeleton--card" style={{ height: 140 }} />
          ))}
        </div>
      </div>
    );
  }

  return hasClub ? renderDashboard() : renderClubSelection();
}
