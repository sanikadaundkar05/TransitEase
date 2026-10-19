import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { Alert } from '../components/Alert';

export function AuthPage() {
  const { login, register, loading } = useAuth();
  const { t } = useLanguage();
  const [activeTab, setActiveTab] = useState('login'); // 'login' | 'register'

  // Login form state
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Register form state
  const [regFullName, setRegFullName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');

  // Status & error states
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  async function handleLoginSubmit(e) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      await login(loginEmail.trim(), loginPassword);
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    }
  }

  async function handleRegisterSubmit(e) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      const res = await register(regFullName.trim(), regEmail.trim(), regPassword);
      setSuccess(res?.message || 'Registration successful! You can now log in.');
      setActiveTab('login');
      setLoginEmail(regEmail.trim());
      setLoginPassword('');
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
    }
  }

  function handleQuickFill(email, password) {
    setActiveTab('login');
    setLoginEmail(email);
    setLoginPassword(password);
    setError(null);
  }

  return (
    <div className="auth-page" id="signin">
      <div className="auth-card">
        <div className="auth-brand">
          <span className="auth-brand-icon"></span>
          <h1 className="auth-title">{t('brandTitle', 'PMPML TransitEase')}</h1>
          <p className="auth-subtitle">{t('tagline', 'Pune Mahanagar Parivahan Pass Portal')}</p>
        </div>

        <div className="tab-buttons">
          <button
            type="button"
            className={`tab-btn ${activeTab === 'login' ? 'active' : ''}`}
            onClick={() => {
              setActiveTab('login');
              setError(null);
            }}
          >
            {t('signIn', 'Sign In')}
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'register' ? 'active' : ''}`}
            onClick={() => {
              setActiveTab('register');
              setError(null);
            }}
          >
            {t('passengerRegister', 'Passenger Registration')}
          </button>
        </div>

        {error && <Alert type="error" message={error} onClose={() => setError(null)} />}
        {success && <Alert type="success" message={success} onClose={() => setSuccess(null)} />}

        {activeTab === 'login' ? (
          <form className="auth-form" onSubmit={handleLoginSubmit}>
            <div className="form-group">
              <label htmlFor="login-email">Email Address</label>
              <input
                id="login-email"
                type="email"
                className="form-control"
                placeholder="you@example.com"
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </div>

            <div className="form-group">
              <label htmlFor="login-password">Password</label>
              <input
                id="login-password"
                type="password"
                className="form-control"
                placeholder="••••••••"
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                required
                autoComplete="current-password"
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-block"
              disabled={loading}
            >
              {loading ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>
        ) : (
          <form className="auth-form" onSubmit={handleRegisterSubmit}>
            <div className="form-group">
              <label htmlFor="reg-name">Full Name</label>
              <input
                id="reg-name"
                type="text"
                className="form-control"
                placeholder="Jane Doe"
                value={regFullName}
                onChange={(e) => setRegFullName(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="reg-email">Email Address</label>
              <input
                id="reg-email"
                type="email"
                className="form-control"
                placeholder="jane@example.com"
                value={regEmail}
                onChange={(e) => setRegEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </div>

            <div className="form-group">
              <label htmlFor="reg-password">Password</label>
              <input
                id="reg-password"
                type="password"
                className="form-control"
                placeholder="Create a password"
                value={regPassword}
                onChange={(e) => setRegPassword(e.target.value)}
                required
                minLength={6}
                autoComplete="new-password"
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-block"
              disabled={loading}
            >
              {loading ? 'Creating Account...' : 'Register as Passenger'}
            </button>
          </form>
        )}

        <div className="demo-accounts-panel">
          <span className="demo-title">Demo Pre-configured Accounts:</span>
          <div className="demo-chips">
            <button
              type="button"
              className="demo-chip"
              onClick={() => handleQuickFill('admin@transitease.com', 'admin123')}
            >
               Admin
            </button>
            <button
              type="button"
              className="demo-chip"
              onClick={() => handleQuickFill('verifier@transitease.com', 'verifier123')}
            >
               Verification Officer
            </button>
            <button
              type="button"
              className="demo-chip"
              onClick={() => handleQuickFill('officer@transitease.com', 'officer123')}
            >
              Approval Officer
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
