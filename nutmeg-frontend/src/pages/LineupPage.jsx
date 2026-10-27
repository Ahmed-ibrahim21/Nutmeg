import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { getMyLineup, getMySquad, updateLineup } from '../api';

/* Maps Formation enum → list of FormationSlot names used in that formation.
   Must match the backend Formation → FormationSlot mapping exactly. */
const FORMATION_SLOTS = {
  F_4_4_2:   ['GK', 'LB', 'CB1', 'CB2', 'RB', 'LM', 'CM1', 'CM2', 'RM', 'ST1', 'ST2'],
  F_4_3_3:   ['GK', 'LB', 'CB1', 'CB2', 'RB', 'CM1', 'CM2', 'CM3', 'LW', 'ST', 'RW'],
  F_4_2_3_1: ['GK', 'LB', 'CB1', 'CB2', 'RB', 'CDM', 'CM1', 'CAM', 'LW', 'ST', 'RW'],
  F_5_3_2:   ['GK', 'LWB', 'CB1', 'CB2', 'CB3', 'RWB', 'CM1', 'CM2', 'CM3', 'ST1', 'ST2'],
  F_4_5_1:   ['GK', 'LB', 'CB1', 'CB2', 'RB', 'LM', 'CM1', 'CM2', 'CM3', 'RM', 'ST'],
  F_3_4_3:   ['GK', 'CB1', 'CB2', 'CB3', 'LM', 'CM1', 'CM2', 'RM', 'LW', 'ST', 'RW'],
};

/* Position on pitch grid (row 0=top=GK, row 4=bottom=forwards)
   Each entry: [gridRow, gridCol] in a 5×7 virtual grid */
const SLOT_POSITIONS = {
  GK:  [1, 4],
  // Defensive
  LB:  [2, 1], LWB: [2, 1],
  CB1: [2, 3], CB2: [2, 5], CB3: [2, 4],
  RB:  [2, 7], RWB: [2, 7],
  // Midfield
  LM:  [3, 1],
  CDM: [3, 3],
  CM1: [3, 3], CM2: [3, 5], CM3: [3, 4],
  CAM: [3, 4],
  RM:  [3, 7],
  // Attack
  LW:  [4, 2],
  ST:  [4, 4], ST1: [4, 3], ST2: [4, 5],
  RW:  [4, 6],
};

function humanizeSlot(slot) {
  return slot.replace(/(\d+)$/, ' $1').trim();
}

function formatCurrency(amount) {
  if (amount >= 1_000_000) return `£${(amount / 1_000_000).toFixed(1)}M`;
  if (amount >= 1_000) return `£${(amount / 1_000).toFixed(0)}K`;
  return `£${amount.toLocaleString()}`;
}

export default function LineupPage() {
  const { token } = useAuth();
  const [lineup, setLineup] = useState(null);
  const [squad, setSquad] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [assignments, setAssignments] = useState({}); // slot -> playerId

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [lineupData, squadData] = await Promise.all([
        getMyLineup(token),
        getMySquad(token),
      ]);
      setLineup(lineupData);
      setSquad(squadData || []);

      // Build initial assignments from existing lineup
      const initial = {};
      if (lineupData?.players) {
        lineupData.players.forEach(p => {
          initial[p.slot] = p.playerId;
        });
      }
      setAssignments(initial);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleAssign = (playerId) => {
    if (!selectedSlot) return;
    setAssignments(prev => {
      // Remove this player from any other slot they're in
      const cleaned = { ...prev };
      Object.entries(cleaned).forEach(([slot, pid]) => {
        if (pid === playerId) delete cleaned[slot];
      });
      cleaned[selectedSlot] = playerId;
      return cleaned;
    });
    setSelectedSlot(null);
    setSuccess('');
  };

  const handleRemoveFromSlot = (slot) => {
    setAssignments(prev => {
      const cleaned = { ...prev };
      delete cleaned[slot];
      return cleaned;
    });
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const lineupPayload = Object.entries(assignments).map(([slot, playerId]) => ({
        slot,
        playerId,
      }));
      const result = await updateLineup(lineupPayload, token);
      setLineup(result);
      setSuccess('Lineup saved!');
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
        <div className="page__header"><h1 className="page__title">Lineup</h1></div>
        <div className="skeleton skeleton--card" style={{ height: 400 }} />
      </div>
    );
  }

  if (!lineup) {
    return (
      <div className="page">
        <div className="page__header"><h1 className="page__title">Lineup</h1></div>
        <div className="empty-state">
          <div className="empty-state__icon">⚡</div>
          <div className="empty-state__title">No lineup available</div>
          <div className="empty-state__desc">Assign a club and set a formation first.</div>
        </div>
      </div>
    );
  }

  const formation = lineup.formation;
  const slots = FORMATION_SLOTS[formation] || [];
  const assignedPlayerIds = new Set(Object.values(assignments));
  const availablePlayers = squad.filter(p => !assignedPlayerIds.has(p.id));
  const filledCount = Object.keys(assignments).length;

  return (
    <div className="page">
      <div className="page__header">
        <div>
          <h1 className="page__title">Starting Lineup</h1>
          <p className="page__subtitle">
            {formation.replace('F_', '').replace(/_/g, '-')} — {filledCount}/11 slots filled
          </p>
        </div>
        <button
          className="btn btn--primary"
          style={{ width: 'auto', padding: '0.6rem 1.5rem' }}
          onClick={handleSave}
          disabled={saving || filledCount !== 11}
        >
          {saving ? (
            <><div className="btn__spinner" /> Saving…</>
          ) : 'Save Lineup'}
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

      <div className="lineup-container animate-fade-in">
        {/* Pitch */}
        <div className="pitch">
          <div className="pitch__field">
            {/* Lines */}
            <div className="pitch__center-circle" />
            <div className="pitch__center-line" />
            <div className="pitch__penalty-area pitch__penalty-area--top" />
            <div className="pitch__penalty-area pitch__penalty-area--bottom" />

            {/* Slots */}
            {slots.map(slot => {
              const pos = SLOT_POSITIONS[slot] || [3, 4];
              const playerId = assignments[slot];
              const player = squad.find(p => p.id === playerId);
              const isSelected = selectedSlot === slot;

              return (
                <div
                  key={slot}
                  className={`pitch__slot${isSelected ? ' pitch__slot--selected' : ''}${player ? ' pitch__slot--filled' : ''}`}
                  style={{
                    gridRow: pos[0],
                    gridColumn: pos[1],
                  }}
                  onClick={() => {
                    if (player) {
                      handleRemoveFromSlot(slot);
                    } else {
                      setSelectedSlot(isSelected ? null : slot);
                    }
                  }}
                  title={player ? `${player.fullName} (click to remove)` : `Click to assign a player to ${humanizeSlot(slot)}`}
                >
                  <div className="pitch__slot-circle">
                    {player
                      ? player.fullName.split(' ').map(n => n[0]).join('').slice(0, 2)
                      : '+'
                    }
                  </div>
                  <div className="pitch__slot-label">
                    {player ? player.fullName.split(' ').pop() : humanizeSlot(slot)}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Player Selection Panel */}
        <div className="lineup-panel">
          <h3 className="lineup-panel__title">
            {selectedSlot
              ? `Select player for ${humanizeSlot(selectedSlot)}`
              : 'Available Players'}
          </h3>
          <p className="lineup-panel__hint">
            {selectedSlot
              ? 'Click a player below to assign them.'
              : 'Click an empty slot on the pitch first.'}
          </p>

          <div className="lineup-panel__list">
            {availablePlayers.map(player => (
              <button
                key={player.id}
                className={`lineup-player${selectedSlot ? '' : ' lineup-player--disabled'}`}
                onClick={() => selectedSlot && handleAssign(player.id)}
                disabled={!selectedSlot}
              >
                <div className="lineup-player__pos">{player.position}</div>
                <div className="lineup-player__info">
                  <div className="lineup-player__name">{player.fullName}</div>
                  <div className="lineup-player__meta">
                    OVR {player.overallRating} • {formatCurrency(player.marketValue)}
                  </div>
                </div>
                <div className="lineup-player__ovr">{player.overallRating}</div>
              </button>
            ))}
            {availablePlayers.length === 0 && (
              <div className="lineup-panel__empty">
                All players are assigned to slots.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
