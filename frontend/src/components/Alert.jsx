import React from 'react';

export function Alert({ type = 'error', message, onClose }) {
  if (!message) return null;

  const isError = type === 'error';
  const isSuccess = type === 'success';

  return (
    <div
      className={`alert-banner ${isError ? 'alert-error' : isSuccess ? 'alert-success' : 'alert-info'}`}
      role="alert"
    >
      <div className="alert-content">
        <span className="alert-icon">{isError ? '⚠️' : isSuccess ? '✅' : 'ℹ️'}</span>
        <span className="alert-message">{message}</span>
      </div>
      {onClose && (
        <button type="button" className="alert-close" onClick={onClose} aria-label="Close alert">
          &times;
        </button>
      )}
    </div>
  );
}
