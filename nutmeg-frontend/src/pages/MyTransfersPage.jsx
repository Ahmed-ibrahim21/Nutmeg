import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import {
  getMySquad, getMyListings, getIncomingOffers, getOutgoingOffers,
  listPlayer, delistPlayer, acceptOffer, rejectOffer, withdrawOffer,
} from '../api';

function formatCurrency(amount) {
  if (amount >= 1_000_000) return `£${(amount / 1_000_000).toFixed(1)}M`;
  if (amount >= 1_000) return `£${(amount / 1_000).toFixed(0)}K`;
  return `£${amount.toLocaleString()}`;
}

export default function MyTransfersPage() {
  const { token } = useAuth();
  const [tab, setTab] = useState('listings');
  const [squad, setSquad] = useState([]);
  const [listings, setListings] = useState([]);
  const [incoming, setIncoming] = useState([]);
  const [outgoing, setOutgoing] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // List player modal
  const [showListModal, setShowListModal] = useState(false);
  const [listPlayerId, setListPlayerId] = useState('');
  const [listPrice, setListPrice] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => { loadAll(); }, []);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [squadData, listingsData, incomingData, outgoingData] = await Promise.allSettled([
        getMySquad(token),
        getMyListings(token),
        getIncomingOffers(token),
        getOutgoingOffers(token),
      ]);
      setSquad(squadData.status === 'fulfilled' ? squadData.value || [] : []);
      setListings(listingsData.status === 'fulfilled' ? listingsData.value || [] : []);
      setIncoming(incomingData.status === 'fulfilled' ? incomingData.value || [] : []);
      setOutgoing(outgoingData.status === 'fulfilled' ? outgoingData.value || [] : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const showMsg = (msg) => {
    setSuccess(msg);
    setTimeout(() => setSuccess(''), 3000);
  };

  const handleListPlayer = async () => {
    if (!listPlayerId || !listPrice) return;
    setSubmitting(true);
    setError('');
    try {
      await listPlayer(listPlayerId, parseInt(listPrice), token);
      showMsg('Player listed on the transfer market!');
      setShowListModal(false);
      setListPlayerId('');
      setListPrice('');
      await loadAll();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelist = async (listingId) => {
    setError('');
    try {
      await delistPlayer(listingId, token);
      showMsg('Player removed from the market.');
      await loadAll();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAccept = async (offerId) => {
    setError('');
    try {
      await acceptOffer(offerId, token);
      showMsg('Offer accepted! Transfer completed.');
      await loadAll();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleReject = async (offerId) => {
    setError('');
    try {
      await rejectOffer(offerId, token);
      showMsg('Offer rejected.');
      await loadAll();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleWithdraw = async (offerId) => {
    setError('');
    try {
      await withdrawOffer(offerId, token);
      showMsg('Offer withdrawn.');
      await loadAll();
    } catch (err) {
      setError(err.message);
    }
  };

  const unlistedPlayers = squad.filter(p => !p.listedOnMarket);

  if (loading) {
    return (
      <div className="page">
        <div className="page__header"><h1 className="page__title">My Transfers</h1></div>
        <div className="stats-grid">
          {[1, 2, 3].map(i => <div key={i} className="skeleton skeleton--card" style={{ height: 100 }} />)}
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page__header">
        <div>
          <h1 className="page__title">My Transfers</h1>
          <p className="page__subtitle">Manage your listings, incoming and outgoing offers</p>
        </div>
        <button
          className="btn btn--primary"
          style={{ width: 'auto', padding: '0.6rem 1.5rem' }}
          onClick={() => setShowListModal(true)}
        >
          + List Player
        </button>
      </div>

      {error && <div className="alert alert--error" role="alert"><span>⚠</span><span>{error}</span></div>}
      {success && <div className="alert alert--success" role="alert"><span>✓</span><span>{success}</span></div>}

      {/* Summary Stats */}
      <div className="stats-grid" style={{ marginBottom: 'var(--space-xl)' }}>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--warning">🏷️</div>
          <div className="stat-card__label">Active Listings</div>
          <div className="stat-card__value">{listings.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--accent">📥</div>
          <div className="stat-card__label">Incoming Offers</div>
          <div className="stat-card__value">{incoming.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--secondary">📤</div>
          <div className="stat-card__label">Outgoing Offers</div>
          <div className="stat-card__value">{outgoing.length}</div>
        </div>
      </div>

      {/* Tabs */}
      <div className="filter-bar">
        <div className="filter-bar__pills">
          <button className={`pill${tab === 'listings' ? ' pill--active' : ''}`} onClick={() => setTab('listings')}>
            My Listings ({listings.length})
          </button>
          <button className={`pill${tab === 'incoming' ? ' pill--active' : ''}`} onClick={() => setTab('incoming')}>
            Incoming ({incoming.length})
          </button>
          <button className={`pill${tab === 'outgoing' ? ' pill--active' : ''}`} onClick={() => setTab('outgoing')}>
            Outgoing ({outgoing.length})
          </button>
        </div>
      </div>

      {/* My Listings Tab */}
      {tab === 'listings' && (
        listings.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state__icon">🏷️</div>
            <div className="empty-state__title">No active listings</div>
            <div className="empty-state__desc">List a player to put them on the transfer market.</div>
          </div>
        ) : (
          <div className="data-table-wrapper animate-fade-in">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Player</th>
                  <th>Pos</th>
                  <th>OVR</th>
                  <th>Market Value</th>
                  <th>Asking Price</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {listings.map(l => (
                  <tr key={l.id}>
                    <td className="data-table__club">{l.playerName}</td>
                    <td><span className="pos-badge">{l.position}</span></td>
                    <td className="data-table__points">{l.overallRating}</td>
                    <td>{formatCurrency(l.marketValue)}</td>
                    <td style={{ color: 'var(--clr-accent)', fontWeight: 600 }}>
                      {formatCurrency(l.askingPrice)}
                    </td>
                    <td>
                      <button className="btn-sm btn-sm--danger" onClick={() => handleDelist(l.id)}>
                        Delist
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}

      {/* Incoming Offers Tab */}
      {tab === 'incoming' && (
        incoming.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state__icon">📥</div>
            <div className="empty-state__title">No incoming offers</div>
            <div className="empty-state__desc">No one has made an offer for your players yet.</div>
          </div>
        ) : (
          <div className="data-table-wrapper animate-fade-in">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Player</th>
                  <th>Pos</th>
                  <th>OVR</th>
                  <th>From</th>
                  <th>Offer Amount</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {incoming.map(o => (
                  <tr key={o.id}>
                    <td className="data-table__club">{o.playerName}</td>
                    <td><span className="pos-badge">{o.position}</span></td>
                    <td className="data-table__points">{o.overallRating}</td>
                    <td>{o.buyingClubName}</td>
                    <td style={{ color: 'var(--clr-accent)', fontWeight: 600 }}>
                      {formatCurrency(o.offerAmount)}
                    </td>
                    <td><span className={`status-badge status-badge--${o.status.toLowerCase()}`}>{o.status}</span></td>
                    <td>
                      {o.status === 'PENDING' && (
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button className="btn-sm btn-sm--accent" onClick={() => handleAccept(o.id)}>Accept</button>
                          <button className="btn-sm btn-sm--danger" onClick={() => handleReject(o.id)}>Reject</button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}

      {/* Outgoing Offers Tab */}
      {tab === 'outgoing' && (
        outgoing.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state__icon">📤</div>
            <div className="empty-state__title">No outgoing offers</div>
            <div className="empty-state__desc">You haven't made any offers yet. Browse the market to find players.</div>
          </div>
        ) : (
          <div className="data-table-wrapper animate-fade-in">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Player</th>
                  <th>Pos</th>
                  <th>OVR</th>
                  <th>To</th>
                  <th>Offer Amount</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {outgoing.map(o => (
                  <tr key={o.id}>
                    <td className="data-table__club">{o.playerName}</td>
                    <td><span className="pos-badge">{o.position}</span></td>
                    <td className="data-table__points">{o.overallRating}</td>
                    <td>{o.sellingClubName}</td>
                    <td style={{ color: 'var(--clr-accent)', fontWeight: 600 }}>
                      {formatCurrency(o.offerAmount)}
                    </td>
                    <td><span className={`status-badge status-badge--${o.status.toLowerCase()}`}>{o.status}</span></td>
                    <td>
                      {o.status === 'PENDING' && (
                        <button className="btn-sm btn-sm--danger" onClick={() => handleWithdraw(o.id)}>
                          Withdraw
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}

      {/* List Player Modal */}
      {showListModal && (
        <div className="modal-overlay" onClick={() => setShowListModal(false)}>
          <div className="modal animate-fade-in" onClick={e => e.stopPropagation()}>
            <h3 className="modal__title">List Player for Sale</h3>
            <p className="modal__subtitle">Choose a player and set an asking price.</p>

            <div className="form-group" style={{ marginTop: 'var(--space-lg)' }}>
              <label className="form-group__label">Player</label>
              <select
                className="form-group__input"
                value={listPlayerId}
                onChange={e => {
                  setListPlayerId(e.target.value);
                  const player = unlistedPlayers.find(p => p.id === e.target.value);
                  if (player) setListPrice(String(player.marketValue));
                }}
              >
                <option value="">Select a player…</option>
                {unlistedPlayers.map(p => (
                  <option key={p.id} value={p.id}>
                    {p.fullName} — {p.position} (OVR {p.overallRating})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-group__label">Asking Price (£)</label>
              <input
                className="form-group__input"
                type="number"
                value={listPrice}
                onChange={e => setListPrice(e.target.value)}
                placeholder="Enter asking price"
                min="0"
              />
            </div>

            <div className="modal__actions">
              <button className="btn btn--ghost" onClick={() => setShowListModal(false)}>Cancel</button>
              <button
                className="btn btn--primary"
                style={{ width: 'auto' }}
                onClick={handleListPlayer}
                disabled={submitting || !listPlayerId || !listPrice}
              >
                {submitting ? 'Listing…' : 'List Player'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
