import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { getBalance, getFinancialSummary, getTransactions, payWeeklyWages } from '../api';

function formatCurrency(amount) {
  if (Math.abs(amount) >= 1_000_000) return `£${(amount / 1_000_000).toFixed(1)}M`;
  if (Math.abs(amount) >= 1_000) return `£${(amount / 1_000).toFixed(0)}K`;
  return `£${amount.toLocaleString()}`;
}

function humanize(str) {
  return str.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('en-GB', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

const INCOME_TYPES = [
  'MATCH_DAY_REVENUE', 'WIN_BONUS', 'DRAW_BONUS', 'PRIZE_MONEY',
  'PLAYER_SALE', 'SPONSORSHIP', 'COMMERCIAL_REVENUE',
];

export default function FinancesPage() {
  const { token } = useAuth();
  const [balance, setBalance] = useState(null);
  const [summary, setSummary] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [payingWages, setPayingWages] = useState(false);
  const [filterType, setFilterType] = useState('ALL');

  useEffect(() => { loadAll(); }, []);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [balData, sumData, txData] = await Promise.all([
        getBalance(token),
        getFinancialSummary(token),
        getTransactions(null, token),
      ]);
      setBalance(balData);
      setSummary(sumData);
      setTransactions(txData || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handlePayWages = async () => {
    setPayingWages(true);
    setError('');
    try {
      await payWeeklyWages(token);
      setSuccess('Weekly wages paid successfully!');
      setTimeout(() => setSuccess(''), 3000);
      await loadAll();
    } catch (err) {
      setError(err.message);
    } finally {
      setPayingWages(false);
    }
  };

  const filteredTx = filterType === 'ALL'
    ? transactions
    : transactions.filter(tx => tx.type === filterType);

  if (loading) {
    return (
      <div className="page">
        <div className="page__header"><h1 className="page__title">Finances</h1></div>
        <div className="stats-grid">
          {[1, 2, 3, 4].map(i => <div key={i} className="skeleton skeleton--card" style={{ height: 120 }} />)}
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page__header">
        <div>
          <h1 className="page__title">Finances</h1>
          <p className="page__subtitle">Club balance, income, expenses and transaction history</p>
        </div>
        <button
          className="btn btn--primary"
          style={{ width: 'auto', padding: '0.6rem 1.5rem' }}
          onClick={handlePayWages}
          disabled={payingWages}
        >
          {payingWages ? (
            <><div className="btn__spinner" /> Paying…</>
          ) : '💵 Pay Weekly Wages'}
        </button>
      </div>

      {error && <div className="alert alert--error" role="alert"><span>⚠</span><span>{error}</span></div>}
      {success && <div className="alert alert--success" role="alert"><span>✓</span><span>{success}</span></div>}

      {/* Summary Stats */}
      <div className="stats-grid" style={{ marginBottom: 'var(--space-xl)' }}>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--accent">💰</div>
          <div className="stat-card__label">Balance</div>
          <div className="stat-card__value">
            {balance ? formatCurrency(balance.balance) : '—'}
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--secondary">📈</div>
          <div className="stat-card__label">Total Income</div>
          <div className="stat-card__value" style={{ color: 'var(--clr-accent)' }}>
            {summary ? formatCurrency(summary.totalIncome) : '—'}
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--danger">📉</div>
          <div className="stat-card__label">Total Expenses</div>
          <div className="stat-card__value" style={{ color: 'var(--clr-danger)' }}>
            {summary ? formatCurrency(summary.totalExpenses) : '—'}
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--warning">📊</div>
          <div className="stat-card__label">Weekly Wages</div>
          <div className="stat-card__value">
            {summary ? formatCurrency(summary.weeklyWageBill) : '—'}
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

      {/* Breakdown by Type */}
      {summary?.breakdownByType && Object.keys(summary.breakdownByType).length > 0 && (
        <>
          <div className="section-header">
            <h2 className="section-header__title">Breakdown by Category</h2>
          </div>
          <div className="breakdown-grid animate-fade-in">
            {Object.entries(summary.breakdownByType).map(([type, amount]) => {
              const isIncome = INCOME_TYPES.includes(type);
              return (
                <div key={type} className="breakdown-item">
                  <span className="breakdown-item__label">{humanize(type)}</span>
                  <span
                    className="breakdown-item__value"
                    style={{ color: isIncome ? 'var(--clr-accent)' : 'var(--clr-danger)' }}
                  >
                    {isIncome ? '+' : '-'}{formatCurrency(Math.abs(amount))}
                  </span>
                </div>
              );
            })}
          </div>
        </>
      )}

      {/* Transaction History */}
      <div className="section-header" style={{ marginTop: 'var(--space-xl)' }}>
        <h2 className="section-header__title">Transaction History</h2>
        <span className="section-header__action">{filteredTx.length} transactions</span>
      </div>

      <div className="filter-bar">
        <div className="filter-bar__pills" style={{ flexWrap: 'wrap' }}>
          <button
            className={`pill${filterType === 'ALL' ? ' pill--active' : ''}`}
            onClick={() => setFilterType('ALL')}
          >
            All
          </button>
          {[...new Set(transactions.map(tx => tx.type))].map(type => (
            <button
              key={type}
              className={`pill${filterType === type ? ' pill--active' : ''}`}
              onClick={() => setFilterType(type)}
            >
              {humanize(type)}
            </button>
          ))}
        </div>
      </div>

      {filteredTx.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state__icon">💵</div>
          <div className="empty-state__title">No transactions</div>
          <div className="empty-state__desc">No financial transactions recorded yet.</div>
        </div>
      ) : (
        <div className="data-table-wrapper animate-fade-in">
          <table className="data-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Type</th>
                <th>Description</th>
                <th>Amount</th>
                <th>Balance After</th>
              </tr>
            </thead>
            <tbody>
              {filteredTx.map(tx => {
                const isIncome = tx.amount > 0;
                return (
                  <tr key={tx.id}>
                    <td style={{ whiteSpace: 'nowrap' }}>{formatDate(tx.createdAt)}</td>
                    <td><span className="pos-badge">{humanize(tx.type)}</span></td>
                    <td>{tx.description || '—'}</td>
                    <td style={{
                      color: isIncome ? 'var(--clr-accent)' : 'var(--clr-danger)',
                      fontWeight: 600,
                    }}>
                      {isIncome ? '+' : ''}{formatCurrency(tx.amount)}
                    </td>
                    <td>{formatCurrency(tx.balanceAfter)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
