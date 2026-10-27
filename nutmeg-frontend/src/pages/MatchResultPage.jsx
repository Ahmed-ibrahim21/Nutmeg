import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import { simulateFixture } from '../api';

const EVENT_ICONS = {
  GOAL: '⚽',
  YELLOW_CARD: '🟨',
  RED_CARD: '🟥',
  SAVE: '🧤',
  FOUL: '⚡',
  SHOT: '🎯',
  ASSIST: '👟',
};

export default function MatchResultPage() {
  const { fixtureId } = useParams();
  const { token } = useAuth();
  const navigate = useNavigate();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadMatch();
  }, [fixtureId]);

  const loadMatch = async () => {
    setLoading(true);
    try {
      const data = await simulateFixture(fixtureId, null, token);
      setResult(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page">
        <div className="page__header"><h1 className="page__title">Match Result</h1></div>
        <div className="skeleton skeleton--card" style={{ height: 300 }} />
      </div>
    );
  }

  if (error) {
    return (
      <div className="page">
        <div className="page__header"><h1 className="page__title">Match Result</h1></div>
        <div className="alert alert--error" role="alert"><span>⚠</span><span>{error}</span></div>
        <button className="btn btn--ghost" onClick={() => navigate('/league')}>← Back to League</button>
      </div>
    );
  }

  if (!result) return null;

  const totalPossessions = result.homePossessions + result.awayPossessions;
  const homePossPct = totalPossessions > 0
    ? Math.round((result.homePossessions / totalPossessions) * 100)
    : 50;
  const awayPossPct = 100 - homePossPct;

  const goals = (result.events || []).filter(e => e.type === 'GOAL');
  const homeGoals = goals.filter(e => e.team === 'HOME');
  const awayGoals = goals.filter(e => e.team === 'AWAY');

  return (
    <div className="page">
      <button className="btn btn--ghost" onClick={() => navigate('/league')} style={{ marginBottom: 'var(--space-lg)' }}>
        ← Back to League
      </button>

      {/* Scoreline */}
      <div className="match-scoreline animate-fade-in">
        <div className="match-scoreline__team">
          <div className="match-scoreline__label">Home</div>
          <div className="match-scoreline__score">{result.homeScore}</div>
          <div className="match-scoreline__scorers">
            {homeGoals.map((g, i) => (
              <span key={i} className="match-scoreline__scorer">⚽ {g.player} {g.minute}'</span>
            ))}
          </div>
        </div>
        <div className="match-scoreline__vs">VS</div>
        <div className="match-scoreline__team">
          <div className="match-scoreline__label">Away</div>
          <div className="match-scoreline__score">{result.awayScore}</div>
          <div className="match-scoreline__scorers">
            {awayGoals.map((g, i) => (
              <span key={i} className="match-scoreline__scorer">⚽ {g.player} {g.minute}'</span>
            ))}
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="section-header">
        <h2 className="section-header__title">Match Stats</h2>
      </div>
      <div className="match-stats animate-fade-in">
        <div className="match-stat">
          <span className="match-stat__home">{homePossPct}%</span>
          <span className="match-stat__label">Possession</span>
          <span className="match-stat__away">{awayPossPct}%</span>
          <div className="match-stat__bar">
            <div className="match-stat__bar-home" style={{ width: `${homePossPct}%` }} />
            <div className="match-stat__bar-away" style={{ width: `${awayPossPct}%` }} />
          </div>
        </div>
        <div className="match-stat">
          <span className="match-stat__home">{result.homePossessions}</span>
          <span className="match-stat__label">Actions</span>
          <span className="match-stat__away">{result.awayPossessions}</span>
          <div className="match-stat__bar">
            <div className="match-stat__bar-home" style={{ width: `${homePossPct}%` }} />
            <div className="match-stat__bar-away" style={{ width: `${awayPossPct}%` }} />
          </div>
        </div>
      </div>

      {/* Events Timeline */}
      {result.events && result.events.length > 0 && (
        <>
          <div className="section-header" style={{ marginTop: 'var(--space-xl)' }}>
            <h2 className="section-header__title">Match Events</h2>
          </div>
          <div className="match-timeline animate-fade-in">
            {result.events.map((evt, i) => (
              <div
                key={i}
                className={`match-event match-event--${evt.team?.toLowerCase() || 'home'}`}
              >
                <span className="match-event__minute">{evt.minute}'</span>
                <span className="match-event__icon">{EVENT_ICONS[evt.type] || '📌'}</span>
                <div className="match-event__info">
                  <span className="match-event__player">{evt.player}</span>
                  {evt.detail && <span className="match-event__detail">{evt.detail}</span>}
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
