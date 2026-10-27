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

export async function simulateRound(leagueId, token) {
  return request(`/leagues/${leagueId}/simulate-round`, {
    method: 'POST',
    token,
  });
}

export async function simulateFixture(fixtureId, seed, token) {
  const qs = seed != null ? `?seed=${seed}` : '';
  return request(`/leagues/${fixtureId}/simulate${qs}`, {
    method: 'POST',
    token,
  });
}

/* ── Manager ── */
export async function assignClub(clubId, token) {
  return request('/managers/assign-club', {
    method: 'POST',
    body: { clubId },
    token,
  });
}

/* ── Club — Tactics ── */
export async function getMyTactics(token) {
  return request('/clubs/my/tactics', { token });
}

export async function updateTactics(tactics, token) {
  return request('/clubs/my/tactics', {
    method: 'PUT',
    body: tactics,
    token,
  });
}

/* ── Club — Lineup ── */
export async function getMyLineup(token) {
  return request('/clubs/my/lineup', { token });
}

export async function updateLineup(lineup, token) {
  return request('/clubs/my/lineup', {
    method: 'PUT',
    body: { lineup },
    token,
  });
}

/* ── Finance ── */
export async function getBalance(token) {
  return request('/clubs/my/finance/balance', { token });
}

export async function getFinancialSummary(token) {
  return request('/clubs/my/finance/summary', { token });
}

export async function getTransactions(type, token) {
  const qs = type ? `?type=${type}` : '';
  return request(`/clubs/my/finance/transactions${qs}`, { token });
}

export async function payWeeklyWages(token) {
  return request('/clubs/my/finance/wages/pay', {
    method: 'POST',
    token,
  });
}

/* ── Transfers — Market ── */
export async function browseMarket(token) {
  return request('/transfers/market', { token });
}

export async function getClubSquad(clubId, token) {
  return request(`/transfers/clubs/${clubId}/squad`, { token });
}

/* ── Transfers — My Squad ── */
export async function getMySquad(token) {
  return request('/clubs/my/transfers/squad', { token });
}

/* ── Transfers — My Listings ── */
export async function getMyListings(token) {
  return request('/clubs/my/transfers/listings', { token });
}

export async function listPlayer(playerId, askingPrice, token) {
  return request('/clubs/my/transfers/listings', {
    method: 'POST',
    body: { playerId, askingPrice },
    token,
  });
}

export async function delistPlayer(listingId, token) {
  return request(`/clubs/my/transfers/listings/${listingId}`, {
    method: 'DELETE',
    token,
  });
}

/* ── Transfers — Offers ── */
export async function makeOffer(playerId, offerAmount, token) {
  return request('/clubs/my/transfers/offers', {
    method: 'POST',
    body: { playerId, offerAmount },
    token,
  });
}

export async function getIncomingOffers(token) {
  return request('/clubs/my/transfers/offers/incoming', { token });
}

export async function getOutgoingOffers(token) {
  return request('/clubs/my/transfers/offers/outgoing', { token });
}

export async function acceptOffer(offerId, token) {
  return request(`/clubs/my/transfers/offers/${offerId}/accept`, {
    method: 'POST',
    token,
  });
}

export async function rejectOffer(offerId, token) {
  return request(`/clubs/my/transfers/offers/${offerId}/reject`, {
    method: 'POST',
    token,
  });
}

export async function withdrawOffer(offerId, token) {
  return request(`/clubs/my/transfers/offers/${offerId}`, {
    method: 'DELETE',
    token,
  });
}
