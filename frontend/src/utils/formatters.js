// Shared formatting utilities for Indian Currency (INR) and Indian Date formats

/**
 * Formats a numeric value into Indian Rupees (INR) using standard Indian number formatting.
 * Examples: ₹70, ₹750, ₹1,500, ₹3,000, ₹5,000
 * @param {number|string} amount
 * @param {object} options - { fractionDigits: number, showDecimalsIfAny: boolean }
 * @returns {string}
 */
export function formatINR(amount, options = {}) {
  if (amount === null || amount === undefined || amount === '' || isNaN(Number(amount))) {
    return '₹0';
  }

  const num = Number(amount);
  let maxDigits = 0;
  if (options.fractionDigits !== undefined) {
    maxDigits = options.fractionDigits;
  } else if (num % 1 !== 0) {
    maxDigits = 2;
  }

  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: maxDigits,
  }).format(num);
}

/**
 * Returns user-friendly price/fare display for a pass category.
 * If fareType is CONFIRMATION_REQUIRED, displays "Fare on Confirmation".
 * If fareType is CONFIGURABLE or CONCESSIONAL and fare is 0, displays "Concessional / Rule-based".
 * Otherwise displays the formatted INR fare.
 * @param {object} category
 * @returns {string}
 */
export function formatCategoryFare(category) {
  if (!category) return '₹0';

  const fareType = category.fareType || 'FIXED';
  const baseFare = Number(category.baseFare) || 0;

  if (fareType === 'CONFIRMATION_REQUIRED') {
    return 'Fare on Confirmation';
  }

  if (fareType === 'CONFIGURABLE' && baseFare === 0) {
    return 'Concessional (Punching Concession)';
  }

  if (fareType === 'CONCESSIONAL' && baseFare === 0) {
    return 'Concessional (Subsidized / Free)';
  }

  return formatINR(baseFare);
}

/**
 * Formats a date string into Indian Standard readable date (e.g., "14 Oct 2026").
 * @param {string|Date} dateStr
 * @returns {string}
 */
export function formatDateIN(dateStr) {
  if (!dateStr) return 'N/A';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return dateStr;

  return date.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

/**
 * Formats validity days into clear readable string.
 * @param {number} days
 * @returns {string}
 */
export function formatValidity(days) {
  if (!days && days !== 0) return 'N/A';
  if (days === 1) return '24 Hours (1 Day)';
  if (days === 7) return '7 Days (Weekly)';
  if (days === 30) return '30 Days (Monthly)';
  if (days === 90) return '90 Days (Quarterly)';
  if (days === 180) return '180 Days (Half-Yearly)';
  if (days === 365) return '365 Days (Annual)';
  return `${days} Days`;
}
