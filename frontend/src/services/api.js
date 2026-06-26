import axios from 'axios';

const resolveBaseURL = () => {
  if (typeof window === 'undefined') {
    return 'http://localhost:8080/api';
  }

  if (window.location.hostname === 'localhost' && window.location.port === '3000') {
    return 'http://localhost:8080/api';
  }

  return '/api';
};

export const api = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 8000
});

export const setApiToken = (token) => {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
    return;
  }

  delete api.defaults.headers.common.Authorization;
};
