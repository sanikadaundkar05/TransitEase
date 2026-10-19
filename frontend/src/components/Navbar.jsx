import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { api } from '../api/client';
import { NotificationModal } from './NotificationModal';
import { isPassenger, normalizeRole } from '../utils/roles';

export function Navbar() {
  const { user, logout } = useAuth();
  const { language, setLanguage, t } = useLanguage();
  const [unreadCount, setUnreadCount] = useState(0);
  const [isNotifOpen, setIsNotifOpen] = useState(false);

  useEffect(() => {
    if (user && isPassenger(user.role)) {
      fetchUnreadCount();
      const interval = setInterval(fetchUnreadCount, 30000); // 30 sec polling
      return () => clearInterval(interval);
    }
  }, [user]);

  async function fetchUnreadCount() {
    try {
      const res = await api.notifications.getUnreadCount();
      if (res && typeof res.count === 'number') {
        setUnreadCount(res.count);
      }
    } catch {
      // quiet fail on background polling
    }
  }

  function getLocalizedRole(role) {
    if (!role) return '';
    const norm = normalizeRole(role);
    if (norm === 'PASSENGER') return t('rolePassenger', 'Passenger');
    if (norm === 'DOCUMENT_VERIFICATION_OFFICER' || norm === 'VERIFICATION_OFFICER') {
      return t('roleVerifier', 'Verification Officer');
    }
    if (norm === 'PASS_APPROVAL_OFFICER' || norm === 'OFFICER') {
      return t('roleApprover', 'Approval Officer');
    }
    if (norm === 'ADMIN') return t('roleAdmin', 'System Admin');
    if (norm === 'ROUTE_FARE_MANAGER') return t('roleRouteManager', 'Route Manager');
    return norm.replace('_', ' ');
  }

  function handleLogout() {
    logout();
    window.location.assign('/');
  }

  function NotificationIcon() {
    return (
      <svg className="navbar-notification-svg" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9ZM10 21h4" />
      </svg>
    );
  }

  return (
    <>
      <header className="navbar">
        <div className="navbar-container">
          <div className="navbar-brand">
            <span className="brand-icon" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M5 16.5V6.8C5 5.25 6.25 4 7.8 4h8.4C17.75 4 19 5.25 19 6.8v9.7M4 16.5h16M7 19h2m6 0h2M7 8h10M8 12h2m4 0h2" /><circle cx="8" cy="16.5" r="1" /><circle cx="16" cy="16.5" r="1" /></svg></span>
            <div className="brand-text">
              <span className="brand-title">TransitEase</span>
              <span className="brand-subtitle">PMPML · {t('brandSubtitle', 'Pune Mahanagar Parivahan')}</span>
            </div>
          </div>

          {!user && (
            <nav className="public-navbar-links" aria-label="Public navigation">
              <a href="/">Home</a>
              <a href="#routes">Routes</a>
              <a href="#passes">Passes</a>
              <a href="#about">About PMPML</a>
            </nav>
          )}

          <div className="navbar-actions">
            {/* Language Selector Dropdown (English, Hindi, Marathi) */}
            <div className="lang-selector-wrapper">
              <label htmlFor="navbar-lang-select" className="visually-hidden">
                {t('selectLanguage', 'Language')}
              </label>
              <span className="lang-globe-icon" aria-hidden="true"></span>
              <select
                id="navbar-lang-select"
                className="lang-dropdown-select"
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
                aria-label="Select Language"
              >
                <option value="en">English (EN)</option>
                <option value="hi">हिन्दी (HI)</option>
                <option value="mr">मराठी (MR)</option>
              </select>
            </div>

            {!user && (
              <a className="btn btn-sm public-signin-btn" href="/signin">
                {t('signIn', 'Sign In')}
              </a>
            )}

            {user && (
              <>
                <div className="user-profile-badge">
                  <span className="user-name">{user.fullName || user.email}</span>
                  <span className={`badge badge-role badge-${normalizeRole(user.role).toLowerCase() || 'user'}`}>
                    {getLocalizedRole(user.role)}
                  </span>
                </div>

                {isPassenger(user.role) && (
                  <button
                    type="button"
                    className="btn btn-icon notif-bell-btn"
                    onClick={() => setIsNotifOpen(true)}
                    aria-label={t('notifications', 'Notifications')}
                    title={t('notifications', 'Notifications')}
                  >
                    <span className="bell-icon"><NotificationIcon /></span>
                    {unreadCount > 0 && (
                      <span className="notif-count-badge">
                        {unreadCount > 99 ? '99+' : unreadCount}
                      </span>
                    )}
                  </button>
                )}

                <button
                  type="button"
                  className="btn btn-sm btn-outline btn-logout"
                  onClick={handleLogout}
                >
                  {t('signOut', 'Sign Out')}
                </button>
              </>
            )}
          </div>
        </div>
      </header>

      {user && isPassenger(user.role) && (
        <NotificationModal
          isOpen={isNotifOpen}
          onClose={() => {
            setIsNotifOpen(false);
            fetchUnreadCount();
          }}
          onCountChange={(cnt) => setUnreadCount(cnt)}
        />
      )}
    </>
  );
}
