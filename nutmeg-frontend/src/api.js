const API_BASE = '/api';

/**
 * Fetch wrapper that handles auth tokens and JSON serialization.
 */
async function request(endpoint, { method = 'GET', body, token } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${endpoint}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    let errorMessage = `Request failed (${res.status})`;
    try {
      const errorBody = await res.json();
      errorMessage = errorBody.message || errorBody.error || errorMessage;
    } catch {
      // response body wasn't JSON
    }
    throw new Error(errorMessage);
  }

  // Some endpoints return 204 No Content
  if (res.status === 204) return null;

  return res.json();
}

/* ── Auth ── */
export async function login(login, password) {
  return request('/auth/login', {
    method: 'POST',
    body: { login, password },
  });
}

export async function register(username, email, password) {
  return request('/auth/register', {
    method: 'POST',
    body: { username, email, password },
  });
}

export async function getMe(token) {
  return request('/auth/me', { token });
}

/* ── Leagues ── */
export async function getLeagues(page = 0, token) {
  return request(`/leagues?page=${page}`, { token });
}

export async function getLeagueTable(leagueId, token) {
  return request(`/leagues/${leagueId}/table`, { token });
}

export async function getLeagueClubs(leagueId, token) {
  return request(`/leagues/${leagueId}/clubs`, { token });
}

/* ── Manager ── */
export async function assignClub(clubId, token) {
  return request('/managers/assign-club', {
    method: 'POST',
    body: { clubId },
    token,
  });
}

/* ── Club ── */
export async function getMyTactics(token) {
  return request('/clubs/my/tactics', { token });
}

export async function getMyLineup(token) {
  return request('/clubs/my/lineup', { token });
}

/* ── Finance ── */
export async function getBalance(token) {
  return request('/clubs/my/finance/balance', { token });
}

export async function getFinancialSummary(token) {
  return request('/clubs/my/finance/summary', { token });
}

/* ── Transfers ── */
export async function browseMarket(token) {
  return request('/transfers/market', { token });
}

export async function getMySquad(token) {
  return request('/clubs/my/transfers/squad', { token });
}
