import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { browseMarket, makeOffer } from '../api';

function formatCurrency(amount) {
  if (amount >= 1_000_000) return `£${(amount / 1_000_000).toFixed(1)}M`;
  if (amount >= 1_000) return `£${(amount / 1_000).toFixed(0)}K`;
  return `£${amount.toLocaleString()}`;
}

const POSITION_FILTERS = ['All', 'GK', 'CB', 'LB', 'RB', 'CM', 'CDM', 'CAM', 'LM', 'RM', 'LW', 'RW', 'ST'];

export default function TransferMarketPage() {
  const { token } = useAuth();
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [filterPos, setFilterPos] = useState('All');
  const [sortBy, setSortBy] = useState('rating-desc');

  // Offer modal
  const [offerTarget, setOfferTarget] = useState(null);
  const [offerAmount, setOfferAmount] = useState('');
  const [submittingOffer, setSubmittingOffer] = useState(false);

  useEffect(() => { loadMarket(); }, []);

  const loadMarket = async () => {
    setLoading(true);
    try {
      const data = await browseMarket(token);
      setListings(data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleMakeOffer = async () => {
    if (!offerTarget || !offerAmount) return;
    setSubmittingOffer(true);
    setError('');
    try {
      await makeOffer(offerTarget.playerId, parseInt(offerAmount), token);
      setSuccess(`Offer submitted for ${offerTarget.playerName}!`);
      setOfferTarget(null);
      setOfferAmount('');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmittingOffer(false);
    }
  };

  const filtered = listings.filter(l => {
    if (filterPos === 'All') return true;
    return l.position === filterPos;
  });

  const sorted = [...filtered].sort((a, b) => {
    switch (sortBy) {
      case 'rating-desc': return b.overallRating - a.overallRating;
      case 'rating-asc': return a.overallRating - b.overallRating;
      case 'price-desc': return b.askingPrice - a.askingPrice;
      case 'price-asc': return a.askingPrice - b.askingPrice;
      case 'value-desc': return b.marketValue - a.marketValue;
      default: return 0;
    }
  });

  if (loading) {
    return (
      <div className="page">
        <div className="page__header"><h1 className="page__title">Transfer Market</h1></div>
        <div className="clubs-grid">
          {[1, 2, 3, 4, 5, 6].map(i => <div key={i} className="skeleton skeleton--card" style={{ height: 200 }} />)}
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page__header">
        <div>
          <h1 className="page__title">Transfer Market</h1>
          <p className="page__subtitle">{listings.length} players available</p>
        </div>
      </div>

      {error && <div className="alert alert--error" role="alert"><span>⚠</span><span>{error}</span></div>}
      {success && <div className="alert alert--success" role="alert"><span>✓</span><span>{success}</span></div>}

      {/* Filters */}
      <div className="filter-bar">
        <div className="filter-bar__pills" style={{ flexWrap: 'wrap' }}>
          {['All', 'GK', 'DEF', 'MID', 'FWD'].map(g => (
            <button
              key={g}
              className={`pill${filterPos === g ? ' pill--active' : ''}`}
              onClick={() => setFilterPos(g === 'DEF' ? 'CB' : g === 'MID' ? 'CM' : g === 'FWD' ? 'ST' : g)}
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
          <option value="rating-desc">Rating ↓</option>
          <option value="rating-asc">Rating ↑</option>
          <option value="price-desc">Price ↓</option>
          <option value="price-asc">Price ↑</option>
          <option value="value-desc">Value ↓</option>
        </select>
      </div>

      {/* Listings */}
      {sorted.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state__icon">🔄</div>
          <div className="empty-state__title">No listings found</div>
          <div className="empty-state__desc">No players currently listed on the transfer market.</div>
        </div>
      ) : (
        <div className="data-table-wrapper animate-fade-in">
          <table className="data-table">
            <thead>
              <tr>
                <th>Player</th>
                <th>Pos</th>
                <th>OVR</th>
                <th>Club</th>
                <th>Market Value</th>
                <th>Asking Price</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {sorted.map(listing => (
                <tr key={listing.id}>
                  <td className="data-table__club">{listing.playerName}</td>
                  <td><span className="pos-badge">{listing.position}</span></td>
                  <td className="data-table__points">{listing.overallRating}</td>
                  <td>{listing.sellingClubName}</td>
                  <td>{formatCurrency(listing.marketValue)}</td>
                  <td style={{ color: 'var(--clr-accent)', fontWeight: 600 }}>
                    {formatCurrency(listing.askingPrice)}
                  </td>
                  <td>
                    <button
                      className="btn-sm btn-sm--accent"
                      onClick={() => {
                        setOfferTarget(listing);
                        setOfferAmount(String(listing.askingPrice));
                      }}
                    >
                      Make Offer
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Offer Modal */}
      {offerTarget && (
        <div className="modal-overlay" onClick={() => setOfferTarget(null)}>
          <div className="modal animate-fade-in" onClick={e => e.stopPropagation()}>
            <h3 className="modal__title">Make Offer</h3>
            <p className="modal__subtitle">
              {offerTarget.playerName} • {offerTarget.position} • OVR {offerTarget.overallRating}
            </p>
            <p className="modal__detail">
              Asking Price: <strong style={{ color: 'var(--clr-accent)' }}>{formatCurrency(offerTarget.askingPrice)}</strong>
            </p>

            <div className="form-group" style={{ marginTop: 'var(--space-lg)' }}>
              <label className="form-group__label">Your Offer (£)</label>
              <input
                className="form-group__input"
                type="number"
                value={offerAmount}
                onChange={e => setOfferAmount(e.target.value)}
                placeholder="Enter amount"
                min="0"
              />
            </div>

            <div className="modal__actions">
              <button className="btn btn--ghost" onClick={() => setOfferTarget(null)}>
                Cancel
              </button>
              <button
                className="btn btn--primary"
                style={{ width: 'auto' }}
                onClick={handleMakeOffer}
                disabled={submittingOffer || !offerAmount}
              >
                {submittingOffer ? 'Submitting…' : `Offer ${offerAmount ? formatCurrency(parseInt(offerAmount)) : '—'}`}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
