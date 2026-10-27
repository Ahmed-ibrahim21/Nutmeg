import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { getMyTactics, updateTactics } from '../api';

const FORMATIONS = [
  { value: 'F_4_4_2',   label: '4-4-2',   archetype: 'Balanced' },
  { value: 'F_4_3_3',   label: '4-3-3',   archetype: 'Attacking' },
  { value: 'F_4_2_3_1', label: '4-2-3-1', archetype: 'Balanced' },
  { value: 'F_5_3_2',   label: '5-3-2',   archetype: 'Defensive' },
  { value: 'F_4_5_1',   label: '4-5-1',   archetype: 'Defensive' },
  { value: 'F_3_4_3',   label: '3-4-3',   archetype: 'Attacking' },
];

const TACTICS_OPTIONS = {
  gamePlan:     { label: 'Game Plan',    options: ['WING_PLAY', 'PASSING_GAME', 'COUNTER_ATTACK', 'SHOOT_ON_SIGHT', 'LONG_BALL'] },
  pressing:     { label: 'Pressing',     options: ['PRESS_HIGH', 'CLOSE_DOWN', 'BALANCED', 'SIT_DEEP'] },
  style:        { label: 'Style',        options: ['ALL_OUT_ATTACK', 'ATTACKING', 'BALANCED', 'DEFENSIVE', 'PARK_THE_BUS'] },
  tempo:        { label: 'Tempo',        options: ['ONE_TOUCH', 'HIGH_SPEED', 'POSSESSION', 'SLOW_BUILD_UP'] },
  tackling:     { label: 'Tackling',     options: ['RECKLESS', 'AGGRESSIVE', 'NORMAL', 'EASY'] },
  marking:      { label: 'Marking',      options: ['ZONAL', 'MAN_TO_MAN'] },
  defenseLine:  { label: 'Defense Line', options: ['ATTACKING_FULL_BACKS', 'SUPPORT_MIDFIELD', 'DEFEND_DEEP'] },
  midfieldLine: { label: 'Midfield',     options: ['PUSH_FORWARD', 'STAY_IN_POSITION', 'PROTECT_DEFENSE'] },
  forwardLine:  { label: 'Forwards',     options: ['ATTACK_ONLY', 'SUPPORT_MIDFIELD', 'DROP_DEEP'] },
};

function humanize(str) {
  return str.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase()).replace(/\bOf\b/gi, 'of');
}

const ARCHETYPE_COLORS = {
  Attacking: { bg: 'rgba(248,113,113,0.15)', color: '#f87171' },
  Balanced:  { bg: 'rgba(52,211,153,0.15)',  color: '#34d399' },
  Defensive: { bg: 'rgba(96,165,250,0.15)',  color: '#60a5fa' },
};

export default function TacticsPage() {
  const { token } = useAuth();
  const [tactics, setTactics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [dirty, setDirty] = useState(false);

  useEffect(() => {
    loadTactics();
  }, []);

  const loadTactics = async () => {
    setLoading(true);
    try {
      const data = await getMyTactics(token);
      setTactics(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (key, value) => {
    setTactics(prev => ({ ...prev, [key]: value }));
    setDirty(true);
    setSuccess('');
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const payload = {
        formation: tactics.formation,
        gamePlan: tactics.gamePlan,
        forwardLine: tactics.forwardLine,
        midfieldLine: tactics.midfieldLine,
        defenseLine: tactics.defenseLine,
        pressing: tactics.pressing,
        style: tactics.style,
        tempo: tactics.tempo,
        tackling: tactics.tackling,
        marking: tactics.marking,
        offsideTrap: tactics.offsideTrap,
      };
      const updated = await updateTactics(payload, token);
      setTactics(updated);
      setDirty(false);
      setSuccess('Tactics saved successfully!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="page">
        <div className="page__header"><h1 className="page__title">Tactics</h1></div>
        <div className="stats-grid">
          {[1, 2, 3].map(i => <div key={i} className="skeleton skeleton--card" style={{ height: 120 }} />)}
        </div>
      </div>
    );
  }

  if (!tactics) {
    return (
      <div className="page">
        <div className="page__header"><h1 className="page__title">Tactics</h1></div>
        <div className="empty-state">
          <div className="empty-state__icon">📋</div>
          <div className="empty-state__title">No tactics available</div>
          <div className="empty-state__desc">Assign a club first to configure tactics.</div>
        </div>
      </div>
    );
  }

  const coherencePercent = Math.round(tactics.coherenceScore * 100);
  const coherenceColor = coherencePercent >= 75 ? 'var(--clr-accent)' : coherencePercent >= 50 ? 'var(--clr-warning)' : 'var(--clr-danger)';

  return (
    <div className="page">
      <div className="page__header">
        <div>
          <h1 className="page__title">Tactics</h1>
          <p className="page__subtitle">Configure your team's game plan and playing style</p>
        </div>
        <button
          className="btn btn--primary"
          style={{ width: 'auto', padding: '0.6rem 1.5rem' }}
          onClick={handleSave}
          disabled={saving || !dirty}
        >
          {saving ? (
            <><div className="btn__spinner" /> Saving…</>
          ) : 'Save Tactics'}
        </button>
      </div>

      {error && (
        <div className="alert alert--error" role="alert">
          <span>⚠</span><span>{error}</span>
        </div>
      )}
      {success && (
        <div className="alert alert--success" role="alert">
          <span>✓</span><span>{success}</span>
        </div>
      )}

      {/* Coherence Score */}
      <div className="tactics-coherence animate-fade-in">
        <div className="tactics-coherence__header">
          <span className="tactics-coherence__label">Tactical Coherence</span>
          <span className="tactics-coherence__value" style={{ color: coherenceColor }}>
            {coherencePercent}%
          </span>
        </div>
        <div className="tactics-coherence__bar">
          <div
            className="tactics-coherence__fill"
            style={{ width: `${coherencePercent}%`, background: coherenceColor }}
          />
        </div>
        <p className="tactics-coherence__hint">
          {coherencePercent >= 75
            ? 'Your tactics are well-balanced. The team will perform reliably.'
            : coherencePercent >= 50
            ? 'Some tactical conflicts detected. Consider adjusting your settings.'
            : 'Major tactical conflicts! Your team will underperform. Review your choices.'}
        </p>
      </div>

      {/* Formation Picker */}
      <div className="section-header">
        <h2 className="section-header__title">Formation</h2>
      </div>
      <div className="formation-grid animate-fade-in">
        {FORMATIONS.map(f => {
          const arch = ARCHETYPE_COLORS[f.archetype];
          const isActive = tactics.formation === f.value;
          return (
            <button
              key={f.value}
              className={`formation-card${isActive ? ' formation-card--active' : ''}`}
              onClick={() => handleChange('formation', f.value)}
            >
              <div className="formation-card__label">{f.label}</div>
              <div
                className="formation-card__archetype"
                style={{ background: arch.bg, color: arch.color }}
              >
                {f.archetype}
              </div>
            </button>
          );
        })}
      </div>

      {/* Tactical Settings */}
      <div className="section-header" style={{ marginTop: 'var(--space-xl)' }}>
        <h2 className="section-header__title">Tactical Settings</h2>
      </div>
      <div className="tactics-grid animate-fade-in">
        {Object.entries(TACTICS_OPTIONS).map(([key, config]) => (
          <div key={key} className="tactics-setting">
            <label className="tactics-setting__label">{config.label}</label>
            <div className="tactics-setting__options">
              {config.options.map(opt => (
                <button
                  key={opt}
                  className={`tactics-pill${tactics[key] === opt ? ' tactics-pill--active' : ''}`}
                  onClick={() => handleChange(key, opt)}
                >
                  {humanize(opt)}
                </button>
              ))}
            </div>
          </div>
        ))}

        {/* Offside Trap Toggle */}
        <div className="tactics-setting">
          <label className="tactics-setting__label">Offside Trap</label>
          <div className="tactics-setting__options">
            <button
              className={`tactics-pill${tactics.offsideTrap ? ' tactics-pill--active' : ''}`}
              onClick={() => handleChange('offsideTrap', true)}
            >
              Enabled
            </button>
            <button
              className={`tactics-pill${!tactics.offsideTrap ? ' tactics-pill--active' : ''}`}
              onClick={() => handleChange('offsideTrap', false)}
            >
              Disabled
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
