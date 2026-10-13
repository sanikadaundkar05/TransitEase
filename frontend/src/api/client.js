const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = localStorage.getItem('transitease_token');

  const headers = {
    ...(options.headers || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // If body is NOT FormData and not already Content-Type specified, set application/json
  if (options.body && !(options.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  const config = {
    ...options,
    headers,
  };

  const response = await fetch(url, config);

  if (!response.ok) {
    let errorMessage = `HTTP Error ${response.status}: ${response.statusText}`;
    try {
      const errorJson = await response.json();
      errorMessage = errorJson.message || errorJson.error || errorMessage;
    } catch {
      // response might be raw text or empty
    }

    if (response.status === 401) {
      // Token expired or invalid
      window.dispatchEvent(new CustomEvent('transitease:unauthorized'));
    }

    const error = new Error(errorMessage);
    error.status = response.status;
    throw error;
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return await response.json();
  }

  return response;
}

export async function downloadFile(endpoint, defaultFilename = 'download.pdf') {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = localStorage.getItem('transitease_token');

  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(url, { headers });
  if (!response.ok) {
    let errorMsg = `Download failed (${response.status})`;
    try {
      const err = await response.json();
      errorMsg = err.message || errorMsg;
    } catch {}
    throw new Error(errorMsg);
  }

  // Try extracting filename from Content-Disposition header
  let filename = defaultFilename;
  const disposition = response.headers.get('content-disposition');
  if (disposition && disposition.includes('filename=')) {
    const match = disposition.match(/filename="?([^"]+)"?/);
    if (match && match[1]) {
      filename = match[1];
    }
  }

  const blob = await response.blob();
  const blobUrl = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = blobUrl;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(blobUrl);
}

export const api = {
  // Auth
  auth: {
    login: (email, password) =>
      request('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      }),
    register: (fullName, email, password) =>
      request('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ fullName, email, password }),
      }),
  },

  // Routes
  routes: {
    getAll: () => request('/api/routes'),
    getById: (id) => request(`/api/routes/${id}`),
    create: (data) =>
      request('/api/routes', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    update: (id, data) =>
      request(`/api/routes/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
    delete: (id) => request(`/api/routes/${id}`, { method: 'DELETE' }),
    toggleStatus: (id) => request(`/api/routes/${id}/toggle-status`, { method: 'PATCH' }),
  },

  // Pass Categories
  passCategories: {
    getAll: () => request('/api/pass-categories'),
    getById: (id) => request(`/api/pass-categories/${id}`),
    create: (data) =>
      request('/api/pass-categories', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    update: (id, data) =>
      request(`/api/pass-categories/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
    delete: (id) => request(`/api/pass-categories/${id}`, { method: 'DELETE' }),
  },

  // Pass Applications
  passApplications: {
    apply: (data, documentUrl) => {
      const payload = typeof data === 'object' ? data : { passCategoryId: data, documentUrl };
      return request('/api/pass-applications', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    },
    getMyApplications: () => request('/api/pass-applications/my-applications'),
    getAll: () => request('/api/pass-applications'),
    updateStatus: (id, status, remarks) =>
      request(`/api/pass-applications/${id}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status, remarks }),
      }),
    uploadDocument: (id, file, documentType) => {
      const formData = new FormData();
      formData.append('file', file);
      if (documentType) {
        formData.append('documentType', documentType);
      }
      return request(`/api/pass-applications/${id}/documents`, {
        method: 'POST',
        body: formData,
      });
    },
    getDocuments: (id) => request(`/api/pass-applications/${id}/documents`),
    downloadDocument: (docId, filename) => downloadFile(`/api/documents/${docId}/download`, filename),
    renew: (passId, data) =>
      request(`/api/pass-applications/renew/${passId}`, {
        method: 'POST',
        body: JSON.stringify(data || {}),
      }),
    getMyHistory: () => request('/api/pass-applications/my-history'),
  },

  // Documents
  documents: {
    updateStatus: (id, status, remarks) =>
      request(`/api/documents/${id}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status, remarks }),
      }),
  },

  // Digital Passes
  digitalPasses: {
    getMyPasses: () => request('/api/passes/my-passes'),
    getById: (id) => request(`/api/passes/${id}`),
    getAll: () => request('/api/passes'),
    downloadPdf: (id, filename) => downloadFile(`/api/passes/${id}/download`, filename),
    renew: (id, data) =>
      request(`/api/passes/${id}/renew`, {
        method: 'POST',
        body: JSON.stringify(data || {}),
      }),
    getMyHistory: () => request('/api/passes/my-history'),
  },

  // Notifications
  notifications: {
    getMyNotifications: (unreadOnly) =>
      request(`/api/notifications${unreadOnly ? '?unreadOnly=true' : ''}`),
    getUnreadCount: () => request('/api/notifications/unread-count'),
    getById: (id) => request(`/api/notifications/${id}`),
    markAsRead: (id) => request(`/api/notifications/${id}/read`, { method: 'PUT' }),
    markAllAsRead: () => request('/api/notifications/read-all', { method: 'PUT' }),
  },

  // Admin
  admin: {
    getUsers: (role) => request(`/api/admin/users${role ? `?role=${role}` : ''}`),
    getUserById: (id) => request(`/api/admin/users/${id}`),
    createStaff: (data) =>
      request('/api/admin/users', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    updateUser: (id, data) =>
      request(`/api/admin/users/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
    updateUserRole: (id, role) =>
      request(`/api/admin/users/${id}/role`, {
        method: 'PATCH',
        body: JSON.stringify({ role }),
      }),
    deleteUser: (id) => request(`/api/admin/users/${id}`, { method: 'DELETE' }),

    // Reports
    getSummaryReport: () => request('/api/admin/reports/summary'),
    getCategoryBreakdown: () => request('/api/admin/reports/by-category'),
    getApplicationStatusDistribution: () => request('/api/admin/reports/applications-by-status'),
    getRecentApplications: () => request('/api/admin/reports/recent-applications'),
  },
};
