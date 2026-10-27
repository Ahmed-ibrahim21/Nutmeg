import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './AuthContext';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import HomePage from './pages/HomePage';
import SquadPage from './pages/SquadPage';
import TacticsPage from './pages/TacticsPage';
import LineupPage from './pages/LineupPage';
import LeaguePage from './pages/LeaguePage';
import TransferMarketPage from './pages/TransferMarketPage';
import MyTransfersPage from './pages/MyTransfersPage';
import FinancesPage from './pages/FinancesPage';
import MatchResultPage from './pages/MatchResultPage';
import AppShell from './components/AppShell';

function ProtectedRoute({ children }) {
  const { token, loading } = useAuth();

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        background: 'var(--clr-bg-primary)',
      }}>
        <div className="btn__spinner" style={{ width: 32, height: 32, borderWidth: 3, borderColor: 'rgba(52,211,153,0.2)', borderTopColor: 'var(--clr-accent)' }} />
      </div>
    );
  }

  return token ? <AppShell>{children}</AppShell> : <Navigate to="/login" replace />;
}

function GuestRoute({ children }) {
  const { token, loading } = useAuth();

  if (loading) return null;

  return token ? <Navigate to="/" replace /> : children;
}

function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <GuestRoute>
            <LoginPage />
          </GuestRoute>
        }
      />
      <Route
        path="/signup"
        element={
          <GuestRoute>
            <SignupPage />
          </GuestRoute>
        }
      />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <HomePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/squad"
        element={
          <ProtectedRoute>
            <SquadPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/tactics"
        element={
          <ProtectedRoute>
            <TacticsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/lineup"
        element={
          <ProtectedRoute>
            <LineupPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/league"
        element={
          <ProtectedRoute>
            <LeaguePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/transfers"
        element={
          <ProtectedRoute>
            <TransferMarketPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/transfers/my"
        element={
          <ProtectedRoute>
            <MyTransfersPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/finances"
        element={
          <ProtectedRoute>
            <FinancesPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/match/:fixtureId"
        element={
          <ProtectedRoute>
            <MatchResultPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
