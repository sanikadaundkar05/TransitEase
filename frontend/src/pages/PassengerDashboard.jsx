import React, { useState, useEffect, useRef } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { Alert } from '../components/Alert';
import { Loading } from '../components/Loading';
import { formatINR, formatCategoryFare, formatDateIN, formatValidity } from '../utils/formatters';

export function PassengerDashboard() {
  const { user } = useAuth();
  const { t, getLocalizedCategory } = useLanguage();

  // Navigation tab state: 'passInfo' | 'apply' | 'passes' | 'applications' | 'routes' | 'history'
  const [activeTab, setActiveTab] = useState('passInfo');
  const [categoryFilter, setCategoryFilter] = useState('ALL'); // 'ALL' | 'GENERAL' | 'STUDENT' | 'SENIOR_CITIZEN' | 'CONCESSIONARY'

  // Data states
  const [passes, setPasses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [applications, setApplications] = useState([]);
  const [history, setHistory] = useState({ applications: [], passes: [] });

  // Async UI states
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Apply form state
  const [selectedCategoryId, setSelectedCategoryId] = useState('');
  const applyFormRef = useRef(null);

  // Passenger Personal Details state
  const [personalDetails, setPersonalDetails] = useState({
    fullName: user?.fullName || '',
    dob: '',
    gender: 'MALE',
    mobileNumber: '',
    email: user?.email || '',
    residentialAddress: '',
    city: 'Pune',
    pincode: '',
    passengerCategory: 'GENERAL',
    sourceStop: '',
    destinationStop: '',
    institutionName: '',
    emergencyContact: '',
  });

  // Separate document upload state: map of docType -> File object
  const [filesByDocType, setFilesByDocType] = useState({});

  // View Details modal for pass catalog
  const [detailsModalCat, setDetailsModalCat] = useState(null);

  // Document management modal for existing application
  const [docModalApp, setDocModalApp] = useState(null);
  const [docList, setDocList] = useState([]);
  const [uploadModalFile, setUploadModalFile] = useState(null);
  const [uploadModalDocType, setUploadModalDocType] = useState('');
  const [docModalLoading, setDocModalLoading] = useState(false);

  // Renewal state
  const [renewingPassId, setRenewingPassId] = useState(null);

  // Sync logged-in user details to form when available
  useEffect(() => {
    if (user) {
      setPersonalDetails((prev) => ({
        ...prev,
        fullName: prev.fullName || user.fullName || '',
        email: prev.email || user.email || '',
      }));
    }
  }, [user]);

  useEffect(() => {
    loadTabInitialData(activeTab);
  }, [activeTab]);

  async function loadTabInitialData(tab) {
    setLoading(true);
    setError(null);
    try {
      if (tab === 'passes') {
        const myPasses = await api.digitalPasses.getMyPasses();
        setPasses(myPasses || []);
      } else if (tab === 'passInfo' || tab === 'categories' || tab === 'apply') {
        const cats = await api.passCategories.getAll();
        setCategories(cats || []);
        if (cats && cats.length > 0 && !selectedCategoryId) {
          setSelectedCategoryId(cats[0].id.toString());
        }
      } else if (tab === 'applications') {
        const apps = await api.passApplications.getMyApplications();
        setApplications(apps || []);
      } else if (tab === 'routes') {
        const rList = await api.routes.getAll();
        setRoutes(rList || []);
      } else if (tab === 'history') {
        const hist = await api.digitalPasses.getMyHistory();
        setHistory(hist || { applications: [], passes: [] });
      }
    } catch (err) {
      setError(err.message || 'Failed to load data.');
    } finally {
      setLoading(false);
    }
  }

  const selectedCategory = categories.find((c) => c.id.toString() === selectedCategoryId);

  // Helper to split document strings into lists
  function getMandatoryDocs(cat) {
    if (!cat) return [];
    if (cat.mandatoryDocumentList && cat.mandatoryDocumentList.length > 0) {
      return cat.mandatoryDocumentList;
    }
    if (cat.mandatoryDocuments) {
      return cat.mandatoryDocuments.split(',').map((s) => s.trim()).filter(Boolean);
    }
    return [];
  }

  function getOptionalDocs(cat) {
    if (!cat) return [];
    if (cat.optionalDocumentList && cat.optionalDocumentList.length > 0) {
      return cat.optionalDocumentList;
    }
    if (cat.optionalDocuments) {
      return cat.optionalDocuments.split(',').map((s) => s.trim()).filter(Boolean);
    }
    return [];
  }

  function handlePersonalDetailsChange(e) {
    const { name, value } = e.target;
    setPersonalDetails((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  // File upload validation helper
  function validateFile(file) {
    if (!file) return 'No file selected';
    if (file.size > 5 * 1024 * 1024) {
      return t('errFileTooLarge', 'File size exceeds the 5 MB limit. Please select a smaller file.');
    }
    const validExtensions = ['.pdf', '.jpg', '.jpeg', '.png'];
    const hasValidExt = validExtensions.some((ext) => file.name.toLowerCase().endsWith(ext));
    const validMimes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    if (!validMimes.includes(file.type) && !hasValidExt) {
      return t('errFileInvalidType', 'Invalid file type. Only PDF, JPG, and PNG files are allowed.');
    }
    return null;
  }

  // Handle file select for a specific document type
  function handleDocFileSelect(docType, e) {
    const file = e.target.files?.[0];
    if (!file) return;

    const validationErr = validateFile(file);
    if (validationErr) {
      setError(validationErr);
      e.target.value = '';
      return;
    }

    setError(null);
    setFilesByDocType((prev) => ({
      ...prev,
      [docType]: file,
    }));
  }

  function handleRemoveDocFile(docType) {
    setFilesByDocType((prev) => {
      const next = { ...prev };
      delete next[docType];
      return next;
    });
  }

  function handleSelectAndApply(catId) {
    setSelectedCategoryId(catId.toString());
    setActiveTab('apply');
    if (applyFormRef.current) {
      applyFormRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }

  // Calculate missing mandatory documents
  const currentCat = selectedCategory ? getLocalizedCategory(selectedCategory) : null;
  const currentMandatoryDocs = currentCat ? getMandatoryDocs(currentCat) : [];
  const currentOptionalDocs = currentCat ? getOptionalDocs(currentCat) : [];
  const missingMandatoryDocs = currentMandatoryDocs.filter((doc) => !filesByDocType[doc]);

  async function handleApplySubmit(e) {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!selectedCategoryId) {
      setError(t('errSelectCategory', 'Please select a pass category.'));
      return;
    }

    // Required Personal Details Validation
    if (!personalDetails.fullName.trim()) {
      setError('Please provide your full name.');
      return;
    }
    if (!personalDetails.dob) {
      setError('Please provide your date of birth.');
      return;
    }
    if (!personalDetails.mobileNumber.trim()) {
      setError('Please provide your mobile number.');
      return;
    }
    if (!personalDetails.email.trim()) {
      setError('Please provide your email address.');
      return;
    }
    if (!personalDetails.residentialAddress.trim()) {
      setError('Please provide your residential address.');
      return;
    }
    if (!personalDetails.city.trim()) {
      setError('Please provide your city.');
      return;
    }
    if (!personalDetails.pincode.trim()) {
      setError('Please provide your PIN code.');
      return;
    }

    // Check mandatory document uploads
    if (missingMandatoryDocs.length > 0) {
      setError(
        `${t('docsMissingAlert', 'Please attach all mandatory documents before submitting.')} Missing: ${missingMandatoryDocs.join(', ')}`
      );
      return;
    }

    setActionLoading(true);

    try {
      // Step 1: Submit application with personal details and pass category
      const appPayload = {
        passCategoryId: Number(selectedCategoryId),
        fullName: personalDetails.fullName.trim(),
        dob: personalDetails.dob,
        gender: personalDetails.gender,
        mobileNumber: personalDetails.mobileNumber.trim(),
        email: personalDetails.email.trim(),
        residentialAddress: personalDetails.residentialAddress.trim(),
        city: personalDetails.city.trim(),
        pincode: personalDetails.pincode.trim(),
        passengerCategory: personalDetails.passengerCategory,
        sourceStop: personalDetails.sourceStop.trim() || null,
        destinationStop: personalDetails.destinationStop.trim() || null,
        institutionName: personalDetails.institutionName.trim() || null,
        emergencyContact: personalDetails.emergencyContact.trim() || null,
      };

      const createdApp = await api.passApplications.apply(appPayload);

      // Step 2: Upload all attached documents linked to application & documentType
      const uploadEntries = Object.entries(filesByDocType);
      let failedUploads = [];

      for (const [docType, file] of uploadEntries) {
        try {
          await api.passApplications.uploadDocument(createdApp.id, file, docType);
        } catch (uploadErr) {
          failedUploads.push(`${docType} (${uploadErr.message})`);
        }
      }

      if (failedUploads.length > 0) {
        setSuccess(
          `Application #${createdApp.id} submitted! However, some documents failed to upload: ${failedUploads.join(', ')}. You can retry uploading them in the Documents modal.`
        );
      } else {
        setSuccess(
          `Application #${createdApp.id} submitted successfully! Status is Pending Verification.`
        );
      }

      // Reset document files
      setFilesByDocType({});
      setActiveTab('applications');
    } catch (err) {
      setError(err.message || 'Failed to submit application.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDownloadPdf(passId, passNumber) {
    setError(null);
    try {
      await api.digitalPasses.downloadPdf(passId, `pmpml-pass-${passNumber}.pdf`);
    } catch (err) {
      setError(err.message || 'Failed to download pass PDF.');
    }
  }

  async function handleRenewPass(passId) {
    if (!window.confirm(t('renewConfirm', 'Are you sure you want to request renewal for this pass?'))) {
      return;
    }
    setRenewingPassId(passId);
    setError(null);
    setSuccess(null);
    try {
      const resp = await api.passApplications.renew(passId, {});
      setSuccess(`Renewal application #${resp.id} submitted! Status: ${resp.status}`);
      const updatedPasses = await api.digitalPasses.getMyPasses();
      setPasses(updatedPasses || []);
    } catch (err) {
      setError(err.message || 'Renewal request failed.');
    } finally {
      setRenewingPassId(null);
    }
  }

  async function openDocModal(app) {
    setDocModalApp(app);
    setUploadModalFile(null);
    setUploadModalDocType('');
    setDocModalLoading(true);
    try {
      const docs = await api.passApplications.getDocuments(app.id);
      setDocList(docs || []);
    } catch (err) {
      setError(err.message || 'Could not load application documents.');
    } finally {
      setDocModalLoading(false);
    }
  }

  async function handleModalDocUpload(e) {
    e.preventDefault();
    if (!uploadModalFile || !docModalApp) return;

    const validationErr = validateFile(uploadModalFile);
    if (validationErr) {
      setError(validationErr);
      return;
    }

    setDocModalLoading(true);
    setError(null);
    try {
      await api.passApplications.uploadDocument(
        docModalApp.id,
        uploadModalFile,
        uploadModalDocType.trim() || 'Supporting Document'
      );
      setSuccess('Document uploaded successfully!');
      setUploadModalFile(null);
      setUploadModalDocType('');
      // Reload documents and application list to reflect re-verification status
      const docs = await api.passApplications.getDocuments(docModalApp.id);
      setDocList(docs || []);
      const updatedApps = await api.passApplications.getMyApplications();
      setApplications(updatedApps || []);
      const updatedCurrentApp = updatedApps.find((a) => a.id === docModalApp.id);
      if (updatedCurrentApp) {
        setDocModalApp(updatedCurrentApp);
      }
    } catch (err) {
      setError(err.message || 'Document upload failed.');
    } finally {
      setDocModalLoading(false);
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
        return <span className="badge badge-warning">{t('statusPending', 'Pending Review')}</span>;
      case 'VERIFIED':
        return <span className="badge badge-info">{t('statusVerified', 'Verified (Awaiting Approval)')}</span>;
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

  // Filter categories by group
  const filteredCategories = categories.filter((c) => {
    if (categoryFilter === 'ALL') return true;
    return (c.categoryGroup || 'GENERAL') === categoryFilter;
  });

  return (
    <div className="dashboard-layout">
      {/* Sidebar Navigation */}
      <nav className="dashboard-sidebar" aria-label="Passenger navigation">
        <div className="sidebar-header">
          <h3>{t('brandTitle', 'PMPML TransitEase')}</h3>
        </div>
        <ul className="sidebar-nav-list">
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'passInfo' || activeTab === 'categories' ? 'active' : ''}`}
              onClick={() => setActiveTab('passInfo')}
            >
              <span className="nav-icon">ℹ️</span> {t('tabPassInfo', 'Pass Information')}
            </button>
          </li>
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'apply' ? 'active' : ''}`}
              onClick={() => setActiveTab('apply')}
            >
              <span className="nav-icon">📝</span> {t('tabApply', 'Apply for Pass')}
            </button>
          </li>
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'passes' ? 'active' : ''}`}
              onClick={() => setActiveTab('passes')}
            >
              <span className="nav-icon">💳</span> {t('tabMyPasses', 'My Digital Passes')}
            </button>
          </li>
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'applications' ? 'active' : ''}`}
              onClick={() => setActiveTab('applications')}
            >
              <span className="nav-icon">📂</span> {t('tabApplications', 'Applications & Tracking')}
            </button>
          </li>
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'routes' ? 'active' : ''}`}
              onClick={() => setActiveTab('routes')}
            >
              <span className="nav-icon">🗺️</span> {t('tabRoutes', 'Routes & Fares (₹)')}
            </button>
          </li>
          <li>
            <button
              type="button"
              className={`sidebar-nav-item ${activeTab === 'history' ? 'active' : ''}`}
              onClick={() => setActiveTab('history')}
            >
              <span className="nav-icon">📜</span> {t('tabHistory', 'Pass History')}
            </button>
          </li>
        </ul>
      </nav>

      {/* Main Content Area */}
      <main className="dashboard-content">
        {error && <Alert type="error" message={error} onClose={() => setError(null)} />}
        {success && <Alert type="success" message={success} onClose={() => setSuccess(null)} />}

        {/* --- TAB: PASS INFORMATION (CATALOG & DETAILS) --- */}
        {(activeTab === 'passInfo' || activeTab === 'categories') && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>{t('tabPassInfo', 'PMPML Pass Information')}</h2>
                <p className="text-muted">
                  {t(
                    'applySubtitle',
                    'Explore official Pune Mahanagar Parivahan Mahamandal Ltd. (PMPML) passes with verified pricing, eligibility, covered area, and required documents.'
                  )}
                </p>
              </div>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setActiveTab('apply')}
              >
                + {t('tabApply', 'Apply for a Pass')}
              </button>
            </div>

            {/* Category Filter Tabs */}
            <div className="category-group-nav">
              <button
                type="button"
                className={`cat-pill ${categoryFilter === 'ALL' ? 'active' : ''}`}
                onClick={() => setCategoryFilter('ALL')}
              >
                {t('allPasses', 'All Passes')} ({categories.length})
              </button>
              <button
                type="button"
                className={`cat-pill ${categoryFilter === 'GENERAL' ? 'active' : ''}`}
                onClick={() => setCategoryFilter('GENERAL')}
              >
                {t('groupGeneral', 'A. General Passenger Passes')}
              </button>
              <button
                type="button"
                className={`cat-pill ${categoryFilter === 'STUDENT' ? 'active' : ''}`}
                onClick={() => setCategoryFilter('STUDENT')}
              >
                {t('groupStudent', 'B. Student Passes')}
              </button>
              <button
                type="button"
                className={`cat-pill ${categoryFilter === 'SENIOR_CITIZEN' ? 'active' : ''}`}
                onClick={() => setCategoryFilter('SENIOR_CITIZEN')}
              >
                {t('groupSenior', 'C. Senior Citizen Passes')}
              </button>
              <button
                type="button"
                className={`cat-pill ${categoryFilter === 'CONCESSIONARY' ? 'active' : ''}`}
                onClick={() => setCategoryFilter('CONCESSIONARY')}
              >
                {t('groupConcessionary', 'D. Other Concessionary Passes')}
              </button>
            </div>

            {loading ? (
              <Loading message={t('loading', 'Loading PMPML pass catalog...')} />
            ) : filteredCategories.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">🎫</span>
                <h3>{t('noPassesFound', 'No pass categories found')}</h3>
                <p>No passes currently match this category group.</p>
              </div>
            ) : (
              <div className="pmpml-category-grid">
                {filteredCategories.map((rawCat) => {
                  const cat = getLocalizedCategory(rawCat);
                  const mandDocs = getMandatoryDocs(cat);
                  const optDocs = getOptionalDocs(cat);

                  return (
                    <div key={rawCat.id} className="pmpml-category-card">
                      <div className="cat-card-header">
                        <span className={`cat-group-tag tag-${(cat.categoryGroup || 'GENERAL').toLowerCase()}`}>
                          {cat.categoryGroup ? cat.categoryGroup.replace('_', ' ') : 'GENERAL'}
                        </span>
                        <span className="cat-validity-badge">
                          ⏱ {formatValidity(rawCat.validityDays)}
                        </span>
                      </div>

                      <h4 className="cat-title">{cat.name}</h4>

                      <div className="cat-price-box">
                        <span className="cat-price-label">{t('applicableFare', 'Applicable Fare')}:</span>
                        <span className="cat-price-amount">{formatCategoryFare(rawCat)}</span>
                      </div>

                      <div className="cat-covered-area">
                        <span>📍</span>
                        <div>
                          <strong>{t('coveredArea', 'Covered Area')}:</strong> {cat.coveredArea}
                        </div>
                      </div>

                      <p className="cat-desc">{cat.description}</p>

                      {cat.eligibility && (
                        <div className="cat-eligibility-block">
                          <strong>{t('eligibility', 'Eligibility Criteria')}:</strong>
                          <span>{cat.eligibility}</span>
                        </div>
                      )}

                      {cat.discountDetails && (
                        <div className="cat-discount-block">
                          <strong>{t('concessionDetails', 'Discount / Waiver')}:</strong>
                          <span>{cat.discountDetails}</span>
                        </div>
                      )}

                      <div className="cat-docs-checklist-preview">
                        <div className="cat-docs-title">
                          📋 {t('requiredDocuments', 'Required Documents')}:
                        </div>
                        {mandDocs.length === 0 && optDocs.length === 0 ? (
                          <div className="doc-item-preview none">
                            <span>✓</span>
                            <span>{t('noDocsRequired', 'No documents required (Onboard check only)')}</span>
                          </div>
                        ) : (
                          <>
                            {mandDocs.map((doc, idx) => (
                              <div key={idx} className="doc-item-preview mandatory">
                                <span className="doc-badge-chip req">* {t('docRequired', 'Required')}</span>
                                <span>{doc}</span>
                              </div>
                            ))}
                            {optDocs.map((doc, idx) => (
                              <div key={idx} className="doc-item-preview optional">
                                <span className="doc-badge-chip opt">{t('docOptional', 'Optional')}</span>
                                <span>{doc}</span>
                              </div>
                            ))}
                          </>
                        )}
                      </div>

                      <div className="cat-card-actions">
                        <button
                          type="button"
                          className="btn btn-outline"
                          onClick={() => setDetailsModalCat(rawCat)}
                        >
                          🔍 {t('viewDetails', 'View Details')}
                        </button>
                        <button
                          type="button"
                          className="btn btn-primary"
                          onClick={() => handleSelectAndApply(rawCat.id)}
                        >
                          📝 {t('applyNow', 'Apply Now')}
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>
        )}

        {/* --- TAB: APPLY FOR PASS (UPGRADED 5-SECTION WORKFLOW) --- */}
        {activeTab === 'apply' && (
          <section className="section-container" ref={applyFormRef}>
            <div className="section-header">
              <div>
                <h2>{t('applyTitle', 'PMPML Transit Pass Application')}</h2>
                <p className="text-muted">
                  Complete passenger personal details, select eligible pass, review document checklist, and securely submit.
                </p>
              </div>
              <button
                type="button"
                className="btn btn-sm btn-outline"
                onClick={() => setActiveTab('passInfo')}
              >
                {t('backToCatalog', '← Back to Pass Information')}
              </button>
            </div>

            {loading ? (
              <Loading message={t('loading', 'Loading application form...')} />
            ) : (
              <form className="transit-form" onSubmit={handleApplySubmit}>
                <div className="form-section-container">
                  {/* SECTION 1: PASSENGER PERSONAL DETAILS */}
                  <div className="form-section-block">
                    <div className="form-section-header">
                      <span className="form-step-number">1</span>
                      <div>
                        <h3>{t('secPassengerDetails', '1. Passenger Personal Details')}</h3>
                        <p className="form-section-desc">
                          Enter applicant's personal identification, residential, and contact information.
                        </p>
                      </div>
                    </div>

                    <div className="form-grid-3">
                      <div className="form-group">
                        <label htmlFor="pd-fullName">{t('fullNameLabel', 'Full Name *')}</label>
                        <input
                          id="pd-fullName"
                          name="fullName"
                          type="text"
                          className="form-control"
                          value={personalDetails.fullName}
                          onChange={handlePersonalDetailsChange}
                          placeholder="e.g. Rahul Ashok Sharma"
                          required
                        />
                      </div>

                      <div className="form-group">
                        <label htmlFor="pd-dob">{t('dobLabel', 'Date of Birth *')}</label>
                        <input
                          id="pd-dob"
                          name="dob"
                          type="date"
                          className="form-control"
                          value={personalDetails.dob}
                          onChange={handlePersonalDetailsChange}
                          required
                        />
                      </div>

                      <div className="form-group">
                        <label htmlFor="pd-gender">{t('genderLabel', 'Gender')}</label>
                        <select
                          id="pd-gender"
                          name="gender"
                          className="form-control"
                          value={personalDetails.gender}
                          onChange={handlePersonalDetailsChange}
                        >
                          <option value="MALE">Male / पुरुष</option>
                          <option value="FEMALE">Female / महिला</option>
                          <option value="OTHER">Other / इतर</option>
                          <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
                        </select>
                      </div>
                    </div>

                    <div className="form-grid-3">
                      <div className="form-group">
                        <label htmlFor="pd-mobile">{t('mobileLabel', 'Mobile Number *')}</label>
                        <input
                          id="pd-mobile"
                          name="mobileNumber"
                          type="tel"
                          maxLength={10}
                          pattern="[0-9]{10}"
                          className="form-control"
                          value={personalDetails.mobileNumber}
                          onChange={handlePersonalDetailsChange}
                          placeholder="10-digit mobile number"
                          required
                        />
                      </div>

                      <div className="form-group">
                        <label htmlFor="pd-email">{t('emailLabel', 'Email Address *')}</label>
                        <input
                          id="pd-email"
                          name="email"
                          type="email"
                          className="form-control"
                          value={personalDetails.email}
                          onChange={handlePersonalDetailsChange}
                          placeholder="passenger@example.com"
                          required
                        />
                      </div>

                      <div className="form-group">
                        <label htmlFor="pd-category">{t('categoryLabel', 'Passenger Category *')}</label>
                        <select
                          id="pd-category"
                          name="passengerCategory"
                          className="form-control"
                          value={personalDetails.passengerCategory}
                          onChange={handlePersonalDetailsChange}
                          required
                        >
                          <option value="GENERAL">General Commuter / सामान्य</option>
                          <option value="STUDENT">Student / विद्यार्थी</option>
                          <option value="SENIOR_CITIZEN">Senior Citizen (65+) / ज्येष्ठ नागरिक</option>
                          <option value="PERSON_WITH_DISABILITY">Divyang / Differently Abled (40%+)</option>
                          <option value="BLIND">Visually Impaired / अंध व्यक्ती</option>
                          <option value="FREEDOM_FIGHTER">Freedom Fighter / स्वातंत्र्य सैनिक</option>
                          <option value="ACCREDITED_JOURNALIST">Accredited Journalist / अधिस्वीकृतीधारक पत्रकार</option>
                        </select>
                      </div>
                    </div>

                    <div className="form-grid-3">
                      <div className="form-group" style={{ gridColumn: 'span 2' }}>
                        <label htmlFor="pd-address">{t('addressLabel', 'Residential Address *')}</label>
                        <input
                          id="pd-address"
                          name="residentialAddress"
                          type="text"
                          className="form-control"
                          value={personalDetails.residentialAddress}
                          onChange={handlePersonalDetailsChange}
                          placeholder="Flat/House No, Street, Locality"
                          required
                        />
                      </div>

                      <div className="form-group">
                        <label htmlFor="pd-city">{t('cityLabel', 'City *')}</label>
                        <input
                          id="pd-city"
                          name="city"
                          type="text"
                          className="form-control"
                          value={personalDetails.city}
                          onChange={handlePersonalDetailsChange}
                          placeholder="Pune / Pimpri-Chinchwad"
                          required
                        />
                      </div>
                    </div>

                    <div className="form-grid-3">
                      <div className="form-group">
                        <label htmlFor="pd-pincode">{t('pincodeLabel', 'PIN Code *')}</label>
                        <input
                          id="pd-pincode"
                          name="pincode"
                          type="text"
                          maxLength={6}
                          pattern="[0-9]{6}"
                          className="form-control"
                          value={personalDetails.pincode}
                          onChange={handlePersonalDetailsChange}
                          placeholder="411001"
                          required
                        />
                      </div>

                      <div className="form-group">
                        <label htmlFor="pd-institution">{t('institutionLabel', 'School / College / Employer')}</label>
                        <input
                          id="pd-institution"
                          name="institutionName"
                          type="text"
                          className="form-control"
                          value={personalDetails.institutionName}
                          onChange={handlePersonalDetailsChange}
                          placeholder="Relevant for Student or Staff concession"
                        />
                      </div>

                      <div className="form-group">
                        <label htmlFor="pd-emergency">{t('emergencyLabel', 'Emergency Contact (Optional)')}</label>
                        <input
                          id="pd-emergency"
                          name="emergencyContact"
                          type="tel"
                          maxLength={10}
                          className="form-control"
                          value={personalDetails.emergencyContact}
                          onChange={handlePersonalDetailsChange}
                          placeholder="Contact person phone number"
                        />
                      </div>
                    </div>

                    <div className="form-grid-2">
                      <div className="form-group">
                        <label htmlFor="pd-source">{t('sourceStopLabel', 'Source / Starting Stop (Optional)')}</label>
                        <input
                          id="pd-source"
                          name="sourceStop"
                          type="text"
                          className="form-control"
                          value={personalDetails.sourceStop}
                          onChange={handlePersonalDetailsChange}
                          placeholder="e.g. Swargate / Katraj / Shivajinagar"
                        />
                      </div>

                      <div className="form-group">
                        <label htmlFor="pd-dest">{t('destinationStopLabel', 'Destination Stop (Optional)')}</label>
                        <input
                          id="pd-dest"
                          name="destinationStop"
                          type="text"
                          className="form-control"
                          value={personalDetails.destinationStop}
                          onChange={handlePersonalDetailsChange}
                          placeholder="e.g. Pune Station / Hinjawadi"
                        />
                      </div>
                    </div>
                  </div>

                  {/* SECTION 2: PASS SELECTION & DETAILS */}
                  <div className="form-section-block">
                    <div className="form-section-header">
                      <span className="form-step-number">2</span>
                      <div>
                        <h3>{t('secPassSelection', '2. Pass Selection & Details')}</h3>
                        <p className="form-section-desc">
                          Choose your pass type and review validity, coverage area, and fare structure.
                        </p>
                      </div>
                    </div>

                    <div className="form-group">
                      <label htmlFor="pass-category">
                        {t('category', 'Select Pass Category')} *
                      </label>
                      <select
                        id="pass-category"
                        className="form-control"
                        value={selectedCategoryId}
                        onChange={(e) => {
                          setSelectedCategoryId(e.target.value);
                          // Clear uploaded files on category change to prevent mismatched docs
                          setFilesByDocType({});
                        }}
                        required
                      >
                        <option value="" disabled>
                          {t('selectCategoryPlaceholder', '-- Select a PMPML Pass --')}
                        </option>
                        {categories.map((c) => {
                          const loc = getLocalizedCategory(c);
                          return (
                            <option key={c.id} value={c.id}>
                              {loc.name} — {formatCategoryFare(c)} ({formatValidity(c.validityDays)})
                            </option>
                          );
                        })}
                      </select>
                    </div>

                    {selectedCategory && (() => {
                      const loc = getLocalizedCategory(selectedCategory);
                      return (
                        <div className="selected-cat-summary-box">
                          <div className="summary-header">
                            <span className={`cat-group-tag tag-${(loc.categoryGroup || 'GENERAL').toLowerCase()}`}>
                              {loc.categoryGroup ? loc.categoryGroup.replace('_', ' ') : 'GENERAL'}
                            </span>
                            <span className="summary-fare font-semibold text-success">
                              {formatCategoryFare(selectedCategory)}
                            </span>
                          </div>

                          <h4 className="summary-title">{loc.name}</h4>

                          <p className="summary-validity">
                            <strong>{t('validity', 'Validity')}:</strong> {formatValidity(selectedCategory.validityDays)}
                          </p>

                          <div className="cat-covered-area" style={{ margin: '0.4rem 0' }}>
                            <span>📍</span>
                            <div>
                              <strong>{t('coveredArea', 'Covered Area')}:</strong> {loc.coveredArea}
                            </div>
                          </div>

                          <p className="summary-desc">{loc.description}</p>

                          {loc.eligibility && (
                            <p className="summary-eligibility" style={{ marginTop: '0.4rem' }}>
                              <strong>{t('eligibility', 'Eligibility Criteria')}:</strong> {loc.eligibility}
                            </p>
                          )}

                          {loc.discountDetails && (
                            <p className="summary-discount" style={{ marginTop: '0.4rem' }}>
                              <strong>{t('concessionDetails', 'Discount / Waiver')}:</strong> {loc.discountDetails}
                            </p>
                          )}
                        </div>
                      );
                    })()}
                  </div>

                  {/* SECTION 3: DYNAMIC DOCUMENT CHECKLIST & SEPARATE UPLOAD CONTROLS */}
                  <div className="form-section-block">
                    <div className="form-section-header">
                      <span className="form-step-number">3</span>
                      <div>
                        <h3>{t('secRequiredDocs', '3. Dynamic Document Checklist & Uploads')}</h3>
                        <p className="form-section-desc">
                          Upload each required document individually. Allowed formats: PDF, JPG, PNG (Max 5 MB each).
                        </p>
                      </div>
                    </div>

                    {currentMandatoryDocs.length === 0 && currentOptionalDocs.length === 0 ? (
                      <div className="alert-banner alert-info" style={{ margin: '0.5rem 0' }}>
                        <div className="alert-content">
                          <span>✓</span>
                          <span>
                            {t(
                              'noDocsRequired',
                              'No supporting documents are required for this daily pass. You can proceed directly to review and submit.'
                            )}
                          </span>
                        </div>
                      </div>
                    ) : (
                      <>
                        {/* Summary Progress Checklist Bar */}
                        <div className="checklist-summary-bar">
                          <div className="checklist-header">
                            <span className="checklist-title font-semibold">
                              {t('docChecklistTitle', 'Document Requirements Checklist')}
                            </span>
                            <span className="checklist-count text-sm">
                              {currentMandatoryDocs.length - missingMandatoryDocs.length} / {currentMandatoryDocs.length}{' '}
                              {t('mandatoryAttached', 'Mandatory Attached')}
                            </span>
                          </div>

                          <div className="checklist-chips-row">
                            {currentMandatoryDocs.map((doc, idx) => {
                              const isAttached = !!filesByDocType[doc];
                              return (
                                <span
                                  key={`mand-${idx}`}
                                  className={`checklist-chip ${isAttached ? 'completed' : 'pending'}`}
                                >
                                  {isAttached ? '✓' : '⚠️'} {doc} (Required)
                                </span>
                              );
                            })}
                            {currentOptionalDocs.map((doc, idx) => {
                              const isAttached = !!filesByDocType[doc];
                              return (
                                <span
                                  key={`opt-${idx}`}
                                  className={`checklist-chip ${isAttached ? 'completed' : 'optional'}`}
                                >
                                  {isAttached ? '✓' : '○'} {doc} (Optional)
                                </span>
                              );
                            })}
                          </div>
                        </div>

                        {/* Separate Upload Controls Grid */}
                        <div className="doc-upload-grid">
                          {/* Mandatory Documents First */}
                          {currentMandatoryDocs.map((docName, idx) => {
                            const file = filesByDocType[docName];
                            return (
                              <div key={`ctrl-mand-${idx}`} className={`doc-upload-card ${file ? 'has-file' : ''}`}>
                                <div className="doc-card-top">
                                  <span className="doc-card-title">{docName}</span>
                                  <span className="doc-badge-chip req">* {t('docRequired', 'Required')}</span>
                                </div>

                                {!file ? (
                                  <div className="doc-card-dropzone">
                                    <label htmlFor={`file-input-${idx}`} className="doc-upload-btn-label">
                                      📎 {t('uploadCardPrompt', 'Attach document file')}
                                    </label>
                                    <input
                                      id={`file-input-${idx}`}
                                      type="file"
                                      className="visually-hidden"
                                      accept=".pdf,.jpg,.jpeg,.png"
                                      onChange={(e) => handleDocFileSelect(docName, e)}
                                    />
                                    <span className="text-muted text-xs">PDF, JPG, PNG &le; 5 MB</span>
                                  </div>
                                ) : (
                                  <div className="file-selected-box">
                                    <div className="file-selected-info">
                                      <span className="file-selected-icon">📄</span>
                                      <div className="file-name-size">
                                        <span className="file-name-text" title={file.name}>
                                          {file.name}
                                        </span>
                                        <span className="file-size-text">
                                          {(file.size / (1024 * 1024)).toFixed(2) >= 1
                                            ? `${(file.size / (1024 * 1024)).toFixed(2)} MB`
                                            : `${(file.size / 1024).toFixed(1)} KB`}
                                        </span>
                                      </div>
                                    </div>
                                    <div className="file-action-buttons">
                                      <label
                                        htmlFor={`file-input-replace-${idx}`}
                                        className="btn btn-xs btn-outline"
                                        style={{ cursor: 'pointer', margin: 0 }}
                                      >
                                        🔄 {t('replaceDocBtn', 'Replace')}
                                      </label>
                                      <input
                                        id={`file-input-replace-${idx}`}
                                        type="file"
                                        className="visually-hidden"
                                        accept=".pdf,.jpg,.jpeg,.png"
                                        onChange={(e) => handleDocFileSelect(docName, e)}
                                      />
                                      <button
                                        type="button"
                                        className="btn btn-xs btn-danger"
                                        onClick={() => handleRemoveDocFile(docName)}
                                      >
                                        ✕
                                      </button>
                                    </div>
                                  </div>
                                )}
                              </div>
                            );
                          })}

                          {/* Optional Documents Next */}
                          {currentOptionalDocs.map((docName, idx) => {
                            const file = filesByDocType[docName];
                            return (
                              <div
                                key={`ctrl-opt-${idx}`}
                                className={`doc-upload-card ${file ? 'has-file' : ''}`}
                              >
                                <div className="doc-card-top">
                                  <span className="doc-card-title">{docName}</span>
                                  <span className="doc-badge-chip opt">{t('docOptional', 'Optional')}</span>
                                </div>

                                {!file ? (
                                  <div className="doc-card-dropzone">
                                    <label htmlFor={`file-input-opt-${idx}`} className="doc-upload-btn-label">
                                      📎 {t('uploadCardPrompt', 'Attach document file')}
                                    </label>
                                    <input
                                      id={`file-input-opt-${idx}`}
                                      type="file"
                                      className="visually-hidden"
                                      accept=".pdf,.jpg,.jpeg,.png"
                                      onChange={(e) => handleDocFileSelect(docName, e)}
                                    />
                                    <span className="text-muted text-xs">PDF, JPG, PNG &le; 5 MB</span>
                                  </div>
                                ) : (
                                  <div className="file-selected-box">
                                    <div className="file-selected-info">
                                      <span className="file-selected-icon">📄</span>
                                      <div className="file-name-size">
                                        <span className="file-name-text" title={file.name}>
                                          {file.name}
                                        </span>
                                        <span className="file-size-text">
                                          {(file.size / (1024 * 1024)).toFixed(2) >= 1
                                            ? `${(file.size / (1024 * 1024)).toFixed(2)} MB`
                                            : `${(file.size / 1024).toFixed(1)} KB`}
                                        </span>
                                      </div>
                                    </div>
                                    <div className="file-action-buttons">
                                      <label
                                        htmlFor={`file-input-replace-opt-${idx}`}
                                        className="btn btn-xs btn-outline"
                                        style={{ cursor: 'pointer', margin: 0 }}
                                      >
                                        🔄 {t('replaceDocBtn', 'Replace')}
                                      </label>
                                      <input
                                        id={`file-input-replace-opt-${idx}`}
                                        type="file"
                                        className="visually-hidden"
                                        accept=".pdf,.jpg,.jpeg,.png"
                                        onChange={(e) => handleDocFileSelect(docName, e)}
                                      />
                                      <button
                                        type="button"
                                        className="btn btn-xs btn-danger"
                                        onClick={() => handleRemoveDocFile(docName)}
                                      >
                                        ✕
                                      </button>
                                    </div>
                                  </div>
                                )}
                              </div>
                            );
                          })}
                        </div>
                      </>
                    )}
                  </div>

                  {/* SECTION 4: REVIEW AND SUBMIT APPLICATION */}
                  <div className="form-section-block">
                    <div className="form-section-header">
                      <span className="form-step-number">4</span>
                      <div>
                        <h3>{t('secReviewSubmit', '4. Review & Submit Application')}</h3>
                        <p className="form-section-desc">
                          Verify all details and attached proofs before submitting for verification officer review.
                        </p>
                      </div>
                    </div>

                    <div className="review-summary-grid">
                      {/* Panel 1: Passenger Personal Details */}
                      <div className="review-panel">
                        <div className="review-panel-title">👤 Passenger Information</div>
                        <div className="review-data-list">
                          <div className="review-data-row">
                            <span className="review-data-label">Name:</span>
                            <span className="review-data-val">{personalDetails.fullName || '—'}</span>
                          </div>
                          <div className="review-data-row">
                            <span className="review-data-label">DOB / Gender:</span>
                            <span className="review-data-val">
                              {personalDetails.dob ? formatDateIN(personalDetails.dob) : '—'} ({personalDetails.gender})
                            </span>
                          </div>
                          <div className="review-data-row">
                            <span className="review-data-label">Mobile:</span>
                            <span className="review-data-val">{personalDetails.mobileNumber || '—'}</span>
                          </div>
                          <div className="review-data-row">
                            <span className="review-data-label">Email:</span>
                            <span className="review-data-val">{personalDetails.email || '—'}</span>
                          </div>
                          <div className="review-data-row">
                            <span className="review-data-label">Address:</span>
                            <span className="review-data-val">
                              {personalDetails.residentialAddress ? `${personalDetails.residentialAddress}, ${personalDetails.city} - ${personalDetails.pincode}` : '—'}
                            </span>
                          </div>
                          <div className="review-data-row">
                            <span className="review-data-label">Category:</span>
                            <span className="review-data-val">{personalDetails.passengerCategory}</span>
                          </div>
                          {personalDetails.institutionName && (
                            <div className="review-data-row">
                              <span className="review-data-label">Institution:</span>
                              <span className="review-data-val">{personalDetails.institutionName}</span>
                            </div>
                          )}
                          {(personalDetails.sourceStop || personalDetails.destinationStop) && (
                            <div className="review-data-row">
                              <span className="review-data-label">Route Stops:</span>
                              <span className="review-data-val">
                                {personalDetails.sourceStop || 'Any'} ➔ {personalDetails.destinationStop || 'Any'}
                              </span>
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Panel 2: Pass Details */}
                      <div className="review-panel">
                        <div className="review-panel-title">🎫 Selected Pass Details</div>
                        {selectedCategory ? (
                          <div className="review-data-list">
                            <div className="review-data-row">
                              <span className="review-data-label">Pass Name:</span>
                              <span className="review-data-val">{getLocalizedCategory(selectedCategory).name}</span>
                            </div>
                            <div className="review-data-row">
                              <span className="review-data-label">Applicable Fare:</span>
                              <span className="review-data-val text-success">
                                {formatCategoryFare(selectedCategory)}
                              </span>
                            </div>
                            <div className="review-data-row">
                              <span className="review-data-label">Validity Period:</span>
                              <span className="review-data-val">{formatValidity(selectedCategory.validityDays)}</span>
                            </div>
                            <div className="review-data-row">
                              <span className="review-data-label">Network Scope:</span>
                              <span className="review-data-val">{getLocalizedCategory(selectedCategory).coveredArea}</span>
                            </div>
                          </div>
                        ) : (
                          <p className="text-muted text-sm">No pass category selected yet.</p>
                        )}
                      </div>

                      {/* Panel 3: Documents Checklist Status */}
                      <div className="review-panel">
                        <div className="review-panel-title">📄 Document Attachments Status</div>
                        {currentMandatoryDocs.length === 0 && currentOptionalDocs.length === 0 ? (
                          <p className="text-success text-sm">✓ No documents required for this pass.</p>
                        ) : (
                          <div className="review-data-list">
                            {currentMandatoryDocs.map((doc, i) => (
                              <div key={`rev-m-${i}`} className="review-data-row">
                                <span className="review-data-label">{doc}:</span>
                                <span className={`review-data-val ${filesByDocType[doc] ? 'text-success' : 'text-danger'}`}>
                                  {filesByDocType[doc] ? `✓ ${filesByDocType[doc].name}` : '✗ Missing (Required)'}
                                </span>
                              </div>
                            ))}
                            {currentOptionalDocs.map((doc, i) => (
                              <div key={`rev-o-${i}`} className="review-data-row">
                                <span className="review-data-label">{doc}:</span>
                                <span className="review-data-val">
                                  {filesByDocType[doc] ? `✓ ${filesByDocType[doc].name}` : 'Optional (Not attached)'}
                                </span>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>

                    {missingMandatoryDocs.length > 0 && (
                      <div className="alert-banner alert-warning" style={{ margin: '1rem 0' }}>
                        <div className="alert-content">
                          <span>⚠️</span>
                          <span>
                            {t('docsMissingAlert', 'Please attach all mandatory documents before submitting.')}{' '}
                            Missing: <strong>{missingMandatoryDocs.join(', ')}</strong>
                          </span>
                        </div>
                      </div>
                    )}

                    <div className="form-actions" style={{ marginTop: '1.5rem' }}>
                      <button
                        type="submit"
                        className="btn btn-primary btn-block btn-lg"
                        disabled={actionLoading || missingMandatoryDocs.length > 0}
                      >
                        {actionLoading
                          ? t('submittingBtn', 'Submitting Application & Documents...')
                          : t('submitApplicationBtn', 'Submit Pass Application')}
                      </button>
                    </div>
                  </div>
                </div>
              </form>
            )}
          </section>
        )}

        {/* --- TAB: MY PASSES --- */}
        {activeTab === 'passes' && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>{t('myPassesTitle', 'My Digital Passes')}</h2>
                <p className="text-muted">
                  {t('myPassesSubtitle', 'View active and historical passes or download official PDF copies.')}
                </p>
              </div>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setActiveTab('passInfo')}
              >
                + {t('tabApply', 'Apply for New Pass')}
              </button>
            </div>

            {loading ? (
              <Loading message={t('loading', 'Loading digital passes...')} />
            ) : passes.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">🎫</span>
                <h3>{t('noPassesFound', 'No digital passes found')}</h3>
                <p>{t('noPassesDesc', "You don't have any approved digital passes yet.")}</p>
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => setActiveTab('passInfo')}
                >
                  {t('tabPassInfo', 'Browse Pass Information')}
                </button>
              </div>
            ) : (
              <div className="pass-card-grid">
                {passes.map((pass) => (
                  <div key={pass.id} className="pass-card">
                    <div className="pass-card-header">
                      <div className="pass-chip">PMPML Digital Pass</div>
                      {getStatusBadge(pass.status)}
                    </div>
                    <div className="pass-number-display">
                      <label>{t('passNumber', 'PASS NUMBER')}</label>
                      <span className="pass-number">{pass.passNumber}</span>
                    </div>
                    <div className="pass-details-row">
                      <div>
                        <label>{t('category', 'CATEGORY')}</label>
                        <p>{pass.passCategoryName || 'Standard'}</p>
                      </div>
                      <div>
                        <label>{t('holder', 'HOLDER')}</label>
                        <p>{pass.passengerName}</p>
                      </div>
                    </div>
                    <div className="pass-validity-row">
                      <div>
                        <label>{t('validFrom', 'VALID FROM')}</label>
                        <p>{formatDateIN(pass.validityStartDate)}</p>
                      </div>
                      <div>
                        <label>{t('expiresOn', 'EXPIRES ON')}</label>
                        <p>{formatDateIN(pass.validityEndDate)}</p>
                      </div>
                    </div>
                    <div className="pass-card-actions">
                      <button
                        type="button"
                        className="btn btn-sm btn-primary"
                        onClick={() => handleDownloadPdf(pass.id, pass.passNumber)}
                      >
                        📥 {t('downloadPdf', 'Download PDF')}
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline"
                        onClick={() => handleRenewPass(pass.id)}
                        disabled={renewingPassId === pass.id}
                      >
                        {renewingPassId === pass.id
                          ? t('renewSubmitting', 'Submitting...')
                          : `🔄 ${t('renewPass', 'Renew Pass')}`}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* --- SECTION 5: TAB APPLICATIONS & TRACKING --- */}
        {activeTab === 'applications' && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>{t('applicationsTitle', 'My Applications & Tracking')}</h2>
                <p className="text-muted">
                  Track verification progress, inspect officer remarks, and replace rejected documents without duplicate applications.
                </p>
              </div>
              <button
                type="button"
                className="btn btn-sm btn-outline"
                onClick={() => loadTabInitialData('applications')}
              >
                🔄 {t('refresh', 'Refresh')}
              </button>
            </div>

            {loading ? (
              <Loading message={t('loading', 'Loading applications...')} />
            ) : applications.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">📋</span>
                <h3>{t('noApplicationsFound', 'No applications yet')}</h3>
                <p>You have not submitted any pass applications.</p>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => setActiveTab('passInfo')}
                >
                  {t('applyNow', 'Apply Now')}
                </button>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>{t('appId', 'App ID')}</th>
                      <th>{t('applicant', 'Passenger')}</th>
                      <th>{t('category', 'Category')}</th>
                      <th>{t('type', 'Type')}</th>
                      <th>{t('appDate', 'Application Date')}</th>
                      <th>{t('status', 'Status')}</th>
                      <th>{t('remarks', 'Officer Remarks')}</th>
                      <th>{t('documents', 'Documents')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {applications.map((app) => {
                      const isRejected = app.status === 'REJECTED';
                      return (
                        <tr key={app.id}>
                          <td><strong>#{app.id}</strong></td>
                          <td>
                            <div>{app.fullName || app.passengerName}</div>
                            <small className="text-muted">{app.mobileNumber || app.passengerEmail}</small>
                          </td>
                          <td>{app.passCategoryName}</td>
                          <td>
                            {app.isRenewal ? (
                              <span className="badge badge-info">Renewal</span>
                            ) : (
                              <span className="badge badge-outline">New</span>
                            )}
                          </td>
                          <td>{formatDateIN(app.applicationDate)}</td>
                          <td>{getStatusBadge(app.status, app.remarks)}</td>
                          <td>
                            {app.remarks ? (
                              <div className={isRejected ? 'text-danger font-semibold' : 'remarks-text'}>
                                {app.remarks}
                              </div>
                            ) : (
                              <span className="text-muted">—</span>
                            )}
                          </td>
                          <td>
                            <button
                              type="button"
                              className={`btn btn-xs ${isRejected ? 'btn-danger' : 'btn-outline'}`}
                              onClick={() => openDocModal(app)}
                            >
                              📎 {isRejected ? t('replaceDocBtn', 'Replace Doc') : t('documents', 'Docs')}
                            </button>
                          </td>
                        </tr>
                      );
                    })}
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
                <h2>{t('routesTitle', 'PMPML Bus Routes & Fares (₹)')}</h2>
                <p className="text-muted">
                  {t(
                    'routesSubtitle',
                    'Explore major city and suburban transit lines, stages, and single journey passenger fares.'
                  )}
                </p>
              </div>
              <button
                type="button"
                className="btn btn-sm btn-outline"
                onClick={() => loadTabInitialData('routes')}
              >
                🔄 {t('refresh', 'Refresh')}
              </button>
            </div>

            {loading ? (
              <Loading message={t('loading', 'Loading routes...')} />
            ) : routes.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">🚌</span>
                <p>No transit routes listed currently.</p>
              </div>
            ) : (
              <div className="route-grid">
                {routes.map((rt) => (
                  <div key={rt.id} className="route-card">
                    <div className="route-card-header">
                      <span className="route-badge">{rt.routeNumber}</span>
                      <span className="route-fare">{formatINR(rt.fare)}</span>
                    </div>
                    <div className="route-endpoints">
                      <span className="origin">{rt.source || rt.origin}</span>
                      <span className="arrow">➔</span>
                      <span className="destination">{rt.destination}</span>
                    </div>
                    {rt.stops && (
                      <div className="route-stops">
                        <small className="text-muted">{t('intermediateStops', 'Intermediate Stops:')}</small>
                        <p className="stops-text">
                          {Array.isArray(rt.stops) ? rt.stops.join(' • ') : rt.stops}
                        </p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* --- TAB: HISTORY --- */}
        {activeTab === 'history' && (
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>{t('historyTitle', 'Pass & Application History')}</h2>
                <p className="text-muted">
                  {t('historySubtitle', 'Audit trail of all previous commuter requests and issued digital passes.')}
                </p>
              </div>
            </div>

            {loading ? (
              <Loading message={t('loading', 'Loading history...')} />
            ) : (
              <div className="history-sections">
                <div className="history-block">
                  <h3>
                    {t('applicationsAudit', 'Applications Audit')} ({history.applications?.length || 0})
                  </h3>
                  {history.applications && history.applications.length > 0 ? (
                    <div className="table-responsive">
                      <table className="data-table">
                        <thead>
                          <tr>
                            <th>{t('appId', 'App #')}</th>
                            <th>{t('category', 'Category')}</th>
                            <th>{t('type', 'Type')}</th>
                            <th>{t('appDate', 'Date')}</th>
                            <th>{t('status', 'Final Status')}</th>
                            <th>{t('remarks', 'Remarks')}</th>
                          </tr>
                        </thead>
                        <tbody>
                          {history.applications.map((app) => (
                            <tr key={app.id}>
                              <td>#{app.id}</td>
                              <td>{app.passCategoryName}</td>
                              <td>{app.isRenewal ? 'Renewal' : 'Initial'}</td>
                              <td>{formatDateIN(app.applicationDate)}</td>
                              <td>{getStatusBadge(app.status, app.remarks)}</td>
                              <td>{app.remarks || '—'}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <p className="text-muted">No past applications.</p>
                  )}
                </div>

                <div className="history-block">
                  <h3>
                    {t('passesAudit', 'Digital Passes Audit')} ({history.passes?.length || 0})
                  </h3>
                  {history.passes && history.passes.length > 0 ? (
                    <div className="table-responsive">
                      <table className="data-table">
                        <thead>
                          <tr>
                            <th>{t('passNumber', 'Pass Number')}</th>
                            <th>{t('category', 'Category')}</th>
                            <th>{t('validFrom', 'Issue Date')}</th>
                            <th>{t('validFrom', 'Valid Start')}</th>
                            <th>{t('expiresOn', 'Valid End')}</th>
                            <th>{t('status', 'Status')}</th>
                          </tr>
                        </thead>
                        <tbody>
                          {history.passes.map((p) => (
                            <tr key={p.id}>
                              <td><strong>{p.passNumber}</strong></td>
                              <td>{p.passCategoryName}</td>
                              <td>{formatDateIN(p.issueDate)}</td>
                              <td>{formatDateIN(p.validityStartDate)}</td>
                              <td>{formatDateIN(p.validityEndDate)}</td>
                              <td>{getStatusBadge(p.status)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <p className="text-muted">No past passes.</p>
                  )}
                </div>
              </div>
            )}
          </section>
        )}
      </main>

      {/* --- VIEW DETAILS MODAL FOR PASS CATALOG --- */}
      {detailsModalCat && (() => {
        const loc = getLocalizedCategory(detailsModalCat);
        const mandDocs = getMandatoryDocs(loc);
        const optDocs = getOptionalDocs(loc);

        return (
          <div
            className="modal-backdrop"
            onClick={() => setDetailsModalCat(null)}
            role="dialog"
            aria-modal="true"
          >
            <div className="modal-dialog modal-md" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <div>
                  <span className={`cat-group-tag tag-${(detailsModalCat.categoryGroup || 'GENERAL').toLowerCase()}`}>
                    {detailsModalCat.categoryGroup ? detailsModalCat.categoryGroup.replace('_', ' ') : 'GENERAL'}
                  </span>
                  <h3 className="modal-title" style={{ marginTop: '0.25rem' }}>
                    {loc.name}
                  </h3>
                </div>
                <button
                  type="button"
                  className="modal-close"
                  onClick={() => setDetailsModalCat(null)}
                >
                  &times;
                </button>
              </div>

              <div className="modal-body">
                <div className="details-modal-grid">
                  <div className="details-modal-metrics">
                    <div className="details-metric-item">
                      <span className="details-metric-label">{t('applicableFare', 'Applicable Fare')}</span>
                      <span className="details-metric-value text-fare">
                        {formatCategoryFare(detailsModalCat)}
                      </span>
                    </div>
                    <div className="details-metric-item">
                      <span className="details-metric-label">{t('validity', 'Validity')}</span>
                      <span className="details-metric-value">
                        {formatValidity(detailsModalCat.validityDays)}
                      </span>
                    </div>
                  </div>

                  <div className="details-section-box">
                    <h5 className="details-section-title">
                      📍 {t('coveredArea', 'Covered Area / Route Scope')}
                    </h5>
                    <p className="details-section-desc">{loc.coveredArea}</p>
                  </div>

                  <div className="details-section-box">
                    <h5 className="details-section-title">
                      ℹ️ {t('tabPassInfo', 'Pass Overview')}
                    </h5>
                    <p className="details-section-desc">{loc.description}</p>
                  </div>

                  {loc.eligibility && (
                    <div className="details-section-box">
                      <h5 className="details-section-title">
                        👥 {t('eligibility', 'Eligibility Criteria')}
                      </h5>
                      <p className="details-section-desc">{loc.eligibility}</p>
                    </div>
                  )}

                  {loc.discountDetails && (
                    <div className="details-section-box">
                      <h5 className="details-section-title">
                        🏷️ {t('concessionDetails', 'Concession / Discount Details')}
                      </h5>
                      <p className="details-section-desc">{loc.discountDetails}</p>
                    </div>
                  )}

                  <div className="details-section-box">
                    <h5 className="details-section-title">
                      📄 {t('requiredDocuments', 'Required Documents Checklist')}
                    </h5>
                    {mandDocs.length === 0 && optDocs.length === 0 ? (
                      <p className="text-success" style={{ fontSize: '0.875rem', margin: 0 }}>
                        {t(
                          'noDocsRequired',
                          '✓ No supporting document upload required for this pass. Identity verification, if any, is performed on-board.'
                        )}
                      </p>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                        {mandDocs.length > 0 && (
                          <div>
                            <span className="doc-badge-req mandatory">
                              {t('mandatoryDocsLabel', '* Mandatory to attach:')}
                            </span>
                            <ul className="modal-doc-checklist mandatory">
                              {mandDocs.map((doc, idx) => (
                                <li key={idx}>
                                  <span>•</span> {doc}
                                </li>
                              ))}
                            </ul>
                          </div>
                        )}

                        {optDocs.length > 0 && (
                          <div>
                            <span className="doc-badge-req optional">
                              {t('optionalDocsLabel', 'Optional supporting documents:')}
                            </span>
                            <ul className="modal-doc-checklist optional">
                              {optDocs.map((doc, idx) => (
                                <li key={idx}>
                                  <span>•</span> {doc}
                                </li>
                              ))}
                            </ul>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </div>

              <div className="modal-footer" style={{ justifyContent: 'space-between' }}>
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => setDetailsModalCat(null)}
                >
                  {t('close', 'Close')}
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => {
                    setDetailsModalCat(null);
                    handleSelectAndApply(detailsModalCat.id);
                  }}
                >
                  {t('applyNow', 'Apply for this Pass')} ➔
                </button>
              </div>
            </div>
          </div>
        );
      })()}

      {/* --- DOCUMENT MODAL FOR EXISTING APPLICATION (WITH REPLACEMENT CAPABILITY) --- */}
      {docModalApp && (
        <div
          className="modal-backdrop"
          onClick={() => setDocModalApp(null)}
          role="dialog"
          aria-modal="true"
        >
          <div className="modal-dialog modal-md" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3 className="modal-title">
                  {t('attachedDocs', 'Documents for Application')} #{docModalApp.id}
                </h3>
                <span className="text-muted text-xs">
                  {docModalApp.passCategoryName} • {getStatusBadge(docModalApp.status, docModalApp.remarks)}
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
              {docModalLoading && <Loading message={t('loading', 'Processing documents...')} />}

              {/* Rejection Alert Callout if applicable */}
              {(docModalApp.status === 'REJECTED' || docModalApp.remarks) && (
                <div className="app-rejection-alert">
                  <div>
                    <strong>Officer Remarks:</strong>
                    <div>{docModalApp.remarks || 'Document verification rejected. Please replace with valid document proofs.'}</div>
                  </div>
                </div>
              )}

              <div className="existing-docs-section">
                <h4>{t('attachedDocs', 'Attached Documents')} ({docList.length})</h4>
                {docList.length === 0 ? (
                  <p className="text-muted">No documents uploaded yet for this application.</p>
                ) : (
                  <ul className="doc-list">
                    {docList.map((doc) => {
                      const docStatusClass = (doc.status || 'PENDING').toLowerCase();
                      return (
                        <li key={doc.id} className="doc-item" style={{ flexDirection: 'column', alignItems: 'stretch' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div className="doc-meta">
                              <span className="doc-icon">📄</span>
                              <div>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                  <strong>{doc.documentType || 'Supporting Document'}</strong>
                                  <span className={`doc-item-status-pill ${docStatusClass}`}>
                                    {doc.status || 'PENDING'}
                                  </span>
                                </div>
                                <span className="text-muted text-xs" style={{ display: 'block' }}>
                                  {doc.fileName} • {doc.fileSize ? `${(doc.fileSize / 1024).toFixed(1)} KB` : 'N/A'}
                                </span>
                              </div>
                            </div>
                            <button
                              type="button"
                              className="btn btn-xs btn-outline"
                              onClick={() => handleDownloadDoc(doc.id, doc.fileName)}
                            >
                              ⬇️ {t('downloadPdf', 'Download')}
                            </button>
                          </div>
                          {doc.remarks && (
                            <div style={{ marginTop: '0.35rem', fontSize: '0.75rem', color: '#991b1b', background: '#fee2e2', padding: '0.25rem 0.5rem', borderRadius: '4px' }}>
                              <strong>Officer Note:</strong> {doc.remarks}
                            </div>
                          )}
                        </li>
                      );
                    })}
                  </ul>
                )}
              </div>

              {/* Allow uploading or replacing documents if status is PENDING or REJECTED */}
              {(docModalApp.status === 'PENDING' || docModalApp.status === 'REJECTED') && (
                <div className="upload-doc-box" style={{ marginTop: '1.25rem' }}>
                  <h4>{t('uploadAdditionalDoc', 'Upload / Replace Document')}</h4>
                  <p className="text-muted text-xs" style={{ marginBottom: '0.5rem' }}>
                    Uploading a replacement document for a rejected application automatically marks it for re-verification without creating a duplicate application.
                  </p>
                  <form onSubmit={handleModalDocUpload}>
                    <div className="form-group" style={{ marginBottom: '0.5rem' }}>
                      <label htmlFor="modal-doc-type" className="text-xs font-semibold">
                        Document Type / Name *
                      </label>
                      <input
                        id="modal-doc-type"
                        type="text"
                        className="form-control form-control-sm"
                        placeholder="e.g. Identity Proof / Student ID / Photograph"
                        value={uploadModalDocType}
                        onChange={(e) => setUploadModalDocType(e.target.value)}
                        required
                      />
                    </div>
                    <div className="form-group" style={{ marginBottom: '0.5rem' }}>
                      <input
                        type="file"
                        className="form-control file-input"
                        accept=".pdf,.jpg,.jpeg,.png"
                        onChange={(e) => setUploadModalFile(e.target.files[0] || null)}
                        required
                      />
                      <small className="form-help">
                        {t('allowedFormats', 'Allowed formats: PDF, JPG, PNG (Max 5 MB).')}
                      </small>
                    </div>
                    <div style={{ marginTop: '0.75rem' }}>
                      <button
                        type="submit"
                        className="btn btn-sm btn-primary"
                        disabled={!uploadModalFile || !uploadModalDocType.trim() || docModalLoading}
                      >
                        📤 {t('uploadDocumentLabel', 'Upload / Replace Document')}
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </div>

            <div className="modal-footer">
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
