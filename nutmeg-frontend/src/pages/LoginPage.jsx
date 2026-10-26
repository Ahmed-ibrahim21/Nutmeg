import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login as apiLogin } from '../api';
import { useAuth } from '../AuthContext';

export default function LoginPage() {
  const [loginValue, setLoginValue] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { loginSuccess } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!loginValue.trim() || !password) {
      setError('Please fill in all fields');
      return;
    }

    setLoading(true);
    try {
      const response = await apiLogin(loginValue.trim(), password);
      loginSuccess(response);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message || 'Invalid credentials. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-layout">
      <div className="auth-layout__bg" />
      <div className="auth-layout__content">
        <div className="auth-card">
          {/* Logo */}
          <div className="auth-card__logo">
            <div className="auth-card__logo-icon">⚽</div>
            <span className="auth-card__logo-text">Nutmeg</span>
          </div>

          <h1 className="auth-card__title">Welcome back</h1>
          <p className="auth-card__subtitle">
            Sign in to manage your club and conquer the league
          </p>

          {error && (
            <div className="alert alert--error" role="alert">
              <span>⚠</span>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label className="form-group__label" htmlFor="login-input">
                Username or Email
              </label>
              <input
                id="login-input"
                className={`form-group__input ${error ? 'form-group__input--error' : ''}`}
                type="text"
                placeholder="Enter your username or email"
                value={loginValue}
                onChange={(e) => setLoginValue(e.target.value)}
                autoComplete="username"
                autoFocus
              />
            </div>

            <div className="form-group">
              <label className="form-group__label" htmlFor="password-input">
                Password
              </label>
              <div className="form-group__password-wrapper">
                <input
                  id="password-input"
                  className={`form-group__input ${error ? 'form-group__input--error' : ''}`}
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  className="form-group__password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? '🙈' : '👁'}
                </button>
              </div>
            </div>

            <button
              id="login-submit"
              type="submit"
              className="btn btn--primary"
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="btn__spinner" />
                  Signing in…
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          <div className="auth-card__footer">
            Don't have an account?{' '}
            <Link to="/signup">Create one</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
