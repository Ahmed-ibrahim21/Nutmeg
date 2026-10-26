import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { getMe } from './api';

const AuthContext = createContext(null);

const TOKEN_KEY = 'nutmeg_token';
const MANAGER_KEY = 'nutmeg_manager';

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [manager, setManager] = useState(() => {
    const stored = localStorage.getItem(MANAGER_KEY);
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(!!localStorage.getItem(TOKEN_KEY));

  // Verify stored token on mount
  useEffect(() => {
    if (!token) {
      setLoading(false);
      return;
    }

    getMe(token)
      .then((profile) => {
        setManager(profile);
        localStorage.setItem(MANAGER_KEY, JSON.stringify(profile));
      })
      .catch(() => {
        // Token expired or invalid
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(MANAGER_KEY);
        setToken(null);
        setManager(null);
      })
      .finally(() => setLoading(false));
  }, [token]);

  const loginSuccess = useCallback((loginResponse) => {
    setToken(loginResponse.accessToken);
    setManager(loginResponse.manager);
    localStorage.setItem(TOKEN_KEY, loginResponse.accessToken);
    localStorage.setItem(MANAGER_KEY, JSON.stringify(loginResponse.manager));
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setManager(null);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(MANAGER_KEY);
  }, []);

  return (
    <AuthContext.Provider value={{ token, manager, loading, loginSuccess, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
