import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Alert } from './Alert';
import { Loading } from './Loading';

export function NotificationModal({ isOpen, onClose, onCountChange }) {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [markingId, setMarkingId] = useState(null);

  useEffect(() => {
    if (isOpen) {
      loadNotifications();
    }
  }, [isOpen]);

  async function loadNotifications() {
    setLoading(true);
    setError(null);
    try {
      const data = await api.notifications.getMyNotifications();
      setNotifications(data || []);
      const unreadCount = (data || []).filter((n) => !n.isRead).length;
      if (onCountChange) onCountChange(unreadCount);
    } catch (err) {
      setError(err.message || 'Failed to load notifications');
    } finally {
      setLoading(false);
    }
  }

  async function handleMarkAsRead(id) {
    setMarkingId(id);
    try {
      await api.notifications.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      const remainingUnread = notifications.filter((n) => n.id !== id && !n.isRead).length;
      if (onCountChange) onCountChange(remainingUnread);
    } catch (err) {
      setError(err.message || 'Failed to mark notification as read');
    } finally {
      setMarkingId(null);
    }
  }

  async function handleMarkAllAsRead() {
    setLoading(true);
    try {
      await api.notifications.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      if (onCountChange) onCountChange(0);
    } catch (err) {
      setError(err.message || 'Failed to mark all notifications as read');
    } finally {
      setLoading(false);
    }
  }

  if (!isOpen) return null;

  return (
    <div className="modal-backdrop" onClick={onClose} role="dialog" aria-modal="true" aria-labelledby="notif-title">
      <div className="modal-dialog modal-md" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-group">
            <h2 id="notif-title" className="modal-title">Notifications</h2>
            <span className="badge badge-primary">
              {notifications.filter((n) => !n.isRead).length} Unread
            </span>
          </div>
          <button type="button" className="modal-close" onClick={onClose} aria-label="Close">
            &times;
          </button>
        </div>

        <div className="modal-body">
          {error && <Alert type="error" message={error} onClose={() => setError(null)} />}

          <div className="notif-actions-bar">
            <button
              type="button"
              className="btn btn-sm btn-outline"
              onClick={loadNotifications}
              disabled={loading}
            >
              🔄 Refresh
            </button>
            {notifications.some((n) => !n.isRead) && (
              <button
                type="button"
                className="btn btn-sm btn-secondary"
                onClick={handleMarkAllAsRead}
                disabled={loading}
              >
                Mark all as read
              </button>
            )}
          </div>

          {loading && notifications.length === 0 ? (
            <Loading message="Loading notifications..." />
          ) : notifications.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">🔔</span>
              <p>No notifications yet.</p>
              <small className="text-muted">Updates about your applications and passes will appear here.</small>
            </div>
          ) : (
            <div className="notification-list">
              {notifications.map((notif) => (
                <div
                  key={notif.id}
                  className={`notification-card ${notif.isRead ? 'read' : 'unread'}`}
                >
                  <div className="notification-header">
                    <span className="notification-dot" />
                    <h4 className="notification-item-title">{notif.title}</h4>
                    <span className="notification-time text-muted">
                      {notif.createdAt ? new Date(notif.createdAt).toLocaleString() : ''}
                    </span>
                  </div>
                  <p className="notification-message">{notif.message}</p>
                  <div className="notification-footer">
                    {notif.eventType && (
                      <span className="badge badge-info">{notif.eventType}</span>
                    )}
                    {!notif.isRead && (
                      <button
                        type="button"
                        className="btn btn-xs btn-outline"
                        onClick={() => handleMarkAsRead(notif.id)}
                        disabled={markingId === notif.id}
                      >
                        {markingId === notif.id ? 'Marking...' : 'Mark as Read'}
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button type="button" className="btn btn-outline" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
