import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { getLeagues, getLeagueTable, getLeagueClubs, simulateRound } from '../api';

export default function LeaguePage() {
  const { token, manager } = useAuth();
  const [leagues, setLeagues] = useState([]);
  const [selectedLeagueId, setSelectedLeagueId] = useState(null);
  const [table, setTable] = useState(null);
  const [clubs, setClubs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [simulating, setSimulating] = useState(false);
  const [simulationResult, setSimulationResult] = useState(null);
  const [error, setError] = useState('');
  const [tab, setTab] = useState('standings');

  useEffect(() => { loadLeagues(); }, []);

  const loadLeagues = async () => {
    setLoading(true);
    try {
      const data = await getLeagues(0, token);
      const leagueList = data.leagues || [];
      setLeagues(leagueList);
      if (leagueList.length > 0) {
        setSelectedLeagueId(leagueList[0].id);
        await loadLeagueData(leagueList[0].id);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadLeagueData = async (leagueId) => {
    try {
      const [tableData, clubsData] = await Promise.all([
        getLeagueTable(leagueId, token),
        getLeagueClubs(leagueId, token),
      ]);
      setTable(tableData);
      setClubs(clubsData || []);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleSimulate = async () => {
    if (!selectedLeagueId) return;
    setSimulating(true);
    setError('');
    setSimulationResult(null);
    try {
      const result = await simulateRound(selectedLeagueId, token);
      setSimulationResult(result);
      // Refresh standings
      await loadLeagueData(selectedLeagueId);
    } catch (err) {
      setError(err.message);
    } finally {
      setSimulating(false);
    }
  };

  const handleLeagueChange = async (leagueId) => {
    setSelectedLeagueId(leagueId);
    setSimulationResult(null);
    setLoading(true);
    await loadLeagueData(leagueId);
    setLoading(false);
  };

  if (loading) {
    return (
      <div className="page">
        <div className="page__header"><h1 className="page__title">League</h1></div>
        <div className="skeleton skeleton--card" style={{ height: 400 }} />
      </div>
    );
  }

  const selectedLeague = leagues.find(l => l.id === selectedLeagueId);

  return (
    <div className="page">
      <div className="page__header">
        <div>
          <h1 className="page__title">League</h1>
          <p className="page__subtitle">
            {selectedLeague ? `${selectedLeague.name} — ${selectedLeague.country}` : 'Select a league'}
            {table?.currentRound != null && ` • Round ${table.currentRound}`}
          </p>
        </div>
        <button
          className="btn btn--primary"
          style={{ width: 'auto', padding: '0.6rem 1.5rem' }}
          onClick={handleSimulate}
          disabled={simulating}
        >
          {simulating ? (
            <><div className="btn__spinner" /> Simulating…</>
          ) : '⚡ Simulate Round'}
        </button>
      </div>

      {error && (
        <div className="alert alert--error" role="alert">
          <span>⚠</span><span>{error}</span>
        </div>
      )}

      {/* League Selector */}
      {leagues.length > 1 && (
        <div className="filter-bar" style={{ marginBottom: 'var(--space-lg)' }}>
          <div className="filter-bar__pills">
            {leagues.map(l => (
              <button
                key={l.id}
                className={`pill${selectedLeagueId === l.id ? ' pill--active' : ''}`}
                onClick={() => handleLeagueChange(l.id)}
              >
                {l.name}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Simulation Results Modal */}
      {simulationResult && (
        <div className="sim-results animate-fade-in">
          <div className="sim-results__header">
            <h3 className="sim-results__title">
              Round {simulationResult.roundSimulated} Results
            </h3>
            <button
              className="sim-results__close"
              onClick={() => setSimulationResult(null)}
            >
              ✕
            </button>
          </div>
          <div className="sim-results__list">
            {simulationResult.results.map(r => (
              <div key={r.fixtureId} className="sim-results__match">
                <span className={`sim-results__team${r.homeScore > r.awayScore ? ' sim-results__team--winner' : ''}`}>
                  {r.homeClub}
                </span>
                <span className="sim-results__score">
                  {r.homeScore} - {r.awayScore}
                </span>
                <span className={`sim-results__team${r.awayScore > r.homeScore ? ' sim-results__team--winner' : ''}`}>
                  {r.awayClub}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tab Buttons */}
      <div className="filter-bar">
        <div className="filter-bar__pills">
          <button className={`pill${tab === 'standings' ? ' pill--active' : ''}`} onClick={() => setTab('standings')}>
            Standings
          </button>
          <button className={`pill${tab === 'clubs' ? ' pill--active' : ''}`} onClick={() => setTab('clubs')}>
            Clubs
          </button>
        </div>
      </div>

      {/* Standings Table */}
      {tab === 'standings' && table && (
        <div className="data-table-wrapper animate-fade-in">
          <table className="data-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Club</th>
                <th>P</th>
                <th>W</th>
                <th>D</th>
                <th>L</th>
                <th>GF</th>
                <th>GA</th>
                <th>GD</th>
                <th>Pts</th>
              </tr>
            </thead>
            <tbody>
              {table.standings.map((row, idx) => (
                <tr key={row.clubId} className={idx < 1 ? 'data-table__row--highlight' : ''}>
                  <td className="data-table__pos">{row.position}</td>
                  <td className="data-table__club">{row.clubName}</td>
                  <td>{row.played}</td>
                  <td>{row.wins}</td>
                  <td>{row.draws}</td>
                  <td>{row.losses}</td>
                  <td>{row.goalsFor}</td>
                  <td>{row.goalsAgainst}</td>
                  <td className={row.goalDifference > 0 ? 'data-table__positive' : row.goalDifference < 0 ? 'data-table__negative' : ''}>
                    {row.goalDifference > 0 ? `+${row.goalDifference}` : row.goalDifference}
                  </td>
                  <td className="data-table__points">{row.points}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Clubs List */}
      {tab === 'clubs' && (
        <div className="clubs-grid animate-fade-in">
          {clubs.map((club, idx) => (
            <div key={club.id} className="club-card" style={{ animationDelay: `${idx * 0.06}s`, cursor: 'default' }}>
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
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
