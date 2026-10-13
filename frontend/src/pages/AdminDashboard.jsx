import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useLanguage } from '../context/LanguageContext';
import { Alert } from '../components/Alert';
import { Loading } from '../components/Loading';
import { formatINR, formatCategoryFare } from '../utils/formatters';

export function AdminDashboard() {
  const { t } = useLanguage();
  const [activeTab, setActiveTab] = useState('reports'); // 'reports' | 'users' | 'routes' | 'categories'

  // Reports state
  const [summaryReport, setSummaryReport] = useState(null);
  const [categoryReport, setCategoryReport] = useState([]);
  const [statusDistribution, setStatusDistribution] = useState(null);
  const [recentApps, setRecentApps] = useState([]);

  // Users state
  const [users, setUsers] = useState([]);
  const [userRoleFilter, setUserRoleFilter] = useState('');
  const [newStaff, setNewStaff] = useState({
    fullName: '',
    email: '',
    password: '',
    role: 'DOCUMENT_VERIFICATION_OFFICER',
  });
  const [isStaffModalOpen, setIsStaffModalOpen] = useState(false);

  // Routes state
  const [routes, setRoutes] = useState([]);
  const [isRouteModalOpen, setIsRouteModalOpen] = useState(false);
  const [routeForm, setRouteForm] = useState({
    routeNumber: '',
    source: '',
    destination: '',
    fare: 25.0,
    distanceKm: 12.0,
    stops: '',
    description: '',
    active: true,
  });

  // Categories state
  const [categories, setCategories] = useState([]);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [categoryForm, setCategoryForm] = useState({
    name: '',
    description: '',
    validityDays: 30,
    baseFare: 750.0,
    categoryGroup: 'GENERAL',
    eligibility: '',
    mandatoryDocuments: '',
    optionalDocuments: '',
    fareType: 'FIXED',
    discountDetails: '',
  });

  // General async states
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    loadTabData(activeTab);
  }, [activeTab]);

  async function loadTabData(tab) {
    setLoading(true);
    setError(null);
    try {
      if (tab === 'reports') {
        const [sum, catRep, statusDist, recent] = await Promise.all([
          api.admin.getSummaryReport(),
          api.admin.getCategoryBreakdown(),
          api.admin.getApplicationStatusDistribution(),
          api.admin.getRecentApplications(),
        ]);
        setSummaryReport(sum);
        setCategoryReport(catRep || []);
        setStatusDistribution(statusDist || null);
        setRecentApps(recent || []);
      } else if (tab === 'users') {
        const data = await api.admin.getUsers(userRoleFilter || undefined);
        setUsers(data || []);
      } else if (tab === 'routes') {
        const data = await api.routes.getAll();
        setRoutes(data || []);
      } else if (tab === 'categories') {
        const data = await api.passCategories.getAll();
        setCategories(data || []);
      }
    } catch (err) {
      setError(err.message || 'Failed to load data.');
    } finally {
      setLoading(false);
    }
  }

  // --- USER ACTIONS ---
  async function handleFilterUsers(role) {
    setUserRoleFilter(role);
    setLoading(true);
    setError(null);
    try {
      const data = await api.admin.getUsers(role || undefined);
      setUsers(data || []);
    } catch (err) {
      setError(err.message || 'Failed to filter users.');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateStaffSubmit(e) {
    e.preventDefault();
    setActionLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const created = await api.admin.createStaff(newStaff);
      setSuccess(`Staff user ${created.fullName} (${created.role}) created successfully!`);
      setIsStaffModalOpen(false);
      setNewStaff({
        fullName: '',
        email: '',
        password: '',
        role: 'DOCUMENT_VERIFICATION_OFFICER',
      });
      const data = await api.admin.getUsers(userRoleFilter || undefined);
      setUsers(data || []);
    } catch (err) {
      setError(err.message || 'Failed to create staff user.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleRoleChange(userId, newRole) {
    if (!window.confirm(`Are you sure you want to change this user's role to ${newRole}?`)) {
      return;
    }
    setActionLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await api.admin.updateUserRole(userId, newRole);
      setSuccess('User role updated successfully.');
      const data = await api.admin.getUsers(userRoleFilter || undefined);
      setUsers(data || []);
    } catch (err) {
      setError(err.message || 'Failed to update user role.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDeleteUser(userId, userEmail) {
    if (!window.confirm(`Are you sure you want to delete user ${userEmail}? This action cannot be undone.`)) {
      return;
    }
    setActionLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await api.admin.deleteUser(userId);
      setSuccess(`User ${userEmail} deleted successfully.`);
      setUsers((prev) => prev.filter((u) => u.id !== userId));
    } catch (err) {
      setError(err.message || 'Failed to delete user.');
    } finally {
      setActionLoading(false);
    }
  }

  // --- ROUTE ACTIONS ---
  async function handleCreateRouteSubmit(e) {
    e.preventDefault();
    setActionLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const payload = {
        ...routeForm,
        fare: Number(routeForm.fare),
        distanceKm: Number(routeForm.distanceKm),
      };
      await api.routes.create(payload);
      setSuccess(`Route ${routeForm.routeNumber} created successfully!`);
      setIsRouteModalOpen(false);
      setRouteForm({
        routeNumber: '',
        source: '',
        destination: '',
        fare: 2.5,
        distanceKm: 10.0,
        stops: '',
        description: '',
        active: true,
      });
      const data = await api.routes.getAll();
      setRoutes(data || []);
    } catch (err) {
      setError(err.message || 'Failed to create route.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleToggleRouteStatus(id) {
    setActionLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const updated = await api.routes.toggleStatus(id);
      setSuccess(`Route ${updated.routeNumber} status updated to ${updated.active ? 'ACTIVE' : 'INACTIVE'}`);
      const data = await api.routes.getAll();
      setRoutes(data || []);
    } catch (err) {
      setError(err.message || 'Failed to toggle route status.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDeleteRoute(id, routeNumber) {
    if (!window.confirm(`Are you sure you want to delete route ${routeNumber}?`)) {
      return;
    }
    setActionLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await api.routes.delete(id);
      setSuccess(`Route ${routeNumber} deleted.`);
      setRoutes((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      setError(err.message || 'Failed to delete route.');
    } finally {
      setActionLoading(false);
    }
  }

  // --- CATEGORY ACTIONS ---
  async function handleCreateCategorySubmit(e) {
    e.preventDefault();
    setActionLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const payload = {
        name: categoryForm.name.trim(),
        description: categoryForm.description.trim(),
        validityDays: Number(categoryForm.validityDays),
        baseFare: Number(categoryForm.baseFare) || 0.0,
        categoryGroup: categoryForm.categoryGroup || 'GENERAL',
        eligibility: categoryForm.eligibility?.trim() || null,
        mandatoryDocuments: categoryForm.mandatoryDocuments?.trim() || null,
        optionalDocuments: categoryForm.optionalDocuments?.trim() || null,
        fareType: categoryForm.fareType || 'FIXED',
        discountDetails: categoryForm.discountDetails?.trim() || null,
      };
      await api.passCategories.create(payload);
      setSuccess(`Category "${categoryForm.name}" created successfully!`);
      setIsCategoryModalOpen(false);
      setCategoryForm({
        name: '',
        description: '',
        validityDays: 30,
        baseFare: 750.0,
        categoryGroup: 'GENERAL',
        eligibility: '',
        mandatoryDocuments: '',
        optionalDocuments: '',
        fareType: 'FIXED',
        discountDetails: '',
      });
      const data = await api.passCategories.getAll();
      setCategories(data || []);
    } catch (err) {
      setError(err.message || 'Failed to create category.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDeleteCategory(id, name) {
    if (!window.confirm(`Are you sure you want to delete category "${name}"?`)) {
      return;
    }
    setActionLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await api.passCategories.delete(id);
      setSuccess(`Category "${name}" deleted.`);
      setCategories((prev) => prev.filter((c) => c.id !== id));
    } catch (err) {
      setError(err.message || 'Failed to delete category.');
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <div className="dashboard-layout">
      {/* Sidebar Navigation */}
      <nav className="dashboard-sidebar" aria-label="Admin navigation">
        <div className="sidebar-header">
          <h3>{t('adminPortal', 'Admin Control Center')}</h3>
        </div>
        <ul className="sidebar-nav-list">
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'reports' ? 'active' : ''}`}
              onClick={() => setActiveTab('reports')}
            >
              <span className="nav-icon">📊</span> {t('tabReports', 'Overview & Reports')}
            </button>
          </li>
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'users' ? 'active' : ''}`}
              onClick={() => setActiveTab('users')}
            >
              <span className="nav-icon">👥</span> {t('tabStaff', 'Staff & Users')}
            </button>
          </li>
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'routes' ? 'active' : ''}`}
              onClick={() => setActiveTab('routes')}
            >
              <span className="nav-icon">🗺️</span> {t('tabRouteMgmt', 'Route Management')}
            </button>
          </li>
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'categories' ? 'active' : ''}`}
              onClick={() => setActiveTab('categories')}
            >
              <span className="nav-icon">🏷️</span> {t('tabCategoryMgmt', 'Pass Categories')}
            </button>
          </li>
        </ul>
      </nav>

      {/* Main Content */}
      <main className="dashboard-content">
        {error && <Alert type="error" message={error} onClose={() => setError(null)} />}
        {success && <Alert type="success" message={success} onClose={() => setSuccess(null)} />}

        {/* --- TAB: OVERVIEW & REPORTS --- */}
        {activeTab === 'reports' && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>{t('tabReports', 'System Overview & Live Reports')}</h2>
                <p className="text-muted">Real-time statistics across users, applications, and digital passes.</p>
              </div>
              <button
                type="button"
                className="btn btn-sm btn-outline"
                onClick={() => loadTabData('reports')}
              >
                🔄 {t('refresh', 'Refresh Reports')}
              </button>
            </div>

            {loading ? (
              <Loading message={t('loading', 'Generating live report metrics...')} />
            ) : summaryReport ? (
              <>
                <div className="stat-cards-grid">
                  <div className="stat-card">
                    <span className="stat-title">{t('totalUsers', 'Total Users')}</span>
                    <span className="stat-number">{summaryReport.totalUsers}</span>
                    <span className="stat-desc">
                      {summaryReport.totalPassengers} Passengers • {summaryReport.totalOfficers} Officers • {summaryReport.totalAdmins} Admins
                    </span>
                  </div>

                  <div className="stat-card">
                    <span className="stat-title">{t('totalApplications', 'Total Applications')}</span>
                    <span className="stat-number">{summaryReport.totalApplications}</span>
                    <span className="stat-desc">
                      {summaryReport.pendingApplications} Pending • {summaryReport.verifiedApplications} Verified
                    </span>
                  </div>

                  <div className="stat-card">
                    <span className="stat-title">{t('approvedIssued', 'Approved / Issued')}</span>
                    <span className="stat-number text-success">{summaryReport.approvedApplications}</span>
                    <span className="stat-desc">
                      {summaryReport.rejectedApplications} Rejected ({summaryReport.renewalApplications} Renewals)
                    </span>
                  </div>

                  <div className="stat-card">
                    <span className="stat-title">{t('activeDigitalPasses', 'Active Digital Passes')}</span>
                    <span className="stat-number text-primary">{summaryReport.activeDigitalPasses}</span>
                    <span className="stat-desc">
                      Out of {summaryReport.totalDigitalPasses} total issued ({summaryReport.expiredDigitalPasses} expired)
                    </span>
                  </div>
                </div>

                {/* Categories Breakdown */}
                <div className="reports-section-block">
                  <h3>Pass Category Breakdown</h3>
                  {categoryReport.length === 0 ? (
                    <p className="text-muted">No category metrics available yet.</p>
                  ) : (
                    <div className="table-responsive">
                      <table className="data-table">
                        <thead>
                          <tr>
                            <th>Category</th>
                            <th>Validity</th>
                            <th>Total Issued</th>
                            <th>Active</th>
                            <th>Estimated Revenue</th>
                          </tr>
                        </thead>
                        <tbody>
                          {categoryReport.map((cat, idx) => (
                            <tr key={idx}>
                              <td><strong>{cat.categoryName}</strong></td>
                              <td>{cat.validityDays} days</td>
                              <td>{cat.totalPassesIssued}</td>
                              <td>{cat.activePasses}</td>
                              <td>{formatINR(cat.estimatedRevenue)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>

                {/* Recent Applications */}
                <div className="reports-section-block">
                  <h3>Recent Applications</h3>
                  {recentApps.length === 0 ? (
                    <p className="text-muted">No recent applications.</p>
                  ) : (
                    <div className="table-responsive">
                      <table className="data-table">
                        <thead>
                          <tr>
                            <th>ID</th>
                            <th>Passenger</th>
                            <th>Category</th>
                            <th>Date</th>
                            <th>Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {recentApps.slice(0, 5).map((app) => (
                            <tr key={app.id}>
                              <td>#{app.id}</td>
                              <td>{app.passengerName}</td>
                              <td>{app.passCategoryName}</td>
                              <td>{app.applicationDate ? new Date(app.applicationDate).toLocaleDateString() : 'N/A'}</td>
                              <td>
                                <span className={`badge badge-${app.status === 'APPROVED' ? 'success' : app.status === 'REJECTED' ? 'danger' : 'warning'}`}>
                                  {app.status}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <p className="text-muted">Unable to retrieve reports.</p>
            )}
          </section>
        )}

        {/* --- TAB: USERS & STAFF --- */}
        {activeTab === 'users' && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>User & Staff Management</h2>
                <p className="text-muted">Manage staff roles, onboard officers, and oversee passenger accounts.</p>
              </div>
              <div className="header-actions">
                <select
                  className="form-control form-control-sm"
                  value={userRoleFilter}
                  onChange={(e) => handleFilterUsers(e.target.value)}
                >
                  <option value="">All Roles</option>
                  <option value="PASSENGER">Passengers</option>
                  <option value="DOCUMENT_VERIFICATION_OFFICER">Verification Officers</option>
                  <option value="PASS_APPROVAL_OFFICER">Approval Officers</option>
                  <option value="ROUTE_FARE_MANAGER">Route Managers</option>
                  <option value="ADMIN">Admins</option>
                </select>
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={() => setIsStaffModalOpen(true)}
                >
                  + Onboard Staff
                </button>
              </div>
            </div>

            {loading ? (
              <Loading message="Loading users..." />
            ) : users.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">👥</span>
                <p>No users found matching the filter.</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>User ID</th>
                      <th>Full Name</th>
                      <th>Email</th>
                      <th>Current Role</th>
                      <th>Change Role</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((u) => (
                      <tr key={u.id}>
                        <td>#{u.id}</td>
                        <td><strong>{u.fullName}</strong></td>
                        <td>{u.email}</td>
                        <td>
                          <span className={`badge badge-role badge-${u.role ? u.role.toLowerCase() : 'user'}`}>
                            {u.role}
                          </span>
                        </td>
                        <td>
                          <select
                            className="form-control form-control-xs"
                            value={u.role || ''}
                            onChange={(e) => handleRoleChange(u.id, e.target.value)}
                            disabled={actionLoading}
                          >
                            <option value="PASSENGER">PASSENGER</option>
                            <option value="DOCUMENT_VERIFICATION_OFFICER">DOCUMENT_VERIFICATION_OFFICER</option>
                            <option value="PASS_APPROVAL_OFFICER">PASS_APPROVAL_OFFICER</option>
                            <option value="ROUTE_FARE_MANAGER">ROUTE_FARE_MANAGER</option>
                            <option value="ADMIN">ADMIN</option>
                          </select>
                        </td>
                        <td>
                          <button
                            type="button"
                            className="btn btn-xs btn-danger"
                            onClick={() => handleDeleteUser(u.id, u.email)}
                            disabled={actionLoading}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        )}

        {/* --- TAB: ROUTES & FARES --- */}
        {activeTab === 'routes' && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>Route Management</h2>
                <p className="text-muted">Configure bus & metro routes, waypoints, and fare structures.</p>
              </div>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={() => setIsRouteModalOpen(true)}
              >
                + Add Route
              </button>
            </div>

            {loading ? (
              <Loading message="Loading routes..." />
            ) : routes.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">🚌</span>
                <p>No transit routes exist yet.</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Route #</th>
                      <th>Source ➔ Destination</th>
                      <th>Fare</th>
                      <th>Distance</th>
                      <th>Intermediate Stops</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {routes.map((rt) => (
                      <tr key={rt.id}>
                        <td><strong>{rt.routeNumber}</strong></td>
                        <td>{rt.source || rt.origin} ➔ {rt.destination}</td>
                        <td>{formatINR(rt.fare)}</td>
                        <td>{rt.distanceKm ? `${rt.distanceKm} km` : '—'}</td>
                        <td className="text-sm">{rt.stops || '—'}</td>
                        <td>
                          <span className={`badge ${rt.active ? 'badge-success' : 'badge-secondary'}`}>
                            {rt.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td>
                          <div className="action-buttons-group">
                            <button
                              type="button"
                              className="btn btn-xs btn-outline"
                              onClick={() => handleToggleRouteStatus(rt.id)}
                              disabled={actionLoading}
                            >
                              {rt.active ? 'Deactivate' : 'Activate'}
                            </button>
                            <button
                              type="button"
                              className="btn btn-xs btn-danger"
                              onClick={() => handleDeleteRoute(rt.id, rt.routeNumber)}
                              disabled={actionLoading}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        )}

        {/* --- TAB: PASS CATEGORIES --- */}
        {activeTab === 'categories' && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>Pass Categories Management</h2>
                <p className="text-muted">Define commuter eligibility groups, validity periods, document rules, and base pricing in Indian Rupees.</p>
              </div>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={() => setIsCategoryModalOpen(true)}
              >
                + Add Category
              </button>
            </div>

            {loading ? (
              <Loading message="Loading categories..." />
            ) : categories.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">🏷️</span>
                <p>No pass categories configured.</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Category Name</th>
                      <th>Group</th>
                      <th>Validity</th>
                      <th>Base Fare</th>
                      <th>Fare Type</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {categories.map((c) => (
                      <tr key={c.id}>
                        <td>
                          <div><strong>{c.name}</strong></div>
                          <small className="text-muted">{c.description || ''}</small>
                        </td>
                        <td>
                          <span className="badge badge-info">{c.categoryGroup || 'GENERAL'}</span>
                        </td>
                        <td>{c.validityDays} days</td>
                        <td className="font-semibold">{formatCategoryFare(c)}</td>
                        <td>
                          <span className="badge badge-outline">{c.fareType || 'FIXED'}</span>
                        </td>
                        <td>
                          <button
                            type="button"
                            className="btn btn-xs btn-danger"
                            onClick={() => handleDeleteCategory(c.id, c.name)}
                            disabled={actionLoading}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        )}
      </main>

      {/* --- MODAL: CREATE STAFF --- */}
      {isStaffModalOpen && (
        <div
          className="modal-backdrop"
          onClick={() => setIsStaffModalOpen(false)}
          role="dialog"
          aria-modal="true"
        >
          <div className="modal-dialog modal-md" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Onboard New Staff Member</h3>
              <button
                type="button"
                className="modal-close"
                onClick={() => setIsStaffModalOpen(false)}
              >
                &times;
              </button>
            </div>

            <form onSubmit={handleCreateStaffSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label htmlFor="staff-name">Full Name *</label>
                  <input
                    id="staff-name"
                    type="text"
                    className="form-control"
                    placeholder="Officer Sarah Smith"
                    value={newStaff.fullName}
                    onChange={(e) => setNewStaff({ ...newStaff, fullName: e.target.value })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="staff-email">Email Address *</label>
                  <input
                    id="staff-email"
                    type="email"
                    className="form-control"
                    placeholder="sarah@transitease.com"
                    value={newStaff.email}
                    onChange={(e) => setNewStaff({ ...newStaff, email: e.target.value })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="staff-pwd">Initial Password *</label>
                  <input
                    id="staff-pwd"
                    type="password"
                    className="form-control"
                    placeholder="••••••••"
                    value={newStaff.password}
                    onChange={(e) => setNewStaff({ ...newStaff, password: e.target.value })}
                    required
                    minLength={6}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="staff-role">Assigned Staff Role *</label>
                  <select
                    id="staff-role"
                    className="form-control"
                    value={newStaff.role}
                    onChange={(e) => setNewStaff({ ...newStaff, role: e.target.value })}
                    required
                  >
                    <option value="DOCUMENT_VERIFICATION_OFFICER">DOCUMENT_VERIFICATION_OFFICER</option>
                    <option value="PASS_APPROVAL_OFFICER">PASS_APPROVAL_OFFICER</option>
                    <option value="ROUTE_FARE_MANAGER">ROUTE_FARE_MANAGER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => setIsStaffModalOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={actionLoading}
                >
                  {actionLoading ? 'Creating...' : 'Create Staff Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* --- MODAL: CREATE ROUTE --- */}
      {isRouteModalOpen && (
        <div
          className="modal-backdrop"
          onClick={() => setIsRouteModalOpen(false)}
          role="dialog"
          aria-modal="true"
        >
          <div className="modal-dialog modal-md" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Create Transit Route</h3>
              <button
                type="button"
                className="modal-close"
                onClick={() => setIsRouteModalOpen(false)}
              >
                &times;
              </button>
            </div>

            <form onSubmit={handleCreateRouteSubmit}>
              <div className="modal-body">
                <div className="form-row">
                  <div className="form-group col-6">
                    <label htmlFor="rt-num">Route Number *</label>
                    <input
                      id="rt-num"
                      type="text"
                      className="form-control"
                      placeholder="e.g. 101-BLUE"
                      value={routeForm.routeNumber}
                      onChange={(e) => setRouteForm({ ...routeForm, routeNumber: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group col-6">
                    <label htmlFor="rt-fare">Fare ($) *</label>
                    <input
                      id="rt-fare"
                      type="number"
                      step="0.01"
                      className="form-control"
                      value={routeForm.fare}
                      onChange={(e) => setRouteForm({ ...routeForm, fare: e.target.value })}
                      required
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group col-6">
                    <label htmlFor="rt-src">Source / Origin *</label>
                    <input
                      id="rt-src"
                      type="text"
                      className="form-control"
                      placeholder="Downtown Central"
                      value={routeForm.source}
                      onChange={(e) => setRouteForm({ ...routeForm, source: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group col-6">
                    <label htmlFor="rt-dest">Destination *</label>
                    <input
                      id="rt-dest"
                      type="text"
                      className="form-control"
                      placeholder="Airport Terminal 2"
                      value={routeForm.destination}
                      onChange={(e) => setRouteForm({ ...routeForm, destination: e.target.value })}
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="rt-stops">Intermediate Stops (Comma separated)</label>
                  <input
                    id="rt-stops"
                    type="text"
                    className="form-control"
                    placeholder="Market St, Civic Center, Tech Park, University"
                    value={routeForm.stops}
                    onChange={(e) => setRouteForm({ ...routeForm, stops: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="rt-desc">Description (Optional)</label>
                  <input
                    id="rt-desc"
                    type="text"
                    className="form-control"
                    placeholder="Express peak-hour line"
                    value={routeForm.description}
                    onChange={(e) => setRouteForm({ ...routeForm, description: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => setIsRouteModalOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={actionLoading}
                >
                  {actionLoading ? 'Creating...' : 'Save Route'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* --- MODAL: CREATE CATEGORY --- */}
      {isCategoryModalOpen && (
        <div
          className="modal-backdrop"
          onClick={() => setIsCategoryModalOpen(false)}
          role="dialog"
          aria-modal="true"
        >
          <div className="modal-dialog modal-md" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Create Pass Category</h3>
              <button
                type="button"
                className="modal-close"
                onClick={() => setIsCategoryModalOpen(false)}
              >
                &times;
              </button>
            </div>

            <form onSubmit={handleCreateCategorySubmit}>
              <div className="modal-body">
                <div className="form-row">
                  <div className="form-group col-6">
                    <label htmlFor="cat-name">Category Name *</label>
                    <input
                      id="cat-name"
                      type="text"
                      className="form-control"
                      placeholder="e.g. Monthly Student Pass"
                      value={categoryForm.name}
                      onChange={(e) => setCategoryForm({ ...categoryForm, name: e.target.value })}
                      required
                    />
                  </div>

                  <div className="form-group col-6">
                    <label htmlFor="cat-group">Category Group *</label>
                    <select
                      id="cat-group"
                      className="form-control"
                      value={categoryForm.categoryGroup}
                      onChange={(e) => setCategoryForm({ ...categoryForm, categoryGroup: e.target.value })}
                      required
                    >
                      <option value="GENERAL">A. General Passenger</option>
                      <option value="STUDENT">B. Student</option>
                      <option value="SENIOR_CITIZEN">C. Senior Citizen</option>
                      <option value="CONCESSIONARY">D. Other Concessionary</option>
                    </select>
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group col-6">
                    <label htmlFor="cat-days">Validity Days *</label>
                    <input
                      id="cat-days"
                      type="number"
                      min="1"
                      className="form-control"
                      value={categoryForm.validityDays}
                      onChange={(e) => setCategoryForm({ ...categoryForm, validityDays: e.target.value })}
                      required
                    />
                  </div>

                  <div className="form-group col-6">
                    <label htmlFor="cat-fare">Base Fare (₹) *</label>
                    <input
                      id="cat-fare"
                      type="number"
                      step="1"
                      min="0"
                      className="form-control"
                      value={categoryForm.baseFare}
                      onChange={(e) => setCategoryForm({ ...categoryForm, baseFare: e.target.value })}
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="cat-fare-type">Fare Type / Billing Mode</label>
                  <select
                    id="cat-fare-type"
                    className="form-control"
                    value={categoryForm.fareType}
                    onChange={(e) => setCategoryForm({ ...categoryForm, fareType: e.target.value })}
                  >
                    <option value="FIXED">FIXED (e.g. ₹70, ₹150, ₹750, ₹1500, ₹3000, ₹4000, ₹5000)</option>
                    <option value="CONFIRMATION_REQUIRED">CONFIRMATION_REQUIRED (e.g. Senior Citizen - Fare on confirmation)</option>
                    <option value="CONFIGURABLE">CONFIGURABLE (e.g. Student Punching Pass)</option>
                    <option value="CONCESSIONAL">CONCESSIONAL (e.g. Disability / Freedom Fighter / Media)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="cat-desc">Description</label>
                  <textarea
                    id="cat-desc"
                    className="form-control"
                    rows={2}
                    placeholder="Route coverage, city area, and transit network privileges"
                    value={categoryForm.description}
                    onChange={(e) => setCategoryForm({ ...categoryForm, description: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="cat-eligibility">Eligibility Criteria</label>
                  <input
                    id="cat-eligibility"
                    type="text"
                    className="form-control"
                    placeholder="e.g. Bonafide students enrolled in recognized institutions"
                    value={categoryForm.eligibility}
                    onChange={(e) => setCategoryForm({ ...categoryForm, eligibility: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="cat-mand-docs">Mandatory Documents (Comma-separated)</label>
                  <input
                    id="cat-mand-docs"
                    type="text"
                    className="form-control"
                    placeholder="e.g. Current student ID, Recent passport photograph"
                    value={categoryForm.mandatoryDocuments}
                    onChange={(e) => setCategoryForm({ ...categoryForm, mandatoryDocuments: e.target.value })}
                  />
                  <small className="form-help">Leave empty if no documents are required (e.g. Daily Pass).</small>
                </div>

                <div className="form-group">
                  <label htmlFor="cat-opt-docs">Optional Documents (Comma-separated)</label>
                  <input
                    id="cat-opt-docs"
                    type="text"
                    className="form-control"
                    placeholder="e.g. Proof of residence, Fee receipt"
                    value={categoryForm.optionalDocuments}
                    onChange={(e) => setCategoryForm({ ...categoryForm, optionalDocuments: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="cat-discount">Concession / Discount Details (Optional)</label>
                  <input
                    id="cat-discount"
                    type="text"
                    className="form-control"
                    placeholder="e.g. Subsidized fare as per municipal resolution"
                    value={categoryForm.discountDetails}
                    onChange={(e) => setCategoryForm({ ...categoryForm, discountDetails: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => setIsCategoryModalOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={actionLoading}
                >
                  {actionLoading ? 'Creating...' : 'Save Category'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
