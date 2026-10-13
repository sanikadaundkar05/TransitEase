import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';
import { normalizeRole } from '../utils/roles';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('transitease_user');
    if (!savedUser) return null;
    try {
      const parsed = JSON.parse(savedUser);
      if (parsed && parsed.role) {
        parsed.role = normalizeRole(parsed.role);
      }
      return parsed;
    } catch {
      return null;
    }
  });
  const [token, setToken] = useState(() => localStorage.getItem('transitease_token'));
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    function handleUnauthorized() {
      logout();
    }
    window.addEventListener('transitease:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('transitease:unauthorized', handleUnauthorized);
  }, []);

  async function login(email, password) {
    setLoading(true);
    try {
      const response = await api.auth.login(email, password);
      // response: { id, fullName, email, role, token, message }
      const userData = {
        id: response.id,
        fullName: response.fullName,
        email: response.email,
        role: normalizeRole(response.role),
      };
      setUser(userData);
      setToken(response.token);
      localStorage.setItem('transitease_user', JSON.stringify(userData));
      localStorage.setItem('transitease_token', response.token);
      return response;
    } finally {
      setLoading(false);
    }
  }

  async function register(fullName, email, password) {
    setLoading(true);
    try {
      const response = await api.auth.register(fullName, email, password);
      return response;
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    setUser(null);
    setToken(null);
    localStorage.removeItem('transitease_user');
    localStorage.removeItem('transitease_token');
  }

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
