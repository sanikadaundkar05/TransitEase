import React from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LanguageProvider } from './context/LanguageContext';
import { Navbar } from './components/Navbar';
import { AuthPage } from './pages/AuthPage';
import { HomePage } from './pages/HomePage';
import { PassengerDashboard } from './pages/PassengerDashboard';
import { OfficerDashboard } from './pages/OfficerDashboard';
import { AdminDashboard } from './pages/AdminDashboard';
import { isAdmin, isOfficer, isRouteManager } from './utils/roles';

function AppContent() {
  const { user } = useAuth();

  if (!user) {
    const isSignInPage = window.location.pathname === '/signin';

    return (
      <div className="app-layout">
        <Navbar />
        {isSignInPage ? <AuthPage /> : <HomePage />}
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

      {/* Content based on role and mode */}
      <div className="main-viewport">
        {userIsAdmin ? (
          <AdminDashboard />
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
