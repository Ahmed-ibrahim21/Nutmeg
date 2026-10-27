import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { getMySquad } from '../api';

function formatCurrency(amount) {
  if (amount >= 1_000_000) return `£${(amount / 1_000_000).toFixed(1)}M`;
  if (amount >= 1_000) return `£${(amount / 1_000).toFixed(0)}K`;
  return `£${amount.toLocaleString()}`;
}

const POSITION_COLORS = {
  GK:  { bg: 'rgba(251,191,36,0.15)', color: '#fbbf24' },
  CB:  { bg: 'rgba(96,165,250,0.15)',  color: '#60a5fa' },
  LB:  { bg: 'rgba(96,165,250,0.15)',  color: '#60a5fa' },
  RB:  { bg: 'rgba(96,165,250,0.15)',  color: '#60a5fa' },
  LWB: { bg: 'rgba(96,165,250,0.15)',  color: '#60a5fa' },
  RWB: { bg: 'rgba(96,165,250,0.15)',  color: '#60a5fa' },
  CM:  { bg: 'rgba(52,211,153,0.15)',  color: '#34d399' },
  CDM: { bg: 'rgba(52,211,153,0.15)',  color: '#34d399' },
  CAM: { bg: 'rgba(52,211,153,0.15)',  color: '#34d399' },
  LM:  { bg: 'rgba(52,211,153,0.15)',  color: '#34d399' },
  RM:  { bg: 'rgba(52,211,153,0.15)',  color: '#34d399' },
  LW:  { bg: 'rgba(248,113,113,0.15)', color: '#f87171' },
  RW:  { bg: 'rgba(248,113,113,0.15)', color: '#f87171' },
  ST:  { bg: 'rgba(248,113,113,0.15)', color: '#f87171' },
  CF:  { bg: 'rgba(248,113,113,0.15)', color: '#f87171' },
};

function getPositionStyle(pos) {
  return POSITION_COLORS[pos] || { bg: 'var(--clr-surface)', color: 'var(--clr-text-secondary)' };
}

const SORT_OPTIONS = [
  { value: 'overall-desc', label: 'OVR (High → Low)' },
  { value: 'overall-asc',  label: 'OVR (Low → High)' },
  { value: 'value-desc',   label: 'Value (High → Low)' },
  { value: 'value-asc',    label: 'Value (Low → High)' },
  { value: 'wage-desc',    label: 'Wage (High → Low)' },
  { value: 'wage-asc',     label: 'Wage (Low → High)' },
  { value: 'name-asc',     label: 'Name (A → Z)' },
];

const POSITION_GROUPS = ['All', 'GK', 'DEF', 'MID', 'FWD'];
const DEF_POSITIONS = ['CB', 'LB', 'RB', 'LWB', 'RWB'];
const MID_POSITIONS = ['CM', 'CDM', 'CAM', 'LM', 'RM'];
const FWD_POSITIONS = ['LW', 'RW', 'ST', 'CF'];

function getPositionGroup(pos) {
  if (pos === 'GK') return 'GK';
  if (DEF_POSITIONS.includes(pos)) return 'DEF';
  if (MID_POSITIONS.includes(pos)) return 'MID';
  return 'FWD';
}

export default function SquadPage() {
  const { token } = useAuth();
  const [squad, setSquad] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sortBy, setSortBy] = useState('overall-desc');
  const [filterPos, setFilterPos] = useState('All');

  useEffect(() => {
    loadSquad();
  }, []);

  const loadSquad = async () => {
    setLoading(true);
    try {
      const data = await getMySquad(token);
      setSquad(data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const filtered = squad.filter(p => {
    if (filterPos === 'All') return true;
    return getPositionGroup(p.position) === filterPos;
  });

  const sorted = [...filtered].sort((a, b) => {
    switch (sortBy) {
      case 'overall-desc': return b.overallRating - a.overallRating;
      case 'overall-asc':  return a.overallRating - b.overallRating;
      case 'value-desc':   return b.marketValue - a.marketValue;
      case 'value-asc':    return a.marketValue - b.marketValue;
      case 'wage-desc':    return b.weeklyWage - a.weeklyWage;
      case 'wage-asc':     return a.weeklyWage - b.weeklyWage;
      case 'name-asc':     return a.fullName.localeCompare(b.fullName);
      default: return 0;
    }
  });

  const totalValue = squad.reduce((sum, p) => sum + p.marketValue, 0);
  const totalWages = squad.reduce((sum, p) => sum + p.weeklyWage, 0);
  const avgRating = squad.length > 0
    ? Math.round(squad.reduce((sum, p) => sum + p.overallRating, 0) / squad.length)
    : 0;

  if (loading) {
    return (
      <div className="page">
        <div className="page__header">
          <h1 className="page__title">Squad</h1>
        </div>
        <div className="stats-grid">
          {[1, 2, 3].map(i => <div key={i} className="skeleton skeleton--card" style={{ height: 100 }} />)}
        </div>
        <div className="clubs-grid">
          {[1, 2, 3, 4, 5, 6].map(i => <div key={i} className="skeleton skeleton--card" style={{ height: 180 }} />)}
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page__header">
        <div>
          <h1 className="page__title">Squad</h1>
          <p className="page__subtitle">{squad.length} players in your squad</p>
        </div>
      </div>

      {error && (
        <div className="alert alert--error" role="alert">
          <span>⚠</span><span>{error}</span>
        </div>
      )}

      {/* Summary Stats */}
      <div className="stats-grid" style={{ marginBottom: 'var(--space-xl)' }}>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--accent">👥</div>
          <div className="stat-card__label">Squad Size</div>
          <div className="stat-card__value">{squad.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--secondary">⭐</div>
          <div className="stat-card__label">Avg Rating</div>
          <div className="stat-card__value">{avgRating}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--warning">💎</div>
          <div className="stat-card__label">Squad Value</div>
          <div className="stat-card__value">{formatCurrency(totalValue)}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--danger">📊</div>
          <div className="stat-card__label">Weekly Wages</div>
          <div className="stat-card__value">{formatCurrency(totalWages)}</div>
        </div>
      </div>

      {/* Filters */}
      <div className="filter-bar">
        <div className="filter-bar__pills">
          {POSITION_GROUPS.map(g => (
            <button
              key={g}
              className={`pill${filterPos === g ? ' pill--active' : ''}`}
              onClick={() => setFilterPos(g)}
            >
              {g}
            </button>
          ))}
        </div>
        <select
          className="filter-bar__select"
          value={sortBy}
          onChange={e => setSortBy(e.target.value)}
        >
          {SORT_OPTIONS.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
      </div>

      {/* Player Grid */}
      {sorted.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state__icon">👥</div>
          <div className="empty-state__title">No players found</div>
          <div className="empty-state__desc">
            {filterPos !== 'All'
              ? `No ${filterPos} players in your squad.`
              : 'Your squad is empty.'}
          </div>
        </div>
      ) : (
        <div className="clubs-grid">
          {sorted.map((player, idx) => {
            const posStyle = getPositionStyle(player.position);
            return (
              <div
                key={player.id}
                className="player-card animate-fade-in"
                style={{ animationDelay: `${idx * 0.04}s` }}
              >
                <div className="player-card__top">
                  <div
                    className="player-card__pos-badge"
                    style={{ background: posStyle.bg, color: posStyle.color }}
                  >
                    {player.position}
                  </div>
                  <div className="player-card__ovr">{player.overallRating}</div>
                </div>

                <div className="player-card__name">{player.fullName}</div>

                <div className="player-card__stats">
                  <div className="player-card__stat">
                    <span className="player-card__stat-label">Value</span>
                    <span className="player-card__stat-value">{formatCurrency(player.marketValue)}</span>
                  </div>
                  <div className="player-card__stat">
                    <span className="player-card__stat-label">Wage</span>
                    <span className="player-card__stat-value">{formatCurrency(player.weeklyWage)}/w</span>
                  </div>
                </div>

                {player.listedOnMarket && (
                  <div className="player-card__badge">🏷️ Listed</div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
