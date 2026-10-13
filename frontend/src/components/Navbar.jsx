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

  return (
    <>
      <header className="navbar">
        <div className="navbar-container">
          <div className="navbar-brand">
            <span className="brand-icon">🚌</span>
            <div className="brand-text">
              <span className="brand-title">{t('brandTitle', 'PMPML TransitEase')}</span>
              <span className="brand-subtitle">{t('brandSubtitle', 'Pune Mahanagar Parivahan')}</span>
            </div>
          </div>

          <div className="navbar-actions">
            {/* Language Selector Dropdown (English, Hindi, Marathi) */}
            <div className="lang-selector-wrapper">
              <label htmlFor="navbar-lang-select" className="visually-hidden">
                {t('selectLanguage', 'Language')}
              </label>
              <span className="lang-globe-icon" aria-hidden="true">🌐</span>
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
                    <span className="bell-icon">🔔</span>
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
                  onClick={logout}
                >
                  {t('signOut', 'Sign Out')}
                </button>
              </>
            )}
          </div>
        </div>
      </header>

      {user && user.role === 'ROLE_PASSENGER' && (
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
