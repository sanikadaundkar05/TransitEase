import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { Alert } from '../components/Alert';
import { Loading } from '../components/Loading';
import { formatDateIN } from '../utils/formatters';
import { isVerificationOfficer, isApprovalOfficer, isAdmin } from '../utils/roles';

export function OfficerDashboard() {
  const { user } = useAuth();
  const { language, t } = useLanguage();
  const [activeTab, setActiveTab] = useState('applications'); // 'applications' | 'review'
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Data states
  const [applications, setApplications] = useState([]);

  // Async UI states
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Status Action Modal State
  const [selectedApp, setSelectedApp] = useState(null);
  const [targetStatus, setTargetStatus] = useState(null); // 'VERIFIED' | 'APPROVED' | 'REJECTED'
  const [remarks, setRemarks] = useState('');

  // View Documents Modal State
  const [docModalApp, setDocModalApp] = useState(null);
  const [docList, setDocList] = useState([]);
  const [docLoading, setDocLoading] = useState(false);
  const [docRejectPromptId, setDocRejectPromptId] = useState(null);
  const [docRejectRemarks, setDocRejectRemarks] = useState('');

  const isVerifier = isVerificationOfficer(user?.role) || isAdmin(user?.role);
  const isApprover = isApprovalOfficer(user?.role) || isAdmin(user?.role);

  useEffect(() => {
    loadData();
  }, [activeTab]);

  async function loadData() {
    setLoading(true);
    setError(null);
    try {
      if (activeTab === 'applications') {
        const data = await api.passApplications.getAll();
        setApplications(data || []);
      } else if (activeTab === 'passes') {
        const data = await api.passApplications.getAll();
        setApplications(data || []);
      }
    } catch (err) {
      setError(err.message || 'Failed to fetch data.');
    } finally {
      setLoading(false);
    }
  }

  // Filtered applications
  const filteredApps = applications.filter((app) => {
    if (statusFilter === 'ALL') return true;
    return app.status === statusFilter;
  });
  const pendingReviewApplications = applications.filter((app) => app.status === 'PENDING');

  const todayKey = new Date().toLocaleDateString('en-CA');
  const monthKey = todayKey.slice(0, 7);
  const applicationsSubmittedToday = applications.filter((app) =>
    app.applicationDate?.slice(0, 10) === todayKey
  ).length;
  const verifiedToday = applications.filter((app) =>
    app.status === 'VERIFIED' && app.applicationDate?.slice(0, 10) === todayKey
  ).length;
  const rejectedThisMonth = applications.filter((app) =>
    app.status === 'REJECTED' && app.applicationDate?.slice(0, 7) === monthKey
  ).length;
  const correctionRequired = applications.filter((app) =>
    app.status === 'REJECTED' && app.remarks?.toLowerCase().includes('document')
  ).length;

  function formatDashboardDate() {
    return new Intl.DateTimeFormat(language === 'hi' ? 'hi-IN' : 'en-IN', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(new Date());
  }

  function getDocumentSummary(app) {
    if (app.documentCount !== undefined && app.requiredDocumentCount !== undefined) {
      return `${app.documentCount} / ${app.requiredDocumentCount}`;
    }
    return app.documentUrl ? '1 / 1' : '0 / 0';
  }

  function formatValidity(app) {
    return app.validityDays ? `${app.validityDays} ${t('days', 'days')}` : t('notAvailable', 'N/A');
  }

  // Open action modal for status update
  function openActionModal(app, newStatus) {
    setSelectedApp(app);
    setTargetStatus(newStatus);
    setRemarks('');
  }

  function openReviewAction(app, newStatus) {
    setDocModalApp(null);
    setSelectedApp(app);
    setTargetStatus(newStatus);
  }

  async function handleStatusSubmit(e) {
    e.preventDefault();
    if (!selectedApp || !targetStatus) return;

    setActionLoading(true);
    setError(null);
    setSuccess(null);

    try {
      await api.passApplications.updateStatus(
        selectedApp.id,
        targetStatus,
        remarks.trim() || null
      );
      setSuccess(`Application #${selectedApp.id} updated to ${targetStatus}!`);
      setSelectedApp(null);
      setTargetStatus(null);
      setRemarks('');
      // Reload applications
      const data = await api.passApplications.getAll();
      setApplications(data || []);
    } catch (err) {
      setError(err.message || `Failed to update status to ${targetStatus}.`);
    } finally {
      setActionLoading(false);
    }
  }

  // Open document viewer modal
  async function openDocViewer(app) {
    setDocModalApp(app);
    setDocRejectPromptId(null);
    setDocRejectRemarks('');
    setDocLoading(true);
    setError(null);
    try {
      const docs = await api.passApplications.getDocuments(app.id);
      setDocList(docs || []);
    } catch (err) {
      setError(err.message || 'Failed to load documents.');
    } finally {
      setDocLoading(false);
    }
  }

  async function handleVerifyIndividualDoc(docId) {
    setDocLoading(true);
    setError(null);
    try {
      await api.documents.updateStatus(docId, 'VERIFIED', 'Verified by officer');
      setSuccess(`Document #${docId} verified successfully.`);
      const docs = await api.passApplications.getDocuments(docModalApp.id);
      setDocList(docs || []);
      const appData = await api.passApplications.getAll();
      setApplications(appData || []);
    } catch (err) {
      setError(err.message || 'Failed to verify document.');
    } finally {
      setDocLoading(false);
    }
  }

  async function handleDocumentStatusChange(doc, nextStatus) {
    if (nextStatus === 'REJECTED') {
      setDocRejectPromptId(doc.id);
      setDocRejectRemarks('');
      return;
    }
    if (nextStatus === 'VERIFIED') {
      await handleVerifyIndividualDoc(doc.id);
      return;
    }
    if (nextStatus === 'PENDING') {
      setDocLoading(true);
      setError(null);
      try {
        await api.documents.updateStatus(doc.id, 'PENDING', null);
        const docs = await api.passApplications.getDocuments(docModalApp.id);
        setDocList(docs || []);
      } catch (err) {
        setError(err.message || 'Failed to update document status.');
      } finally {
        setDocLoading(false);
      }
    }
  }

  async function handleRejectIndividualDoc(docId) {
    if (!docRejectRemarks.trim()) {
      setError('Please provide rejection remarks for this document.');
      return;
    }
    setDocLoading(true);
    setError(null);
    try {
      await api.documents.updateStatus(docId, 'REJECTED', docRejectRemarks.trim());
      setSuccess(`Document #${docId} rejected with remarks. Application marked for document replacement.`);
      setDocRejectPromptId(null);
      setDocRejectRemarks('');
      const docs = await api.passApplications.getDocuments(docModalApp.id);
      setDocList(docs || []);
      const appData = await api.passApplications.getAll();
      setApplications(appData || []);
      const updatedApp = appData.find((a) => a.id === docModalApp.id);
      if (updatedApp) {
        setDocModalApp(updatedApp);
      }
    } catch (err) {
      setError(err.message || 'Failed to reject document.');
    } finally {
      setDocLoading(false);
    }
  }

  async function handleDownloadDoc(docId, fileName) {
    try {
      await api.passApplications.downloadDocument(docId, fileName);
    } catch (err) {
      setError(err.message || 'Failed to download document.');
    }
  }

  function getStatusBadge(status, remarks) {
    if (status === 'REJECTED' && remarks && remarks.toLowerCase().includes('document')) {
      return <span className="badge badge-danger">{t('statusDocsRejected', 'Documents Rejected')}</span>;
    }
    switch (status) {
      case 'PENDING':
        return <span className="badge badge-warning">{t('statusPending', 'Pending')}</span>;
      case 'VERIFIED':
        return <span className="badge badge-info">{t('statusVerified', 'Verified')}</span>;
      case 'APPROVED':
      case 'ACTIVE':
        return <span className="badge badge-success">{t('statusApproved', 'Approved / Active')}</span>;
      case 'REJECTED':
        return <span className="badge badge-danger">{t('statusRejected', 'Rejected')}</span>;
      case 'EXPIRED':
        return <span className="badge badge-secondary">{t('statusExpired', 'Expired')}</span>;
      default:
        return <span className="badge badge-outline">{status}</span>;
    }
  }

  return (
    <div className="dashboard-layout">
      {/* Sidebar Navigation */}
      <nav className="dashboard-sidebar" aria-label="Officer navigation">
        <div className="sidebar-header">
          <h3>{t('officerPortal', 'Officer Portal')}</h3>
        </div>
        <ul className="sidebar-nav-list">
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'applications' ? 'active' : ''}`}
              onClick={() => setActiveTab('applications')}
            >
              <span className="nav-icon"></span> {t('verificationsTitle', 'Verifications')}
            </button>
          </li>
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'passes' ? 'active' : ''}`}
              onClick={() => setActiveTab('passes')}
            >
              <span className="nav-icon"></span> {t('reviewApplicationsTitle', 'Review application')}
            </button>
          </li>
        </ul>
      </nav>

      {/* Main Content Area */}
      <main className="dashboard-content">
        {error && <Alert type="error" message={error} onClose={() => setError(null)} />}
        {success && <Alert type="success" message={success} onClose={() => setSuccess(null)} />}

        {/* --- TAB: VERIFICATION DASHBOARD --- */}
        {activeTab === 'applications' && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <div className="dashboard-date">{formatDashboardDate()}</div>
                <h2>{t('verificationDashboardTitle', 'Dashboard')}</h2>
              </div>
              <div className="last-synced">{t('lastSynced', 'Last synced just now')}</div>
            </div>

            <div className="verification-kpis">
              <div className="verification-kpi verification-kpi-pending">
                <span className="verification-kpi-icon">◷</span>
                <strong>{applications.filter((app) => app.status === 'PENDING').length}</strong>
                <b>{t('awaitingVerification', 'Awaiting verification')}</b>
                <small>{applicationsSubmittedToday} {t('submittedToday', 'submitted today')}</small>
              </div>
              <div className="verification-kpi verification-kpi-correction">
                <span className="verification-kpi-icon">▤</span>
                <strong>{correctionRequired}</strong>
                <b>{t('correctionRequired', 'Correction required')}</b>
                <small>{t('needApplicantAction', 'Need applicant action')}</small>
              </div>
              <div className="verification-kpi verification-kpi-verified">
                <span className="verification-kpi-icon">✓</span>
                <strong>{verifiedToday}</strong>
                <b>{t('verifiedToday', 'Verified today')}</b>
                <small>{t('readyForApproval', 'Ready for approval')}</small>
              </div>
              <div className="verification-kpi verification-kpi-rejected">
                <span className="verification-kpi-icon">×</span>
                <strong>{rejectedThisMonth}</strong>
                <b>{t('rejectedAtVerification', 'Rejected at verification')}</b>
                <small>{t('thisMonth', 'This month')}</small>
              </div>
            </div>

            <div className="verification-queue">
              <div className="verification-queue-header">
                <div>
                  <h3>{t('verificationQueueTitle', 'Verification Queue')}</h3>
                  <p className="text-muted">{t('verificationQueueSubtitle', 'Applications waiting for document review')}</p>
                </div>
                <div className="filter-group">
                <label htmlFor="status-filter">{t('filterStatus', 'Status:')}</label>
                <select
                  id="status-filter"
                  className="form-control form-control-sm"
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <option value="ALL">All ({applications.length})</option>
                  <option value="PENDING">
                    {t('statusPending', 'Pending')} ({applications.filter((a) => a.status === 'PENDING').length})
                  </option>
                  <option value="VERIFIED">
                    {t('statusVerified', 'Verified')} ({applications.filter((a) => a.status === 'VERIFIED').length})
                  </option>
                  <option value="APPROVED">
                    {t('statusApproved', 'Approved')} ({applications.filter((a) => a.status === 'APPROVED').length})
                  </option>
                  <option value="REJECTED">
                    {t('statusRejected', 'Rejected')} ({applications.filter((a) => a.status === 'REJECTED').length})
                  </option>
                </select>
                <button
                  type="button"
                  className="btn btn-sm btn-outline"
                  onClick={loadData}
                >
                   {t('refresh', 'Refresh')}
                </button>
                </div>
              </div>

              {loading ? (
                <Loading message={t('loading', 'Loading application queue...')} />
              ) : filteredApps.length === 0 ? (
                <div className="empty-state">
                  <span className="empty-icon"></span>
                  <h3>{t('noApplicationsFound', 'No applications found')}</h3>
                  <p>No applications match the current filter criteria.</p>
                </div>
              ) : (
                <div className="table-responsive verification-queue-table">
                  <table className="data-table">
                  <thead>
                    <tr>
                      <th>{t('applicationColumn', 'Application')}</th>
                      <th>{t('applicantColumn', 'Applicant')}</th>
                      <th>{t('category', 'Category')}</th>
                      <th>{t('submittedColumn', 'Submitted')}</th>
                      <th>{t('documents', 'Documents')}</th>
                      <th>{t('actionColumn', 'Action')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredApps.map((app) => (
                      <tr key={app.id}>
                        <td><strong>#{app.id}</strong></td>
                        <td>
                          <div className="font-semibold">{app.fullName || app.passengerName}</div>
                          <small className="text-muted" style={{ display: 'block' }}>
                            {app.mobileNumber || app.passengerEmail}
                          </small>
                          {app.passengerCategory && (
                            <span className="badge badge-outline" style={{ fontSize: '0.6875rem', marginTop: '2px' }}>
                              {app.passengerCategory}
                            </span>
                          )}
                        </td>
                        <td>{app.passCategoryName}</td>
                        <td>{app.applicationDate ? formatDateIN(app.applicationDate) : 'N/A'}</td>
                        <td>
                          <span className="document-count">{getDocumentSummary(app)}</span>
                        </td>
                        <td>{getStatusBadge(app.status, app.remarks)}</td>
                        <td>
                          <div className="action-buttons-group">
                            {/* Verification Workflow: PENDING -> VERIFY */}
                            {app.status === 'PENDING' && isVerifier && (
                              <button
                                type="button"
                                className="btn btn-xs btn-info"
                                onClick={() => openActionModal(app, 'VERIFIED')}
                              >
                                {t('btnVerify', '✓ Verify')}
                              </button>
                            )}

                            {/* Approval Workflow: VERIFIED -> APPROVE */}
                            {app.status === 'VERIFIED' && isApprover && (
                              <button
                                type="button"
                                className="btn btn-xs btn-success"
                                onClick={() => openActionModal(app, 'APPROVED')}
                              >
                                {t('btnApprove', '✓ Approve')}
                              </button>
                            )}

                            {/* Rejection Option */}
                            {(app.status === 'PENDING' || app.status === 'VERIFIED') && (
                              <button
                                type="button"
                                className="btn btn-xs btn-danger"
                                onClick={() => openActionModal(app, 'REJECTED')}
                              >
                                {t('btnReject', '✗ Reject')}
                              </button>
                            )}

                            {/* Approved badge / Finalized */}
                            {app.status === 'APPROVED' && (
                              <span className="text-success font-semibold" style={{ fontSize: '0.8125rem' }}>
                                ✓ {t('statusApproved', 'Approved')}
                              </span>
                            )}
                            {app.status === 'REJECTED' && (
                              <span className="text-danger font-semibold" style={{ fontSize: '0.8125rem' }}>
                                ✗ {t('statusRejected', 'Rejected')}
                              </span>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  </table>
                </div>
              )}
            </div>
          </section>
        )}

        {/* --- TAB: REVIEW APPLICATIONS --- */}
        {activeTab === 'passes' && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>{t('reviewApplicationsTitle', 'Review application')}</h2>
                <p className="text-muted">{t('reviewApplicationsSubtitle', 'Review submitted applications and their supporting documents.')}</p>
              </div>
              <button
                type="button"
                className="btn btn-sm btn-outline"
                onClick={loadData}
              >
                 {t('refresh', 'Refresh')}
              </button>
            </div>

            {loading ? (
              <Loading message={t('loading', 'Loading applications...')} />
            ) : pendingReviewApplications.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon"></span>
                <h3>{t('noPendingApplications', 'No pending applications')}</h3>
                <p>{t('noPendingApplicationsDesc', 'There are no pending applications available for review.')}</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>{t('applicationColumn', 'Application')}</th>
                      <th>{t('applicantColumn', 'Applicant')}</th>
                      <th>{t('category', 'Category')}</th>
                      <th>{t('submittedColumn', 'Submitted')}</th>
                      <th>{t('documents', 'Documents')}</th>
                      <th>{t('actionColumn', 'Action')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pendingReviewApplications.map((app) => (
                      <tr key={app.id}>
                        <td><strong>APP-{app.id}</strong></td>
                        <td>
                          <div className="font-semibold">{app.fullName || app.passengerName}</div>
                          <small className="text-muted">{app.passengerCategory || 'GENERAL'}</small>
                        </td>
                        <td>{app.passCategoryName}</td>
                        <td>{app.applicationDate ? formatDateIN(app.applicationDate) : 'N/A'}</td>
                        <td><span className="document-count">{getDocumentSummary(app)}</span></td>
                        <td>
                          <button
                            type="button"
                            className="btn btn-xs btn-outline"
                            onClick={() => openDocViewer(app)}
                          >
                             {t('review', 'Review')}
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

      {/* --- WORKFLOW ACTION MODAL (VERIFY / APPROVE / REJECT) --- */}
      {selectedApp && targetStatus && (
        <div
          className="modal-backdrop"
          onClick={() => setSelectedApp(null)}
          role="dialog"
          aria-modal="true"
        >
          <div className="modal-dialog modal-md" onClick={(e) => e.stopPropagation()}>
            <form onSubmit={handleStatusSubmit}>
              <div className="modal-header">
                <h3 className="modal-title">
                  {targetStatus === 'VERIFIED'
                    ? t('verifyModalTitle', 'Verify Application')
                    : targetStatus === 'APPROVED'
                    ? t('approveModalTitle', 'Approve Application')
                    : t('rejectModalTitle', 'Reject Application')}{' '}
                  #{selectedApp.id}
                </h3>
                <button
                  type="button"
                  className="modal-close"
                  onClick={() => setSelectedApp(null)}
                >
                  &times;
                </button>
              </div>

              <div className="modal-body">
                {/* Passenger Eligibility Summary for Officer Verification */}
                <div className="review-panel" style={{ marginBottom: '1rem' }}>
                  <div className="review-panel-title"> Passenger & Eligibility Details</div>
                  <div className="review-data-list">
                    <div className="review-data-row">
                      <span className="review-data-label">Full Name:</span>
                      <span className="review-data-val">{selectedApp.fullName || selectedApp.passengerName}</span>
                    </div>
                    <div className="review-data-row">
                      <span className="review-data-label">DOB / Gender:</span>
                      <span className="review-data-val">
                        {selectedApp.dob ? formatDateIN(selectedApp.dob) : 'N/A'} ({selectedApp.gender || 'N/A'})
                      </span>
                    </div>
                    <div className="review-data-row">
                      <span className="review-data-label">Mobile / Email:</span>
                      <span className="review-data-val">
                        {selectedApp.mobileNumber || 'N/A'} • {selectedApp.email || selectedApp.passengerEmail}
                      </span>
                    </div>
                    <div className="review-data-row">
                      <span className="review-data-label">Address:</span>
                      <span className="review-data-val">
                        {selectedApp.residentialAddress ? `${selectedApp.residentialAddress}, ${selectedApp.city} - ${selectedApp.pincode}` : 'N/A'}
                      </span>
                    </div>
                    <div className="review-data-row">
                      <span className="review-data-label">Passenger Category:</span>
                      <span className="review-data-val">{selectedApp.passengerCategory || 'GENERAL'}</span>
                    </div>
                    <div className="review-data-row">
                      <span className="review-data-label">Pass Applied:</span>
                      <span className="review-data-val text-success font-semibold">{selectedApp.passCategoryName}</span>
                    </div>
                    {selectedApp.institutionName && (
                      <div className="review-data-row">
                        <span className="review-data-label">Institution / Employer:</span>
                        <span className="review-data-val">{selectedApp.institutionName}</span>
                      </div>
                    )}
                    {(selectedApp.sourceStop || selectedApp.destinationStop) && (
                      <div className="review-data-row">
                        <span className="review-data-label">Route Stops:</span>
                        <span className="review-data-val">
                          {selectedApp.sourceStop || 'Any'} ➔ {selectedApp.destinationStop || 'Any'}
                        </span>
                      </div>
                    )}
                    {selectedApp.emergencyContact && (
                      <div className="review-data-row">
                        <span className="review-data-label">Emergency Contact:</span>
                        <span className="review-data-val">{selectedApp.emergencyContact}</span>
                      </div>
                    )}
                  </div>
                </div>

                <div className="form-group" style={{ marginTop: '1rem' }}>
                  <label htmlFor="modal-remarks">
                    {targetStatus === 'REJECTED'
                      ? t('rejectionReasonRequired', 'Rejection Reason / Remarks *')
                      : `${t('remarks', 'Remarks')} (${t('optional', 'Optional')})`}
                  </label>
                  <textarea
                    id="modal-remarks"
                    className="form-control"
                    rows={3}
                    placeholder={
                      targetStatus === 'REJECTED'
                        ? 'e.g. Supporting document unreadable or expired identity proof'
                        : t('remarksPlaceholder', 'Enter remarks...')
                    }
                    value={remarks}
                    onChange={(e) => setRemarks(e.target.value)}
                    required={targetStatus === 'REJECTED'}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => setSelectedApp(null)}
                >
                  {t('cancel', 'Cancel')}
                </button>
                <button
                  type="submit"
                  className={`btn ${
                    targetStatus === 'REJECTED'
                      ? 'btn-danger'
                      : targetStatus === 'APPROVED'
                      ? 'btn-success'
                      : 'btn-info'
                  }`}
                  disabled={actionLoading}
                >
                  {actionLoading
                    ? t('loading', 'Updating...')
                    : `${t('confirmStatus', 'Confirm')} ${targetStatus}`}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* --- REVIEW APPLICATION & DOCUMENTS MODAL --- */}
      {docModalApp && (
        <div
          className="modal-backdrop"
          onClick={() => setDocModalApp(null)}
          role="dialog"
          aria-modal="true"
        >
          <div className="modal-dialog modal-lg" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3 className="modal-title">
                  {t('reviewApplicationsTitle', 'Review application')}
                  <span className="review-modal-status">{getStatusBadge(docModalApp.status, docModalApp.remarks)}</span>
                </h3>
                <span className="text-muted text-xs">
                  APP-{docModalApp.id} • {t('submittedColumn', 'Submitted')}: {formatDateIN(docModalApp.applicationDate)}
                </span>
              </div>
              <button
                type="button"
                className="modal-close"
                onClick={() => setDocModalApp(null)}
              >
                &times;
              </button>
            </div>

            <div className="modal-body">
              {docLoading ? (
                <Loading message={t('loading', 'Loading application & documents...')} />
              ) : (
                <>
                  <div className="review-applicant-summary">
                    <div>
                      <strong>{docModalApp.fullName || docModalApp.passengerName}</strong>
                      <span>
                        {docModalApp.passengerCategory || 'GENERAL'} • {docModalApp.mobileNumber || docModalApp.passengerEmail || 'N/A'}
                      </span>
                    </div>
                  </div>

                  <div className="review-pass-summary">
                    <div>
                      <span>{t('requestedPass', 'Requested pass')}</span>
                      <strong>{docModalApp.passCategoryName}</strong>
                    </div>
                    <div>
                      <span>{t('validity', 'Validity')}</span>
                      <strong>{formatValidity(docModalApp)}</strong>
                    </div>
                  </div>

                  {/* Document Review Section */}
                  <div className="review-documents-heading">
                    <div>
                      <h4>{t('documentChecklist', 'Document checklist')}</h4>
                    </div>
                    <span className="text-muted">
                      {docList.filter((doc) => doc.status === 'VERIFIED').length} / {docList.length} {t('verifiedLabel', 'verified')}
                    </span>
                  </div>
                  {docList.length === 0 ? (
                    <div className="empty-state" style={{ padding: '1rem' }}>
                      <p className="text-muted">No documents uploaded for this application.</p>
                    </div>
                  ) : (
                    <ul className="doc-list">
                      {docList.map((doc) => {
                        const docStatus = (doc.status || 'PENDING').toLowerCase();
                        return (
                          <li key={doc.id} className="doc-item" style={{ flexDirection: 'column', alignItems: 'stretch' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <div className="doc-meta">
                                <span className="doc-icon"></span>
                                <div>
                                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <strong>{doc.documentType || 'Supporting Document'}</strong>
                                    <span className={`doc-item-status-pill ${docStatus}`}>
                                      {doc.status || 'PENDING'}
                                    </span>
                                  </div>
                                  <small className="text-muted" style={{ display: 'block' }}>
                                    {doc.fileName} • {doc.fileSize ? `${(doc.fileSize / 1024).toFixed(1)} KB` : 'N/A'} •{' '}
                                    {doc.uploadDate ? new Date(doc.uploadDate).toLocaleString() : ''}
                                  </small>
                                </div>
                              </div>
                              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                                <button
                                  type="button"
                                  className="btn btn-xs btn-outline"
                                  onClick={() => handleDownloadDoc(doc.id, doc.fileName)}
                                >
                                  {t('preview', 'Preview')}
                                </button>
                                {isVerifier && (
                                  <select
                                    className="form-control form-control-sm review-doc-status"
                                    value={doc.status || 'PENDING'}
                                    onChange={(e) => handleDocumentStatusChange(doc, e.target.value)}
                                    aria-label={`${t('documentStatus', 'Document status')}: ${doc.documentType || doc.fileName}`}
                                  >
                                    <option value="PENDING">{t('statusPendingShort', 'Pending')}</option>
                                    <option value="VERIFIED">{t('statusVerifiedShort', 'Verified')}</option>
                                    <option value="REJECTED">{t('statusRejectedShort', 'Rejected')}</option>
                                  </select>
                                )}
                              </div>
                            </div>

                            {/* Remarks display */}
                            {doc.remarks && (
                              <div style={{ marginTop: '0.35rem', fontSize: '0.75rem', color: '#991b1b', background: '#fee2e2', padding: '0.25rem 0.5rem', borderRadius: '4px' }}>
                                <strong>Remarks:</strong> {doc.remarks}
                              </div>
                            )}

                            {/* Reject Prompt Inline */}
                            {docRejectPromptId === doc.id && (
                              <div style={{ marginTop: '0.5rem', padding: '0.75rem', background: '#fff1f2', border: '1px solid #fecdd3', borderRadius: '6px' }}>
                                <label className="text-xs font-semibold text-danger" style={{ display: 'block', marginBottom: '0.25rem' }}>
                                  Reason for rejecting {doc.documentType || doc.fileName}:
                                </label>
                                <input
                                  type="text"
                                  className="form-control form-control-sm"
                                  placeholder="e.g. Unclear image / Expired document / Name mismatch"
                                  value={docRejectRemarks}
                                  onChange={(e) => setDocRejectRemarks(e.target.value)}
                                  autoFocus
                                />
                                <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
                                  <button
                                    type="button"
                                    className="btn btn-xs btn-danger"
                                    onClick={() => handleRejectIndividualDoc(doc.id)}
                                    disabled={!docRejectRemarks.trim()}
                                  >
                                    Confirm Rejection & Request Replacement
                                  </button>
                                  <button
                                    type="button"
                                    className="btn btn-xs btn-outline"
                                    onClick={() => setDocRejectPromptId(null)}
                                  >
                                    Cancel
                                  </button>
                                </div>
                              </div>
                            )}
                          </li>
                        );
                      })}
                    </ul>
                  )}
                </>
              )}
            </div>

            {docModalApp.status === 'PENDING' && isVerifier && (
              <div className="review-decision">
                <label htmlFor="review-remarks">{t('officerRemarks', 'Officer remarks')}</label>
                <textarea
                  id="review-remarks"
                  className="form-control"
                  rows={3}
                  placeholder={t('reviewRemarksPlaceholder', 'Add a note for the applicant or the approval team...')}
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                />
              </div>
            )}

            <div className="modal-footer" style={{ justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                {docModalApp.status === 'PENDING' && isVerifier && (
                  <>
                    <button
                      type="button"
                      className="btn btn-warning btn-sm"
                      onClick={() => openReviewAction(docModalApp, 'REJECTED')}
                    >
                      {t('requestCorrection', 'Request correction')}
                    </button>
                    <button
                      type="button"
                      className="btn btn-info btn-sm"
                      onClick={() => openReviewAction(docModalApp, 'VERIFIED')}
                    >
                      {t('verifySendForward', 'Verify & send forward')}
                    </button>
                    <button
                      type="button"
                      className="btn btn-danger btn-sm"
                      onClick={() => openReviewAction(docModalApp, 'REJECTED')}
                    >
                      {t('rejectAtVerification', 'Reject at verification')}
                    </button>
                  </>
                )}
                {docModalApp.status === 'VERIFIED' && isApprover && (
                  <button
                    type="button"
                    className="btn btn-success btn-sm"
                    onClick={() => {
                      const appToApprove = docModalApp;
                      setDocModalApp(null);
                      openActionModal(appToApprove, 'APPROVED');
                    }}
                  >
                    ✓ Approve Application & Issue Pass
                  </button>
                )}
              </div>
              <button
                type="button"
                className="btn btn-outline"
                onClick={() => setDocModalApp(null)}
              >
                {t('close', 'Close')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
