import React from 'react';

export function Loading({ message = 'Loading...' }) {
  return (
    <div className="loading-container" role="status" aria-live="polite">
      <div className="loading-spinner"></div>
      <p className="loading-text">{message}</p>
    </div>
  );
}
