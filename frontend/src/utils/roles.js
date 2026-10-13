/**
 * Role Utility Functions for TransitEase
 * Handles role normalization between backend enum values (e.g. 'ADMIN')
 * and Spring Security prefixed formats (e.g. 'ROLE_ADMIN').
 */

/**
 * Normalizes a role string to uppercase without any 'ROLE_' prefix.
 * e.g., 'ROLE_ADMIN' -> 'ADMIN', 'admin' -> 'ADMIN', 'ADMIN' -> 'ADMIN'
 * @param {string} role 
 * @returns {string}
 */
export function normalizeRole(role) {
  if (!role || typeof role !== 'string') return '';
  const cleaned = role.trim().toUpperCase();
  return cleaned.startsWith('ROLE_') ? cleaned.substring(5) : cleaned;
}

/**
 * Checks if the user has an Admin role.
 * @param {string} role 
 * @returns {boolean}
 */
export function isAdmin(role) {
  return normalizeRole(role) === 'ADMIN';
}

/**
 * Checks if the user is a Document Verification Officer.
 * @param {string} role 
 * @returns {boolean}
 */
export function isVerificationOfficer(role) {
  const norm = normalizeRole(role);
  return norm === 'DOCUMENT_VERIFICATION_OFFICER' || norm === 'VERIFICATION_OFFICER';
}

/**
 * Checks if the user is a Pass Approval Officer.
 * @param {string} role 
 * @returns {boolean}
 */
export function isApprovalOfficer(role) {
  const norm = normalizeRole(role);
  return norm === 'PASS_APPROVAL_OFFICER' || norm === 'OFFICER';
}

/**
 * Checks if the user has an officer role (either Verification or Approval).
 * @param {string} role 
 * @returns {boolean}
 */
export function isOfficer(role) {
  return isVerificationOfficer(role) || isApprovalOfficer(role);
}

/**
 * Checks if the user is a Route & Fare Manager.
 * @param {string} role 
 * @returns {boolean}
 */
export function isRouteManager(role) {
  return normalizeRole(role) === 'ROUTE_FARE_MANAGER';
}

/**
 * Checks if the user is a Passenger.
 * @param {string} role 
 * @returns {boolean}
 */
export function isPassenger(role) {
  const norm = normalizeRole(role);
  return norm === 'PASSENGER' || norm === 'USER';
}
