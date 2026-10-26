import { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register as apiRegister } from '../api';
import { useAuth } from '../AuthContext';

function getPasswordStrength(pw) {
  if (!pw) return { score: 0, label: '' };
  let score = 0;
  if (pw.length >= 8) score++;
  if (pw.length >= 12) score++;
  if (/[A-Z]/.test(pw)) score++;
  if (/[0-9]/.test(pw)) score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;

  if (score <= 1) return { score: 1, label: 'Weak' };
  if (score <= 3) return { score: 2, label: 'Medium' };
  return { score: 3, label: 'Strong' };
}

export default function SignupPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);

  const { loginSuccess } = useAuth();
  const navigate = useNavigate();

  const strength = useMemo(() => getPasswordStrength(password), [password]);

  const validate = () => {
    const errs = {};
    if (!username.trim()) errs.username = 'Username is required';
    if (!email.trim()) errs.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errs.email = 'Invalid email address';
    if (!password) errs.password = 'Password is required';
    else if (password.length < 8) errs.password = 'Password must be at least 8 characters';
    if (password !== confirmPassword) errs.confirmPassword = 'Passwords do not match';
    return errs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');

    const validationErrors = validate();
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) return;

    setLoading(true);
    try {
      const response = await apiRegister(username.trim(), email.trim(), password);
      loginSuccess(response);
      navigate('/', { replace: true });
    } catch (err) {
      setServerError(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const strengthBars = [1, 2, 3].map((level) => {
    let className = 'password-strength__bar';
    if (strength.score >= level) {
      if (strength.score === 1) className += ' password-strength__bar--weak';
      else if (strength.score === 2) className += ' password-strength__bar--medium';
      else className += ' password-strength__bar--strong';
    }
    return <div key={level} className={className} />;
  });

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

          <h1 className="auth-card__title">Create your account</h1>
          <p className="auth-card__subtitle">
            Start your managerial career and lead your club to glory
          </p>

          {serverError && (
            <div className="alert alert--error" role="alert">
              <span>⚠</span>
              <span>{serverError}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            {/* Username */}
            <div className="form-group">
              <label className="form-group__label" htmlFor="signup-username">
                Username
              </label>
              <input
                id="signup-username"
                className={`form-group__input ${errors.username ? 'form-group__input--error' : ''}`}
                type="text"
                placeholder="Choose a unique username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
                autoFocus
              />
              {errors.username && (
                <span className="form-group__error">⚠ {errors.username}</span>
              )}
            </div>

            {/* Email */}
            <div className="form-group">
              <label className="form-group__label" htmlFor="signup-email">
                Email
              </label>
              <input
                id="signup-email"
                className={`form-group__input ${errors.email ? 'form-group__input--error' : ''}`}
                type="email"
                placeholder="your@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
              />
              {errors.email && (
                <span className="form-group__error">⚠ {errors.email}</span>
              )}
            </div>

            {/* Password */}
            <div className="form-group">
              <label className="form-group__label" htmlFor="signup-password">
                Password
              </label>
              <div className="form-group__password-wrapper">
                <input
                  id="signup-password"
                  className={`form-group__input ${errors.password ? 'form-group__input--error' : ''}`}
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Min. 8 characters"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="new-password"
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
              {password && (
                <>
                  <div className="password-strength">{strengthBars}</div>
                  <div className="password-strength__label">{strength.label}</div>
                </>
              )}
              {errors.password && (
                <span className="form-group__error">⚠ {errors.password}</span>
              )}
            </div>

            {/* Confirm Password */}
            <div className="form-group">
              <label className="form-group__label" htmlFor="signup-confirm-password">
                Confirm Password
              </label>
              <input
                id="signup-confirm-password"
                className={`form-group__input ${errors.confirmPassword ? 'form-group__input--error' : ''}`}
                type={showPassword ? 'text' : 'password'}
                placeholder="Re-enter your password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                autoComplete="new-password"
              />
              {errors.confirmPassword && (
                <span className="form-group__error">⚠ {errors.confirmPassword}</span>
              )}
            </div>

            <button
              id="signup-submit"
              type="submit"
              className="btn btn--primary"
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="btn__spinner" />
                  Creating account…
                </>
              ) : (
                'Create Account'
              )}
            </button>
          </form>

          <div className="auth-card__footer">
            Already have an account?{' '}
            <Link to="/login">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
