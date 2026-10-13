import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LanguageProvider, useLanguage } from './context/LanguageContext';
import { Navbar } from './components/Navbar';
import { AuthPage } from './pages/AuthPage';
import { PassengerDashboard } from './pages/PassengerDashboard';
import { OfficerDashboard } from './pages/OfficerDashboard';
import { AdminDashboard } from './pages/AdminDashboard';
import { isAdmin, isOfficer, isRouteManager } from './utils/roles';

function AppContent() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const [adminViewMode, setAdminViewMode] = useState('admin'); // 'admin' | 'officer' | 'passenger'

  if (!user) {
    return (
      <div className="app-layout">
        <Navbar />
        <AuthPage />
      </div>
    );
  }

  // Determine what dashboard to render
  const role = user.role || '';
  const userIsAdmin = isAdmin(role);
  const userIsOfficer = isOfficer(role);
  const userIsRouteManager = isRouteManager(role);

  return (
    <div className="app-layout">
      <Navbar />

      {/* If Admin, show a top switch bar allowing admin to inspect officer & passenger experiences as well */}
      {userIsAdmin && (
        <div className="admin-view-switcher">
          <div className="container view-switch-bar">
            <span className="view-switch-label">Admin Demo Switcher:</span>
            <div className="btn-group">
              <button
                type="button"
                className={`btn btn-xs ${adminViewMode === 'admin' ? 'btn-primary' : 'btn-outline'}`}
                onClick={() => setAdminViewMode('admin')}
              >
                🛠️ {t('adminPortal', 'Admin Control Center')}
              </button>
              <button
                type="button"
                className={`btn btn-xs ${adminViewMode === 'officer' ? 'btn-primary' : 'btn-outline'}`}
                onClick={() => setAdminViewMode('officer')}
              >
                🛡️ {t('officerPortal', 'Verification & Approval Queue')}
              </button>
              <button
                type="button"
                className={`btn btn-xs ${adminViewMode === 'passenger' ? 'btn-primary' : 'btn-outline'}`}
                onClick={() => setAdminViewMode('passenger')}
              >
                🎫 {t('rolePassenger', 'Passenger Portal')}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Content based on role and mode */}
      <div className="main-viewport">
        {userIsAdmin ? (
          adminViewMode === 'admin' ? (
            <AdminDashboard />
          ) : adminViewMode === 'officer' ? (
            <OfficerDashboard />
          ) : (
            <PassengerDashboard />
          )
        ) : userIsOfficer ? (
          <OfficerDashboard />
        ) : userIsRouteManager ? (
          <AdminDashboard />
        ) : (
          <PassengerDashboard />
        )}
      </div>
    </div>
  );
}

export default function App() {
  return (
    <LanguageProvider>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </LanguageProvider>
  );
}

