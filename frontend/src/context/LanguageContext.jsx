import React, { createContext, useContext, useState, useEffect } from 'react';
import { translations, getLocalizedPassInfo } from '../i18n/translations';

const LanguageContext = createContext(null);

export function LanguageProvider({ children }) {
  const [language, setLanguageState] = useState(() => {
    const saved = localStorage.getItem('transitease_lang');
    if (saved && (saved === 'en' || saved === 'hi' || saved === 'mr')) {
      return saved;
    }
    return 'en'; // English is the default for first-time visitors
  });

  useEffect(() => {
    try {
      localStorage.setItem('transitease_lang', language);
      document.documentElement.lang = language;
    } catch {
      // ignore storage errors
    }
  }, [language]);

  function setLanguage(lang) {
    if (lang === 'en' || lang === 'hi' || lang === 'mr') {
      setLanguageState(lang);
    }
  }

  function t(key, fallback) {
    const langDict = translations[language];
    if (langDict && langDict[key] !== undefined) {
      return langDict[key];
    }
    const enDict = translations['en'];
    if (enDict && enDict[key] !== undefined) {
      return enDict[key];
    }
    return fallback !== undefined ? fallback : key;
  }

  function getLocalizedCategory(category) {
    return getLocalizedPassInfo(category, language);
  }

  return (
    <LanguageContext.Provider value={{ language, setLanguage, t, getLocalizedCategory }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
}
