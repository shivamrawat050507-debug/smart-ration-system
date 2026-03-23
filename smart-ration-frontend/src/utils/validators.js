export const isEmpty = (value) => !value || !value.trim();

export const isValidPhone = (phone) => /^\d{10}$/.test(phone);

export const hasMinLength = (value, minLength) => value.trim().length >= minLength;
